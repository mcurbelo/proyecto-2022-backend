package com.shopnow.shopnow.service;

import com.shopnow.shopnow.controller.responsetypes.RegistrarUsuarioResponse;
import com.shopnow.shopnow.model.Direccion;
import com.shopnow.shopnow.model.Generico;
import com.shopnow.shopnow.model.Tarjeta;
import com.shopnow.shopnow.model.Usuario;
import com.shopnow.shopnow.model.datatypes.DtImagen;
import com.shopnow.shopnow.model.datatypes.DtUsuario;
import com.shopnow.shopnow.model.enumerados.Rol;
import com.shopnow.shopnow.repository.UsuarioRepository;
import com.shopnow.shopnow.security.JWTUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

class AuthServiceTest {

    @InjectMocks
    AuthService authService;
    @Mock
    UsuarioRepository usuarioRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    @Mock
    JWTUtil jwtUtil;

    private Usuario comprador;

    private Tarjeta tarjeta;

    private Direccion direccion1;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        tarjeta = new Tarjeta("4111111111111111", "12/2023", "", "1111", "1");
        direccion1 = Direccion.builder().id(100).calle("Bulevar").numero("100").localidad("Montevideo").departamento("Montevideo").notas("").build();
        Map<Integer, Direccion> direccionesEnvio = new HashMap<>();
        direccionesEnvio.put(direccion1.getId(), direccion1);
        Map<String, Tarjeta> tarjetas = new HashMap<>();
        tarjetas.put(tarjeta.getIdTarjeta(), tarjeta);
        comprador = Generico.builder().id(UUID.fromString("37894b62-ae0a-475f-a425-ffd489effbc1")).fechaNac(new Date())
                .reclamos(new HashMap<>())
                .ventas(new HashMap<>())
                .productos(new HashMap<>())
                .compras(new HashMap<>())
                .braintreeCustomerId("123")
                .calificaciones(new HashMap<>())
                .direccionesEnvio(direccionesEnvio)
                .tarjetas(tarjetas).build();
    }

    @Test
    void registrarUsuario() {
        DtUsuario dtUsuario = new DtUsuario(new Date(2000, 9, 10), null, null, "s", "s", "aa", "s", "09", Float.parseFloat("0"), Rol.Comprador, null, null);
        when(usuarioRepository.findByCorreoAndEstado(any(), any())).thenReturn(Optional.empty());
        doReturn(comprador).when(usuarioRepository).save(any());
        doReturn("aa").when(passwordEncoder).encode(any());
        doReturn("aa").when(jwtUtil).generateToken(any(), any());
        //when(generico.getId()).thenReturn(UUID.fromString("37894b62-ae0a-475f-a425-ffd489effbc1"));
        assertNotNull( authService.registrarUsuario(dtUsuario));

    }
}