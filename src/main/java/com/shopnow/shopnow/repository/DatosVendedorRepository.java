package com.shopnow.shopnow.repository;

import com.shopnow.shopnow.model.DatosVendedor;
import com.shopnow.shopnow.model.Generico;
import com.shopnow.shopnow.model.enumerados.EstadoSolicitud;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface DatosVendedorRepository extends JpaRepository<DatosVendedor, Integer> {

    boolean existsByRutOrNombreEmpresaOrTelefonoEmpresa(String rut, String nombre, String tel);

    boolean existsByNombreEmpresa(String nombre);

    boolean existsByTelefonoEmpresa(String telefono);

    Page<DatosVendedor> findByEstadoSolicitud(EstadoSolicitud estadoSolicitud, Pageable pageable);

    @Query(value = "SELECT u FROM Usuario u left outer join u.datosVendedor d where d.id=?1 and TYPE(u) = Generico")
    Generico obtenerSolicitante(Integer id);
}
