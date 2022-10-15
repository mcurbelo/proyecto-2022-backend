package com.shopnow.shopnow.repository;

import com.shopnow.shopnow.model.Producto;
import com.shopnow.shopnow.model.enumerados.EstadoProducto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.util.Optional;
import java.util.UUID;

public interface ProductoRepository extends JpaRepository<Producto, UUID> {
    @Modifying
    @Transactional
    @Query(value = "delete from categoria_productos where productos_key=?1", nativeQuery = true)
    void eliminarProductoCategoria(UUID id);


    Optional<Producto> findByIdAndEstado(UUID id, EstadoProducto estado);
}
