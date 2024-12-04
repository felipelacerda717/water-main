package com.control.water.services;

import com.control.water.models.*;
import com.control.water.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.time.LocalDate;
import java.time.YearMonth;
@Service
public class ConsumoService {

    private static final double CAPACIDADE_MAXIMA_MEDIDOR = 999.99;

    @Autowired
    private ConsumoRepository consumoRepository;

    @Autowired
    private ConsumoMensalRepository consumoMensalRepository;

    @Autowired
    private MetaRepository metaRepository;

    @Autowired
    private MetaService metaService;

    @Autowired
    private UserService userService;

    public List<Consumo> getHistoricoConsumo(User user) {
        return consumoRepository.findByUserOrderByDataDesc(user);
    }

    public User getUserLogado(Authentication authentication) {
        return userService.findByUsername(authentication.getName());
    }

    public Set<YearMonth> getMesesDisponiveis(User user) {
        List<Integer> yearMonths = consumoRepository.findDistinctYearMonthsByUser(user);
        Set<YearMonth> meses = new TreeSet<>();
        
        for (Integer yearMonth : yearMonths) {
            int year = yearMonth / 100;
            int month = yearMonth % 100;
            meses.add(YearMonth.of(year, month));
        }
        
        return meses;
    }

    public List<Consumo> getConsumosDiarios(User user, YearMonth mes) {
        if (mes == null) {
            mes = YearMonth.now();
        }
        LocalDate inicio = mes.atDay(1);
        LocalDate fim = mes.atEndOfMonth();
        return consumoRepository.findConsumosByUserAndPeriod(user, inicio, fim);
    }

    public List<ConsumoMensal> getConsumosMensais(User user) {
        return consumoMensalRepository.findByUserOrderByMesReferenciaDesc(user);
    }

    @Transactional
    public Consumo registrarConsumoDiario(User user, LocalDate data, Double leitura, String tipo, String observacoes) {
        Consumo novoConsumo = new Consumo();
        novoConsumo.setUser(user);
        novoConsumo.setData(data);
        novoConsumo.setLeitura(leitura);
        novoConsumo.setTipo(tipo);
        novoConsumo.setObservacoes(observacoes);
        
        return registrarConsumoCompleto(novoConsumo);
    }

    @Transactional
    public Consumo registrarConsumoCompleto(Consumo novoConsumo) {
        try {
            Optional<Consumo> ultimoConsumoOpt = consumoRepository.findLastConsumoByUser(novoConsumo.getUser());
            
            if (ultimoConsumoOpt.isPresent()) {
                Consumo ultimoConsumo = ultimoConsumoOpt.get();
                double leituraAnterior = ultimoConsumo.getLeitura();
                double leituraAtual = novoConsumo.getLeitura();
                
                double consumoLitros;
                if (leituraAtual == leituraAnterior) {
                    consumoLitros = leituraAtual * 1000;  // Usa a leitura atual em vez da diferença
                } else if (leituraAtual < leituraAnterior) {
                    // Caso onde o medidor reiniciou
                    double consumoAteReiniciar = (CAPACIDADE_MAXIMA_MEDIDOR - leituraAnterior);
                    double consumoAposReiniciar = leituraAtual;
                    consumoLitros = (consumoAteReiniciar + consumoAposReiniciar) * 1000;
                } else {
                    // Caso normal: diferença entre leituras
                    consumoLitros = (leituraAtual - leituraAnterior) * 1000;
                }
                
                
                novoConsumo.setConsumoLitros(consumoLitros);
                calcularEconomia(novoConsumo);
            } else {
                // Primeiro registro: consumo é a própria leitura
                novoConsumo.setConsumoLitros(novoConsumo.getLeitura() * 1000);
                novoConsumo.setEconomia(0.0);
            }
            
            if (novoConsumo.getData() == null) {
                novoConsumo.setData(LocalDate.now());
            }

            Consumo consumoSalvo = consumoRepository.save(novoConsumo);
            atualizarConsumoMensal(novoConsumo.getUser(), YearMonth.from(novoConsumo.getData()));

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

    private void calcularEconomia(Consumo novoConsumo) {
        // Busca primeiro mês de consumo do usuário para estabelecer a média base
        LocalDate primeiroMes = consumoRepository.findFirstConsumoDate(novoConsumo.getUser());
        
        if (primeiroMes == null || novoConsumo.getData().getYear() == primeiroMes.getYear() 
            && novoConsumo.getData().getMonth() == primeiroMes.getMonth()) {
            // Primeiro mês - estabelece média (economia = 0)
            novoConsumo.setEconomia(0.0);
            return;
        }
    
        // Pega a média do primeiro mês
        LocalDate inicioPrimeiroMes = primeiroMes.withDayOfMonth(1);
        LocalDate fimPrimeiroMes = primeiroMes.withDayOfMonth(primeiroMes.lengthOfMonth());
        
        Double mediaBaseMensal = consumoRepository.findTotalConsumoByUserAndPeriod(
            novoConsumo.getUser(),
            inicioPrimeiroMes,
            fimPrimeiroMes
        );
    
        if (mediaBaseMensal == null || mediaBaseMensal == 0.0) {
            novoConsumo.setEconomia(0.0);
            return;
        }
    
        // Calcula economia com base na média do primeiro mês
        // Positivo se consumiu menos que a média, negativo se consumiu mais
        double economia = mediaBaseMensal - novoConsumo.getConsumoLitros();
        novoConsumo.setEconomia(economia);
    }


    public Double getEconomiaTotal(User user) {
        // Busca o primeiro mês para estabelecer a média base
        LocalDate primeiroMes = consumoRepository.findFirstConsumoDate(user);
        if (primeiroMes == null) {
            return 0.0;
        }
    
        YearMonth mesPrimeiro = YearMonth.from(primeiroMes);
        LocalDate inicioPrimeiroMes = mesPrimeiro.atDay(1);
        LocalDate fimPrimeiroMes = mesPrimeiro.atEndOfMonth();
    
        // Pega o consumo do primeiro mês (esse será nossa base de comparação)
        Double consumoPrimeiroMes = consumoRepository.findTotalConsumoByUserAndPeriod(
            user,
            inicioPrimeiroMes,
            fimPrimeiroMes
        );
    
        if (consumoPrimeiroMes == null || consumoPrimeiroMes == 0.0) {
            return 0.0;
        }
    
        // Calcula a economia de cada mês subsequente
        double economiaTotal = 0.0;
        YearMonth mesAtual = YearMonth.now();
        YearMonth mesLoop = mesPrimeiro.plusMonths(1); // Começa do segundo mês
    
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
                // Se consumiu mais que o primeiro mês = economia negativa
                economiaTotal += (consumoPrimeiroMes - consumoMes);
            }
    
            mesLoop = mesLoop.plusMonths(1);
        }
    
        return economiaTotal;
    }
    private void atualizarConsumoMensal(User user, YearMonth mes) {
        LocalDate inicio = mes.atDay(1);
        LocalDate fim = mes.atEndOfMonth();
        Double totalConsumo = consumoRepository.findTotalConsumoByUserAndPeriod(user, inicio, fim);
        
        if (totalConsumo == null) totalConsumo = 0.0;

        Optional<ConsumoMensal> consumoMensalOpt = consumoMensalRepository.findByUserAndMesReferencia(user, mes);
        ConsumoMensal consumoMensal = consumoMensalOpt.orElseGet(() -> {
            ConsumoMensal novo = new ConsumoMensal();
            novo.setUser(user);
            novo.setMesReferencia(mes);
            return novo;
        });
        
        consumoMensal.setLeituraTotal(totalConsumo);
        consumoMensalRepository.save(consumoMensal);
    }

    private void atualizarProgressoMeta(Meta meta, User user) {
        try {
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

    @Transactional
    public void registrarConsumoMensal(User user, YearMonth mes, Double leituraTotal, String observacoes) {
        LocalDate inicioDeMes = mes.atDay(1);
        LocalDate fimDeMes = mes.atEndOfMonth();
        int diasNoMes = mes.lengthOfMonth();
        
        // Busca a última leitura do mês anterior
        Optional<Consumo> ultimoConsumoOpt = consumoRepository.findLastConsumoBeforeDate(user.getId(), inicioDeMes);
    double leituraAnterior = ultimoConsumoOpt.map(Consumo::getLeitura).orElse(0.0);
    
    double consumoReal = leituraTotal - leituraAnterior; // Consumo real em m³
    double mediaDiariaM3 = consumoReal / diasNoMes;      // Média diária correta
    double consumoDiarioLitros = consumoReal * 1000 / diasNoMes; // Em litros
        
        // Exclui registros existentes
        List<Consumo> consumosExistentes = consumoRepository.findConsumosByUserAndPeriod(user, inicioDeMes, fimDeMes);
        if (!consumosExistentes.isEmpty()) {
            consumoRepository.deleteAll(consumosExistentes);
        }
        
        // Cria os registros diários
        for (int dia = 1; dia <= diasNoMes; dia++) {
            Consumo consumo = new Consumo();
            consumo.setUser(user);
            consumo.setData(mes.atDay(dia));
            consumo.setLeitura(leituraTotal);           // Mantém a leitura total
            consumo.setTipo("REGULAR");
            consumo.setObservacoes(observacoes);
            consumo.setConsumoLitros(consumoDiarioLitros); // Consumo diário baseado na diferença
            
            consumoRepository.save(consumo);
        }
    
        // Atualiza o registro mensal
        ConsumoMensal consumoMensal = consumoMensalRepository
            .findByUserAndMesReferencia(user, mes)
            .orElse(new ConsumoMensal());
            
        consumoMensal.setUser(user);
        consumoMensal.setMesReferencia(mes);
        consumoMensal.setLeituraTotal(leituraTotal);       // Leitura total em m³
        consumoMensal.setMediaDiaria(mediaDiariaM3);       // Média diária em m³ (baseada na diferença)
        consumoMensal.setConsumoTotal(consumoReal * 1000); // Consumo total em litros (baseado na diferença)
        consumoMensal.setObservacoes(observacoes);
        
        consumoMensalRepository.save(consumoMensal);
    
        Meta metaAtiva = metaService.getMetaAtiva(user);
        if (metaAtiva != null) {
            atualizarProgressoMeta(metaAtiva, user);
        }
    }

@Transactional
    public void excluirTodosConsumosDiarios(User user) {
        consumoRepository.deleteByUser(user);
        
        // Atualiza todos os registros mensais
        List<ConsumoMensal> consumosMensais = consumoMensalRepository.findByUserOrderByMesReferenciaDesc(user);
        for (ConsumoMensal mensal : consumosMensais) {
            mensal.setLeituraTotal(0.0);
            mensal.setConsumoTotal(0.0);
            mensal.setMediaDiaria(0.0);
        }
        consumoMensalRepository.saveAll(consumosMensais);
        
        Meta metaAtiva = metaService.getMetaAtiva(user);
        if (metaAtiva != null) {
            atualizarProgressoMeta(metaAtiva, user);
        }
    }

    @Transactional
    public void excluirTodosConsumosMensais(User user) {
        // Primeiro, excluir todos os registros mensais
        consumoMensalRepository.deleteByUser(user);
        
        // Em seguida, excluir todos os consumos diários
        consumoRepository.deleteByUser(user);
        
        // Por fim, atualizar a meta se existir
        Meta metaAtiva = metaService.getMetaAtiva(user);
        if (metaAtiva != null) {
            atualizarProgressoMeta(metaAtiva, user);
        }
    }

    @Transactional
    public void excluirConsumoDiario(Long id) {
        Consumo consumo = consumoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Registro não encontrado"));
            
        User user = consumo.getUser();
        YearMonth mes = YearMonth.from(consumo.getData());
        
        consumoRepository.delete(consumo);
        atualizarConsumoMensal(user, mes);
        
        Meta metaAtiva = metaService.getMetaAtiva(user);
        if (metaAtiva != null) {
            atualizarProgressoMeta(metaAtiva, user);
        }
    }
    @Transactional
    public void excluirConsumoMensal(Long id) {
        ConsumoMensal consumoMensal = consumoMensalRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Registro mensal não encontrado"));
            
        User user = consumoMensal.getUser();
        YearMonth mes = consumoMensal.getMesReferencia();
        
        // Exclui todos os consumos diários do mês
        LocalDate inicio = mes.atDay(1);
        LocalDate fim = mes.atEndOfMonth();
        List<Consumo> consumosDiarios = consumoRepository.findConsumosByUserAndPeriod(user, inicio, fim);
        consumoRepository.deleteAll(consumosDiarios);
        
        // Exclui o registro mensal
        consumoMensalRepository.delete(consumoMensal);
        
        Meta metaAtiva = metaService.getMetaAtiva(user);
        if (metaAtiva != null) {
            atualizarProgressoMeta(metaAtiva, user);
        }
    }

   


}