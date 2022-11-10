package com.shopnow.shopnow.model;

import com.shopnow.shopnow.model.enumerados.EstadoCompra;
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
@Builder
public class Compra {
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    private UUID id;

    @Column(nullable = false, updatable = false)
    private Date fecha;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private EstadoCompra estado = EstadoCompra.EsperandoConfirmacion;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "tarjeta_pago_id", updatable = false, nullable = false)
    private Tarjeta tarjetaPago;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "entrega_info", nullable = false, updatable = false)
    private CompraProducto infoEntrega;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private Cupon cuponAplicado;

    @Column(nullable = false, unique = true)
    private String idTransaccion;

    private String idChat;
}
