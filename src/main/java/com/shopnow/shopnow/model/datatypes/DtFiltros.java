package com.shopnow.shopnow.model.datatypes;

import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class DtFiltros {
    private String nombre;
    private List<String> categorias;
    private UUID idEventoPromocional;
    private Boolean recibirInfoEventoActivo;
}
