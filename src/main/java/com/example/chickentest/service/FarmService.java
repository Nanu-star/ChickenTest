package com.example.chickentest.service;

import com.example.chickentest.entity.Farm;
import com.example.chickentest.entity.User;
import com.example.chickentest.repository.FarmRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class FarmService {

    @Autowired
    private FarmRepository farmRepository;

    public Farm createFarm(String name, User owner) {
        Farm farm = new Farm(name, owner);
        return farmRepository.save(farm);
    }

    public List<Farm> getFarmsByOwner(User owner) {
        return farmRepository.findByOwner(owner);
    }

    public Optional<Farm> getFarmByIdAndOwner(Long farmId, User owner) {
        return farmRepository.findByIdAndOwner(farmId, owner);
    }

    public Optional<Farm> getFarmById(Long farmId) {
        return farmRepository.findById(farmId);
    }
}
