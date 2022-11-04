package com.shopnow.shopnow.repository;

import com.shopnow.shopnow.model.Calificacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CalificacionRepository extends JpaRepository<Calificacion, UUID> {

}