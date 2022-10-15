package com.shopnow.shopnow.model.datatypes;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.util.UUID;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class DtCompra {

    @NotBlank
    private UUID IdComprador; //O correo?

    @NotBlank
    private String correoComprador;

    @NotBlank
    private UUID idVendedor;

    @NotBlank
    private UUID idProducto;

    @NotBlank
    @Min(1)
    private Integer cantidad;

    private String codigoCanje;

    @NotBlank
    private String idTarjeta;


    @NotBlank
    private Boolean esParaEnvio;

    private Integer IdDireccionEnvio;

    private Integer IdDireccionLocal;

}
