package com.shopnow.shopnow.service;

import com.braintreegateway.Result;
import com.braintreegateway.Transaction;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.shopnow.shopnow.model.*;
import com.shopnow.shopnow.model.datatypes.DtCompra;
import com.shopnow.shopnow.model.datatypes.DtConfirmarCompra;
import com.shopnow.shopnow.model.enumerados.EstadoCompra;
import com.shopnow.shopnow.model.enumerados.EstadoProducto;
import com.shopnow.shopnow.model.enumerados.EstadoSolicitud;
import com.shopnow.shopnow.model.enumerados.EstadoUsuario;
import com.shopnow.shopnow.repository.*;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

class CompraServiceTest {

    @Mock
    CompraRepository compraRepository;
    @Mock
    FirebaseMessagingService firebaseMessagingService;
    @Mock
    GoogleSMTP googleSMTP;
    @Mock
    UtilService utilService;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private ProductoRepository productoRepository;
    @Mock
    private TarjetaRepository tarjetaRepository;
    @Mock
    private DireccionRepository direccionRepository;
    @Mock
    private BraintreeUtils braintreeUtils;
    @InjectMocks
    private CompraService compraService;
    @Mock
    private Result<Transaction> transactionResult;

    @InjectMocks
    private Generico usuario;

    private Tarjeta tarjeta;

    private Direccion direccion1;

    private Generico comprador;

    private Generico vendedor;

    private DatosVendedor datosVendedor;

    private Producto producto;
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
                .correo("comprador@shopnow.com")
                .reclamos(new HashMap<>())
                .ventas(new HashMap<>())
                .productos(new HashMap<>())
                .compras(new HashMap<>())
                .braintreeCustomerId("123")
                .calificaciones(new HashMap<>())
                .direccionesEnvio(direccionesEnvio)
                .tarjetas(tarjetas).build();
        datosVendedor = new DatosVendedor(0, "Prueba", "123123", "222", EstadoSolicitud.Aceptado, new HashMap<>());

        producto = Producto.builder()
                .id(UUID.fromString("2c72e1b3-07c4-4d9d-b1f0-a21e0b291d25"))
                .nombre("Television")
                .stock(200)
                .imagenesURL(new ArrayList<URLimagen>())
                .descripcion("50 pulgadas")
                .fechaInicio(new Date())
                .fechaFin(new Date(2022, 12, 10))
                .estado(EstadoProducto.Activo)
                .precio(Float.parseFloat("10000"))
                .diasGarantia(60)
                .permiteEnvio(true)
                .reportes(new HashMap<>())
                .comentarios(new HashMap<>()).build();

        Map<UUID, Producto> productos = new HashMap<>();
        productos.put(producto.getId(), producto);

        CompraProducto compraProducto = new CompraProducto(1, null, null, false, null, Float.parseFloat("1000"), 1, Float.parseFloat("1000"), producto, null);
        compra = Compra.builder().id(UUID.fromString("f998d287-9b75-4de2-9a39-ef43c8ab6de2")).fecha(new Date(2023, 11, 10)).estado(EstadoCompra.Confirmada).tarjetaPago(tarjeta).infoEntrega(compraProducto).cuponAplicado(null).idTransaccion("00").build();

        Map<UUID, Compra> ventas = new HashMap<>();
        ventas.put(compra.getId(), compra);

        vendedor = Generico.builder().id(UUID.fromString("d652bd18-0d70-4f73-b72f-6627620bc5c5")).fechaNac(new Date())
                .reclamos(new HashMap<>())
                .correo("vendedor@shopnow.com")
                .ventas(ventas)
                .productos(productos)
                .webToken("")
                .compras(new HashMap<>())
                .calificaciones(new HashMap<>())
                .direccionesEnvio(new HashMap<>())
                .tarjetas(new HashMap<>()).datosVendedor(datosVendedor).build();


    }

    @Test
    void nuevaCompra() throws FirebaseMessagingException, FirebaseAuthException {
        //idcomprador 37894b62-ae0a-475f-a425-ffd489effbc1
        //idvendedor d652bd18-0d70-4f73-b72f-6627620bc5c5
        //idproducto 2c72e1b3-07c4-4d9d-b1f0-a21e0b291d25
        when(usuarioRepository.findByIdAndEstado(UUID.fromString("37894b62-ae0a-475f-a425-ffd489effbc1"), EstadoUsuario.Activo)).thenReturn(Optional.of(comprador));
        //assertNotNull(usuarioRepository.findByIdAndEstado(UUID.fromString("37894b62-ae0a-475f-a425-ffd489effbc1"), EstadoUsuario.Activo));
        when(usuarioRepository.findByIdAndEstado(UUID.fromString("d652bd18-0d70-4f73-b72f-6627620bc5c5"), EstadoUsuario.Activo)).thenReturn(Optional.of(vendedor));
        //assertNotNull(usuarioRepository.findByIdAndEstado(UUID.fromString("d652bd18-0d70-4f73-b72f-6627620bc5c5"), EstadoUsuario.Activo));
        when(productoRepository.findByIdAndEstado(producto.getId(), EstadoProducto.Activo)).thenReturn(Optional.of(producto));
        //assertNotNull(productoRepository.findByIdAndEstado(producto.getId(), EstadoProducto.Activo));
        when(tarjetaRepository.findById(tarjeta.getIdTarjeta())).thenReturn(Optional.of(tarjeta));
        when(direccionRepository.findById(direccion1.getId())).thenReturn(Optional.of(direccion1));
        Result<Transaction> mockResult = (Result<Transaction>) mock(Result.class);
        ///when(Mockito.mock(Transaction.class).getId()).thenReturn("22");
        when(braintreeUtils.hacerPago("123", "1", "10000.0")).thenReturn(mockResult);
        when(mockResult.isSuccess()).thenReturn(true);
        Transaction transaction = mock(Transaction.class);

        doReturn(transaction).when(mockResult).getTarget();
        doReturn("33").when(transaction).getId();

        doReturn(new Compra()).when(compraRepository).saveAndFlush(any());
        doReturn(comprador).when(usuarioRepository).save(comprador);
        doReturn(vendedor).when(usuarioRepository).save(vendedor);
        doReturn(new Producto()).when(productoRepository).save(any());
        doNothing().when(firebaseMessagingService).enviarNotificacion(any(), any());
        doNothing().when(googleSMTP).enviarCorreo(any(), any(), any());

        when(utilService.detallesCompra(any(), any(), any(), any(), any())).thenReturn("DetalleCompra");
//        when(usuarioRepository.save(any())).thenReturn("");
//        when(productoRepository.save(any())).thenReturn("");
        DtCompra dtcompra = new DtCompra(vendedor.getId(), producto.getId(), 1, null, tarjeta.getIdTarjeta(), true, direccion1.getId(), null);
        assertNotNull(compraService.nuevaCompra(dtcompra, comprador.getId()));
    }

    @Test
    void cambiarEstadoVenta() throws FirebaseMessagingException, FirebaseAuthException {
        when(usuarioRepository.findByIdAndEstado(UUID.fromString("d652bd18-0d70-4f73-b72f-6627620bc5c5"), EstadoUsuario.Activo)).thenReturn(Optional.of(vendedor));
        when(compraRepository.findById(compra.getId())).thenReturn(Optional.of(compra));
        when(compraRepository.obtenerComprador(compra.getId())).thenReturn((Generico) comprador);
        doReturn(compra).when(compraRepository).save(compra);
        doNothing().when(firebaseMessagingService).enviarNotificacion(any(), any());
        doNothing().when(googleSMTP).enviarCorreo(any(), any(), any());
        DtConfirmarCompra dtConfirmarCompra = new DtConfirmarCompra(new Date(2023, 11, 10), new Date(2023, 11, 20), "");
        compraService.cambiarEstadoVenta(vendedor.getId(), compra.getId(), EstadoCompra.Completada, dtConfirmarCompra);
    }

    @Test
    void confirmarEntregaoReciboProductoComprador() throws FirebaseMessagingException, FirebaseAuthException {
        CompraProducto compraProducto = new CompraProducto(875, DateUtils.addDays(new Date(), -1), null, true, direccion1, Float.parseFloat("1000"), 1, Float.parseFloat("1000"), producto, null);
        Compra compraNueva = Compra.builder().id(UUID.fromString("d1def114-24c4-4c62-82af-ab0322794ec2")).fecha(DateUtils.addDays(new Date(), -5)).estado(EstadoCompra.Confirmada).tarjetaPago(tarjeta).infoEntrega(compraProducto).cuponAplicado(null).idTransaccion("08").build();
        comprador.getCompras().put(compraNueva.getId(), compraNueva);
        vendedor.getVentas().put(compraNueva.getId(), compraNueva);
        when(compraRepository.findById(any())).thenReturn(Optional.of(compraNueva));
        when(compraRepository.obtenerComprador(any())).thenReturn(comprador);
        when(compraRepository.obtenerVendedor(any())).thenReturn(vendedor);
        when(compraRepository.save(any())).thenReturn(null);
        doNothing().when(firebaseMessagingService).enviarNotificacion(any(), any());
        doNothing().when(googleSMTP).enviarCorreo(any(), any(), any());
        compraService.confirmarEntregaoReciboProducto(compraNueva.getId(), comprador.getCorreo());
        verify(googleSMTP).enviarCorreo(any(), any(), any());
    }

    @Test
    void confirmarEntregaoReciboProductoVendedor() throws FirebaseMessagingException, FirebaseAuthException {
        CompraProducto compraProducto = new CompraProducto(875, DateUtils.addDays(new Date(), -1), null, true, direccion1, Float.parseFloat("1000"), 1, Float.parseFloat("1000"), producto, null);
        Compra compraNueva = Compra.builder().id(UUID.fromString("d1def114-24c4-4c62-82af-ab0322794ec2")).fecha(DateUtils.addDays(new Date(), -5)).estado(EstadoCompra.Confirmada).tarjetaPago(tarjeta).infoEntrega(compraProducto).cuponAplicado(null).idTransaccion("08").build();
        comprador.getCompras().put(compraNueva.getId(), compraNueva);
        vendedor.getVentas().put(compraNueva.getId(), compraNueva);
        comprador.setMobileToken("MobileToken");
        comprador.setWebToken("WebToken");
        when(compraRepository.findById(any())).thenReturn(Optional.of(compraNueva));
        when(compraRepository.obtenerComprador(any())).thenReturn(comprador);
        when(compraRepository.obtenerVendedor(any())).thenReturn(vendedor);
        when(compraRepository.save(any())).thenReturn(null);
        doNothing().when(firebaseMessagingService).enviarNotificacion(any(), any());
        doNothing().when(googleSMTP).enviarCorreo(any(), any(), any());
        compraService.confirmarEntregaoReciboProducto(compraNueva.getId(), vendedor.getCorreo());
        verify(googleSMTP).enviarCorreo(any(), any(), any());
    }
}