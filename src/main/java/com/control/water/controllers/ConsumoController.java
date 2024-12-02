package com.control.water.controllers;

import com.control.water.models.Consumo;
import com.control.water.models.User;
import com.control.water.services.ConsumoService;
import com.control.water.services.MetaService;
import com.control.water.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/consumo")
public class ConsumoController {

    @Autowired
    private ConsumoService consumoService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MetaService metaService;

    @GetMapping
    public String mostrarPaginaConsumo(Model model, Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName());
        
        List<Consumo> ultimosRegistros = consumoService.getHistoricoConsumo(user);
        model.addAttribute("consumos", ultimosRegistros); // Nome corrigido para "consumos"
        model.addAttribute("hoje", LocalDate.now());
        model.addAttribute("metaAtual", metaService.getMetaAtiva(user));
        
        return "pages/consumo";
    }

    @PostMapping("/registrar")
    public String registrarConsumo(
            @RequestParam LocalDate data,
            @RequestParam Double leitura,
            @RequestParam String tipo,
            @RequestParam(required = false) String observacoes,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        
        try {
            User user = userRepository.findByEmail(authentication.getName());
            
            // Criar novo consumo
            Consumo novoConsumo = new Consumo();
            novoConsumo.setUser(user);
            novoConsumo.setData(data);
            novoConsumo.setLeitura(leitura);
            novoConsumo.setTipo(tipo);
            novoConsumo.setObservacoes(observacoes);

            // Salva o consumo e atualiza dados relacionados
            Consumo consumoSalvo = consumoService.registrarConsumoCompleto(novoConsumo);
            
            // Atualiza meta após novo consumo
            metaService.atualizarProgressoAposConsumo(consumoSalvo);
            
            redirectAttributes.addFlashAttribute("sucessoMsg", "Consumo registrado com sucesso!");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erroMsg", "Erro ao registrar consumo: " + e.getMessage());
        }
        
        return "redirect:/consumo";
    }
    
    @GetMapping("/historico")
    @ResponseBody
    public List<Consumo> historico(Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName());
        return consumoService.getHistoricoConsumo(user);
    }

    @GetMapping("/consumo-mensal")
    @ResponseBody
    public Double consumoMensal(Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName());
        return consumoService.getConsumoMesAtual(user);
    }
}