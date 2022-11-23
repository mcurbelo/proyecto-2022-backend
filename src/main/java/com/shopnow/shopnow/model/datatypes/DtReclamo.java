package com.shopnow.shopnow.model.datatypes;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.shopnow.shopnow.model.enumerados.TipoReclamo;
import com.shopnow.shopnow.model.enumerados.TipoResolucion;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Date;
import java.util.UUID;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class DtReclamo {

    private DtInfoCompra datosCompra;

    private TipoReclamo tipo;

    private TipoResolucion estado;

    @JsonFormat(pattern = "dd/MM/yyyy")
    private Date fechaRealizado;

    private String autor;

    private UUID idReclamo;

    private String descripcion;

    private Boolean tieneChat;
}
