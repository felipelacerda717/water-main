package com.control.water.controllers;

import com.control.water.models.*;
import com.control.water.services.GamificacaoService;
import com.control.water.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/conquistas")
public class ConquistaController {

    @Autowired
    private GamificacaoService gamificacaoService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public String conquistas(Model model, Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName());
        model.addAttribute("conquistas", gamificacaoService.getConquistasUsuario(user));
        return "pages/conquistas";
    }
}