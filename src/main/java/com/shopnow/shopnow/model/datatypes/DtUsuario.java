package com.shopnow.shopnow.model.datatypes;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.shopnow.shopnow.model.enumerados.Rol;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Date;

@Getter
@RequiredArgsConstructor
@SuperBuilder
public class DtUsuario {

    @JsonFormat(pattern = "DD/MM/yyyy")
    public Date fechaNac;
    public DtImagen imagen;
    public DtDatosVendedor datosVendedor;
    private String correo;
    private String password;
    private String nombre;
    private String apellido;
    private String telefono;
    private Float calificacion;
    private Rol rol;
    private String tokenWeb;
    private String tokenMobile;

}
