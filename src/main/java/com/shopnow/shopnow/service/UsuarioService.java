package com.shopnow.shopnow.service;

import com.shopnow.shopnow.model.Usuario;
import com.shopnow.shopnow.model.datatypes.DtImagen;
import com.shopnow.shopnow.model.datatypes.DtUsuario;
import com.shopnow.shopnow.model.enumerados.EstadoUsuario;
import com.shopnow.shopnow.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;
import com.braintreegateway.BraintreeGateway;
import com.braintreegateway.CreditCardRequest;
import com.braintreegateway.CustomerRequest;
import com.shopnow.shopnow.controller.responsetypes.Excepcion;
import com.shopnow.shopnow.model.Generico;
import com.shopnow.shopnow.model.Tarjeta;
import com.shopnow.shopnow.model.Usuario;
import com.shopnow.shopnow.model.datatypes.DtModificarUsuario;
import com.shopnow.shopnow.model.datatypes.DtTarjeta;
import com.shopnow.shopnow.model.enumerados.EstadoSolicitud;
import com.shopnow.shopnow.model.enumerados.EstadoUsuario;
import com.shopnow.shopnow.repository.DatosVendedorRepository;
import com.shopnow.shopnow.repository.TarjetasRepository;
import com.shopnow.shopnow.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.swing.text.html.Option;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Service
public class UsuarioService {

    @Autowired
    UsuarioRepository usuarioRepository;

    public DtUsuario infoUsuario(String correo){

        Optional<Usuario> usuarioBaseDatos = usuarioRepository.findByCorreo(correo);

        Usuario usuario = usuarioBaseDatos.get();

        DtUsuario usuarioReturn = DtUsuario.builder()
                .nombre(usuario.getNombre())
                .apellido(usuario.getApellido())
                .correo(usuario.getCorreo())
                .imagen(DtImagen.builder().data(usuario.getImagen()).build())
                .telefono(usuario.getTelefono()).build();

        return usuarioReturn;
    }

    @Autowired
    DatosVendedorRepository datosVendedorRepository;
    @Autowired
    TarjetasRepository tarjetasRepository;

    @Autowired
    FirebaseStorageService firebaseStorageService;
    @Autowired
    BraintreeUtils braintreeUtils;


    public void modificarDatosUsuario(UUID id, DtModificarUsuario datos, MultipartFile imagen) throws IOException {
        Generico usuario;
        Optional<Usuario> res = usuarioRepository.findByIdAndEstado(id, EstadoUsuario.Activo);
        if (res.isEmpty()) {
            throw new Excepcion("El usuario no existe");
        } else {
            usuario = (Generico) res.get();
        }
        if (datos.getCorreo() != null) {
            if (usuarioRepository.existsByCorreoAndEstado(datos.getCorreo(), EstadoUsuario.Activo)) {
                throw new Excepcion("El correo nuevo ya existe.");
            } else
                usuario.setCorreo(datos.getCorreo());
        }

        if (imagen != null && !imagen.isEmpty()) { //Solo se cambia el link
            String idImagen = UUID.randomUUID().toString();
            String link = firebaseStorageService.uploadFile(imagen, idImagen + "-UsuarioImg");
            usuario.setImagen(link);
        }

        if (datos.getContrasenaVieja() != null && datos.getContrasenaNueva() != null) {
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            if (encoder.matches(datos.getContrasenaVieja(), usuario.getPassword())) {
                String nuevaContrasena = encoder.encode(datos.getContrasenaNueva());
                usuario.setPassword(nuevaContrasena);
            } else {
                throw new Excepcion("Contraseña antigua incorrecta. No se realizó la modificacion");
            }
        }
        if (datos.getTelefonoContacto() != null)
            usuario.setTelefono(datos.getTelefonoContacto());

        if (usuario.getDatosVendedor() != null && usuario.getDatosVendedor().getEstadoSolicitud() == EstadoSolicitud.Aceptado) {
            //Esta parte de la funcionalidad deberia estar solo para vendedores aceptados, igualmente valido

            /* TODO Validacion del telefono :)
            if (datos.getTelefonoEmpresa().length() >= 8 || datos.getTelefonoEmpresa().matches("[+-]?\\d*(\\.\\d+)?")) {
                throw new Excepcion("El numero de telefono es invalido");
            }
        */
            if (datos.getNombreEmpresa() != null && datosVendedorRepository.existsByNombreEmpresa(datos.getNombreEmpresa()))
                throw new Excepcion("El nombre de la empresa ya existe");
            else
                usuario.getDatosVendedor().setNombreEmpresa(datos.getNombreEmpresa());

            if (datos.getTelefonoEmpresa() != null && datosVendedorRepository.existsByTelefonoEmpresa(datos.getNombreEmpresa()))
                throw new Excepcion("El numero de telefono de la empresa ya esta en uso.");
            else
                usuario.getDatosVendedor().setTelefonoEmpresa(datos.getTelefonoEmpresa());
        }

        usuarioRepository.save(usuario);

    }

    //TODO Verificar tarjeta duplicada para el usuario y verificar tarjeta existente en el sistema en general
    public void agregarTarjeta(DtTarjeta dtTarjeta, UUID userId) {
        Optional<Usuario> user = usuarioRepository.findByIdAndEstado(userId, EstadoUsuario.Activo);
        if(user.isPresent() && user.get() instanceof Generico usuarioGenerico) {
            if(usuarioGenerico.getBraintreeCustomerId() == null) {
                String braintreeId = braintreeUtils.generateCustomerId(usuarioGenerico);
                if(braintreeId == null) throw new Excepcion("Ha ocurrido un error inesperado");
                usuarioGenerico.setBraintreeCustomerId(braintreeId);
                usuarioRepository.save(usuarioGenerico);
            }
            Tarjeta nuevaTarjeta = braintreeUtils.agregarTarjeta(dtTarjeta, usuarioGenerico.getBraintreeCustomerId());
            if(nuevaTarjeta == null) throw new Excepcion("Ha ocurrido un error al agregar la tarjeta");
            tarjetasRepository.save(nuevaTarjeta);
            usuarioGenerico.getTarjetas().put(nuevaTarjeta.getIdTarjeta(), nuevaTarjeta);
            usuarioRepository.save(usuarioGenerico);
        } else {
            throw new Excepcion("Ha ocurrido un error inesperado");
        }
    }
}
