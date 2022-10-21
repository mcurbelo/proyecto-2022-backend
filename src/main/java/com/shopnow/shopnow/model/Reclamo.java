package com.shopnow.shopnow.model;

import com.shopnow.shopnow.model.enumerados.TipoReclamo;
import com.shopnow.shopnow.model.enumerados.TipoResolucion;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

@Entity
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Reclamo {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    private UUID id;

    @Column(nullable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    private TipoReclamo tipo;

    @Column(nullable = false, updatable = false)
    private Date fecha;

    @Column(nullable = false, updatable = false)
    private String descripcion;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TipoResolucion resuelto;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "compra_id")
    private Compra compra;
}
