package com.shopnow.shopnow.repository;

import com.shopnow.shopnow.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProductoRepository  extends JpaRepository<Producto, UUID> {
}
