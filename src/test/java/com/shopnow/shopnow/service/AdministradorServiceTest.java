package com.shopnow.shopnow.service;

import com.shopnow.shopnow.model.*;
import com.shopnow.shopnow.model.datatypes.DtUsuarioSlim;
import com.shopnow.shopnow.model.enumerados.EstadoCompra;
import com.shopnow.shopnow.model.enumerados.EstadoProducto;
import com.shopnow.shopnow.model.enumerados.EstadoSolicitud;
import com.shopnow.shopnow.model.enumerados.EstadoUsuario;
import com.shopnow.shopnow.repository.CompraRepository;
import com.shopnow.shopnow.repository.DatosVendedorRepository;
import com.shopnow.shopnow.repository.ProductoRepository;
import com.shopnow.shopnow.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.security.NoSuchAlgorithmException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdministradorServiceTest {

    @Mock
    UsuarioRepository usuarioRepository;

    @Mock
    GoogleSMTP googleSMTP;

    @Mock
    BraintreeUtils braintreeUtils;

    @Mock
    CompraRepository compraRepository;

    @InjectMocks
    AdministradorService administradorService;

    @Mock
    ProductoRepository productoRepository;

    @Mock
    DatosVendedorRepository datosVendedorRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    private Tarjeta tarjeta;

    private Direccion direccion1;

    private Generico comprador, comprador2, vendedor, vendedor2;

    private Compra compra;

    private Producto producto1, producto2;

    private DatosVendedor datosVendedor;

    private Map<UUID, Producto> productos = new HashMap<>();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        tarjeta = new Tarjeta("4111111111111111", "12/2023", "", "1111", "1");
        direccion1 = Direccion.builder().id(100).calle("Bulevar").numero("100").localidad("Montevideo").departamento("Montevideo").notas("").build();
        Map<Integer, Direccion> direccionesEnvio = new HashMap<>();
        direccionesEnvio.put(direccion1.getId(), direccion1);
        Map<String, Tarjeta> tarjetas = new HashMap<>();
        tarjetas.put(tarjeta.getIdTarjeta(), tarjeta);


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
        Map<UUID, Producto> productos2 = new HashMap<>();
        productos2.put(producto2.getId(), producto2);

        CompraProducto compraProducto = new CompraProducto(1, new Date(2023, 11, 15), new Date(2023, 11, 15), false, direccion1, Float.parseFloat("1000"), 1, Float.parseFloat("1000"), producto1, null);
        compra = Compra.builder().id(UUID.fromString("f998d287-9b75-4de2-9a39-ef43c8ab6de2")).fecha(new Date(2023, 11, 10)).estado(EstadoCompra.EsperandoConfirmacion).tarjetaPago(tarjeta).infoEntrega(compraProducto).cuponAplicado(null).idTransaccion("00").build();

        Map<UUID, Compra> compras = new HashMap<>();
        compras.put(compra.getId(), compra);

        comprador = Generico.builder().id(UUID.fromString("37894b62-ae0a-475f-a425-ffd489effbc1")).fechaNac(new Date())
                .reclamos(new HashMap<>())
                .correo("ss")
                .ventas(new HashMap<>())
                .productos(new HashMap<>())
                .estado(EstadoUsuario.Activo)
                .compras(compras)
                .braintreeCustomerId("123")
                .calificaciones(new HashMap<>())
                .direccionesEnvio(direccionesEnvio)
                .tarjetas(tarjetas).build();

        comprador2 = Generico.builder().id(UUID.fromString("50894b62-ae0a-475f-a425-ffd489effbc2")).fechaNac(new Date())
                .reclamos(new HashMap<>())
                .correo("ss")
                .ventas(new HashMap<>())
                .productos(new HashMap<>())
                .estado(EstadoUsuario.Eliminado)
                .compras(compras)
                .braintreeCustomerId("123")
                .calificaciones(new HashMap<>())
                .direccionesEnvio(direccionesEnvio)
                .tarjetas(tarjetas).build();

        datosVendedor = new DatosVendedor(0, "Prueba", "123123", "222", EstadoSolicitud.Pendiente, new HashMap<>());
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
        vendedor2 = Generico.builder().id(UUID.fromString("d652bd18-0d70-4f73-b72f-6627620bc5c6")).fechaNac(new Date())
                .reclamos(new HashMap<>())
                .ventas(new HashMap<>())
                .productos(productos2)
                .webToken("")
                .estado(EstadoUsuario.Activo)
                .compras(new HashMap<>())
                .calificaciones(new HashMap<>())
                .direccionesEnvio(new HashMap<>())
                .tarjetas(new HashMap<>()).datosVendedor(datosVendedor).build();
    }

    @Test
    void bloquearUsuario() {
        when(usuarioRepository.findByIdAndEstado(UUID.fromString("37894b62-ae0a-475f-a425-ffd489effbc1"), EstadoUsuario.Activo)).thenReturn(Optional.of(comprador));
        doReturn(comprador).when(usuarioRepository).save(comprador);
        doNothing().when(googleSMTP).enviarCorreo(any(), any(), any());
        administradorService.bloquearUsuario(UUID.fromString("37894b62-ae0a-475f-a425-ffd489effbc1"), "");
    }

    @Test
    void desbloquearUsuario() {
        when(usuarioRepository.findByIdAndEstado(UUID.fromString("37894b62-ae0a-475f-a425-ffd489effbc1"), EstadoUsuario.Bloqueado)).thenReturn(Optional.of(comprador));
        doReturn(comprador).when(usuarioRepository).save(comprador);
        doNothing().when(googleSMTP).enviarCorreo(any(), any(), any());
        administradorService.desbloquearUsuario(UUID.fromString("37894b62-ae0a-475f-a425-ffd489effbc1"));
    }

    @Test
    void eliminarUsuario() {
        when(usuarioRepository.findById(UUID.fromString("37894b62-ae0a-475f-a425-ffd489effbc1"))).thenReturn(Optional.of(comprador));
        when(usuarioRepository.findById(UUID.fromString("50894b62-ae0a-475f-a425-ffd489effbc2"))).thenReturn(Optional.of(comprador2));
        when(usuarioRepository.findById(UUID.fromString("d652bd18-0d70-4f73-b72f-6627620bc5c5"))).thenReturn(Optional.of(vendedor));
        doReturn(true).when(braintreeUtils).devolverDinero("00");
        when(compraRepository.obtenerVendedor(compra.getId())).thenReturn(vendedor);
        when(compraRepository.obtenerComprador(compra.getId())).thenReturn(comprador);
        doReturn(comprador).when(usuarioRepository).save(comprador);
        doNothing().when(googleSMTP).enviarCorreo(any(), any(), any());
        administradorService.eliminarUsuario(UUID.fromString("37894b62-ae0a-475f-a425-ffd489effbc1"), "Comprador");
        administradorService.eliminarUsuario(UUID.fromString("d652bd18-0d70-4f73-b72f-6627620bc5c5"), "Vendedor");
        assertThrowsExactly(com.shopnow.shopnow.controller.responsetypes.Excepcion.class, () -> administradorService.eliminarUsuario(UUID.fromString("50894b62-ae0a-475f-a425-ffd489effbc2"), "Vendedor"));
    }

    @Test
    void respuestaSolicitud() {
        when(usuarioRepository.findByIdAndEstado(UUID.fromString("d652bd18-0d70-4f73-b72f-6627620bc5c5"), EstadoUsuario.Activo)).thenReturn(Optional.of(vendedor));
        when(usuarioRepository.findByIdAndEstado(UUID.fromString("d652bd18-0d70-4f73-b72f-6627620bc5c6"), EstadoUsuario.Activo)).thenReturn(Optional.of(vendedor2));
        doReturn(vendedor2).when(usuarioRepository).save(vendedor2);
        doReturn(vendedor).when(usuarioRepository).saveAndFlush(vendedor);
        doNothing().when(googleSMTP).enviarCorreo(any(), any(), any());
        doReturn(producto1).when(productoRepository).save(producto1);
        doReturn(producto1).when(productoRepository).saveAndFlush(producto1);
        when(productoRepository.findById(producto1.getId())).thenReturn(Optional.of(producto1));

        doNothing().when(productoRepository).eliminarProductoCategoria(producto1.getId());
        doNothing().when(productoRepository).delete(producto1);
        doNothing().when(datosVendedorRepository).deleteById(vendedor.getDatosVendedor().getId());
        administradorService.respuestaSolicitud(UUID.fromString("d652bd18-0d70-4f73-b72f-6627620bc5c5"), false, "");
        administradorService.respuestaSolicitud(UUID.fromString("d652bd18-0d70-4f73-b72f-6627620bc5c6"), true, "");
    }

    @Test
    void crearOtroAdministrador() throws NoSuchAlgorithmException {
        when(usuarioRepository.save(any())).thenReturn(null);
        doNothing().when(googleSMTP).enviarCorreo(any(), any(), any());
        when(passwordEncoder.encode(anyString())).thenReturn("PasswordEncodeada");
        administradorService.crearAdministrador(new DtUsuarioSlim(null, "nuevoadm@shopnow.com", "Admin1", "DelSistema", null));
        verify(googleSMTP).enviarCorreo(any(), any(), any());
    }
}