package com.shopnow.shopnow.model;

import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Entity
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Comentario {
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    private UUID id;

    @Column(nullable = false, updatable = false)
    private String comentario;

    @Column(nullable = false, updatable = false)
    private Date fecha;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private Map<UUID,Comentario> respuestas  = new HashMap<>();

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private Usuario autor;
}
