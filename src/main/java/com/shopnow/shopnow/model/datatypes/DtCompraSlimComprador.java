package com.shopnow.shopnow.model.datatypes;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.shopnow.shopnow.model.enumerados.EstadoCompra;
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
public class DtCompraSlimComprador {

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

    private String imagenURL;

    private Boolean esEnvio;

    private Boolean puedeCompletar;

    private Boolean puedeCalificar;

    private Boolean puedeReclamar;
    
    @JsonFormat(pattern = "dd/MM/yyyy")
    private Date fechaEntrega;
}