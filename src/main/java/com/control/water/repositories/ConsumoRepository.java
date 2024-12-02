package com.control.water.repositories;

import com.control.water.models.Consumo;
import com.control.water.models.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ConsumoRepository extends JpaRepository<Consumo, Long> {
    // Métodos existentes
    List<Consumo> findByUserOrderByDataDesc(User user);
    
    Optional<Consumo> findByUserAndData(User user, LocalDate data);
    
    @Query("SELECT c FROM Consumo c WHERE c.user = :user AND c.data BETWEEN :startDate AND :endDate ORDER BY c.data")
    List<Consumo> findConsumosByUserAndPeriod(
        @Param("user") User user, 
        @Param("startDate") LocalDate startDate, 
        @Param("endDate") LocalDate endDate
    );
    
    @Query("SELECT SUM(c.consumoLitros) FROM Consumo c WHERE c.user = :user AND c.data BETWEEN :startDate AND :endDate")
    Double findTotalConsumoByUserAndPeriod(
        @Param("user") User user, 
        @Param("startDate") LocalDate startDate, 
        @Param("endDate") LocalDate endDate
    );
    
    @Query("SELECT c FROM Consumo c WHERE c.user = :user ORDER BY c.data DESC LIMIT 1")
    Optional<Consumo> findLastConsumoByUser(@Param("user") User user);
    
    // Novos métodos
    @Query("SELECT c FROM Consumo c WHERE c.user = :user ORDER BY c.data DESC")
    List<Consumo> findLatestByUser(@Param("user") User user, Pageable pageable);
    
    @Query("SELECT AVG(c.consumoLitros) FROM Consumo c WHERE c.user = :user AND c.data BETWEEN :startDate AND :endDate")
    Double findAverageConsumoByUserAndPeriod(
        @Param("user") User user, 
        @Param("startDate") LocalDate startDate, 
        @Param("endDate") LocalDate endDate
    );
    
    @Query("SELECT COALESCE(SUM(CASE WHEN c.consumoLitros < 0 THEN ABS(c.consumoLitros) ELSE 0 END), 0) " +
           "FROM Consumo c WHERE c.user = :user AND c.data BETWEEN :startDate AND :endDate")
    Double findTotalEconomiaByUserAndPeriod(
        @Param("user") User user, 
        @Param("startDate") LocalDate startDate, 
        @Param("endDate") LocalDate endDate
    );
    
    @Query(value = "SELECT c.* FROM consumo c " +
                   "WHERE c.user_id = :userId AND c.data <= :date " +
                   "ORDER BY c.data DESC LIMIT 1", nativeQuery = true)
    Optional<Consumo> findLastConsumoBeforeDate(
        @Param("userId") Long userId, 
        @Param("date") LocalDate date
    );
}