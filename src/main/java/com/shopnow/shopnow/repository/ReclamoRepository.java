package com.shopnow.shopnow.repository;

import com.shopnow.shopnow.model.Reclamo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface ReclamoRepository extends JpaRepository<Reclamo, UUID> {


    @Query(value = "Select cast (id as varchar) from usuario_reclamos u join reclamo r on reclamos_key=id where u.generico_id=?1 and date(r.fecha)=cast(?2 as date)", nativeQuery = true)
    List<UUID> misReclamosHechosPorFecha(UUID id, String fecha);

    @Query(value = "Select cast (id as varchar) from usuario_reclamos u join reclamo r on reclamos_key=id where u.generico_id=?1 and r.tipo=?2", nativeQuery = true)
    List<UUID> misReclamosHechosPorTipo(UUID id, String estado);

    @Query(value = "Select cast (r.id as varchar) from (((usuario_reclamos u join reclamo r on reclamos_key=id) join (compra c join compra_producto cp on c.entrega_info=cp.id) on r.compra_id=c.id)) join producto p on p.id=cp.producto_id where u.generico_id=?1 and p.nombre like %?2%", nativeQuery = true)
    List<UUID> misReclamosHechosPorNombreProducto(UUID id, String nombre);

    @Query(value = "Select cast (r.id as varchar) from (usuario_ventas uv join usuario u on uv.generico_id=u.id) join (usuario_reclamos ur join reclamo r on reclamos_key=id) on r.compra_id=ventas_key where ur.generico_id=?1 and u.nombre like %?2% ", nativeQuery = true)
    List<UUID> misReclamosHechosPorNombreVendedor(UUID id, String nombre);

    @Query(value = "Select cast (id as varchar) from usuario_reclamos u join reclamo r on reclamos_key=id where u.generico_id=?1 and resuelto='Devolucion' or resuelto='PorChat'", nativeQuery = true)
    List<UUID> misReclamosHechosResueltos(UUID id);

    @Query(value = "Select cast (id as varchar) from usuario_reclamos u join reclamo r on reclamos_key=id where u.generico_id=?1 and resuelto='NoResuelto'", nativeQuery = true)
    List<UUID> misReclamosHechosNoResueltos(UUID id);

    @Query(value = "Select r.* from usuario_reclamos u join reclamo r on reclamos_key=id where u.generico_id=?1",
            countQuery = "Select count(r.*) from usuario_reclamos u join reclamo r on reclamos_key=id where u.generico_id=?1", nativeQuery = true)
    Page<Reclamo> misReclamosHechos(UUID id, Pageable pageable);

    Page<Reclamo> findByIdIn(List<UUID> ids, Pageable pageable);

    @Query(value = "(select cast (id as varchar) from reclamo where date(fecha)=cast(?2 as date) and compra_id in (Select c.id from compra c join usuario_ventas u on c.id=u.ventas_key where u.generico_id=?1 and u.ventas_key) ", nativeQuery = true)
    List<UUID> reclamosRecibidosPorFecha(UUID id, String fecha);

    @Query(value = "select cast (id as varchar) from reclamo where tipo=?2 and compra_id in (select c.id from compra c join usuario_ventas u on c.id=u.ventas_key where u.generico_id=?1)", nativeQuery = true)
    List<UUID> reclamosRecibidosPorTipo(UUID id, String tipo);

    @Query(value = "select cast (id as varchar) from reclamo where compra_id in (select c.id from ((usuario_ventas uv join compra c on uv.ventas_key=c.id) join compra_producto cp on c.entrega_info=cp.id) join producto p on cp.producto_id=p.id) where u.generico_id=?1 and p.nombre like %?2%)", nativeQuery = true)
    List<UUID> reclamosRecibosPorNombreProducto(UUID id, String nombre);

    @Query(value = "select cast (r.id as varchar) from reclamo r join (usuario usu join usuario_reclamos ur on usu.id=ur.generico_id) on usu.id=ur.generico_id where usu.nombre like %?2% compra_id in (select c.id from compra c join usuario_ventas u on c.id=u.ventas_key)where u.generico_id=?1)", nativeQuery = true)
    List<UUID> reclamosRecibidosPorNombreComprador(UUID id, String nombre);


    @Query(value = "select cast (id as varchar) from reclamo where resuelto='NoResuelto' and compra_id in (select c.id from compra c join usuario_ventas u on c.id=u.ventas_key where u.generico_id=?1)", nativeQuery = true)
    List<UUID> reclamosRecibidosPorEstadoNoResuelto(UUID id);

    @Query(value = "select cast (id as varchar) from reclamo where resuelto='PorChat' or resuelto='Devolucion' and compra_id in (select c.id from compra c join usuario_ventas u on c.id=u.ventas_key where u.generico_id=?1)", nativeQuery = true)
    List<UUID> reclamosRecibidosPorEstadoResuelto(UUID id);

}