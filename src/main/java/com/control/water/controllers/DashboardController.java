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
    private UserRepository userRepository;

    @GetMapping
    public String dashboard(Model model, Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName());
        
        // Dados de consumo
        Double consumoHoje = consumoService.getConsumoHoje(user);
        Double consumoMesAtual = consumoService.getConsumoMesAtual(user);
        Double consumoMesAnterior = consumoService.getConsumoMesAnterior(user);
        Map<String, Double> consumoSemanal = consumoService.getConsumoUltimaSemana(user);
        
        // Cálculo de variação
        double variacaoMensal = 0.0;
        if (consumoMesAnterior != null && consumoMesAnterior > 0) {
            variacaoMensal = ((consumoMesAtual - consumoMesAnterior) / consumoMesAnterior) * 100;
        }

        // Previsão e alertas
        Double previsaoMensal = consumoService.getPrevisaoConsumoMensal(user);
        List<String> alertas = consumoService.getAlertasConsumo(user);
        
        // Meta atual
        Meta metaAtual = metaService.getMetaAtiva(user);

        // Adiciona ao modelo
        model.addAttribute("consumoHoje", consumoHoje != null ? consumoHoje : 0.0);
        model.addAttribute("consumoMesAtual", consumoMesAtual != null ? consumoMesAtual : 0.0);
        model.addAttribute("consumoMesAnterior", consumoMesAnterior != null ? consumoMesAnterior : 0.0);
        model.addAttribute("variacaoMensal", variacaoMensal);
        model.addAttribute("consumoSemanal", consumoSemanal);
        model.addAttribute("previsaoMensal", previsaoMensal != null ? previsaoMensal : 0.0);
        model.addAttribute("metaAtual", metaAtual);
        model.addAttribute("alertas", alertas);
        model.addAttribute("active", "dashboard");

        return "pages/dashboard";
    }
}