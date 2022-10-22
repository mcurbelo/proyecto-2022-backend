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
public class DtCompraSlimVendedor {

    public UUID idCompra;

    public UUID idComprador;

    public String nombreComprador;

    public String nombreProducto;

    public Integer cantidad;

    @JsonFormat(pattern = "dd/MM/yyyy")
    public Date fecha;

    public EstadoCompra estadoCompra;

    public Float montoTotal;

    public Float montoUnitario;
}
