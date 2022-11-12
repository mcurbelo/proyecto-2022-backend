package com.shopnow.shopnow.repository;

import com.shopnow.shopnow.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface CategoriaRepository extends JpaRepository<Categoria, String> {
    @Query(value = "Select nombre from categorias c join categorias_productos cp on c.nombre=cp.categoria_nombre where productos_key=?1", nativeQuery = true)
    List<String> categoriasProducto(UUID idProducto);

}