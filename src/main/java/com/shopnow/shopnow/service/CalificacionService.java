package com.shopnow.shopnow.service;

import com.shopnow.shopnow.controller.responsetypes.Excepcion;
import com.shopnow.shopnow.model.Calificacion;
import com.shopnow.shopnow.model.Compra;
import com.shopnow.shopnow.model.Generico;
import com.shopnow.shopnow.model.datatypes.DtCalificacion;
import com.shopnow.shopnow.model.enumerados.EstadoCompra;
import com.shopnow.shopnow.model.enumerados.EstadoUsuario;
import com.shopnow.shopnow.repository.CompraRepository;
import com.shopnow.shopnow.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CalificacionService {

    @Autowired
    CompraRepository compraRepository;

    @Autowired
    UsuarioRepository usuarioRepository;


    public void agregarCalificacion(UUID idCompra, DtCalificacion datos, UUID idUsuario, Boolean esComprador) {
        //Si EsComprador es true, es porque califica la compra. De lo contrario Vendedor califica a Comprador

        Compra compra = compraRepository.findById(idCompra).orElseThrow();
        Generico usuario = (Generico) usuarioRepository.findByIdAndEstado(idUsuario, EstadoUsuario.Activo).orElse(null);

        if (usuario == null) {
            throw new Excepcion("Este usuario no esta habilitado para utilizar esta funcionalidad");
        }

        if (compra.getEstado() != EstadoCompra.Completada) {
            throw new Excepcion("Esta funcionalidad esta habilitada solo para compras completadas");
        }

        if (esComprador && compra.getInfoEntrega().getCalificacion() != null) {
            throw new Excepcion("Esta compra ya tiene una calificacion. Solo se puede calificar una vez");
        }

        //TODO falta validacion para que Vendedor pueda califacar solo una vez al Comprador

        //Logica

        Calificacion calificacion;

        if (esComprador) {
            calificacion = new Calificacion(null, datos.getComentario(), datos.getPuntuacion(), usuario);
            compra.getInfoEntrega().setCalificacion(calificacion);
        } else {
            calificacion = new Calificacion(null, datos.getComentario(), datos.getPuntuacion(), usuario);
        }


    }
}
