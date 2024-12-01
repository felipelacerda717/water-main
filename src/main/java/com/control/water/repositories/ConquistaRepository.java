package com.control.water.repositories;

import com.control.water.models.Conquista;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ConquistaRepository extends JpaRepository<Conquista, Long> {
    List<Conquista> findByTipo(String tipo);
    Optional<Conquista> findByNomeAndTipo(String nome, String tipo);
}