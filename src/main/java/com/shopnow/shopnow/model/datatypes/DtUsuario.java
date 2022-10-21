package com.shopnow.shopnow.model.datatypes;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotBlank;
import java.util.Date;

@Getter
@Setter
@RequiredArgsConstructor
@SuperBuilder
public class DtUsuario {
    @JsonFormat(pattern = "dd/MM/yyyy")
    public Date fechaNac;
    public DtImagen imagen;
    @NotBlank
    private String correo;
    @NotBlank
    private String password;
    @NotBlank
    private String nombre;
    @NotBlank
    private String apellido;
    private String telefono;

}
