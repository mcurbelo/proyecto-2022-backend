package com.shopnow.shopnow.model.datatypes;

import com.shopnow.shopnow.model.Direccion;
import com.shopnow.shopnow.model.enumerados.EstadoSolicitud;
import lombok.*;
import lombok.experimental.SuperBuilder;

import javax.persistence.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
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
    private Map<Integer, Direccion> locales = new HashMap<>();

}
