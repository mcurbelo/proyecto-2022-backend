package com.shopnow.shopnow.repository;

import com.shopnow.shopnow.model.Usuario;
import com.shopnow.shopnow.model.enumerados.EstadoUsuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {
    Optional<Usuario> findByCorreo(String correo);

    Optional<Usuario> findByCorreoAndEstado(String correo, EstadoUsuario estado);

    boolean existsByCorreoAndEstado(String correo, EstadoUsuario estado);

    Optional<Usuario> findByIdAndEstado(UUID id, EstadoUsuario estado);
}