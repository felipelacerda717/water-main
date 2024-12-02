package com.control.water.services;

import com.control.water.models.User;
import com.control.water.models.Consumo;
import com.control.water.models.Meta;
import com.control.water.repositories.ConsumoRepository;
import com.control.water.repositories.MetaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ConsumoService {

    private static final double CAPACIDADE_MAXIMA_MEDIDOR = 999.99;

    @Autowired
    private ConsumoRepository consumoRepository;

    @Autowired
    private MetaRepository metaRepository;

    @Autowired
    private MetaService metaService;

    public List<Consumo> getHistoricoConsumo(User user) {
        return consumoRepository.findByUserOrderByDataDesc(user);
    }

    @Transactional
    public Consumo registrarConsumoCompleto(Consumo novoConsumo) {
        try {
            Optional<Consumo> ultimoConsumoOpt = consumoRepository.findLastConsumoByUser(novoConsumo.getUser());
            
            if (ultimoConsumoOpt.isPresent()) {
                Consumo ultimoConsumo = ultimoConsumoOpt.get();
                double leituraAnterior = ultimoConsumo.getLeitura();
                double leituraAtual = novoConsumo.getLeitura();
                
                // Calcula o consumo considerando possível reinício do medidor
                double consumoLitros;
                if (leituraAtual < leituraAnterior) {
                    // Medidor reiniciou - soma o que faltava até CAPACIDADE_MAXIMA_MEDIDOR + nova leitura
                    double consumoAteReiniciar = (CAPACIDADE_MAXIMA_MEDIDOR - leituraAnterior);
                    double consumoAposReiniciar = leituraAtual;
                    consumoLitros = (consumoAteReiniciar + consumoAposReiniciar) * 1000;
                } else {
                    // Cálculo normal
                    consumoLitros = (leituraAtual - leituraAnterior) * 1000;
                }
                
                novoConsumo.setConsumoLitros(consumoLitros);
                
                // Verifica se houve economia em relação à média
                Double mediaConsumo = getMediaDiariaUltimos30Dias(novoConsumo.getUser()) * 30;
                if (mediaConsumo > 0 && consumoLitros < mediaConsumo) {
                    novoConsumo.setEconomia(mediaConsumo - consumoLitros);
                } else {
                    novoConsumo.setEconomia(0.0);
                }
            } else {
                // Primeiro registro
                novoConsumo.setConsumoLitros(novoConsumo.getLeitura() * 1000);
                novoConsumo.setEconomia(0.0);
            }
            
            // Garante valores padrão
            if (novoConsumo.getData() == null) {
                novoConsumo.setData(LocalDate.now());
            }

            // Salva o consumo
            Consumo consumoSalvo = consumoRepository.save(novoConsumo);

            // Atualiza a meta se existir
            Meta metaAtiva = metaService.getMetaAtiva(novoConsumo.getUser());
            if (metaAtiva != null) {
                atualizarProgressoMeta(metaAtiva, novoConsumo.getUser());
            }

            return consumoSalvo;
        } catch (Exception e) {
            System.err.println("Erro ao registrar consumo: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    private void atualizarProgressoMeta(Meta meta, User user) {
        try {
            // Recalcula progresso da meta
            double consumoTotal = calcularConsumoTotalPeriodo(
                user, 
                meta.getDataInicio(), 
                meta.getDataFim() != null ? meta.getDataFim() : LocalDate.now()
            );
            
            double progresso = meta.getMetaConsumo() > 0 ? 
                (consumoTotal / meta.getMetaConsumo()) * 100 : 0.0;
            
            meta.setProgresso(Math.min(progresso, 100.0));
            
            if (progresso >= 100.0) {
                meta.setAtiva(false);
            }
            
            metaRepository.save(meta);
        } catch (Exception e) {
            System.err.println("Erro ao atualizar progresso da meta: " + e.getMessage());
            throw e;
        }
    }

    private Double calcularConsumoTotalPeriodo(User user, LocalDate inicio, LocalDate fim) {
        try {
            Double total = consumoRepository.findTotalConsumoByUserAndPeriod(user, inicio, fim);
            return total != null ? total : 0.0;
        } catch (Exception e) {
            System.err.println("Erro ao calcular consumo total: " + e.getMessage());
            return 0.0;
        }
    }

    public Double getConsumoHoje(User user) {
        LocalDate hoje = LocalDate.now();
        Double consumo = consumoRepository.findTotalConsumoByUserAndPeriod(user, hoje, hoje);
        return consumo != null ? consumo : 0.0;
    }

    public Double getConsumoMesAtual(User user) {
        LocalDate inicio = LocalDate.now().withDayOfMonth(1);
        LocalDate fim = LocalDate.now();
        Double consumo = consumoRepository.findTotalConsumoByUserAndPeriod(user, inicio, fim);
        return consumo != null ? consumo : 0.0;
    }

    public Double getConsumoMesAnterior(User user) {
        LocalDate inicio = LocalDate.now().minusMonths(1).withDayOfMonth(1);
        LocalDate fim = YearMonth.now().minusMonths(1).atEndOfMonth();
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
        try {
            Double consumoAtual = getConsumoMesAtual(user);
            int diaAtual = LocalDate.now().getDayOfMonth();
            int diasNoMes = YearMonth.now().lengthOfMonth();
            
            if (consumoAtual == 0.0 || diaAtual == 0) return 0.0;
            
            return (consumoAtual / diaAtual) * diasNoMes;
        } catch (Exception e) {
            System.err.println("Erro ao calcular previsão mensal: " + e.getMessage());
            return 0.0;
        }
    }

    public List<String> getAlertasConsumo(User user) {
        List<String> alertas = new ArrayList<>();
        try {
            Double consumoHoje = getConsumoHoje(user);
            Double mediaConsumo = getMediaDiariaUltimos30Dias(user);
            
            if (mediaConsumo > 0) {
                if (consumoHoje > mediaConsumo * 1.3) {
                    alertas.add("Atenção: Consumo de hoje está 30% acima da média!");
                }
                
                Double previsao = getPrevisaoConsumoMensal(user);
                if (previsao > mediaConsumo * 30 * 1.2) {
                    alertas.add("Alerta: Previsão de consumo mensal 20% acima do esperado!");
                }
            }
        } catch (Exception e) {
            System.err.println("Erro ao gerar alertas: " + e.getMessage());
        }
        return alertas;
    }

    public Double getEconomiaTotal(User user) {
        try {
            Double economiaTotal = consumoRepository.findTotalEconomiaByUserAndPeriod(
                user,
                LocalDate.now().minusMonths(1),
                LocalDate.now()
            );
            return economiaTotal != null ? economiaTotal : 0.0;
        } catch (Exception e) {
            System.err.println("Erro ao calcular economia total: " + e.getMessage());
            return 0.0;
        }
    }

    private Double getMediaDiariaUltimos30Dias(User user) {
        try {
            LocalDate fim = LocalDate.now();
            LocalDate inicio = fim.minusDays(30);
            Double consumoTotal = consumoRepository.findTotalConsumoByUserAndPeriod(user, inicio, fim);
            return consumoTotal != null ? consumoTotal / 30 : 0.0;
        } catch (Exception e) {
            System.err.println("Erro ao calcular média diária: " + e.getMessage());
            return 0.0;
        }
    }
}