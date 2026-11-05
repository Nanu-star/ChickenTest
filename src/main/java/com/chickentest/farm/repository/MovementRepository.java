package com.chickentest.farm.repository;

import com.chickentest.farm.domain.Movement;
import com.chickentest.farm.domain.MovementType;
import com.chickentest.user.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MovementRepository extends JpaRepository<Movement, Long> {

  @Query("SELECT COUNT(*) FROM Movement m WHERE m.type = :type")
  Long countProducedBatches(@Param("type") MovementType type);

  @Query("SELECT SUM(m.amount) FROM Movement m WHERE m.type = :type")
  Double calculateTotalSales(@Param("type") MovementType type);

  List<Movement> findAllByUser(User user);

  boolean existsByArticleIdAndType(Long articleId, MovementType type);
}
