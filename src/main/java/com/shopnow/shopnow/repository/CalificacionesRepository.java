package com.shopnow.shopnow.repository;

import com.shopnow.shopnow.model.Calificacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CalificacionesRepository extends JpaRepository<Calificacion, String> {
    List<Optional<Calificacion>> findAllById(UUID id);
}
