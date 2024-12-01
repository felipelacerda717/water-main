package com.control.water.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "conquistas_usuario")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConquistaUsuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    
    @ManyToOne
    @JoinColumn(name = "conquista_id")
    private Conquista conquista;
    
    private LocalDateTime dataConquista;
    private Integer pontosAtuais;
}