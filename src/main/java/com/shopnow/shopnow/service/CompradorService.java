package com.shopnow.shopnow.service;

import com.shopnow.shopnow.controller.responsetypes.Excepcion;
import com.shopnow.shopnow.model.DatosVendedor;
import com.shopnow.shopnow.model.Direccion;
import com.shopnow.shopnow.model.Generico;
import com.shopnow.shopnow.model.Usuario;
import com.shopnow.shopnow.model.datatypes.DtDireccion;
import com.shopnow.shopnow.model.datatypes.DtSolicitud;
import com.shopnow.shopnow.model.enumerados.EstadoSolicitud;
import com.shopnow.shopnow.repository.DatosVendedorRepository;
import com.shopnow.shopnow.repository.DireccionRepository;
import com.shopnow.shopnow.repository.ProductoRepository;
import com.shopnow.shopnow.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class CompradorService {

    @Autowired
    UsuarioRepository usuarioRepository;

    @Autowired
    ProductoService productoService;

    @Autowired
    DireccionRepository direccionRepository;

    @Autowired
    GoogleSMTP googleSMTP;

    @Autowired
    ProductoRepository productoRepository;

    @Autowired
    DatosVendedorRepository datosVendedorRepository;

    public void crearSolicitud(DtSolicitud datos, MultipartFile[] imagenes) throws IOException {

        Optional<Usuario> resultado = usuarioRepository.findByCorreo(datos.getEmail());
        Generico usuario;
        if (resultado.isEmpty()) {
            throw new Excepcion("El usuario no existe");
        } else {
            usuario = (Generico) resultado.get();
        }
        if (usuario.getDatosVendedor() != null) {
            if (usuario.getDatosVendedor().getEstadoSolicitud() == EstadoSolicitud.Aceptado) {
                throw new Excepcion("No puedes utilizar esta funcionalidad");
            }

            if (usuario.getDatosVendedor().getEstadoSolicitud() == EstadoSolicitud.Pendiente) {
                throw new Excepcion("No puedes utilizar esta funcionalidad, cuando tienes una solicitud pendiente");
            }

            if (usuario.getDatosVendedor().getEstadoSolicitud() == EstadoSolicitud.Rechazado) { //Ya hizo una solicitud

                // productoRepository.deleteAllById(usuario.getProductos().keySet());  //Elimino productos de la bd
                // usuario.getProductos().clear(); //Vacio el map
                // datosVendedorRepository.deleteById(usuario.getDatosVendedor().getId()); //
                //usuario.setDatosVendedor(null);
            }
        }
        if (!datos.getEmail().equals(datos.getProducto().getEmailVendedor())) {
            throw new Excepcion("Informacion invalida");
        }
        productoService.agregarProducto(datos.getProducto(), imagenes);

        DtDireccion infoLocal = datos.getLocal();
        Direccion local = new Direccion(null, infoLocal.getCalle(), infoLocal.getNumero(), infoLocal.getDepartamento(), infoLocal.getNotas());
        direccionRepository.saveAndFlush(local);
        Map<Integer, Direccion> locales = new HashMap<>();
        locales.put(local.getId(), local);
        DatosVendedor solicitud = new DatosVendedor(null, datos.getNombreEmpresa(), datos.getRut(), datos.getTelefonoEmpresa(), EstadoSolicitud.Pendiente, locales);
        usuario.setDatosVendedor(solicitud);
        usuarioRepository.save(usuario);
        //googleSMTP.enviarCorreo("nicolas16ardilla@hotmail.com", "Hay una nueva solicitud pendiente para ser vendedor (" + usuario.getNombre() + " " + usuario.getApellido() + ").", "Solicitud rol vendedor");
    }
}
