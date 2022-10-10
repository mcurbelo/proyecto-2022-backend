package com.shopnow.shopnow.model;

import com.shopnow.shopnow.model.enumerados.TipoReporte;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Reporte {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    private UUID id;

    @Column(nullable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    private TipoReporte tipo;

    @Column(nullable = false, updatable = false)
    private Date fecha;

    @Column(updatable = false)
    private String descripcion;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "autor_id")
    private Usuario Autor;


}
