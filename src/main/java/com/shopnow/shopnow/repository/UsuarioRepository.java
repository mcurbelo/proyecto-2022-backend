package com.shopnow.shopnow.repository;

import com.shopnow.shopnow.model.Administrador;
import com.shopnow.shopnow.model.Compra;
import com.shopnow.shopnow.model.Generico;
import com.shopnow.shopnow.model.Usuario;
import com.shopnow.shopnow.model.enumerados.EstadoUsuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.persistence.Tuple;
import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {
    Optional<Usuario> findByCorreo(String correo);

    Optional<Usuario> findByCorreoAndEstado(String correo, EstadoUsuario estado);

    boolean existsByCorreoAndEstado(String correo, EstadoUsuario estado);

    Optional<Usuario> findByIdAndEstado(UUID id, EstadoUsuario estado);

    @Query(value = "select cast(id as varchar) from usuario where nombre like %?1% and estado != 'Eliminado'", nativeQuery = true)
    List<UUID> usuariosConNombre(String nombre);

//    @Query(value = "select cast(id as varchar) from usuario where nombre like %?1%" and estado != 'Eliminado', nativeQuery = true)
//    List<UUID> usuariosConNombre(String nombre);

    @Query(value = "select cast(id as varchar) from usuario where apellido like %?1% and estado != 'Eliminado'", nativeQuery = true)
    List<UUID> usuariosConApellido(String nombre);

    @Query(value = "select cast(id as varchar) from usuario where correo like %?1% and estado != 'Eliminado'", nativeQuery = true)
    List<UUID> usuariosConCorreo(String correo);

    @Query(value = "select cast(id as varchar) from usuario where estado=?1", nativeQuery = true)
    List<UUID> usuariosConEstado(String estado);

    @Query(value = "SELECT cast(id as varchar) FROM (Select lower(concat(nombre, apellido)) as nom, * FROM usuario) as nombreconcat where nom like %?1%", nativeQuery = true)
    List<UUID> usuarioNombreApellido(String nombre);

    Page<Usuario> findByIdIn(List<UUID> ids, Pageable pageable);

    @Query(value = "select * from usuario where estado != 'Eliminado'", countQuery = "select count(*) from usuario where estado != 'Eliminado'", nativeQuery = true)
    Page<Usuario> todosLosUsuarios(Pageable pageable);

    Optional<Usuario> findByResetPasswordToken(String id);

    @Modifying
    @Transactional
    @Query(value = "Update USUARIO set web_token='' where web_token=?1", nativeQuery = true)
    void quitarTokenWeb(String token);

    @Modifying
    @Transactional
    @Query(value = "Update USUARIO set mobile_token='' where mobile_token=?1", nativeQuery = true)
    void quitarTokenMobile(String token);

    @Query(value = "SELECT v FROM Usuario u left outer join u.ventas v where u.id=?1 and TYPE(u) = Generico and v.fecha>=?2 and v.fecha<=?3 and v.estado='Completada'")
    List<Compra> ventasPorFechaCompletadas(UUID idUsuario, Date fechaInicio, Date fechaFin);

    @Query(value = "SELECT v FROM Usuario u left outer join u.ventas v where u.id=?1 and TYPE(u) = Generico and v.estado='Completada'")
    List<Compra> ventasTotalesCompletadas(UUID idUsuario);

    @Query(value = "select p.nombre, CAST (SUM(cp.cantidad) AS INTEGER) as cantidad " +
            "from ((compra c join usuario_ventas uv on id=ventas_key) " +
            "join compra_producto cp on c.entrega_info=cp.id) join producto p on p.id=cp.producto_id " +
            " where uv.generico_id=?1 and c.estado='Completada' group by p.nombre order by cantidad DESC LIMIT 10", nativeQuery = true)
    List<Tuple> topteenProductosVendidos(UUID idUsuario);

    @Query(value = "select p.nombre, CAST (SUM(cp.cantidad) AS INTEGER) as cantidad " +
            "from ((compra c join usuario_ventas uv on id=ventas_key) " +
            "join compra_producto cp on c.entrega_info=cp.id) join producto p on p.id=cp.producto_id " +
            "where uv.generico_id=?1 and date(c.fecha)>=cast(?2 as date) and date(c.fecha)<=cast(?3 as date) and c.estado='Completada' " +
            "group by p.nombre order by cantidad DESC LIMIT 10", nativeQuery = true)
    List<Tuple> topteenProductosVendidosEntreFecha(UUID idUsuario, Date fechaInicio, Date fechaFin);


    @Query(value = "select p.nombre, cast (ROUND(CAST (AVG(cal.puntuacion) AS NUMERIC) ,2) as real) promedio, CAST (count(*) AS INTEGER) cantidadVentas from ((((compra c " +
            "join usuario_ventas uv on id=ventas_key) " +
            "join compra_producto cp on c.entrega_info=cp.id) " +
            "join producto p on p.id=cp.producto_id) " +
            "join compra_producto_calificaciones cpc on cpc.compra_producto_id=cp.id) " +
            "join calificacion cal on cpc.calificaciones_id=cal.id and cal.autor_id!=?1 " +
            "where uv.generico_id=?1 and c.estado='Completada' " +
            "group by p.nombre order by promedio DESC LIMIT 10", nativeQuery = true)
    List<Tuple> promedioCalificacionPorProducto(UUID idUsuario);


    @Query(value = "select p.nombre, cast (ROUND(CAST (AVG(cal.puntuacion) AS NUMERIC) ,2) as real) promedio, CAST (count(*) AS INTEGER) cantidadVentas from ((((compra c " +
            "join usuario_ventas uv on id=ventas_key) " +
            "join compra_producto cp on c.entrega_info=cp.id) " +
            "join producto p on p.id=cp.producto_id) " +
            "join compra_producto_calificaciones cpc on cpc.compra_producto_id=cp.id) " +
            "join calificacion cal on cpc.calificaciones_id=cal.id and cal.autor_id!=?1 " +
            "where uv.generico_id=?1 and c.estado='Completada' and date(c.fecha)>=cast(?2 as date) and date(c.fecha)<=cast(?3 as date)" +
            "group by p.nombre order by promedio DESC LIMIT 10", nativeQuery = true)
    List<Tuple> promedioCalificacionPorProductoEntreFecha(UUID idUsuario, Date fechaInicio, Date fechaFin);

    @Query(value = "SELECT u FROM Usuario u where TYPE(u) = Generico")
    List<Generico> usuariosSistema();

    @Query(value = "SELECT u FROM Usuario u where TYPE(u) = Generico and fechaRegistro>=?1 and fechaRegistro<=?2")
    List<Generico> usuariosSistemaRango(Date fechaInicio, Date fechaFin);

    @Query(value = "SELECT u FROM Usuario u where TYPE(u) = Administrador")
    List<Administrador> administradoresSistema();

    @Query(value = "SELECT u FROM Usuario u where TYPE(u) = Administrador and u.fechaRegistro>=?1 and u.fechaRegistro<=?2")
    List<Administrador> administradoresSistemaRango(Date fechaInicio, Date fechaFin);

    @Query("SELECT COUNT(u) FROM Usuario u")
    Integer totalUsuarios();
}