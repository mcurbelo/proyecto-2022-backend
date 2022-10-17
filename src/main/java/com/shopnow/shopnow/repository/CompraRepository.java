package com.shopnow.shopnow.repository;

import com.shopnow.shopnow.model.Compra;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CompraRepository extends JpaRepository<Compra, UUID> {

}