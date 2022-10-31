package com.shopnow.shopnow.model.datatypes;

import com.shopnow.shopnow.model.enumerados.EstadoUsuario;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class DtFiltrosUsuario {
    private String nombre;
    private String apellido;
    private String correo;
    private EstadoUsuario estado;
}
