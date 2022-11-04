package com.shopnow.shopnow.model;

import lombok.*;

import javax.persistence.*;

@Entity
@Getter
@Setter

@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Direccion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false)
    private Integer id;

    @Column(nullable = false)
    private String calle;

    @Column(nullable = false)
    private String numero;

    @Column(nullable = false)
    private String departamento;

    @Column(nullable = false)
    private String localidad;

    private String notas;

    @Override
    public String toString() {
        return this.calle + " " + this.numero + ", " + localidad + ", " + this.departamento;
    }

}