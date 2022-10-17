package com.shopnow.shopnow.model.datatypes;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.shopnow.shopnow.model.enumerados.EstadoProducto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Date;
import java.util.List;

@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class DtFiltosMisProductos {

    @JsonFormat(pattern = "dd/MM/yyyy")
    private Date fecha;

    private String nombre;

    private List<String> categorias;

    private EstadoProducto estado;

}
