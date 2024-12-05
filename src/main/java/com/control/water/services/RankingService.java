package com.control.water.services;

import com.control.water.models.User;
import com.control.water.repositories.UserRepository;
import com.control.water.repositories.ConsumoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RankingService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ConsumoRepository consumoRepository;

    @Autowired
    private GamificacaoService gamificacaoService;

    // Método novo para pegar a posição do usuário no ranking
    public Integer getPosicaoUsuario(User user) {
        List<Map<String, Object>> ranking = getRankingGeral();
        for (int i = 0; i < ranking.size(); i++) {
            if (ranking.get(i).get("user").equals(user)) {
                return i + 1;
            }
        }
        return 0;
    }

    // Método novo para contar total de usuários
    public Integer getTotalUsuarios() {
        return (int) userRepository.count();
    }

    public List<Map<String, Object>> getRankingGeral() {
        return userRepository.findAll().stream()
            .map(user -> Map.of(
                "user", user,
                "pontos", gamificacaoService.calcularPontuacaoTotal(user),
                "conquistas", gamificacaoService.getConquistasUsuario(user).size(),
                "economia", calcularEconomiaTotal(user)
            ))
            // Alterado aqui: ordena por economia ao invés de pontos
            .sorted((a, b) -> ((Double) b.get("economia")).compareTo((Double) a.get("economia")))
            .collect(Collectors.toList());
    }

    public List<Map<String, Object>> getRankingMensal() {
        LocalDate inicio = LocalDate.now().withDayOfMonth(1);
        LocalDate fim = LocalDate.now();
    
        return userRepository.findAll().stream()
            .map(user -> {
                Double consumoMensal = consumoRepository.findTotalConsumoByUserAndPeriod(user, inicio, fim);
                Double economia = calcularEconomiaMensal(user);
                return Map.of(
                    "user", user,
                    "consumo", consumoMensal != null ? consumoMensal : 0.0,
                    "economia", economia != null ? economia : 0.0
                );
            })
            // Alterado aqui: ordena por economia
            .sorted((a, b) -> ((Double) b.get("economia")).compareTo((Double) a.get("economia")))
            .collect(Collectors.toList());
    }

    private Double calcularEconomiaTotal(User user) {
        LocalDate primeiroMes = consumoRepository.findFirstConsumoDate(user);
        if (primeiroMes == null) {
            return 0.0;
        }
    
        // Pega o consumo do primeiro mês como base
        LocalDate inicioPrimeiroMes = primeiroMes.withDayOfMonth(1);
        LocalDate fimPrimeiroMes = YearMonth.from(primeiroMes).atEndOfMonth();
        
        Double consumoPrimeiroMes = consumoRepository.findTotalConsumoByUserAndPeriod(
            user,
            inicioPrimeiroMes,
            fimPrimeiroMes
        );
    
        if (consumoPrimeiroMes == null || consumoPrimeiroMes == 0.0) {
            return 0.0;
        }
    
        // Pega os meses seguintes para comparar
        Double economiaTotal = 0.0;
        YearMonth mesAtual = YearMonth.from(LocalDate.now());
        YearMonth mesPrimeiro = YearMonth.from(primeiroMes);
        YearMonth mesLoop = mesPrimeiro.plusMonths(1);
    
        // Calcula a economia mês a mês
        while (!mesLoop.isAfter(mesAtual)) {
            LocalDate inicioMes = mesLoop.atDay(1);
            LocalDate fimMes = mesLoop.atEndOfMonth();
    
            Double consumoMes = consumoRepository.findTotalConsumoByUserAndPeriod(
                user,
                inicioMes,
                fimMes
            );
    
            if (consumoMes != null) {
                // Se consumiu menos que o primeiro mês = economia positiva
                Double economiaMes = consumoPrimeiroMes - consumoMes;
                economiaTotal += economiaMes;
            }
    
            mesLoop = mesLoop.plusMonths(1);
        }
    
        return economiaTotal;
    }

    private Double calcularEconomiaMensal(User user) {
        YearMonth mesAtual = YearMonth.now();
        LocalDate inicio = mesAtual.atDay(1);
        LocalDate fim = mesAtual.atEndOfMonth();

        // Busca o primeiro mês como base de comparação
        LocalDate primeiroMes = consumoRepository.findFirstConsumoDate(user);
        if (primeiroMes == null || YearMonth.from(primeiroMes).equals(mesAtual)) {
            return 0.0; // Primeiro mês não tem economia
        }

        // Calcula a média do primeiro mês
        LocalDate inicioPrimeiroMes = YearMonth.from(primeiroMes).atDay(1);
        LocalDate fimPrimeiroMes = YearMonth.from(primeiroMes).atEndOfMonth();
        Double consumoPrimeiroMes = consumoRepository.findTotalConsumoByUserAndPeriod(
            user,
            inicioPrimeiroMes,
            fimPrimeiroMes
        );

        if (consumoPrimeiroMes == null || consumoPrimeiroMes == 0.0) {
            return 0.0;
        }

        // Pega o consumo do mês atual
        Double consumoMesAtual = consumoRepository.findTotalConsumoByUserAndPeriod(
            user,
            inicio,
            fim
        );

        if (consumoMesAtual == null) {
            return 0.0;
        }

        // Economia mensal = consumo do primeiro mês - consumo atual
        return consumoPrimeiroMes - consumoMesAtual;
    }
}