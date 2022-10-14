package com.shopnow.shopnow.model.datatypes;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class DtEventoInfo {
    List<DtProductoSlim> productos;
    private UUID id;
    private String nombre;
    private Date fechaFin;
    private List<String> categorias;
}
