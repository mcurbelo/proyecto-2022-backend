package com.shopnow.shopnow.repository;

import com.shopnow.shopnow.model.Compra;
import com.shopnow.shopnow.model.Reclamo;
import com.shopnow.shopnow.model.enumerados.TipoResolucion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public interface ReclamoRepository extends JpaRepository<Reclamo, UUID> {


    @Query(value = "Select cast (id as varchar) from usuario_reclamos u join reclamo r on u.reclamos_key=r.id where u.generico_id=?1 and date(r.fecha)=cast(?2 as date)", nativeQuery = true)
    List<UUID> misReclamosHechosPorFecha(UUID id, String fecha);

    @Query(value = "Select cast (id as varchar) from usuario_reclamos u join reclamo r on reclamos_key=id where u.generico_id=?1 and r.tipo=?2", nativeQuery = true)
    List<UUID> misReclamosHechosPorTipo(UUID id, String estado);

    @Query(value = "Select cast (r.id as varchar) from (((usuario_reclamos u join reclamo r on reclamos_key=id) join (compra c join compra_producto cp on c.entrega_info=cp.id) on r.compra_id=c.id)) join producto p on p.id=cp.producto_id where u.generico_id=?1 and p.nombre like %?2%", nativeQuery = true)
    List<UUID> misReclamosHechosPorNombreProducto(UUID id, String nombre);

    @Query(value = "Select cast (r.id as varchar) from ((usuario_ventas uv join usuario u on uv.generico_id=u.id) join (usuario_reclamos ur join reclamo r on reclamos_key=id) on r.compra_id=ventas_key) join datos_vendedor dv on u.datos_vendedor_id= dv.id where ur.generico_id=?1 and coalesce(dv.nombre_empresa,u.nombre) like %?2% ", nativeQuery = true)
    List<UUID> misReclamosHechosPorNombreVendedor(UUID id, String nombre);

    @Query(value = "Select cast (id as varchar) from usuario_reclamos u join reclamo r on reclamos_key=id where u.generico_id=?1 and resuelto=?2", nativeQuery = true)
    List<UUID> misReclamosHechosPorResolucion(UUID id, String resuelto);

    @Query(value = "Select r.* from reclamo r join usuario_reclamos u on u.reclamos_key=r.id where u.generico_id=?1",
            countQuery = "Select count(r.*) from reclamo r join usuario_reclamos u on u.reclamos_key=r.id where u.generico_id=?1", nativeQuery = true)
    Page<Reclamo> misReclamosHechos(UUID id, Pageable pageable);

    Page<Reclamo> findByIdIn(List<UUID> ids, Pageable pageable);

    @Query(value = "select cast (id as varchar) from reclamo where date(fecha)=cast(?2 as date) and compra_id in (select ventas_key from usuario_ventas where generico_id=?1) ", nativeQuery = true)
    List<UUID> reclamosRecibidosPorFecha(UUID id, String fecha);

    @Query(value = "select cast (id as varchar) from reclamo where tipo=?2 and compra_id in (select ventas_key from usuario_ventas where generico_id=?1)", nativeQuery = true)
    List<UUID> reclamosRecibidosPorTipo(UUID id, String tipo);

    @Query(value = "select cast (id as varchar) from reclamo where compra_id in (select c.id from ((usuario_ventas uv join compra c on uv.ventas_key=c.id) join compra_producto cp on c.entrega_info=cp.id) join producto p on cp.producto_id=p.id where uv.generico_id=?1 and p.nombre like %?2%)", nativeQuery = true)
    List<UUID> reclamosRecibosPorNombreProducto(UUID id, String nombre);

    @Query(value = "select cast (r.id as varchar) from reclamo r join (usuario usu join usuario_reclamos ur on usu.id=ur.generico_id) on r.id=ur.reclamos_key where usu.nombre like %?2% and compra_id in (select ventas_key from usuario_ventas where generico_id=?1)", nativeQuery = true)
    List<UUID> reclamosRecibidosPorNombreComprador(UUID id, String nombre);

    @Query(value = "select cast (id as varchar) from reclamo where resuelto=?2 and compra_id in (select ventas_key from usuario_ventas where generico_id=?1)", nativeQuery = true)
    List<UUID> reclamosRecibidosPorEstado(UUID id, String resolucion);

    @Query(value = "select reclamo.* from reclamo where compra_id in (select ventas_key from usuario_ventas where generico_id=?1)",
            countQuery = "select reclamo.* from reclamo where compra_id in (select ventas_key from usuario_ventas where generico_id=?1)", nativeQuery = true)
    Page<Reclamo> misReclamosRecibidos(UUID id, Pageable pageable);

    Boolean existsByCompraAndResuelto(Compra compra, TipoResolucion resolucion);

    @Query(value = "SELECT r FROM Reclamo r where fecha>=?1 and fecha<=?2")
    List<Reclamo> reclamosPorRango(Date fechaInicio, Date fechaFin);

    @Query("SELECT COUNT(r) FROM Reclamo r")
    Integer totalReclamos();

    List<Reclamo> findByCompra(Compra compra);
}
