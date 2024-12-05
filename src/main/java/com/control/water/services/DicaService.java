package com.control.water.services;

import com.control.water.models.Dica;
import com.control.water.repositories.DicaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DicaService {

    @Autowired
    private DicaRepository dicaRepository;

    public double calcularEconomiaVazamento(int gotasPorMinuto) {
        // 1 minuto -> 1 hora -> 1 dia -> 1 mês
        long gotasPorHora = gotasPorMinuto * 60L;
        long gotasPorDia = gotasPorHora * 24L;
        long gotasPorMes = gotasPorDia * 30L;

        // Converter gotas em litros (20.000 gotas = 1 litro)
        return gotasPorMes / 20000.0;
    }

    public double calcularEconomiaBanho(int minutosBanho) {
        // Vazão média de 9 litros por minuto
        int vazaoChuveiro = 9;
        int tempoRecomendado = 5; // minutos

        if (minutosBanho <= tempoRecomendado) {
            return 0.0;
        }

        int minutosExcedentes = minutosBanho - tempoRecomendado;
        // Calcula consumo excedente por dia e multiplica por 30 dias
        return minutosExcedentes * vazaoChuveiro * 30;
    }
}