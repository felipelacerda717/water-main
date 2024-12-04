package com.control.water.controllers;

import com.control.water.models.Consumo;
import com.control.water.models.ConsumoMensal;
import com.control.water.models.User;
import com.control.water.services.ConsumoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/consumo")
public class ConsumoController {

    @Autowired
    private ConsumoService consumoService;

    @GetMapping
    public String mostrarPaginaConsumo(
            @RequestParam(required = false) YearMonth mes,
            Model model, 
            Authentication authentication) {
        
        User user = consumoService.getUserLogado(authentication);
        
        // Carrega dados para a view
        Set<YearMonth> mesesDisponiveis = consumoService.getMesesDisponiveis(user);
        List<Consumo> consumosDiarios = consumoService.getConsumosDiarios(user, mes);
        List<ConsumoMensal> consumosMensais = consumoService.getConsumosMensais(user);
        
        model.addAttribute("mesesDisponiveis", mesesDisponiveis);
        model.addAttribute("consumosDiarios", consumosDiarios);
        model.addAttribute("consumosMensais", consumosMensais);
        model.addAttribute("mesSelecionado", mes);
        
        return "pages/consumo";
    }

    @PostMapping("/registrar")
    public String registrarConsumoDiario(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate data,
            @RequestParam Double leitura,
            @RequestParam String tipo,
            @RequestParam(required = false) String observacoes,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        
        try {
            User user = consumoService.getUserLogado(authentication);
            consumoService.registrarConsumoDiario(user, data, leitura, tipo, observacoes);
            redirectAttributes.addFlashAttribute("sucessoMsg", "Consumo diário registrado com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erroMsg", "Erro ao registrar consumo: " + e.getMessage());
        }
        
        return "redirect:/consumo";
    }

    @PostMapping("/registrar-mensal")
    public String registrarConsumoMensal(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth mes,
            @RequestParam Double leituraTotal,
            @RequestParam(required = false) String observacoes,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        
        try {
            User user = consumoService.getUserLogado(authentication);
            consumoService.registrarConsumoMensal(user, mes, leituraTotal, observacoes);
            redirectAttributes.addFlashAttribute("sucessoMsg", "Consumo mensal registrado com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erroMsg", "Erro ao registrar consumo mensal: " + e.getMessage());
        }
        
        return "redirect:/consumo";
    }

    @PostMapping("/excluir/{id}")
    public String excluirConsumoDiario(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            consumoService.excluirConsumoDiario(id);
            redirectAttributes.addFlashAttribute("sucessoMsg", "Registro excluído com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erroMsg", "Erro ao excluir registro: " + e.getMessage());
        }
        return "redirect:/consumo";
    }

    @PostMapping("/excluir-mensal/{id}")
    public String excluirConsumoMensal(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            consumoService.excluirConsumoMensal(id);
            redirectAttributes.addFlashAttribute("sucessoMsg", "Registro mensal excluído com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erroMsg", "Erro ao excluir registro mensal: " + e.getMessage());
        }
        return "redirect:/consumo";
    }

    @PostMapping("/excluir-todos-diarios")
    public String excluirTodosConsumosDiarios(Authentication authentication, RedirectAttributes redirectAttributes) {
        try {
            User user = consumoService.getUserLogado(authentication);
            consumoService.excluirTodosConsumosDiarios(user);
            redirectAttributes.addFlashAttribute("sucessoMsg", "Todos os registros diários foram excluídos!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erroMsg", "Erro ao excluir registros: " + e.getMessage());
        }
        return "redirect:/consumo";
    }

    @PostMapping("/excluir-todos-mensais")
    public String excluirTodosConsumosMensais(Authentication authentication, RedirectAttributes redirectAttributes) {
        try {
            User user = consumoService.getUserLogado(authentication);
            consumoService.excluirTodosConsumosMensais(user);
            redirectAttributes.addFlashAttribute("sucessoMsg", "Todos os registros mensais foram excluídos!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erroMsg", "Erro ao excluir registros mensais: " + e.getMessage());
        }
        return "redirect:/consumo";
    }
}