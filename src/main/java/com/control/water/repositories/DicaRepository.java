package com.control.water.repositories;

import com.control.water.models.Dica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DicaRepository extends JpaRepository<Dica, Long> {
    List<Dica> findByCategoria(String categoria);
    List<Dica> findByNivelDificuldade(String nivelDificuldade);
}