package com.control.water.config;

import com.control.water.models.Conquista;
import com.control.water.repositories.ConquistaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class ConquistaInitializer implements CommandLineRunner {

    @Autowired
    private ConquistaRepository conquistaRepository;

    @Override
    public void run(String... args) {
        // Verifica se já existem conquistas
        if (conquistaRepository.count() == 0) {
            // Conquistas de Economia
            conquistaRepository.save(new Conquista(null, 
                "Primeiro Passo", 
                "Registre seu primeiro consumo de água", 
                "REGISTRO", 
                1, 
                "FACIL"));

            conquistaRepository.save(new Conquista(null,
                "Economista Iniciante",
                "Economize 100 litros em um único dia",
                "ECONOMIA",
                100,
                "FACIL"));

            conquistaRepository.save(new Conquista(null,
                "Mestre da Economia",
                "Economize 1000 litros em um mês",
                "ECONOMIA",
                1000,
                "MEDIO"));

            // Conquistas de Metas
            conquistaRepository.save(new Conquista(null,
                "Primeira Meta",
                "Complete sua primeira meta de economia",
                "META",
                1,
                "FACIL"));

            conquistaRepository.save(new Conquista(null,
                "Consistência",
                "Complete 3 metas seguidas",
                "META",
                3,
                "MEDIO"));

            // Conquistas de Registro
            conquistaRepository.save(new Conquista(null,
                "Comprometido",
                "Registre consumo por 7 dias seguidos",
                "REGISTRO",
                7,
                "MEDIO"));

            conquistaRepository.save(new Conquista(null,
                "Analista de Água",
                "Registre consumo por 30 dias seguidos",
                "REGISTRO",
                30,
                "DIFICIL"));
        }
    }
}