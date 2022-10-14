package com.shopnow.shopnow.repository;

import com.shopnow.shopnow.model.Producto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.util.List;
import java.util.UUID;

public interface ProductoRepository extends JpaRepository<Producto, UUID> {
    @Modifying
    @Transactional
    @Query(value = "delete from categoria_productos where productos_key=?1", nativeQuery = true)
    void eliminarProductoCategoria(UUID id);

    Page<Producto> findByNombreContaining(String nombre, Pageable pageable);

    @Query(value = "select * from producto where id in (select producto_id from evento_promocional_productos where evento_promocional_id = ?1) and position(nombre in ?2)", nativeQuery = true)
    Page<Producto> buscarProductoEnEventoYporNombre(UUID idEvento, String nombre, Pageable pageable);

    @Query(value = "select cast(id as varchar) from producto where id in (select producto_id from evento_promocional_productos where evento_promocional_id = ?1) and position(nombre in ?2)", nativeQuery = true)
    List<UUID> buscarProductoEnEventoYporNombre(UUID idEvento, String nombre);

    Page<Producto> findByIdIn(Iterable<UUID> ids, Pageable pageable);


}
