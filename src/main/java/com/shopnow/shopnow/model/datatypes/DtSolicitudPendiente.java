package com.shopnow.shopnow.model.datatypes;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class DtSolicitudPendiente {

    private Integer id;

    private UUID idSolicitante;

    private DtMiProducto producto;

    private String nombreApellido;

    private float calificacion;

    private String imagenPerfil;

    private String correo;

    private String telefono;

    private String nombreEmpresa;

    private String telefonoEmpresa;

    private String rut;

    private String direccionLocal;
}
