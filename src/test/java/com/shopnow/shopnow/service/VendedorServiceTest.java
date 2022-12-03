package com.shopnow.shopnow.service;

import com.shopnow.shopnow.model.DatosVendedor;
import com.shopnow.shopnow.model.Generico;
import com.shopnow.shopnow.model.Producto;
import com.shopnow.shopnow.model.URLimagen;
import com.shopnow.shopnow.model.enumerados.EstadoProducto;
import com.shopnow.shopnow.model.enumerados.EstadoSolicitud;
import com.shopnow.shopnow.model.enumerados.EstadoUsuario;
import com.shopnow.shopnow.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class VendedorServiceTest {

    @InjectMocks
    VendedorService vendedorService;
    @Mock
    UsuarioRepository usuarioRepository;

    @Mock
    ProductoService productoService;

    @Mock
    DireccionRepository direccionRepository;

    @Mock
    GoogleSMTP googleSMTP;

    @Mock
    ProductoRepository productoRepository;

    @Mock
    DatosVendedorRepository datosVendedorRepository;

    @Mock
    FirebaseStorageService firebaseStorageService;

    @Mock
    CompraRepository compraRepository;


    private Producto producto1;
    private Producto producto2;
    private Generico vendedor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        List<URLimagen> imagenes = new ArrayList<URLimagen>();
        imagenes.add(new URLimagen("urldeimagen"));


        producto1 = Producto.builder()
                .id(UUID.fromString("2c72e1b3-07c4-4d9d-b1f0-a21e0b291d25"))
                .nombre("Television")
                .stock(200)
                .imagenesURL(imagenes)
                .descripcion("50 pulgadas")
                .fechaInicio(new Date())
                .fechaFin(new Date(2022, 12, 10))
                .estado(EstadoProducto.Activo)
                .precio(Float.parseFloat("10000"))
                .diasGarantia(60)
                .permiteEnvio(true)
                .reportes(new HashMap<>())
                .comentarios(new HashMap<>()).build();
        producto2 = Producto.builder()
                .id(UUID.fromString("2c72e1b3-07c4-4d9d-b1f0-a21e0b291d30"))
                .nombre("Playstation 5")
                .stock(200)
                .imagenesURL(imagenes)
                .descripcion("Splim")
                .fechaInicio(new Date())
                .fechaFin(new Date(2022, 12, 10))
                .estado(EstadoProducto.Activo)
                .precio(Float.parseFloat("20000"))
                .diasGarantia(180)
                .permiteEnvio(true)
                .reportes(new HashMap<>())
                .comentarios(new HashMap<>()).build();

        Map<UUID, Producto> productos = new HashMap<>();
        productos.put(producto1.getId(), producto1);
        productos.put(producto2.getId(), producto2);

        vendedor = Generico.builder().id(UUID.fromString("d652bd18-0d70-4f73-b72f-6627620bc5c8")).fechaNac(new Date())
                .reclamos(new HashMap<>())
                .ventas(new HashMap<>())
                .productos(productos)
                .webToken("")
                .estado(EstadoUsuario.Activo)
                .compras(new HashMap<>())
                .calificaciones(new HashMap<>())
                .direccionesEnvio(new HashMap<>())
                .tarjetas(new HashMap<>())
                .datosVendedor(new DatosVendedor(0, "EmpresaPrueba", "123123", "87448456654", EstadoSolicitud.Aceptado, new HashMap<>()))
                .build();
    }

    @Test
    void cambiarEstadoProducto() {
        when(productoRepository.findById(any())).thenReturn(Optional.of(producto1));
        when(usuarioRepository.findByIdAndEstado(any(), any())).thenReturn(Optional.of(vendedor));
        when(productoRepository.save(any())).thenReturn(producto1);
        vendedorService.cambiarEstadoProducto(UUID.fromString("2c72e1b3-07c4-4d9d-b1f0-a21e0b291d25"), UUID.fromString("d652bd18-0d70-4f73-b72f-6627620bc5c8"), EstadoProducto.Pausado);
        vendedorService.cambiarEstadoProducto(UUID.fromString("2c72e1b3-07c4-4d9d-b1f0-a21e0b291d25"), UUID.fromString("d652bd18-0d70-4f73-b72f-6627620bc5c8"), EstadoProducto.Activo);
        verify(productoRepository, times(2)).save(any());
    }

    @Test
    void historialVentas() {

    }
}