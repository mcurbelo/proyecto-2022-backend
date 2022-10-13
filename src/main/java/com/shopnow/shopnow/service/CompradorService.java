package com.shopnow.shopnow.service;

import com.shopnow.shopnow.controller.responsetypes.Excepcion;
import com.shopnow.shopnow.model.*;
import com.shopnow.shopnow.model.datatypes.DtDireccion;
import com.shopnow.shopnow.model.datatypes.DtSolicitud;
import com.shopnow.shopnow.model.enumerados.EstadoSolicitud;
import com.shopnow.shopnow.repository.DatosVendedorRepository;
import com.shopnow.shopnow.repository.DireccionRepository;
import com.shopnow.shopnow.repository.ProductoRepository;
import com.shopnow.shopnow.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.*;

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

    @Autowired
    FirebaseStorageService firebaseStorageService;

    public void crearSolicitud(DtSolicitud datos, MultipartFile[] imagenes) throws IOException {
        boolean esEmpresa = contieneDatosEmpresa(datos.getNombreEmpresa(), datos.getRut(), datos.getTelefonoEmpresa());
        if (esEmpresa && !datosEmpresaValidos(datos.getNombreEmpresa(), datos.getRut(), datos.getTelefonoEmpresa())) {
            throw new Excepcion("Los datos de la empresa no estan completos");
        }
        if (esEmpresa) {
            if (datosVendedorRepository.existsByRutOrNombreEmpresaOrTelefonoEmpresa(datos.getRut(), datos.getNombreEmpresa(), datos.getTelefonoEmpresa()))
                throw new Excepcion("Ya existen los datos ingresados de la empresa");

/*                             Validaciones de largo y que sean numericos

            if (datos.getRut().length() != 12 || datos.getRut().matches("[+-]?\\d*(\\.\\d+)?")) {
                throw new Excepcion("Valor RUT invalido");
            }
            if (datos.getTelefonoEmpresa().length() >= 8 || datos.getTelefonoEmpresa().matches("[+-]?\\d*(\\.\\d+)?")) {
                throw new Excepcion("Valor RUT invalido");
            }
*/
        }
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
                Optional<UUID> primeraKey = usuario.getProductos().keySet().stream().findFirst();//Es solo un producto siempre
                UUID idProducto;
                if (primeraKey.isPresent())
                    idProducto = primeraKey.get();
                else
                    throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error en el sistema");
                int datosVendedorViejo = usuario.getDatosVendedor().getId();
                usuario.getProductos().clear(); //Vacio el map
                usuario.setDatosVendedor(null); //Vacio solicitud
                usuarioRepository.saveAndFlush(usuario);


                Optional<Producto> res = productoRepository.findById(idProducto);
                Producto producto;
                if (res.isPresent())
                    producto = res.get();
                else
                    throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error en el sistema");
                //Borrar imagenes de Storage?
                producto.getImagenesURL().clear();
                productoRepository.saveAndFlush(producto);
                productoRepository.eliminarProductoCategoria(idProducto);
                productoRepository.delete(producto);
                datosVendedorRepository.deleteById(datosVendedorViejo);
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
        DatosVendedor solicitud;
        if (esEmpresa)
            solicitud = new DatosVendedor(null, datos.getNombreEmpresa(), datos.getRut(), datos.getTelefonoEmpresa(), EstadoSolicitud.Pendiente, locales);
        else {
            solicitud = new DatosVendedor(null, null, null, null, EstadoSolicitud.Pendiente, locales);
        }
        usuario.setDatosVendedor(solicitud);
        usuarioRepository.save(usuario);
        googleSMTP.enviarCorreo("nicolas16ardilla@hotmail.com", "Hay una nueva solicitud pendiente para ser vendedor (" + usuario.getNombre() + " " + usuario.getApellido() + ").", "Solicitud rol vendedor");
    }

    boolean datosEmpresaValidos(String nombre, String rut, String telefono) {
        List<String> datos = new ArrayList<>(Arrays.asList(nombre, rut, telefono));
        return !datos.contains(null) && !datos.contains("");
    }

    boolean contieneDatosEmpresa(String nombre, String rut, String telefono) {
        if (nombre != null && !nombre.isEmpty())
            return true;
        if (rut != null && !rut.isEmpty())
            return true;
        return telefono != null && !telefono.isEmpty();
    }
}
