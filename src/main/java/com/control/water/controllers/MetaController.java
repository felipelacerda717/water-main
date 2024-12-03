package com.control.water.controllers;

import com.control.water.models.Meta;
import com.control.water.models.User;
import com.control.water.repositories.MetaRepository;
import com.control.water.repositories.UserRepository;
import com.control.water.services.MetaService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/metas")
public class MetaController {

     @Autowired
    private MetaService metaService;

    @Autowired
    private MetaRepository metaRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public String metas(Model model, Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName());
        
        // Busca meta ativa
        Meta metaAtual = metaRepository.findByUserAndAtiva(user, true)
                                     .orElse(null);
        
        // Busca histórico de metas
        List<Meta> historicoMetas = metaRepository.findByUserOrderByDataInicioDesc(user);
        
        model.addAttribute("metaAtual", metaAtual);
        model.addAttribute("historicoMetas", historicoMetas);
        
        return "pages/metas";
    }

    @PostMapping("/criar")
    public String criarMeta(@RequestParam LocalDate dataInicio,
                           @RequestParam LocalDate dataFim,
                           @RequestParam Double metaConsumo,
                           @RequestParam String descricao,
                           Authentication authentication,
                           RedirectAttributes redirectAttributes) {
                            try {
                                User user = userRepository.findByEmail(authentication.getName());
                                
                                // Desativa outras metas existentes
                                List<Meta> metasAtivas = metaRepository.findByUserAndAtiva(user, true)
                                                                     .stream().toList();
                                metasAtivas.forEach(meta -> {
                                    meta.setAtiva(false);
                                    metaRepository.save(meta);
                                });
                                
                                // Cria nova meta
                                Meta novaMeta = new Meta();
                                novaMeta.setUser(user);
                                novaMeta.setDataInicio(dataInicio);
                                novaMeta.setDataFim(dataFim);
                                novaMeta.setMetaConsumo(metaConsumo);
                                novaMeta.setDescricao(descricao);
                                novaMeta.setAtiva(true);
                                novaMeta.setProgresso(0.0); // Inicia com 0% de progresso
                                
                                metaRepository.save(novaMeta);
                                
                                redirectAttributes.addFlashAttribute("sucessoMsg", "Meta criada com sucesso!");
                            } catch (Exception e) {
                                redirectAttributes.addFlashAttribute("erroMsg", "Erro ao criar meta: " + e.getMessage());
                            }
                            
                            return "redirect:/metas";
                        }
                    
                        @PostMapping("/{id}/desativar")
                        public String desativarMeta(@PathVariable Long id,
                                                   Authentication authentication,
                                                   RedirectAttributes redirectAttributes) {
                            try {
                                Meta meta = metaRepository.findById(id)
                                        .orElseThrow(() -> new RuntimeException("Meta não encontrada"));
                                
                                meta.setAtiva(false);
                                metaRepository.save(meta);
                                
                                redirectAttributes.addFlashAttribute("sucessoMsg", "Meta desativada com sucesso!");
                            } catch (Exception e) {
                                redirectAttributes.addFlashAttribute("erroMsg", "Erro ao desativar meta: " + e.getMessage());
                            }
                            
                            return "redirect:/metas";
                        }

                        @PostMapping("/excluir-todas")
                        public String excluirTodasMetas(Authentication authentication, RedirectAttributes redirectAttributes) {
                            try {
                                User user = userRepository.findByEmail(authentication.getName());
                                metaService.excluirTodasMetas(user);
                                redirectAttributes.addFlashAttribute("sucessoMsg", "Todas as metas foram excluídas!");
                            } catch (Exception e) {
                                redirectAttributes.addFlashAttribute("erroMsg", "Erro ao excluir metas: " + e.getMessage());
                            }
                            return "redirect:/metas";
                        }
                    }