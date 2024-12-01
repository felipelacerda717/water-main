package com.control.water.controllers;

import com.control.water.models.Consumo;
import com.control.water.models.User;
import com.control.water.repositories.ConsumoRepository;
import com.control.water.services.GamificacaoService;
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
    private ConsumoRepository consumoRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired 
    private GamificacaoService gamificacaoService;

    @GetMapping
    public String registroConsumo(Model model, Authentication authentication) {
        // Busca o usuário logado
        User user = userRepository.findByEmail(authentication.getName());
        
        // Busca os últimos registros de consumo do usuário
        List<Consumo> ultimosRegistros = consumoRepository.findByUserOrderByDataDesc(user);
        
        // Adiciona os dados ao modelo
        model.addAttribute("consumos", ultimosRegistros);
        model.addAttribute("hoje", LocalDate.now());
        
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
            // Verificar autenticação
            if (authentication == null) {
                redirectAttributes.addFlashAttribute("erroMsg", "Usuário não autenticado");
                return "redirect:/consumo";
            }
    
            // Log do email do usuário
            String userEmail = authentication.getName();
            System.out.println("Email do usuário: " + userEmail);
    
            // Buscar usuário
            User user = userRepository.findByEmail(userEmail);
            if (user == null) {
                redirectAttributes.addFlashAttribute("erroMsg", "Usuário não encontrado para o email: " + userEmail);
                return "redirect:/consumo";
            }
    
            // Criar consumo
            Consumo consumo = new Consumo();
            consumo.setUser(user);
            consumo.setData(data);
            consumo.setLeitura(leitura);
            consumo.setTipo(tipo);
            consumo.setObservacoes(observacoes);
    
            // Calcular consumo em litros
            consumoRepository.findLastConsumoByUser(user).ifPresentOrElse(
                ultimoConsumo -> {
                    double consumoLitros = (leitura - ultimoConsumo.getLeitura()) * 1000;
                    consumo.setConsumoLitros(consumoLitros);
                },
                () -> consumo.setConsumoLitros(leitura * 1000)
            );
    
            consumoRepository.save(consumo);
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
        return consumoRepository.findByUserOrderByDataDesc(user);
    }

    @GetMapping("/consumo-mensal")
    @ResponseBody
    public Double consumoMensal(Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName());
        LocalDate inicio = LocalDate.now().withDayOfMonth(1);
        LocalDate fim = LocalDate.now();
        return consumoRepository.findTotalConsumoByUserAndPeriod(user, inicio, fim);
    }
}