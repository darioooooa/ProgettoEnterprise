package com.example.progettoenterprise.data.repositories;

import com.example.progettoenterprise.data.entities.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {
    Optional<Tag> findByNomeTag(String nomeTag);
}
