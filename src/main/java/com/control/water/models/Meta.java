package com.control.water.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "metas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Meta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    private LocalDate dataInicio;
    private LocalDate dataFim;
    private Double metaConsumo;  // Meta em litros
    private String descricao;
    private Boolean ativa;
    private Double progresso;    // Progresso atual em porcentagem
}