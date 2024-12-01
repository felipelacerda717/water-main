package com.control.water.controllers;

import com.control.water.models.Dica;
import com.control.water.repositories.DicaRepository;
import com.control.water.repositories.ConsumoRepository;
import com.control.water.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/dicas")
public class DicaController {

    @Autowired
    private DicaRepository dicaRepository;

    @GetMapping
    public String dicas(Model model) {
        List<Dica> dicas = dicaRepository.findAll();
        model.addAttribute("dicas", dicas);
        return "pages/dicas";
    }
}