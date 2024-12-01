package com.control.water.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.control.water.models.User;
import com.control.water.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;


@Controller
public class LoginController {

    @Autowired
    private UserService userService;

    @GetMapping("/login")
    public String login() {
        return "pages/login";
    }

    @GetMapping("/register")
    public String registroForm(Model model) {
        model.addAttribute("user", new User());
        return "pages/register";
    }

    @PostMapping("/register")
    public String registrar(@ModelAttribute User user, RedirectAttributes redirectAttributes) {
        try {
            // Verifica se o email já existe
            if (userService.findByEmail(user.getEmail()) != null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Email já cadastrado");
                return "redirect:/register";
            }

            userService.registrar(user);
            redirectAttributes.addFlashAttribute("successMessage", "Registro realizado com sucesso!");
            return "redirect:/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erro ao registrar: " + e.getMessage());
            return "redirect:/register";
        }
    }
}