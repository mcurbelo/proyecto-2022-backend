package com.shopnow.shopnow.service;

import com.shopnow.shopnow.controller.responsetypes.Excepcion;
import com.shopnow.shopnow.model.DatosVendedor;
import com.shopnow.shopnow.model.Direccion;
import com.shopnow.shopnow.model.Generico;
import com.shopnow.shopnow.model.Usuario;
import com.shopnow.shopnow.model.datatypes.DtDireccion;
import com.shopnow.shopnow.model.datatypes.DtSolicitud;
import com.shopnow.shopnow.model.enumerados.EstadoSolicitud;
import com.shopnow.shopnow.model.enumerados.EstadoUsuario;
import com.shopnow.shopnow.repository.DatosVendedorRepository;
import com.shopnow.shopnow.repository.DireccionRepository;
import com.shopnow.shopnow.repository.ProductoRepository;
import com.shopnow.shopnow.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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

    public void agregarDreccion(DtDireccion datos, String correoUsuario) {
        Optional<Usuario> usuario = usuarioRepository.findByCorreoAndEstado(correoUsuario, EstadoUsuario.Activo);

        if(usuario.isEmpty()) throw new Excepcion("Algo ha salido mal");
        Generico usuarioCasteado = (Generico) usuario.get();
        Direccion direccion = Direccion.builder()
                .calle(datos.getCalle())
                .numero(datos.getNumero())
                .departamento(datos.getDepartamento())
                .notas(datos.getNotas())
                .build();

        for(Direccion dir : usuarioCasteado.getDireccionesEnvio().values())
            if (Objects.equals(dir.getCalle(), direccion.getCalle()) &&
                    Objects.equals(dir.getNumero(), direccion.getNumero()) &&
                    Objects.equals(dir.getDepartamento(), direccion.getDepartamento())
            ) throw new Excepcion("Direccioón ya existente");

        direccionRepository.save(direccion);
        usuarioCasteado.getDireccionesEnvio().put(direccion.getId(), direccion);
        usuarioRepository.save(usuarioCasteado);
    }

    public void crearSolicitud(DtSolicitud datos, MultipartFile[] imagenes) throws IOException {
        boolean esEmpresa = contieneDatosEmpresa(datos.getNombreEmpresa(), datos.getRut(), datos.getTelefonoEmpresa());
        if (esEmpresa && !datosEmpresaValidos(datos.getNombreEmpresa(), datos.getRut(), datos.getTelefonoEmpresa())) {
            throw new Excepcion("Los datos de la empresa no estan completos");
        }
        if (esEmpresa && datosVendedorRepository.existsByRutOrNombreEmpresaOrTelefonoEmpresa(datos.getRut(), datos.getNombreEmpresa(), datos.getTelefonoEmpresa()))
            throw new Excepcion("Ya existen los datos ingresados de la empresa");


        //TODO
/*                            Validaciones de largo y que sean numericos

        if (esEmpresa && datos.getRut().length() != 12 || datos.getRut().matches("[+-]?\\d*(\\.\\d+)?")) {
            throw new Excepcion("Valor RUT invalido");
          }
        if (esEmpresa && datos.getTelefonoEmpresa().length() >= 8 || datos.getTelefonoEmpresa().matches("[+-]?\\d*(\\.\\d+)?")) {
            throw new Excepcion("Valor RUT invalido");
        }
 */
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
        }
        if (!datos.getEmail().equals(datos.getProducto().getEmailVendedor())) {
            throw new Excepcion("Informacion invalida");
        }
        productoService.agregarProducto(datos.getProducto(), imagenes);

        DtDireccion infoLocal = datos.getLocal();
        Integer idDireccion = datos.getIdDireccion();

        if (infoLocal == null && idDireccion == null)
            throw new Excepcion("Se debe ingresar una direccion valida");

        if (infoLocal != null && idDireccion != null) {
            throw new Excepcion("Se debe ingresar una sola direccion");
        }
        Direccion local;
        if (infoLocal != null) {
            local = new Direccion(null, infoLocal.getCalle(), infoLocal.getNumero(), infoLocal.getDepartamento(), infoLocal.getNotas());
            direccionRepository.saveAndFlush(local);
        } else {
            Optional<Direccion> resulDireccion = direccionRepository.findById(idDireccion);
            if (resulDireccion.isEmpty())
                throw new Excepcion("El identificador de direccion no existe");
            local = resulDireccion.get();
        }
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
        googleSMTP.enviarCorreo("proyecto.tecnologo.2022@gmail.com", "Hay una nueva solicitud pendiente para ser vendedor (" + usuario.getNombre() + " " + usuario.getApellido() + ").", "Solicitud rol vendedor");
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
