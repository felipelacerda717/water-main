package com.control.water.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "conquistas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Conquista {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String nome;
    private String descricao;
    private String icone;
    private Integer pontosRequeridos;
    private String tipo; // ECONOMIA, META_ATINGIDA, REGISTRO_CONSTANTE, etc.
}