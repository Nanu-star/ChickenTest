package com.example.chickentest.repository;

import com.example.chickentest.entity.Farm;
import com.example.chickentest.entity.Movement;
import com.example.chickentest.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovementRepository extends JpaRepository<Movement, Long> {
    List<Movement> findByUserAndFarm(User user, Farm farm);
    List<Movement> findByFarm(Farm farm);
}
