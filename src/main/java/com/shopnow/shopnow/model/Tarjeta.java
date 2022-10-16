package com.shopnow.shopnow.model;

import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.util.UUID;

@Entity
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Tarjeta {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String idTarjeta;

    private String vencimiento;
    private String imageUrl;

    @Column(nullable = false, updatable = false)
    private String last4;

    @Column(nullable = false, updatable = false)
    private String token;
}
