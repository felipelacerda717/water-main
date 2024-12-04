package com.control.water.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "dicas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Dica {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String titulo;
    private String descricao;
    private String categoria;
    private Integer economiaEstimada;
    private String nivelDificuldade;
}