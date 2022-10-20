package com.shopnow.shopnow.repository;

import com.shopnow.shopnow.model.Usuario;
import com.shopnow.shopnow.model.enumerados.EstadoUsuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {
    Optional<Usuario> findByCorreo(String correo);

    Optional<Usuario> findByCorreoAndEstado(String correo, EstadoUsuario estado);

    boolean existsByCorreoAndEstado(String correo, EstadoUsuario estado);

    Optional<Usuario> findByIdAndEstado(UUID id, EstadoUsuario estado);


    @Query(value = "select cast(id as varchar) from usuario where nombre like %?1%", nativeQuery = true)
    List<UUID> usuariosConNombre(String nombre);

    @Query(value = "select cast(id as varchar) from usuario where apellido like %?1%", nativeQuery = true)
    List<UUID> usuariosConApellido(String nombre);

    @Query(value = "select cast(id as varchar) from usuario where correo like %?1%", nativeQuery = true)
    List<UUID> usuariosConCorreo(String correo);

    @Query(value = "select cast(id as varchar) from usuario where estado=?1", nativeQuery = true)
    List<UUID> usuariosConEstado(String estado);

    Page<Usuario> findByIdIn(List<UUID> ids, Pageable pageable);

    @Query(value = "select * from usuario", countQuery = "select count(*) from usuario", nativeQuery = true)
    Page<Usuario> todosLosUsuarios(Pageable pageable);
}