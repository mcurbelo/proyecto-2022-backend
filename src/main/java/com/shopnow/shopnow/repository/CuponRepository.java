package com.shopnow.shopnow.repository;

import com.shopnow.shopnow.model.Cupon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CuponRepository extends JpaRepository<Cupon, UUID> {

    Optional<Cupon> findByCodigoCanje(String codigo);

}