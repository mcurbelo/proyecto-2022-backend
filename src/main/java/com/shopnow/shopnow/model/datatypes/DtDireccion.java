package com.shopnow.shopnow.model.datatypes;

import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class DtDireccion {

    @NotBlank
    private String calle;

    @NotBlank
    private String numero;

    @NotBlank
    private String departamento;

    private String notas;
    @NotNull
    private Boolean esLocal;
}