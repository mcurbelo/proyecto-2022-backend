package com.shopnow.shopnow.service;

import com.shopnow.shopnow.controller.responsetypes.Excepcion;
import com.shopnow.shopnow.model.Generico;
import com.shopnow.shopnow.model.Usuario;
import com.shopnow.shopnow.model.datatypes.DtModificarUsuario;
import com.shopnow.shopnow.model.enumerados.EstadoSolicitud;
import com.shopnow.shopnow.model.enumerados.EstadoUsuario;
import com.shopnow.shopnow.repository.DatosVendedorRepository;
import com.shopnow.shopnow.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Service
public class UsuarioService {

    @Autowired
    UsuarioRepository usuarioRepository;

    @Autowired
    DatosVendedorRepository datosVendedorRepository;

    @Autowired
    FirebaseStorageService firebaseStorageService;


    public void modificarDatosUsuario(String correo, DtModificarUsuario datos, MultipartFile imagen) throws IOException {
        Generico usuario;
        Optional<Usuario> res = usuarioRepository.findByCorreoAndEstado(correo, EstadoUsuario.Activo);
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

        if (imagen.getSize() > 0) { //Solo se cambia el link
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
}
