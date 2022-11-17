package com.shopnow.shopnow.model.datatypes;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.shopnow.shopnow.model.enumerados.EstadoCompra;
import lombok.*;

import java.util.Date;

@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class DtFiltrosVentas {
    private String nombre;
    @JsonFormat(pattern = "dd/MM/yyyy")
    private Date fecha;
    private EstadoCompra estado;
}
