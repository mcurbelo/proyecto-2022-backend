package com.shopnow.shopnow.model.datatypes;

import com.shopnow.shopnow.model.enumerados.EstadoCompra;
import lombok.*;

import java.util.Date;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class DtFiltrosVentas {
    private String nombre;
    private Date fecha;
    private EstadoCompra estado;
}
