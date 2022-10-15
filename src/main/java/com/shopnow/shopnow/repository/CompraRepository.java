package com.shopnow.shopnow.repository;

import com.shopnow.shopnow.model.Compra;
import com.shopnow.shopnow.model.enumerados.EstadoCompra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public interface CompraRepository extends JpaRepository<Compra, UUID> {

    @Query(value = "select cast(id as varchar) from compra c join usuario_ventas u on c.id=u.ventas_key where c.fecha=?1 and u.generico_id=?2", nativeQuery = true)
    List<UUID> ventasPorFechaYIdusuario(Date fecha, UUID usuario);


    @Query(value = "select cast(id as varchar) from compra c join usuario_ventas u on c.id=u.ventas_key where c.estado=?1 and u.generico_id=?2", nativeQuery = true)
    List<UUID> ventasPorEstadoYIdusuario(EstadoCompra estado, UUID usuario);

    @Query(value = "select cast(id as varchar) from (compra c join usuario_ventas u on c.id=u.ventas_key) join (usuario comprador join usuario_compras on id=generico_id) where u.generico_id=?1 and comprador.nombre=?2", nativeQuery = true)
    List<UUID> ventasPorIdUsuarioYNombreComprador(UUID idVendedor, String nombre);
}