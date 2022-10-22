package com.shopnow.shopnow.model.datatypes;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotBlank;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@RequiredArgsConstructor
@SuperBuilder
public class DtUsuario {

   private String correo;
   private String password;
   private String nombre;
   private String apellido;
   private String telefono;
   @JsonFormat(pattern = "DD/MM/yyyy")
   public Date fechaNac;
   public DtImagen imagen;
   public DtDatosVendedor datosVendedor;
   private Float calificacion;


}
