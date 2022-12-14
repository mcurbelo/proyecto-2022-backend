package com.shopnow.shopnow.model.datatypes;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.shopnow.shopnow.model.enumerados.TipoReclamo;
import com.shopnow.shopnow.model.enumerados.TipoResolucion;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Date;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class DtFiltroReclamo {

    @JsonFormat(pattern = "dd/MM/yyyy")
    private Date fecha;

    private String nombreProducto;

    private String nombreUsuario;

    private TipoReclamo tipo;

    private TipoResolucion resolucion;
}
