package com.control.water.services;

import com.control.water.models.User;
import com.control.water.models.Consumo;
import com.control.water.repositories.ConsumoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.control.water.repositories.UserRepository;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ConsumoService {

    @Autowired
    private ConsumoRepository consumoRepository;

    @Autowired
    private UserRepository userRepository;


    public Double getConsumoHoje(User user) {
        LocalDate hoje = LocalDate.now();
        return consumoRepository.findTotalConsumoByUserAndPeriod(user, hoje, hoje);
    }

    public Double getConsumoMesAtual(User user) {
        LocalDate inicio = YearMonth.now().atDay(1);
        LocalDate fim = LocalDate.now();
        Double consumo = consumoRepository.findTotalConsumoByUserAndPeriod(user, inicio, fim);
        return consumo != null ? consumo : 0.0;
    }

    public Double getConsumoMesAnterior(User user) {
        YearMonth mesAnterior = YearMonth.now().minusMonths(1);
        LocalDate inicio = mesAnterior.atDay(1);
        LocalDate fim = mesAnterior.atEndOfMonth();
        Double consumo = consumoRepository.findTotalConsumoByUserAndPeriod(user, inicio, fim);
        return consumo != null ? consumo : 0.0;
    }

    public Map<String, Double> getConsumoUltimaSemana(User user) {
        Map<String, Double> consumoSemanal = new LinkedHashMap<>();
        LocalDate hoje = LocalDate.now();
        
        for (int i = 6; i >= 0; i--) {
            LocalDate data = hoje.minusDays(i);
            Double consumo = consumoRepository.findTotalConsumoByUserAndPeriod(user, data, data);
            consumoSemanal.put(data.getDayOfWeek().toString(), consumo != null ? consumo : 0.0);
        }
        
        return consumoSemanal;
    }

    public Double getPrevisaoConsumoMensal(User user) {
        Double consumoAtual = getConsumoMesAtual(user);
        if (consumoAtual == null || consumoAtual == 0.0) return 0.0;
        
        int diaAtual = LocalDate.now().getDayOfMonth();
        int diasNoMes = YearMonth.now().lengthOfMonth();
        
        return (consumoAtual / diaAtual) * diasNoMes;
    }

    public List<String> getAlertasConsumo(User user) {
        List<String> alertas = new ArrayList<>();
        
        Double consumoHoje = getConsumoHoje(user);
        Double mediaConsumo = getMediaDiariaUltimos30Dias(user);
        
        if (consumoHoje != null && mediaConsumo != null && mediaConsumo > 0) {
            if (consumoHoje > mediaConsumo * 1.3) {
                alertas.add("Atenção: Consumo de hoje está 30% acima da média!");
            }
        }
        
        Double previsao = getPrevisaoConsumoMensal(user);
        Double mediaMensal = mediaConsumo * 30;
        
        if (previsao != null && mediaMensal != null && mediaMensal > 0) {
            if (previsao > mediaMensal * 1.2) {
                alertas.add("Alerta: Previsão de consumo mensal 20% acima do esperado!");
            }
        }
        
        return alertas;
    }

    public Double getEconomiaTotal(User user) {
        // Calcula a economia comparando com a média de consumo do sistema
        LocalDate inicio = LocalDate.now().minusMonths(1);
        LocalDate fim = LocalDate.now();
        
        Double consumoUsuario = consumoRepository.findTotalConsumoByUserAndPeriod(user, inicio, fim);
        if (consumoUsuario == null) consumoUsuario = 0.0;

        // Pega a média de consumo de todos os usuários
        List<User> usuarios = userRepository.findAll();
        double consumoTotal = 0.0;
        int count = 0;
        
        for (User u : usuarios) {
            Double consumo = consumoRepository.findTotalConsumoByUserAndPeriod(u, inicio, fim);
            if (consumo != null && consumo > 0) {
                consumoTotal += consumo;
                count++;
            }
        }
        
        if (count == 0) return 0.0;
        
        double mediaConsumo = consumoTotal / count;
        
        // A economia é a diferença entre a média e o consumo do usuário
        // Se negativo, significa que economizou
        return Math.max(0.0, mediaConsumo - consumoUsuario);
    }

    private Double getMediaDiariaUltimos30Dias(User user) {
        LocalDate hoje = LocalDate.now();
        LocalDate inicio = hoje.minusDays(30);
        Double consumoTotal = consumoRepository.findTotalConsumoByUserAndPeriod(user, inicio, hoje);
        return consumoTotal != null ? consumoTotal / 30 : 0.0;
    }
}