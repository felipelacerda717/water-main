package com.control.water.repositories;

import com.control.water.models.Consumo;
import com.control.water.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ConsumoRepository extends JpaRepository<Consumo, Long> {
    List<Consumo> findByUserOrderByDataDesc(User user);
    
    // Buscar consumo por usuário e data
    Optional<Consumo> findByUserAndData(User user, LocalDate data);
    
    // Buscar consumos do último mês
    @Query("SELECT c FROM Consumo c WHERE c.user = :user AND c.data BETWEEN :startDate AND :endDate ORDER BY c.data")
    List<Consumo> findConsumosByUserAndPeriod(
        @Param("user") User user, 
        @Param("startDate") LocalDate startDate, 
        @Param("endDate") LocalDate endDate
    );
    
    // Buscar consumo total do mês
    @Query("SELECT SUM(c.consumoLitros) FROM Consumo c WHERE c.user = :user AND c.data BETWEEN :startDate AND :endDate")
    Double findTotalConsumoByUserAndPeriod(
        @Param("user") User user, 
        @Param("startDate") LocalDate startDate, 
        @Param("endDate") LocalDate endDate
    );
    
    // Buscar último registro de consumo do usuário
    @Query("SELECT c FROM Consumo c WHERE c.user = :user ORDER BY c.data DESC LIMIT 1")
    Optional<Consumo> findLastConsumoByUser(@Param("user") User user);
}