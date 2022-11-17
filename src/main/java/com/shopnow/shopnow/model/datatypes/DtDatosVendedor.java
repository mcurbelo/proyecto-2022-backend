package com.shopnow.shopnow.model.datatypes;

import com.shopnow.shopnow.model.Direccion;
import com.shopnow.shopnow.model.enumerados.EstadoSolicitud;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.util.Map;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@SuperBuilder
public class DtDatosVendedor {

    private String nombreEmpresa;
    private String rut;
    private String telefonoEmpresa;
    private EstadoSolicitud estadoSolicitud;
    private float calificacion;
    private Map<Integer, Direccion> locales;

}
