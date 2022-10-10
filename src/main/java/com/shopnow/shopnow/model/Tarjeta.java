package com.shopnow.shopnow.model;

import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.Size;
import java.util.UUID;

@Entity
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Tarjeta {

    @Id
    private String idTarjeta;

    @Column(nullable = false)
   //@Size(min=13)
    private Integer numero;

    @Column(nullable = false, updatable = false)
    private String sello;

    @Column(nullable = false, updatable = false)
    private String nombreTitular;
}
