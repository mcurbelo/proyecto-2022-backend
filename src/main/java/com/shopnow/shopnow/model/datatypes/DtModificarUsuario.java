package com.shopnow.shopnow.model.datatypes;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class DtModificarUsuario {

    private String correo;

    private String contrasenaNueva;

    private String contrasenaVieja;

    private String telefonoContacto;

    //En caso de ser empresa

    private String nombreEmpresa;

    private String telefonoEmpresa;
}
