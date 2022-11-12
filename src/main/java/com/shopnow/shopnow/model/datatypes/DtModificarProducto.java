package com.shopnow.shopnow.model.datatypes;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Date;
import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class DtModificarProducto {

    private String descripcion;
    private Date fechaFin;
    private Integer stock;
    private Float precio;
    private List<Integer> imagenesQuitar;
    private Boolean permiteEnvio;
}
