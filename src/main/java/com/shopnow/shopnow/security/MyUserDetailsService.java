package com.shopnow.shopnow.security;


import com.shopnow.shopnow.model.Administrador;
import com.shopnow.shopnow.model.Generico;
import com.shopnow.shopnow.model.Usuario;
import com.shopnow.shopnow.model.enumerados.EstadoSolicitud;
import com.shopnow.shopnow.model.enumerados.Rol;
import com.shopnow.shopnow.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.Collections;
import java.util.Optional;

@Transactional
@Component
public class MyUserDetailsService implements UserDetailsService {

    @Autowired
    private UsuarioRepository userRepo;

    @Override
    public UserDetails loadUserByUsername(String correo) throws UsernameNotFoundException {
        Optional<Usuario> userRes = userRepo.findByCorreo(correo);
        if (userRes.isEmpty())
            throw new UsernameNotFoundException("No se pude encontrar un usuario con el correo " + correo);
        Usuario user = userRes.get();
        String rol;
        if (user instanceof Administrador)
            rol = "ROLE_" + String.valueOf(Rol.ADM).toUpperCase();
        else {
            Generico generico = (Generico) user;
            if (generico.getDatosVendedor() != null && generico.getDatosVendedor().getEstadoSolicitud() == EstadoSolicitud.Aceptado)
                rol = "ROLE_" + String.valueOf(Rol.Vendedor).toUpperCase();
            else
                rol = "ROLE_" + String.valueOf(Rol.Comprador).toUpperCase();
        }
        return new org.springframework.security.core.userdetails.User(
                correo,
                user.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority(rol)));
    }
}

