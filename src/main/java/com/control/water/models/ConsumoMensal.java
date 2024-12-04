package com.control.water.models;

import jakarta.persistence.*;
import lombok.Data;
import java.time.YearMonth;

@Entity
@Data
public class ConsumoMensal {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private YearMonth mesReferencia;

    @Column(nullable = false)
    private Double leituraTotal;

    private Double mediaDiaria;

    private Double consumoTotal;

    private String observacoes;

    @PrePersist
    @PreUpdate
    public void calcularMedias() {
        if (leituraTotal != null) {
            mediaDiaria = leituraTotal / mesReferencia.lengthOfMonth();
            consumoTotal = leituraTotal * 1000; // Convertendo para litros
        }
    }
}