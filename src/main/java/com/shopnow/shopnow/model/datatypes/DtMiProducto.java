package com.shopnow.shopnow.model.datatypes;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.shopnow.shopnow.model.enumerados.EstadoProducto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class DtMiProducto {

    private UUID idProducto;

    private String nombre;

    private List<String> imagenes;
    @JsonFormat(pattern = "dd/MM/yyyy")
    private Date fechaInicio;
    @JsonFormat(pattern = "dd/MM/yyyy")
    private Date fechaFin;

    private List<String> categorias;

    private Float precio;

    private Integer stock;

    private EstadoProducto estado;

}
