package com.shopnow.shopnow.model;

import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Calificacion {
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    private UUID id;

    @Column(updatable = false)
    private String comentario;

    @Column(nullable = false, updatable = false, precision=3, scale=2)
    private Float puntuacion;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private Generico autor;
}
