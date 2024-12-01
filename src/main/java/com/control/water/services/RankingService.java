package com.control.water.services;

import com.control.water.models.User;
import com.control.water.repositories.UserRepository;
import com.control.water.repositories.ConsumoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
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

    public List<Map<String, Object>> getRankingGeral() {
        return userRepository.findAll().stream()
            .map(user -> Map.of(
                "user", user,
                "pontos", gamificacaoService.calcularPontuacaoTotal(user),
                "conquistas", gamificacaoService.getConquistasUsuario(user).size(),
                "economia", calcularEconomiaTotal(user)
            ))
            .sorted((a, b) -> ((Integer) b.get("pontos")).compareTo((Integer) a.get("pontos")))
            .collect(Collectors.toList());
    }

    public List<Map<String, Object>> getRankingMensal() {
        LocalDate inicio = LocalDate.now().withDayOfMonth(1);
        LocalDate fim = LocalDate.now();

        return userRepository.findAll().stream()
            .map(user -> {
                Double consumoMensal = consumoRepository.findTotalConsumoByUserAndPeriod(user, inicio, fim);
                return Map.of(
                    "user", user,
                    "consumo", consumoMensal != null ? consumoMensal : 0.0,
                    "economia", calcularEconomiaMensal(user)
                );
            })
            .sorted((a, b) -> ((Double) a.get("consumo")).compareTo((Double) b.get("consumo")))
            .collect(Collectors.toList());
    }

    private Double calcularEconomiaTotal(User user) {
        // Implementar cálculo de economia total
        return 0.0;
    }

    private Double calcularEconomiaMensal(User user) {
        // Implementar cálculo de economia mensal
        return 0.0;
    }
}