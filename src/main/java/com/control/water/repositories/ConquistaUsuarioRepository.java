package com.control.water.repositories;

import com.control.water.models.ConquistaUsuario;
import com.control.water.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import org.springframework.stereotype.Repository;
import com.control.water.models.Conquista;
import java.util.Optional;

@Repository
public interface ConquistaUsuarioRepository extends JpaRepository<ConquistaUsuario, Long> {
    List<ConquistaUsuario> findByUser(User user);
    List<ConquistaUsuario> findByUserOrderByDataConquistaDesc(User user);
    Optional<ConquistaUsuario> findByUserAndConquista(User user, Conquista conquista);
}