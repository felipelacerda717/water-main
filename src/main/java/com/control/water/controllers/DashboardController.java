package com.control.water.controllers;

import com.control.water.models.User;
import com.control.water.models.Consumo;
import com.control.water.models.Meta;
import com.control.water.services.ConsumoService;
import com.control.water.services.MetaService;
import com.control.water.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import com.control.water.services.RankingService;
import java.time.LocalDate;
import java.util.Map;
import java.util.List;

@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    @Autowired
    private ConsumoService consumoService;

    @Autowired
    private MetaService metaService;

    @Autowired
    private RankingService rankingService; // Novo

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public String dashboard(Model model, Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName());
        
        // Dados existentes
        Double consumoHoje = consumoService.getConsumoHoje(user);
        Double consumoMesAtual = consumoService.getConsumoMesAtual(user);
        Double consumoMesAnterior = consumoService.getConsumoMesAnterior(user);
        Map<String, Double> consumoSemanal = consumoService.getConsumoUltimaSemana(user);
        
        // Novos dados
        Integer posicaoRanking = rankingService.getPosicaoUsuario(user);
        Integer totalUsuarios = rankingService.getTotalUsuarios();
        Double previsaoConsumo = consumoService.getPrevisaoConsumoMensal(user);
        Double economiaTotal = consumoService.getEconomiaTotal(user);
        
        // Meta e alertas
        Meta metaAtual = metaService.getMetaAtiva(user);
        List<String> alertas = consumoService.getAlertasConsumo(user);

        // Adiciona ao modelo
        model.addAttribute("active", "dashboard");
        model.addAttribute("consumoHoje", consumoHoje != null ? consumoHoje : 0.0);
        model.addAttribute("consumoMesAtual", consumoMesAtual != null ? consumoMesAtual : 0.0);
        model.addAttribute("consumoMesAnterior", consumoMesAnterior != null ? consumoMesAnterior : 0.0);
        model.addAttribute("consumoSemanal", consumoSemanal);
        model.addAttribute("metaAtual", metaAtual);
        model.addAttribute("alertas", alertas);
        
        // Novos atributos
        model.addAttribute("posicaoRanking", posicaoRanking);
        model.addAttribute("totalUsuarios", totalUsuarios);
        model.addAttribute("previsaoConsumo", previsaoConsumo);
        model.addAttribute("economiaTotal", economiaTotal);

        return "pages/dashboard";
    }
}