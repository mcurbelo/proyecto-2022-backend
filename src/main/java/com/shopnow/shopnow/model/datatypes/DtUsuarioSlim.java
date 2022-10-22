package com.shopnow.shopnow.model.datatypes;

import com.shopnow.shopnow.model.enumerados.EstadoUsuario;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import java.util.UUID;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class DtUsuarioSlim {
    private UUID id;
    @NotBlank
    private String correo;
    @NotBlank
    private String nombre;
    @NotBlank
    private String apellido;
    private EstadoUsuario estadoUsuario;
}
