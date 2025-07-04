package com.chickentest.repository;

import com.chickentest.domain.Movement;
import com.chickentest.domain.MovementType;
import com.chickentest.domain.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MovementRepository extends JpaRepository<Movement, Long> {

    @Query("SELECT COUNT(*) FROM Movement m WHERE m.type = :type")
    Long countProducedBatches(@Param("type") MovementType type);

    @Query("SELECT SUM(m.amount) FROM Movement m WHERE m.type = :type")
    Double calculateTotalSales(@Param("type") MovementType type);

    List<Movement> findAllByUser(User user);
}
