package com.shopnow.shopnow.model.datatypes;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.google.common.primitives.Bytes;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.format.annotation.DateTimeFormat;


import java.util.Date;

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

}
