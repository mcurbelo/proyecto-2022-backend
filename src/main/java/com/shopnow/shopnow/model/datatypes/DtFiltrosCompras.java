package com.shopnow.shopnow.model.datatypes;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.shopnow.shopnow.model.enumerados.EstadoCompra;
import lombok.*;

import java.util.Date;

@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class DtFiltrosCompras {
    @JsonFormat(pattern = "dd/MM/yyyy")
    private Date fecha;
    private String nombreVendedor;
    private String nombreProducto;
    private EstadoCompra estado;
}
