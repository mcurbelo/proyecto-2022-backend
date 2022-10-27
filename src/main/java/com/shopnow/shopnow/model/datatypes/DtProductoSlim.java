package com.shopnow.shopnow.model.datatypes;


import lombok.*;

import java.util.UUID;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class DtProductoSlim {
    private UUID idProducto;

    private String nombre;

    private String imagen;

    private Float precio;

    private Integer stock;

    private Boolean permiteEnvio;
}
