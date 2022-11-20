package com.shopnow.shopnow.model.datatypes;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class DtAdminEst {
        private Integer cantidad;
        private Integer cantidadActivos;
        private Integer cantidadBloqueados;
        private Integer cantidadEliminados;
}
