package com.shopnow.shopnow.model.datatypes;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class DtBalance {
    Float totalGanado, ganadoPorEnvio, ganadoPorRetiro, perdidoPorComision;
    Integer cantidadPorEnvio, cantidadPorRetiro;
}
