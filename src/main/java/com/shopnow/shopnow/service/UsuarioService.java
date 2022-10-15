package com.shopnow.shopnow.service;

import com.shopnow.shopnow.model.Usuario;
import com.shopnow.shopnow.model.datatypes.DtImagen;
import com.shopnow.shopnow.model.datatypes.DtUsuario;
import com.shopnow.shopnow.model.enumerados.EstadoUsuario;
import com.shopnow.shopnow.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

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
}
