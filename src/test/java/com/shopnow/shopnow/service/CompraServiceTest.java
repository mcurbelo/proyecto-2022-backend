package com.shopnow.shopnow.service;

import com.braintreegateway.BraintreeGateway;
import com.braintreegateway.Result;
import com.braintreegateway.Transaction;
import com.braintreegateway.TransactionRequest;
import com.braintreegateway.util.NodeWrapper;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.shopnow.shopnow.controller.responsetypes.Excepcion;
import com.shopnow.shopnow.model.*;
import com.shopnow.shopnow.model.datatypes.DtCompra;
import com.shopnow.shopnow.model.enumerados.EstadoProducto;
import com.shopnow.shopnow.model.enumerados.EstadoSolicitud;
import com.shopnow.shopnow.model.enumerados.EstadoUsuario;
import com.shopnow.shopnow.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CompraServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    CompraRepository compraRepository;

    @Mock
    private TarjetaRepository tarjetaRepository;

    @Mock
    private DireccionRepository direccionRepository;

    @InjectMocks
    FirebaseMessagingService firebaseMessagingService;

    @Mock
    private BraintreeUtils braintreeUtils;
    @InjectMocks
    private CompraService compraService;

    @Mock
    GoogleSMTP googleSMTP;

    @Mock
    UtilService utilService;

    @Mock
    private Result<Transaction> transactionResult;

    @InjectMocks
    private Generico usuario;

    private Tarjeta tarjeta;

    private Direccion direccion1;

    private Usuario comprador;

    private Usuario vendedor;

    private DatosVendedor datosVendedor;

    private Producto producto;

    @BeforeEach
    void setUp(){
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
        datosVendedor = new DatosVendedor(0, "Prueba", "123123", "222", EstadoSolicitud.Aceptado, new HashMap<>());

        producto =  Producto.builder()
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

        vendedor = Generico.builder().id(UUID.fromString("d652bd18-0d70-4f73-b72f-6627620bc5c5")).fechaNac(new Date())
                .reclamos(new HashMap<>())
                .ventas(new HashMap<>())
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

        doNothing().when(googleSMTP).enviarCorreo(any(), any(), any());

        when(utilService.detallesCompra(any(), any(), any(),any(), any())).thenReturn("DetalleCompra");
//        when(usuarioRepository.save(any())).thenReturn("");
//        when(productoRepository.save(any())).thenReturn("");
        DtCompra dtcompra = new DtCompra(vendedor.getId(), producto.getId(), 1, null, tarjeta.getIdTarjeta(), true, direccion1.getId(), null);
        assertNotNull(compraService.nuevaCompra(dtcompra, comprador.getId()));
    }
}