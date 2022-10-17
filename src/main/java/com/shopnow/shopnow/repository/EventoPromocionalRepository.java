package com.shopnow.shopnow.repository;

import com.shopnow.shopnow.model.EventoPromocional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface EventoPromocionalRepository extends JpaRepository<EventoPromocional, UUID> {

    @Query(value = "select evento_promocional.* from evento_promocional where now() >= fecha_inicio and now() < fecha_fin", nativeQuery = true)
    Optional<EventoPromocional> eventoActivo();

    @Query(value = "select evento_promocional.* from evento_promocional where now() >= fecha_inicio and now() < fecha_fin and id in (select evento_promocional_id from evento_promocional_productos where productos_key=?1) ", nativeQuery = true)
    Optional<EventoPromocional> eventoActivoConProducto(UUID idProducto);
}