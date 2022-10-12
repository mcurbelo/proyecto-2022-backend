package com.shopnow.shopnow.repository;

import com.shopnow.shopnow.model.DatosVendedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DatosVendedorRepository extends JpaRepository<DatosVendedor, Integer> {


}
