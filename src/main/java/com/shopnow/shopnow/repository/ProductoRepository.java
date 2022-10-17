package com.shopnow.shopnow.repository;

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

    @Modifying
    @Transactional
    @Query(value = "delete from categoria_productos where productos_key=?1", nativeQuery = true)
    void eliminarProductoCategoria(UUID id);

    Optional<Producto> findByIdAndEstado(UUID id, EstadoProducto estado);

    @Query(value = "select cast(id as varchar) from producto where id in (select producto_id from evento_promocional_productos where evento_promocional_id = ?1) and position(nombre in ?2)", nativeQuery = true)
    List<UUID> buscarProductoEnEventoYporNombre(UUID idEvento, String nombre);

    Page<Producto> findByIdIn(Iterable<UUID> ids, Pageable pageable);

    @Query(value = "select cast(id as varchar) from producto where estado='Activo' and stock>0 and id in (select p.id from (usuario_productos up join producto p on up.productos_key=p.id) join usuario u on u.id=up.generico_id where u.estado='Activo')", nativeQuery = true)
    List<UUID> productosValidosParaListar();

    @Query(value = "select cast(generico_id as varchar)  from usuario_productos where productos_key=?1", nativeQuery = true)
    UUID vendedorProducto(UUID idProducto);

    @Query(value = "select cast(p.id as varchar) from usuario_productos up join producto p on p.id=up.productos_key where up.generico_id=?1 and date(p.fecha_inicio)=cast(?1 as date)", nativeQuery = true)
    List<UUID> misProductosPorFecha(UUID id, String fecha);

    @Query(value = "select cast(p.id as varchar) from usuario_productos up join producto p on p.id=up.productos_key where up.generico_id=?1 and p.estado=?2", nativeQuery = true)
    List<UUID> misProductosPorEstado(UUID id, String estado);

    @Query(value = "select cast(p.id as varchar) from usuario_productos up join producto p on p.id=up.productos_key where up.generico_id=?1 and p.nombre like %?2%", nativeQuery = true)
    List<UUID> misProductosConNombre(UUID id, String estado);

    @Query(value = "select cast(p.id as varchar) from (usuario_productos up join producto p on p.id=up.productos_key) join categoria_productos cp on p.id=cp.productos_id where up.generico_id=?1 and cp.categoria_nombre=?2", nativeQuery = true)
    List<UUID> misProductosEnCategoria(UUID id, String categoria);

    @Query(value = "select cast(p.id as varchar) from usuario_productos up join producto p on p.id=up.productos_key where up.generico_id=?1", nativeQuery = true)
    List<UUID> misProductos(UUID id);

    @Query(value = "select categoria_nombre from categoria_productos where productos_key=?1", nativeQuery = true)
    List<String> categoriasDelProducto(UUID id);
}
