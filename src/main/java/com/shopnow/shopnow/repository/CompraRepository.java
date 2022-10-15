package com.shopnow.shopnow.repository;

import com.shopnow.shopnow.model.Compra;
import com.shopnow.shopnow.model.enumerados.EstadoCompra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public interface CompraRepository extends JpaRepository<Compra, UUID> {

    @Query(value = "select cast(id as varchar) from compra c join usuario_compras u on c.id=u.compras_key where c.fecha=?1 and u.generico_id=?2", nativeQuery = true)
    List<UUID> ventasPorFechaYIdusuario(Date fecha, UUID usuario);


    @Query(value = "select cast(id as varchar) from compra c join usuario_compras u on c.id=u.compras_key where c.estado=?1 and u.generico_id=?2", nativeQuery = true)
    List<UUID> ventasPorEstadoYIdusuario(EstadoCompra estado, UUID usuario);
}