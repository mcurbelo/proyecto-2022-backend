package com.shopnow.shopnow.model.datatypes;

import com.shopnow.shopnow.model.Comentario;
import com.shopnow.shopnow.model.Direccion;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class DtProducto {

    UUID idProducto; //Facilitar CU realizar compra

    UUID idVendedor; //Facilitar CU realizar compra

    List<String> imagenes;

    String nombre;

    String descripcion;

    Float precio;

    Boolean permiteEnvio;

    Map<UUID, Comentario> comentarios;

    //Informacion de vendedor

    String nombreVendedor;

    Float calificacion;

    String imagenDePerfil;

    Map<Integer, Direccion> localesParaRetiro; //Necesario para el realizar compra, segun lo definido cuando hicimos el DSS

}
