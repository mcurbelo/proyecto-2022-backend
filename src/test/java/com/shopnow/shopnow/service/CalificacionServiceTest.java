package com.shopnow.shopnow.service;

import com.shopnow.shopnow.model.*;
import com.shopnow.shopnow.model.datatypes.DtCalificacion;
import com.shopnow.shopnow.model.enumerados.EstadoCompra;
import com.shopnow.shopnow.model.enumerados.EstadoProducto;
import com.shopnow.shopnow.model.enumerados.EstadoSolicitud;
import com.shopnow.shopnow.model.enumerados.EstadoUsuario;
import com.shopnow.shopnow.repository.CalificacionRepository;
import com.shopnow.shopnow.repository.CompraRepository;
import com.shopnow.shopnow.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CalificacionServiceTest {
    @InjectMocks
    CalificacionService calificacionService;

    @Mock
    CompraRepository compraRepository;

    @Mock
    UsuarioRepository usuarioRepository;

    @Mock
    CalificacionRepository calificacionRepository;

    private Usuario comprador;

    private Usuario vendedor;

    private Tarjeta tarjeta;

    private Producto producto1, producto2;

    private Map<UUID, Producto> productos = new HashMap<>();

    private List<Producto> productosLista = new ArrayList<>();

    private Direccion direccion1;

    private Categoria categoria;

    private DatosVendedor datosVendedor;

    private Compra compra;


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
                .correo("ss")
                .ventas(new HashMap<>())
                .productos(new HashMap<>())
                .estado(EstadoUsuario.Activo)
                .compras(new HashMap<>())
                .braintreeCustomerId("123")
                .calificaciones(new HashMap<>())
                .direccionesEnvio(direccionesEnvio)
                .tarjetas(tarjetas).build();
        datosVendedor = new DatosVendedor(0, "Prueba", "123123", "222", EstadoSolicitud.Aceptado, new HashMap<>());

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

        productos.put(producto1.getId(), producto1);
        productos.put(producto2.getId(), producto2);
        productosLista.add(producto1);
        productosLista.add(producto2);


        CompraProducto compraProducto = new CompraProducto(1, new Date(2022, 11, 4), null, false, direccion1, Float.parseFloat("1000"), 1, Float.parseFloat("1000"), producto1, new ArrayList<>());
        compra = Compra.builder().id(UUID.fromString("f998d287-9b75-4de2-9a39-ef43c8ab6de2")).fecha(new Date(2023, 11, 10)).estado(EstadoCompra.Completada).tarjetaPago(tarjeta).infoEntrega(compraProducto).cuponAplicado(null).idTransaccion("00").build();

        Map<String, Compra> ventas = new HashMap<>();
        ventas.put(compra.getIdChat(), compra);


        vendedor = Generico.builder().id(UUID.fromString("d652bd18-0d70-4f73-b72f-6627620bc5c5")).fechaNac(new Date())
                .reclamos(new HashMap<>())
                .ventas(new HashMap<>())
                .productos(productos)
                .webToken("")
                .estado(EstadoUsuario.Activo)
                .compras(new HashMap<>())
                .calificaciones(new HashMap<>())
                .direccionesEnvio(new HashMap<>())
                .tarjetas(new HashMap<>()).datosVendedor(datosVendedor).build();
        categoria = Categoria.builder().productos(productos).nombre("Tecnologia").build();
    }

    @Test
    void agregarCalificacionEsVendedor() {
        when(compraRepository.findById(any())).thenReturn(Optional.of(compra));
        when(usuarioRepository.findByIdAndEstado(any(), any())).thenReturn(Optional.of(vendedor));

        when(compraRepository.obtenerComprador(any())).thenReturn((Generico) comprador);
        when(compraRepository.obtenerVendedor(any())).thenReturn((Generico) vendedor);
        when(calificacionRepository.saveAndFlush(any())).thenReturn(null);
        when(compraRepository.save(any())).thenReturn(null);
        calificacionService.agregarCalificacion(UUID.fromString("f998d287-9b75-4de2-9a39-ef43c8ab6de2"), new DtCalificacion(4.5f, "Muy buen producto", vendedor.getId()));
        verify(usuarioRepository).save(any());
    }

    @Test
    void agregarCalificacionEsComprador() {
        when(compraRepository.findById(any())).thenReturn(Optional.of(compra));
        when(usuarioRepository.findByIdAndEstado(any(), any())).thenReturn(Optional.of(comprador));
        when(compraRepository.obtenerComprador(any())).thenReturn((Generico) comprador);
        when(compraRepository.obtenerVendedor(any())).thenReturn((Generico) vendedor);
        when(calificacionRepository.saveAndFlush(any())).thenReturn(null);
        when(compraRepository.save(any())).thenReturn(null);
        calificacionService.agregarCalificacion(UUID.fromString("f998d287-9b75-4de2-9a39-ef43c8ab6de2"), new DtCalificacion(4.5f, "Muy buen producto", comprador.getId()));
        verify(compraRepository).save(any());
        verify(usuarioRepository, times(0)).save(any());
    }


}