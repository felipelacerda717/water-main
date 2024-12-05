package com.control.water.controllers;

import com.control.water.models.Dica;
import com.control.water.repositories.DicaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import java.util.Arrays;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import com.control.water.services.DicaService;
import java.util.Map;
import java.util.List;

@Controller
@RequestMapping("/dicas")
public class DicaController {

    @Autowired
    private DicaRepository dicaRepository;

    @Autowired
    private DicaService dicaService;

    @GetMapping
    public String dicas(Model model) {
        // Carregar dicas
        List<Dica> dicas = dicaRepository.findAll();
        model.addAttribute("dicas", dicas);

        // Adicionar categorias disponíveis
        List<String> categorias = Arrays.asList("BANHO", "TORNEIRA", "LAVAGEM", "GERAL");
        model.addAttribute("categorias", categorias);

        // Adicionar níveis de dificuldade
        List<String> niveisDificuldade = Arrays.asList("FACIL", "MEDIO", "DIFICIL");
        model.addAttribute("niveisDificuldade", niveisDificuldade);

        return "pages/dicas";
    }

    @PostMapping("/calcular-economia")
    @ResponseBody
    public Map<String, Double> calcularEconomia(
            @RequestParam(required = false, defaultValue = "0") int tempoBanho,
            @RequestParam(required = false, defaultValue = "0") int vazamentos) {
        
        double economiaBanho = dicaService.calcularEconomiaBanho(tempoBanho);
        double economiaVazamento = dicaService.calcularEconomiaVazamento(vazamentos);
        double economiaTotal = economiaBanho + economiaVazamento;

        return Map.of(
            "economiaBanho", economiaBanho,
            "economiaVazamento", economiaVazamento,
            "economiaTotal", economiaTotal
        );
    }
}