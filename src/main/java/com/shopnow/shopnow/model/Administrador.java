package com.shopnow.shopnow.model;

import lombok.*;
import lombok.experimental.SuperBuilder;

import javax.persistence.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Entity
@DiscriminatorValue("Administrador")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class Administrador extends Usuario{

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Builder.Default
    private Map<UUID, EventoPromocional> eventosCreados = new HashMap<>();

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Builder.Default
    private Map<UUID, Cupon> cuponesCreados = new HashMap<>();

}
