package com.shopnow.shopnow.repository;

import com.shopnow.shopnow.model.Direccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface DireccionRepository extends JpaRepository<Direccion, Integer> {
    //Si ya pertenece a una emprsa

    @Query(value = "select exists (select locales_key from datos_vendedor_locales where locales_id=?1)", nativeQuery = true)
    boolean yaPerteneceAUnaEmpresa(Integer idLocal);
}
