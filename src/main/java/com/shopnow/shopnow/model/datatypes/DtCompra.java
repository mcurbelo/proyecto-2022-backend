package com.shopnow.shopnow.model.datatypes;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.UUID;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class DtCompra {

    @NotNull
    private UUID idVendedor;

    @NotNull
    private UUID idProducto;

    @NotNull
    @Min(1)
    private Integer cantidad;

    private String codigoCanje;

    @NotBlank
    private String idTarjeta;

    @NotNull
    private Boolean esParaEnvio;

    private Integer idDireccionEnvio;

    private Integer idDireccionLocal;

}
