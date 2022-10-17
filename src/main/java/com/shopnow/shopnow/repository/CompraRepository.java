package com.shopnow.shopnow.repository;

import com.shopnow.shopnow.model.Compra;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface CompraRepository extends JpaRepository<Compra, UUID> {

    @Query(value = "select cast(id as varchar) from compra c join usuario_ventas u on c.id=u.ventas_key where date(c.fecha)=cast(?1 as date) and u.generico_id=?2", nativeQuery = true)
    List<UUID> ventasPorFechaYIdusuario(String fecha, UUID usuario);

    @Query(value = "select cast(id as varchar) from compra c join usuario_ventas u on c.id=u.ventas_key where c.estado=?1 and u.generico_id=?2", nativeQuery = true)
    List<UUID> ventasPorEstadoYIdusuario(String estado, UUID usuario);

    @Query(value = "select cast(uv.ventas_key as varchar) from usuario_ventas uv join (usuario comprador join usuario_compras uc on id=generico_id) ON uv.ventas_key=uc.compras_key where uv.generico_id=?1 and comprador.nombre=?2", nativeQuery = true)
    List<UUID> ventasPorIdUsuarioYNombreComprador(UUID idVendedor, String nombre);

    @Query(value = "select cast(id as varchar) from compra c join usuario_ventas u on c.id=u.ventas_key where u.generico_id=?1", nativeQuery = true)
    List<UUID> ventasPorIdUsuario(UUID idVendedor);

    Page<Compra> findByIdIn(List<UUID> ids, Pageable pageable);

    @Query(value = "select CAST(id as VARCHAR) from (usuario g join usuario_compras u on g.id=u.generico_id) where compras_key=?1", nativeQuery = true)
    UUID obtenerComprador(UUID idCompra);

    @Query(value = "select cast(id as varchar) from compra c join usuario_compras u on c.id=u.compras_key where date(c.fecha)=cast(?1 as date) and u.generico_id=?2", nativeQuery = true)
    List<UUID> comprasPorFechaYIdusuario(String fecha, UUID usuario);

    @Query(value = "select cast(id as varchar) from compra c join usuario_compras u on c.id=u.compras_key where c.estado=?1 and u.generico_id=?2", nativeQuery = true)
    List<UUID> comprasPorEstadoYIdusuario(String estado, UUID usuario);

    @Query(value = "select cast(uc.compras_key as varchar) from usuario_compras uc join (usuario vendedor join usuario_ventas uv on id=generico_id) ON uv.ventas_key=uc.compras_key where uc.generico_id=?1 and vendedor.nombre=?2", nativeQuery = true)
    List<UUID> comprasPorIdUsuarioYNombreVendedor(UUID idComprador, String nombre);

    @Query(value = "Select cast(c.id as varchar) from (Compra c join usuario_compras uc on uc.compras_key=c.id) join (compra_producto cp join producto p on p.id=cp.producto_id)on cp.id=c.entrega_info where uc.generico_id=?1 and p.nombre like %?2%", nativeQuery = true)
    List<UUID> comprasPorIdUsuarioYNombreProducto(UUID idComprador, String nombre);

    @Query(value = "select cast(id as varchar) from compra c join usuario_compras u on c.id=u.compras_key where u.generico_id=?1", nativeQuery = true)
    List<UUID> comprasPorIdUsuario(UUID idComprador);

    @Query(value = "select CAST(id as VARCHAR) from (usuario g join usuario_ventas u on g.id=u.generico_id) where ventas_key=?1", nativeQuery = true)
    UUID obtenerVendedor(UUID idCompra);

    @Query(value = "select p.nombre from (compra c join compra_producto cp on c.entrega_info=cp.id) join producto p on p.id=cp.producto_id where c.id=?1", nativeQuery = true)
    String obtenerNombreProducto(UUID idCompra);
}