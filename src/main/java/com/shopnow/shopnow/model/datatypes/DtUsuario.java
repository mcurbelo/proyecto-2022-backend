package com.shopnow.shopnow.model.datatypes;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@RequiredArgsConstructor
public class DtUsuario {
    @JsonFormat(pattern = "dd/MM/yyyy")
    public Date fechaNac;
    private String correo;
    private String password;
    private String nombre;
    private String apellido;
    private String telefono;
}
