package com.control.water.controllers;

import com.control.water.services.RankingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/ranking")
public class RankingController {

    @Autowired
    private RankingService rankingService;

    @GetMapping
    public String ranking(Model model, Authentication authentication) {
        model.addAttribute("active", "ranking");
        model.addAttribute("rankingGeral", rankingService.getRankingGeral());
        model.addAttribute("rankingMensal", rankingService.getRankingMensal());
        return "pages/ranking";
    }
}