package com.shopnow.shopnow.repository;

import com.shopnow.shopnow.model.Generico;
import com.shopnow.shopnow.model.Producto;
import com.shopnow.shopnow.model.enumerados.EstadoProducto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.util.Optional;
import java.util.List;

import java.util.UUID;

public interface ProductoRepository extends JpaRepository<Producto, UUID> {
    List<Producto> findByNombreContainingIgnoreCaseAndEstado(String nombre, EstadoProducto estado);

    @Query(value = "select cast(id as varchar) from producto where estado='Activo' and stock>0 and COALESCE(fecha_fin,now())>=now() and nombre like %?1%", nativeQuery = true)
    List<UUID> productosContenganNombre(String nombre);

    @Modifying
    @Transactional
    @Query(value = "delete from categoria_productos where productos_key=?1", nativeQuery = true)
    void eliminarProductoCategoria(UUID id);

    Optional<Producto> findByIdAndEstado(UUID id, EstadoProducto estado);

    @Query(value = "select cast(id as varchar) from producto where id in (select producto_id from evento_promocional_productos where evento_promocional_id = ?1) and position(nombre in ?2)", nativeQuery = true)
    List<UUID> buscarProductoEnEventoYporNombre(UUID idEvento, String nombre);

    Page<Producto> findByIdIn(Iterable<UUID> ids, Pageable pageable);

    @Query(value = "select producto.* from producto where estado='Activo' and stock>0  and COALESCE(fecha_fin,now())>=now() and id in (select p.id from (usuario_productos up join producto p on up.productos_key=p.id) join usuario u on u.id=up.generico_id where u.estado='Activo')",
            countQuery = "select count(*) from producto where estado='Activo' and stock>0 and id in (select p.id from (usuario_productos up join producto p on up.productos_key=p.id) join usuario u on u.id=up.generico_id where u.estado='Activo')", nativeQuery = true)
    Page<Producto> productosValidosParaListar(Pageable pageable);

    @Query(value = "SELECT b FROM Usuario b left outer join b.productos c where c.id=?1 and TYPE(b) = Generico")
    Generico vendedorProducto(UUID idCompra);

    @Query(value = "select cast(p.id as varchar) from usuario_productos up join producto p on p.id=up.productos_key where up.generico_id=?1 and date(p.fecha_inicio)=cast(?2 as date)", nativeQuery = true)
    List<UUID> misProductosPorFecha(UUID id, String fecha);

    @Query(value = "select cast(p.id as varchar) from usuario_productos up join producto p on p.id=up.productos_key where up.generico_id=?1 and p.estado=?2", nativeQuery = true)
    List<UUID> misProductosPorEstado(UUID id, String estado);

    @Query(value = "select cast(p.id as varchar) from usuario_productos up join producto p on p.id=up.productos_key where up.generico_id=?1 and p.nombre like %?2%", nativeQuery = true)
    List<UUID> misProductosConNombre(UUID id, String estado);

    @Query(value = "select cast(p.id as varchar) from (usuario_productos up join producto p on p.id=up.productos_key) join categoria_productos cp on p.id=cp.productos_id where up.generico_id=?1 and cp.categoria_nombre=?2", nativeQuery = true)
    List<UUID> misProductosEnCategoria(UUID id, String categoria);

    @Query(value = "select cast(p.id as varchar) from usuario_productos up join producto p on p.id=up.productos_key where up.generico_id=?1",
            countQuery = "select count(*) from usuario_productos up join producto p on p.id=up.productos_key where up.generico_id=?1", nativeQuery = true)
    Page<Producto> misProductos(UUID id, Pageable pageable);

    @Query(value = "select categoria_nombre from categoria_productos where productos_key=?1", nativeQuery = true)
    List<String> categoriasDelProducto(UUID id);

    @Query(value = "select CAST(id as VARCHAR) from (usuario g join usuario_compras u on g.id=u.generico_id) where compras_key=?1", nativeQuery = true)
    UUID obtenerIdComprador(UUID idCompra);
}
