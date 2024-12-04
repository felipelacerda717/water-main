package com.control.water.repositories;

import com.control.water.models.ConsumoMensal;
import com.control.water.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

@Repository
public interface ConsumoMensalRepository extends JpaRepository<ConsumoMensal, Long> {
    
    List<ConsumoMensal> findByUserOrderByMesReferenciaDesc(User user);
    
    Optional<ConsumoMensal> findByUserAndMesReferencia(User user, YearMonth mesReferencia);
    
    void deleteByUser(User user);
}