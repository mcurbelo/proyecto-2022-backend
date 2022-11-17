package com.shopnow.shopnow.model.datatypes;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class DtDireccion {

    private Integer id;

    @NotBlank
    private String calle;

    @NotBlank
    private String numero;

    @NotBlank
    private String departamento;

    @NotBlank
    private String localidad;

    private String notas;
    @NotNull
    private Boolean esLocal;
}