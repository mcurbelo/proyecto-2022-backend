package com.shopnow.shopnow.model.datatypes;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.shopnow.shopnow.model.enumerados.EstadoCompra;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.UUID;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class DtInfoCompra {

    private UUID idCompra;

    private UUID idVendedor;

    private String nombreVendedor;

    private String nombreProducto;

    private Integer cantidad;

    @JsonFormat(pattern = "dd/MM/yyyy")
    private Date fecha;

    private EstadoCompra estadoCompra;

    private Float montoTotal;

    private Float montoUnitario;

    @JsonFormat(pattern = "dd/MM/yyyy HH:mm")
    private Date fechaEntrega;

    private String direccionEntrega;

    private Boolean esEnvio;

    private String avatarVendedor;

    private String avatarComprador;

    private String imagenProducto;
}
