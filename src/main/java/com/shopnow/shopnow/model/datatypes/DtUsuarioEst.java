package com.shopnow.shopnow.model.datatypes;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class DtUsuarioEst {
    private Integer cantidadVendedores;
    private Integer cantidadSoloCompradores;
    private Integer cantidadActivos;
    private Integer cantidadBloqueados;
    private Integer cantidadEliminados;
}
