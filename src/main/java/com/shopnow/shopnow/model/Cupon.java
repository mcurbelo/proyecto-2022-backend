package com.shopnow.shopnow.model;

import lombok.*;
import org.checkerframework.common.aliasing.qual.Unique;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
import java.util.Date;
import java.util.UUID;

@Entity
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Cupon {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    private UUID id;

    @Column(unique = true)
    private String codigoCanje;

    @Column(nullable = false, updatable = false)
    private String nombre;

    @Column(nullable = false, updatable = false)
    private Date validez;

    @Column(nullable = false, updatable = false)
    @Min(5)
    @Max(100)
    private Integer descuento;

}

