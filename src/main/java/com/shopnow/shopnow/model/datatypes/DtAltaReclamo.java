package com.shopnow.shopnow.model.datatypes;


import com.shopnow.shopnow.model.enumerados.TipoReclamo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class DtAltaReclamo {

    @NotBlank
    private String descripcion;

    @NotNull
    private TipoReclamo tipo;
}
