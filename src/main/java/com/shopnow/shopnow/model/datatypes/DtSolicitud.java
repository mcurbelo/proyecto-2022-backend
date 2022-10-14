package com.shopnow.shopnow.model.datatypes;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class DtSolicitud {
    @NotBlank
    private String email;

    private String nombreEmpresa;

    private String rut;

    private String telefonoEmpresa;

    @NotNull
    private DtAltaProducto producto;

    private DtDireccion local;

    private Integer idDireccion;
}
