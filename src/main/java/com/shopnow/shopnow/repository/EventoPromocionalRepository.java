package com.shopnow.shopnow.repository;

import com.shopnow.shopnow.model.EventoPromocional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface EventoPromocionalRepository extends JpaRepository<EventoPromocional, UUID> {

}