package com.control.water.models;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Data
public class Consumo {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDate data;

    @Column(nullable = false)
    private Double leitura;

    @Column(nullable = false)
    private String tipo;

    private String observacoes;

    @Column(nullable = false)
    private Double consumoLitros = 0.0;

    @Column(nullable = false)
    private Double economia = 0.0;

    @PrePersist
    public void prePersist() {
        if (data == null) {
            data = LocalDate.now();
        }
        if (consumoLitros == null) {
            consumoLitros = 0.0;
        }
        if (economia == null) {
            economia = 0.0;
        }
    }
}