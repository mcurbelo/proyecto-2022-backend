package com.shopnow.shopnow.service;

import com.shopnow.shopnow.model.DatosVendedor;
import com.shopnow.shopnow.model.Direccion;
import com.shopnow.shopnow.model.Generico;
import com.shopnow.shopnow.model.Tarjeta;
import com.shopnow.shopnow.model.datatypes.DtDireccion;
import com.shopnow.shopnow.model.enumerados.EstadoSolicitud;
import com.shopnow.shopnow.model.enumerados.EstadoUsuario;
import com.shopnow.shopnow.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CompradorServiceTest {

    @InjectMocks
    CompradorService compradorService;

    @Mock
    UsuarioRepository usuarioRepository;

    @Mock
    ProductoService productoService;

    @Mock
    DireccionRepository direccionRepository;

    @InjectMocks
    GoogleSMTP googleSMTP;

    @Mock
    CompraRepository compraRepository;

    @Mock
    DatosVendedorRepository datosVendedorRepository;

    @InjectMocks
    FirebaseStorageService firebaseStorageService;

    @Mock
    ReclamoRepository reclamoRepository;

    @InjectMocks
    FirebaseMessagingService firebaseMessagingService;

    private Generico vendedor;

    private Direccion direccion1;


    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        direccion1 = Direccion.builder().id(100).calle("18 de julio").numero("123").localidad("Montevideo").departamento("Montevideo").notas("").build();
        Direccion direccion2 = Direccion.builder().id(101).calle("Agraciada").numero("889").localidad("Montevideo").departamento("Montevideo").notas("").build();
        Map<Integer, Direccion> direccionesEnvio = new HashMap<>();
        Map<Integer, Direccion> direcionesLocales = new HashMap<>();
        direccionesEnvio.put(direccion1.getId(), direccion1);
        direccionesEnvio.put(direccion2.getId(), direccion2);
        direcionesLocales.put(direccion1.getId(), direccion1);
        Map<String, Tarjeta> tarjetas = new HashMap<>();
        vendedor = Generico.builder().id(UUID.fromString("37894b62-ae0a-475f-a425-ffd489effbc1")).fechaNac(new Date())
                .reclamos(new HashMap<>())
                .ventas(new HashMap<>())
                .productos(new HashMap<>())
                .compras(new HashMap<>())
                .correo("vendedor@shopnow.com")
                .braintreeCustomerId("123")
                .password("aa")
                .calificaciones(new HashMap<>())
                .direccionesEnvio(direccionesEnvio)
                .datosVendedor(new DatosVendedor(80, "Empresa test", "123456789012", "12345678", EstadoSolicitud.Aceptado, direcionesLocales))
                .tarjetas(tarjetas).build();
    }

    @Test
    void listarDirecciones() {
        when(usuarioRepository.findByCorreoAndEstado("vendedor@shopnow.com", EstadoUsuario.Activo)).thenReturn(Optional.of(vendedor));
        List<DtDireccion> direcciones = compradorService.obtenerDirecciones("vendedor@shopnow.com");
        assertEquals(3, direcciones.size());
    }

    @Test
    void agregarDireccionLocal() {
        when(usuarioRepository.findByCorreoAndEstado("vendedor@shopnow.com", EstadoUsuario.Activo)).thenReturn(Optional.of(vendedor));
        Direccion direccion = new Direccion(99, "18 de Julio", "456", "Montevideo", "Montevideo", null);
        doReturn(direccion).when(direccionRepository).save(any());
        doReturn(vendedor).when(usuarioRepository).save(any());
        DtDireccion dtDireccion = new DtDireccion(null, "18 de Julio", "456", "Montevideo", "Montevideo", null, true);
        compradorService.agregarDireccion(dtDireccion, "vendedor@shopnow.com");
        verify(usuarioRepository).save(any());
    }

    @Test
    void agregarDireccionEnvio() {
        when(usuarioRepository.findByCorreoAndEstado("vendedor@shopnow.com", EstadoUsuario.Activo)).thenReturn(Optional.of(vendedor));
        Direccion direccion = new Direccion(99, "18 de Julio", "456", "Montevideo", "Montevideo", null);
        doReturn(direccion).when(direccionRepository).save(any());
        doReturn(vendedor).when(usuarioRepository).save(any());
        DtDireccion dtDireccion = new DtDireccion(null, "18 de Julio", "456", "Montevideo", "Montevideo", null, false);
        compradorService.agregarDireccion(dtDireccion, "vendedor@shopnow.com");
        verify(usuarioRepository).save(any());
    }

    @Test
    void borrarDireccionLocal() {
        when(usuarioRepository.findByCorreo("vendedor@shopnow.com")).thenReturn(Optional.of(vendedor));
        when(direccionRepository.findById(80)).thenReturn(Optional.of(direccion1));
        doReturn(vendedor).when(usuarioRepository).save(any());
        compradorService.borrarDireccion("80", "vendedor@shopnow.com", true);
        verify(usuarioRepository).save(any());
    }

    @Test
    void borrarDireccionEnvio() {
        when(usuarioRepository.findByCorreo("vendedor@shopnow.com")).thenReturn(Optional.of(vendedor));
        when(direccionRepository.findById(80)).thenReturn(Optional.of(direccion1));
        doReturn(vendedor).when(usuarioRepository).save(any());
        compradorService.borrarDireccion("80", "vendedor@shopnow.com", false);
        verify(usuarioRepository).save(any());
    }

    @Test
    void editarDireccion() {
        when(direccionRepository.findById(80)).thenReturn(Optional.of(direccion1));
        DtDireccion dtDireccion = new DtDireccion(80, "CalleEditada", "456", "Montevideo", "Montevideo", null, true);
        compradorService.editarDireccion(dtDireccion);
        verify(direccionRepository).save(any());
    }

    //Eliminar direccion de retiro no puede ser y validar direccion cuando se edite que no se igual a otra que ya tiene

}


