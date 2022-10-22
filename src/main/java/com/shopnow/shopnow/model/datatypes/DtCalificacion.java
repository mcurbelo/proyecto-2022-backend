package com.shopnow.shopnow.model.datatypes;

import com.shopnow.shopnow.model.Generico;
import lombok.*;
import lombok.experimental.SuperBuilder;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@SuperBuilder
public class DtCalificacion {

    private UUID id;

    private String comentario;

    private Float puntuacion;

    private Generico autor;
}
