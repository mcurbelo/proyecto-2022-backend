package com.shopnow.shopnow.service;

import com.shopnow.shopnow.controller.responsetypes.Excepcion;
import com.shopnow.shopnow.model.Calificacion;
import com.shopnow.shopnow.model.Compra;
import com.shopnow.shopnow.model.Generico;
import com.shopnow.shopnow.model.datatypes.DtCalificacion;
import com.shopnow.shopnow.model.enumerados.EstadoCompra;
import com.shopnow.shopnow.model.enumerados.EstadoUsuario;
import com.shopnow.shopnow.repository.CalificacionRepository;
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

    @Autowired
    CalificacionRepository calificacionRepository;


    public void agregarCalificacion(UUID idCompra, DtCalificacion datos) {
        //Si EsComprador es true, es porque califica la compra. De lo contrario Vendedor califica a Comprador

        Compra compra = compraRepository.findById(idCompra).orElseThrow();
        Generico usuario = (Generico) usuarioRepository.findByIdAndEstado(datos.getAutor(), EstadoUsuario.Activo).orElse(null);
        boolean esComprador = false;
        Generico comprador = compraRepository.obtenerComprador(idCompra);

        if (usuario == null) {
            throw new Excepcion("Este usuario invalido");
        }
        if (usuario.getId().compareTo(compraRepository.obtenerComprador(idCompra).getId()) == 0) {
            esComprador = true;
        }

        if (!esComprador && usuario.getId().compareTo(compraRepository.obtenerVendedor(idCompra).getId()) != 0) {
            throw new Excepcion("Este usuario no participo en la compra");
        }

        if (compra.getEstado() != EstadoCompra.Completada) {
            throw new Excepcion("Esta funcionalidad esta habilitada solo para compras completadas");
        }

        if (!compra.getInfoEntrega().getCalificaciones().isEmpty()) {
            for (Calificacion calificacionItem : compra.getInfoEntrega().getCalificaciones()) {
                if (calificacionItem.getAutor().getId() == usuario.getId()) {
                    throw new Excepcion("Esta compra ya tiene una calificacion de este usuario. Solo se puede calificar una vez");
                }
            }
        }
        if (datos.getPuntuacion() > 5) {
            throw new Excepcion("Puntuacion invalida");
        }
        //Logica
        Calificacion calificacion;
        calificacion = new Calificacion(null, datos.getComentario(), datos.getPuntuacion(), usuario);
        calificacionRepository.saveAndFlush(calificacion);
        compra.getInfoEntrega().getCalificaciones().add(calificacion);
        compraRepository.save(compra);
        if (!esComprador) {
            comprador.getCalificaciones().put(calificacion.getId(), calificacion);
            usuarioRepository.save(usuario);
        }
    }
}
