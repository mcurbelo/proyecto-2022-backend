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
public class DtCompraSlim {

    public UUID idCompra;

    public UUID idComprador;

    public String nombre;

    @JsonFormat(pattern = "dd/MM/yyyy")
    public Date fecha;

    public EstadoCompra estadoCompra;

    public Float montoTotal;
}
