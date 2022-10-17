package com.shopnow.shopnow.model.datatypes;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@SuperBuilder
public class DtImagen {
    private String data;
    private String nombre;
    private Integer tamaño;
    private String formato;
}
