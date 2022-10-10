package com.shopnow.shopnow.model;

import com.shopnow.shopnow.model.enumerados.EstadoCompra;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Compra {
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    private UUID id;

    @Column(nullable = false, updatable = false)
    private Date fecha;

    @Enumerated(EnumType.STRING)
    private EstadoCompra estado;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "tarjeta_pago_id", updatable = false, nullable = false)
    private Tarjeta tarjetaPago;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name= "entrega_info", nullable = false, updatable = false)
    private CompraProducto InfoEntrega;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private Cupon cuponAplicado;
}
