package com.shopnow.shopnow.model.datatypes;

import com.shopnow.shopnow.model.enumerados.EstadoUsuario;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.UUID;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class DtUsuarioSlim {
    private UUID id;
    private String correo;
    private String nombre;
    private String apellido;
    private EstadoUsuario estadoUsuario;
}
