package com.control.water.services;

import com.control.water.models.Meta;
import com.control.water.models.User;
import com.control.water.models.Consumo;
import com.control.water.repositories.MetaRepository;
import com.control.water.repositories.ConsumoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class MetaService {

    @Autowired
    private MetaRepository metaRepository;

    @Autowired
    private ConsumoRepository consumoRepository;

    public Meta getMetaAtiva(User user) {
        return metaRepository.findByUserAndAtiva(user, true).orElse(null);
    }

    public Meta criarMeta(User user, Meta meta) {
        // Desativa metas anteriores
        List<Meta> metasAtivas = metaRepository.findByUserAndAtivaOrderByDataInicio(user, true);
        metasAtivas.forEach(metaAtiva -> {
            metaAtiva.setAtiva(false);
            metaRepository.save(metaAtiva);
        });

        // Configura e salva nova meta
        meta.setUser(user);
        meta.setAtiva(true);
        meta.setProgresso(0.0);
        return metaRepository.save(meta);
    }

    public void atualizarProgressoMeta(Meta meta) {
        if (meta == null) return;

        // Calcula consumo total no período da meta
        Double consumoTotal = consumoRepository.findTotalConsumoByUserAndPeriod(
            meta.getUser(),
            meta.getDataInicio(),
            meta.getDataFim() != null ? meta.getDataFim() : LocalDate.now()
        );

        if (consumoTotal == null) consumoTotal = 0.0;

        // Calcula progresso
        double progresso = (consumoTotal / meta.getMetaConsumo()) * 100;
        progresso = Math.min(progresso, 100.0);
        
        meta.setProgresso(progresso);

        // Se atingiu 100%, desativa a meta
        if (progresso >= 100.0) {
            meta.setAtiva(false);
        }

        metaRepository.save(meta);
    }

    // Atualiza todas as metas ativas a cada hora
    @Scheduled(fixedRate = 3600000)
    public void atualizarTodasMetas() {
        List<Meta> metasAtivas = metaRepository.findByUserAndAtivaOrderByDataInicio(null, true);
        metasAtivas.forEach(this::atualizarProgressoMeta);
    }

    public void atualizarProgressoAposConsumo(Consumo consumo) {
        Optional<Meta> metaAtiva = metaRepository.findByUserAndAtiva(consumo.getUser(), true);
        metaAtiva.ifPresent(this::atualizarProgressoMeta);
    }

    public List<Meta> getHistoricoMetas(User user) {
        return metaRepository.findByUserOrderByDataInicioDesc(user);
    }

    public boolean verificarMetaAtingida(Meta meta) {
        return meta != null && meta.getProgresso() >= 100.0;
    }

    public void encerrarMeta(Meta meta) {
        if (meta != null) {
            meta.setAtiva(false);
            metaRepository.save(meta);
        }
    }

    public Double getProgressoAtual(User user) {
        Meta metaAtiva = getMetaAtiva(user);
        return metaAtiva != null ? metaAtiva.getProgresso() : null;
    }

    public String getStatusMeta(Meta meta) {
        if (meta == null) return "SEM_META";
        
        if (!meta.getAtiva()) return "CONCLUIDA";
        
        if (meta.getProgresso() >= 100.0) return "ATINGIDA";
        
        if (meta.getProgresso() >= 90.0) return "PROXIMA";
        
        return "EM_ANDAMENTO";
    }
}