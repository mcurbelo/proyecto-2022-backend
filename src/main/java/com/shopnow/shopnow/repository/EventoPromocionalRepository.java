package com.shopnow.shopnow.repository;

import com.shopnow.shopnow.model.EventoPromocional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface EventoPromocionalRepository extends JpaRepository<EventoPromocional, UUID> {

    @Query(value = "select evento_promocional.* from evento_promocional where now() >= fecha_inicio and now() < fecha_fin", nativeQuery = true)
    Optional<EventoPromocional> eventoActivo();
}