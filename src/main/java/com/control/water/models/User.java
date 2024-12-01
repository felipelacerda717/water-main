package com.control.water.models;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    private String email;
    private String password;
    private String residenceType; // CASA, APARTAMENTO, COMERCIAL
    private Integer residents;    // Número de residentes
}