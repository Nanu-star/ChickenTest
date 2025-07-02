package com.example.chickentest.repository;

import com.example.chickentest.entity.Farm;
import com.example.chickentest.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FarmRepository extends JpaRepository<Farm, Long> {
    List<Farm> findByOwner(User owner);
    Optional<Farm> findByIdAndOwner(Long id, User owner);
}
