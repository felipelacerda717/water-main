package com.control.water.repositories;

import com.control.water.models.Meta;
import com.control.water.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MetaRepository extends JpaRepository<Meta, Long> {
    List<Meta> findByUserOrderByDataInicioDesc(User user);
    Optional<Meta> findByUserAndAtiva(User user, Boolean ativa);
    List<Meta> findByUserAndAtivaOrderByDataInicio(User user, Boolean ativa);
}