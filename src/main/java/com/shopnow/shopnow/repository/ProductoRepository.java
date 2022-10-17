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
    @Modifying
    @Transactional
    @Query(value = "delete from categoria_productos where productos_key=?1", nativeQuery = true)
    void eliminarProductoCategoria(UUID id);

    Optional<Producto> findByIdAndEstado(UUID id, EstadoProducto estado);

    List<UUID> findByNombreContainingAndEstado(String nombre, EstadoProducto estado);

    @Query(value = "select cast(id as varchar) from producto where id in (select producto_id from evento_promocional_productos where evento_promocional_id = ?1) and position(nombre in ?2)", nativeQuery = true)
    List<UUID> buscarProductoEnEventoYporNombre(UUID idEvento, String nombre);

    Page<Producto> findByIdIn(Iterable<UUID> ids, Pageable pageable);

    @Query(value = "select cast(id as varchar) from producto where estado='Activo' and stock>0 and id in (select p.id from (usuario_productos up join producto p on up.productos_key=p.id) join usuario u on u.id=up.generico_id where u.estado='Activo')", nativeQuery = true)
    List<UUID> productosValidosParaListar();

    @Query(value = "select cast(generico_id as varchar)  from usuario_productos where productos_key=?1", nativeQuery = true)
    UUID vendedorProducto(UUID idProducto);

}
