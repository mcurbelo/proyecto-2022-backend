package com.shopnow.shopnow.repository;

import com.shopnow.shopnow.model.Reclamo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ReclamoRepository extends JpaRepository<Reclamo, UUID> {

}