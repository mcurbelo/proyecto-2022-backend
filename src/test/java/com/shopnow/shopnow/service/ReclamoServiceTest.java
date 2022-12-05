package com.shopnow.shopnow.service;

import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.shopnow.shopnow.model.*;
import com.shopnow.shopnow.model.datatypes.DtAltaReclamo;
import com.shopnow.shopnow.model.enumerados.*;
import com.shopnow.shopnow.repository.CompraRepository;
import com.shopnow.shopnow.repository.ReclamoRepository;
import com.shopnow.shopnow.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ReclamoServiceTest {

    @InjectMocks
    ReclamoService reclamoService;
    @Mock
    UsuarioRepository usuarioRepository;

    @Mock
    CompraRepository compraRepository;

    @Mock
    ReclamoRepository reclamoRepository;

    @Mock
    FirebaseMessagingService firebaseMessagingService;

    @Mock
    GoogleSMTP googleSMTP;

    @Mock
    private BraintreeUtils braintreeUtils;

    private Generico comprador;

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


        CompraProducto compraProducto = new CompraProducto(1, new Date(2023, 11, 15), new Date(2023, 11, 15), false, direccion1, Float.parseFloat("1000"), 1, Float.parseFloat("1000"), producto1, null);
        compra = Compra.builder().id(UUID.fromString("f998d287-9b75-4de2-9a39-ef43c8ab6de2")).fecha(new Date(2023, 11, 10)).estado(EstadoCompra.Confirmada).tarjetaPago(tarjeta).infoEntrega(compraProducto).cuponAplicado(null).idTransaccion("00").build();

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
    void iniciarReclamo() throws FirebaseMessagingException, FirebaseAuthException {
        when(usuarioRepository.findByIdAndEstado(UUID.fromString("37894b62-ae0a-475f-a425-ffd489effbc1"), EstadoUsuario.Activo)).thenReturn(Optional.of(comprador));
        when(compraRepository.obtenerVendedor(UUID.fromString("f998d287-9b75-4de2-9a39-ef43c8ab6de2"))).thenReturn((Generico) vendedor);
//        CompraProducto compraProducto1 = new CompraProducto(1, new Date(2022, 11, 15), new Date("2022-11-15T15:00:00"), false, direccion1, Float.parseFloat("1000"), 1, Float.parseFloat("1000"), producto1, null);
//        Compra compra1 = Compra.builder().id(UUID.fromString("f998d287-9b75-4de2-9a39-ef43c8ab6de2")).fecha(new Date(2022, 11, 10)).estado(EstadoCompra.Cancelada).tarjetaPago(tarjeta).infoEntrega(compraProducto1).cuponAplicado(null).idTransaccion("00").build();
//        assertEquals(Exception.class, )
        when(reclamoRepository.existsByCompraAndResuelto(any(), any())).thenReturn(false);
        when(compraRepository.findById(UUID.fromString("f998d287-9b75-4de2-9a39-ef43c8ab6de2"))).thenReturn(Optional.of(compra));
        doReturn(new Reclamo()).when(reclamoRepository).saveAndFlush(any());
        doReturn(comprador).when(usuarioRepository).save(comprador);
        doNothing().when(firebaseMessagingService).enviarNotificacion(any(), any());
        doNothing().when(googleSMTP).enviarCorreo(any(), any(), any());
        DtAltaReclamo dtAltaReclamo = new DtAltaReclamo("", TipoReclamo.Otro);
        reclamoService.iniciarReclamo(dtAltaReclamo, compra.getId(), comprador.getId(), "ss");
    }

    @Test
    void reclamoResueltoPorDevolucion() throws FirebaseMessagingException, FirebaseAuthException {
        Reclamo reclamo = new Reclamo(UUID.fromString("1cf8f86d-6fba-48d9-aa9f-5dead3c2bed0"), TipoReclamo.Otro, new Date(), "Descripcion prueba", TipoResolucion.NoResuelto, compra);
        comprador.getReclamos().put(reclamo.getId(), reclamo);
        when(reclamoRepository.findById(any())).thenReturn(Optional.of(reclamo));
        when(compraRepository.findById(any())).thenReturn(Optional.of(compra));
        when(usuarioRepository.findByIdAndEstado(any(), any())).thenReturn(Optional.of(vendedor));
        when(compraRepository.obtenerComprador(any())).thenReturn((Generico) comprador);
        when(braintreeUtils.devolverDinero(any())).thenReturn(true);
        when(reclamoRepository.save(any())).thenReturn(reclamo);
        when(compraRepository.save(any())).thenReturn(compra);
        doNothing().when(firebaseMessagingService).enviarNotificacion(any(), any());
        doNothing().when(googleSMTP).enviarCorreo(any(), any(), any());
        reclamoService.gestionReclamo(compra.getId(), UUID.fromString("1cf8f86d-6fba-48d9-aa9f-5dead3c2bed0"), vendedor.getId(), TipoResolucion.Devolucion);
        verify(googleSMTP).enviarCorreo(any(), any(), any());
    }

    @Test
    void marcarReclamoResuelto() throws FirebaseMessagingException, FirebaseAuthException {
        Reclamo reclamo = new Reclamo(UUID.fromString("dfd5a8df-c524-4808-b2d5-aceb02b11486"), TipoReclamo.Otro, new Date(), "Descripcion prueba", TipoResolucion.NoResuelto, compra);
        comprador.getReclamos().put(reclamo.getId(), reclamo);
        when(reclamoRepository.findById(any())).thenReturn(Optional.of(reclamo));
        when(compraRepository.obtenerVendedor(any())).thenReturn((Generico) vendedor);
        when(compraRepository.obtenerComprador(any())).thenReturn(comprador);
        doNothing().when(firebaseMessagingService).enviarNotificacion(any(), any());
        doNothing().when(googleSMTP).enviarCorreo(any(), any(), any());
        reclamoService.marcarComoResuelto(compra.getId(), reclamo.getId(), comprador.getId(), comprador.getCorreo());
        verify(googleSMTP).enviarCorreo(any(), any(), any());

    }
}