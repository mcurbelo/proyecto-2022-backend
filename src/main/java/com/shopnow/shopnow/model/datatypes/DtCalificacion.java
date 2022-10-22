package com.shopnow.shopnow.model.datatypes;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import java.util.UUID;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class DtCalificacion {
    @NotNull
    private Float puntuacion;

    private String comentario;

    @NotNull
    private UUID autor;
}
