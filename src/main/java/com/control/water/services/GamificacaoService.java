package com.control.water.services;

import com.control.water.models.*;
import com.control.water.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class GamificacaoService {

    @Autowired
    private ConquistaRepository conquistaRepository;

    @Autowired
    private ConquistaUsuarioRepository conquistaUsuarioRepository;

    @Autowired
    private ConsumoRepository consumoRepository;

    // Método principal para verificar conquistas após registro de consumo
    public void verificarConquistasConsumo(Consumo consumo) {
        User user = consumo.getUser();
        
        verificarConquistaPrimeiroRegistro(user);
        verificarConquistaEconomiaDiaria(user, consumo);
        verificarConquistaSequenciaRegistros(user);
        verificarMetasEconomia(user);
    }

    // Verifica conquistas relacionadas a metas
    public void verificarConquistasMeta(Meta meta) {
        User user = meta.getUser();
        verificarConquistaPrimeiraMeta(user);
        verificarConquistaSequenciaMetas(user);
    }

    // Verificações específicas de conquistas
    private void verificarConquistaPrimeiroRegistro(User user) {
        Optional<Conquista> conquista = conquistaRepository.findByNomeAndTipo("Primeiro Passo", "REGISTRO");
        if (conquista.isPresent() && !hasConquista(user, conquista.get())) {
            atribuirConquista(user, conquista.get());
        }
    }

    private void verificarConquistaEconomiaDiaria(User user, Consumo consumo) {
        LocalDate ontem = consumo.getData().minusDays(1);
        Optional<Consumo> consumoOntem = consumoRepository.findByUserAndData(user, ontem);
        
        if (consumoOntem.isPresent()) {
            double economia = consumoOntem.get().getConsumoLitros() - consumo.getConsumoLitros();
            if (economia >= 100) {
                Optional<Conquista> conquista = conquistaRepository.findByNomeAndTipo("Economista Iniciante", "ECONOMIA");
                conquista.ifPresent(c -> atribuirConquista(user, c));
            }
        }
    }

    private void verificarConquistaSequenciaRegistros(User user) {
        LocalDate hoje = LocalDate.now();
        int diasSeguidos = 0;
        
        for (int i = 0; i < 30; i++) {
            LocalDate data = hoje.minusDays(i);
            if (consumoRepository.findByUserAndData(user, data).isPresent()) {
                diasSeguidos++;
            } else {
                break;
            }
        }

        if (diasSeguidos >= 7) {
            Optional<Conquista> conquistaSemanal = conquistaRepository.findByNomeAndTipo("Comprometido", "REGISTRO");
            conquistaSemanal.ifPresent(c -> atribuirConquista(user, c));
        }

        if (diasSeguidos >= 30) {
            Optional<Conquista> conquistaMensal = conquistaRepository.findByNomeAndTipo("Analista de Água", "REGISTRO");
            conquistaMensal.ifPresent(c -> atribuirConquista(user, c));
        }
    }

    private void verificarMetasEconomia(User user) {
        Double economiaTotal = calcularEconomiaMensal(user);
        if (economiaTotal >= 1000) {
            Optional<Conquista> conquista = conquistaRepository.findByNomeAndTipo("Mestre da Economia", "ECONOMIA");
            conquista.ifPresent(c -> atribuirConquista(user, c));
        }
    }

    private void verificarConquistaPrimeiraMeta(User user) {
        Optional<Conquista> conquista = conquistaRepository.findByNomeAndTipo("Primeira Meta", "META");
        if (conquista.isPresent() && !hasConquista(user, conquista.get())) {
            atribuirConquista(user, conquista.get());
        }
    }

    private void verificarConquistaSequenciaMetas(User user) {
        Optional<Conquista> conquista = conquistaRepository.findByNomeAndTipo("Consistência", "META");
        if (conquista.isPresent()) {
            int metasConsecutivas = calcularMetasConsecutivas(user);
            if (metasConsecutivas >= 3 && !hasConquista(user, conquista.get())) {
                atribuirConquista(user, conquista.get());
            }
        }
    }

    // Métodos auxiliares
    private boolean hasConquista(User user, Conquista conquista) {
        return conquistaUsuarioRepository.findByUserAndConquista(user, conquista).isPresent();
    }

    private void atribuirConquista(User user, Conquista conquista) {
        if (!hasConquista(user, conquista)) {
            ConquistaUsuario conquistaUsuario = new ConquistaUsuario();
            conquistaUsuario.setUser(user);
            conquistaUsuario.setConquista(conquista);
            conquistaUsuario.setDataConquista(LocalDateTime.now());
            conquistaUsuario.setPontosAtuais(conquista.getPontosRequeridos());
            conquistaUsuarioRepository.save(conquistaUsuario);
        }
    }

    private Double calcularEconomiaMensal(User user) {
        LocalDate inicio = LocalDate.now().withDayOfMonth(1);
        LocalDate fim = LocalDate.now();
        return consumoRepository.findTotalConsumoByUserAndPeriod(user, inicio, fim);
    }

    private int calcularMetasConsecutivas(User user) {
        // Implementar lógica para contar metas consecutivas
        return 0; // Placeholder
    }

    // Métodos públicos para acesso aos dados
    public List<ConquistaUsuario> getConquistasUsuario(User user) {
        return conquistaUsuarioRepository.findByUserOrderByDataConquistaDesc(user);
    }

    public int calcularPontuacaoTotal(User user) {
        return conquistaUsuarioRepository.findByUser(user)
                .stream()
                .mapToInt(cu -> cu.getPontosAtuais())
                .sum();
    }

    public List<ConquistaUsuario> getUltimasConquistas(User user, int limit) {
        return conquistaUsuarioRepository.findByUserOrderByDataConquistaDesc(user)
                .stream()
                .limit(limit)
                .toList();
    }
}