package com.shopnow.shopnow.service;

import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.shopnow.shopnow.model.*;
import com.shopnow.shopnow.model.datatypes.*;
import com.shopnow.shopnow.model.enumerados.EstadoCompra;
import com.shopnow.shopnow.model.enumerados.EstadoProducto;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.io.IOException;
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

    @Mock
    GoogleSMTP googleSMTP;

    @Mock
    CompraRepository compraRepository;

    @Mock
    DatosVendedorRepository datosVendedorRepository;

    @InjectMocks
    FirebaseStorageService firebaseStorageService;

    @Mock
    ReclamoRepository reclamoRepository;

    @Mock
    FirebaseMessagingService firebaseMessagingService;

    private Generico vendedor;

    private Tarjeta tarjeta;
    private Usuario comprador;
    private Administrador admin;
    private Producto producto1, producto2;
    private Map<UUID, Producto> productos = new HashMap<>();

    private Direccion direccion1;

    private Generico compradorCompras;


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

        tarjeta = new Tarjeta("4111111111111111", "12/2023", "", "1111", "1");
        direccion1 = Direccion.builder().id(100).calle("Bulevar").numero("100").localidad("Montevideo").departamento("Montevideo").notas("").build();
        Map<String, Tarjeta> tarjetas2 = new HashMap<>();
        tarjetas.put(tarjeta.getIdTarjeta(), tarjeta);
        comprador = Generico.builder().id(UUID.fromString("37894b62-ae0a-475f-a425-ffd489effbc1")).fechaNac(new Date())
                .reclamos(new HashMap<>())
                .ventas(new HashMap<>())
                .productos(new HashMap<>())
                .compras(new HashMap<>())
                .correo("comprador@shopnow.com")
                .braintreeCustomerId("123")
                .password("aa")
                .calificaciones(new HashMap<>())
                .direccionesEnvio(direccionesEnvio)
                .tarjetas(tarjetas2).build();
        admin = Administrador.builder().id(UUID.fromString("37894b62-ae0a-475f-a425-ffd489effbc1"))
                .webToken("asdasd")
                .mobileToken("aaaa")
                .estado(EstadoUsuario.Activo)
                .password("aa")
                .correo("comprador@shopnow.com").build();
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

//        productosLista.add(producto1);
//        productosLista.add(producto2);


        CompraProducto infoEntrega = new CompraProducto(987, new Date(), null, true, new Direccion(), 80.00f, 2, 160.00f, producto1, new ArrayList<>());
        Compra compra1 = new Compra(UUID.fromString("6e91c407-717c-4e8a-9adc-ffc412475a0c"), new Date(), EstadoCompra.Completada, tarjeta, infoEntrega, null, "T1", null);
        Map<UUID, Compra> comprasList = new HashMap<>();
        comprasList.put(compra1.getId(), compra1);
        compradorCompras = Generico.builder().id(UUID.fromString("37894b62-ae0a-475f-a425-ffd489effbc8")).fechaNac(new Date())
                .reclamos(new HashMap<>())
                .ventas(new HashMap<>())
                .productos(new HashMap<>())
                .compras(comprasList)
                .correo("compradorCompras@shopnow.com")
                .braintreeCustomerId("123")
                .password("12")
                .calificaciones(new HashMap<>())
                .direccionesEnvio(direccionesEnvio)
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

    @Test
    void crearSolicitud() throws IOException, FirebaseMessagingException, FirebaseAuthException {
        when(datosVendedorRepository.existsByRutOrNombreEmpresaOrTelefonoEmpresa(any(), any(), any())).thenReturn(false);
        when(usuarioRepository.findByCorreo(comprador.getCorreo())).thenReturn(Optional.of(comprador));
        doNothing().when(productoService).agregarProducto(any(), any(), any(), anyBoolean());
        doReturn(direccion1).when(direccionRepository).saveAndFlush(any());
        //doReturn(Optional.of(direccion1)).when(direccionRepository).findById(direccion1.getId());
        doNothing().when(googleSMTP).enviarCorreo(any(), any(), any());
        doReturn(comprador).when(usuarioRepository).save(any());
        List<Administrador> administradores = new ArrayList<Administrador>();
        administradores.add((Administrador) admin);
        when(usuarioRepository.administradoresActivosConToken()).thenReturn(administradores);
        doNothing().when(firebaseMessagingService).enviarNotificacion(any(), any());
        //doReturn(false).when(direccionRepository).yaPerteneceAUnaEmpresa(any());
        List<String> categorias = new ArrayList<>();
        categorias.add("Tecnologia");
        DtAltaProducto altaProducto = new DtAltaProducto("Producto", 10, "", new Date(2023, 12, 30), Float.parseFloat("3000"), 180, false, categorias);
        DtDireccion local = DtDireccion.builder().esLocal(true).localidad("Montevideo").calle("").numero("a").departamento("").notas("").id(direccion1.getId()).build();
        DtSolicitud solicitud = new DtSolicitud("Cr", "11", "00", altaProducto, local, null);
        compradorService.crearSolicitud(solicitud, null, comprador.getCorreo());
        verify(usuarioRepository).save(any());
    }


    @Test
    void historialComprasFiltros() {
        DtFiltrosCompras filtros = new DtFiltrosCompras(new Date(), "vendedor", "Television", EstadoCompra.Completada);
        List<UUID> result = new ArrayList<>();
        result.add(UUID.fromString("6e91c407-717c-4e8a-9adc-ffc412475a0c"));
        when(compraRepository.comprasPorFechaYIdusuario(any(), any())).thenReturn(result);
        when(compraRepository.comprasPorEstadoYIdusuario(any(), any())).thenReturn(result);
        when(compraRepository.comprasPorIdUsuarioYNombreVendedor(any(), any())).thenReturn(result);
        when(compraRepository.comprasPorIdUsuarioYNombreProducto(any(), any())).thenReturn(result);

        Page<Compra> pageResponse = new PageImpl(Arrays.asList(compradorCompras.getCompras().values().toArray()));
        when(compraRepository.findByIdIn(any(), any())).thenReturn(pageResponse);
        when(compraRepository.obtenerVendedor(any())).thenReturn(vendedor);
        Map<String, Object> historial = compradorService.historialDeCompras(0, 20, "fecha", "asc", filtros, UUID.fromString("37894b62-ae0a-475f-a425-ffd489effbc8"));

        List<DtCompraSlimComprador> compras = (List<DtCompraSlimComprador>) historial.get("compras");
        assertEquals(1, compras.size());
    }

    @Test
    void historialComprasSinFiltros() {
        Page<Compra> pageResponse = new PageImpl(Arrays.asList(compradorCompras.getCompras().values().toArray()));
        when(compraRepository.comprasPorIdUsuario(any(), any())).thenReturn(pageResponse);
        when(compraRepository.obtenerVendedor(any())).thenReturn(vendedor);
        Map<String, Object> historial = compradorService.historialDeCompras(0, 20, "fecha", "asc", null, UUID.fromString("37894b62-ae0a-475f-a425-ffd489effbc8"));
        List<DtCompraSlimComprador> compras = (List<DtCompraSlimComprador>) historial.get("compras");
        assertEquals(1, compras.size());
    }


    //Eliminar direccion de retiro no puede ser y validar direccion cuando se edite que no se igual a otra que ya tiene
}


