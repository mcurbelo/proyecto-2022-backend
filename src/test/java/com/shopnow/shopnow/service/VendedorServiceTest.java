package com.shopnow.shopnow.service;

import com.shopnow.shopnow.model.*;
import com.shopnow.shopnow.model.datatypes.DtCompraSlimVendedor;
import com.shopnow.shopnow.model.datatypes.DtFiltrosVentas;
import com.shopnow.shopnow.model.enumerados.EstadoCompra;
import com.shopnow.shopnow.model.enumerados.EstadoProducto;
import com.shopnow.shopnow.model.enumerados.EstadoSolicitud;
import com.shopnow.shopnow.model.enumerados.EstadoUsuario;
import com.shopnow.shopnow.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.text.ParseException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

    private Generico comprador;

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

        Direccion direccion1 = Direccion.builder().id(100).calle("18 de julio").numero("123").localidad("Montevideo").departamento("Montevideo").notas("").build();
        Map<Integer, Direccion> direccionesEnvio = new HashMap<>();

        CompraProducto infoEntrega = new CompraProducto(888, new Date(), null, true, direccion1, 80.00f, 2, 160.00f, producto1, new ArrayList<>());
        Compra venta1 = new Compra(UUID.fromString("4e913bb6-73b1-4280-b5dc-af70621c6639"), new Date(), EstadoCompra.Completada, new Tarjeta(), infoEntrega, null, "T1Venta", null);
        Map<UUID, Compra> ventas = new HashMap<>();
        ventas.put(venta1.getId(), venta1);

        vendedor = Generico.builder().id(UUID.fromString("d652bd18-0d70-4f73-b72f-6627620bc5c8")).fechaNac(new Date())
                .reclamos(new HashMap<>())
                .ventas(new HashMap<>())
                .productos(productos)
                .webToken("")
                .estado(EstadoUsuario.Activo)
                .ventas(ventas)
                .compras(new HashMap<>())
                .calificaciones(new HashMap<>())
                .direccionesEnvio(new HashMap<>())
                .tarjetas(new HashMap<>())
                .datosVendedor(new DatosVendedor(0, "EmpresaPrueba", "123123", "87448456654", EstadoSolicitud.Aceptado, new HashMap<>()))
                .build();

        direccionesEnvio.put(direccion1.getId(), direccion1);
        Map<String, Tarjeta> tarjetas = new HashMap<>();
        Map<UUID, Compra> compras = new HashMap<>();
        compras.put(venta1.getId(), venta1);

        comprador = Generico.builder().id(UUID.fromString("37894b62-ae0a-475f-a425-ffd489effbc1")).fechaNac(new Date())
                .reclamos(new HashMap<>())
                .ventas(new HashMap<>())
                .productos(new HashMap<>())
                .compras(compras)
                .correo("comprador@shopnow.com")
                .braintreeCustomerId("123")
                .password("aa")
                .calificaciones(new HashMap<>())
                .direccionesEnvio(direccionesEnvio)
                .tarjetas(tarjetas).build();

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
    void historialVentasFiltros() throws ParseException {
        DtFiltrosVentas filtros = new DtFiltrosVentas("Producto1", new Date(), EstadoCompra.Completada);
        List<UUID> result = new ArrayList<>();
        result.add(UUID.fromString("4e913bb6-73b1-4280-b5dc-af70621c6639"));
        when(compraRepository.ventasPorEstadoYIdusuario(any(), any())).thenReturn(result);
        when(compraRepository.ventasPorFechaYIdusuario(any(), any())).thenReturn(result);
        when(compraRepository.ventasPorIdUsuarioYNombreComprador(any(), any())).thenReturn(result);

        Page<Compra> pageResponse = new PageImpl(Arrays.asList(vendedor.getVentas().values().toArray()));
        when(compraRepository.findByIdIn(any(), any())).thenReturn(pageResponse);
        when(compraRepository.obtenerComprador(any())).thenReturn(comprador);
        Map<String, Object> historial = vendedorService.historialVentas(0, 20, "fecha", "asc", filtros, UUID.fromString("d652bd18-0d70-4f73-b72f-6627620bc5c8"));

        List<DtCompraSlimVendedor> ventas = (List<DtCompraSlimVendedor>) historial.get("ventas");
        assertEquals(1, ventas.size());
    }

    @Test
    void historialVentasSinFiltros() throws ParseException {
        Page<Compra> pageResponse = new PageImpl(Arrays.asList(vendedor.getVentas().values().toArray()));
        when(compraRepository.ventasPorIdUsuario(any(), any())).thenReturn(pageResponse);
        when(compraRepository.obtenerComprador(any())).thenReturn(comprador);
        Map<String, Object> historial = vendedorService.historialVentas(0, 20, "fecha", "asc", null, UUID.fromString("d652bd18-0d70-4f73-b72f-6627620bc5c8"));
        List<DtCompraSlimVendedor> ventas = (List<DtCompraSlimVendedor>) historial.get("ventas");
        assertEquals(1, ventas.size());
    }
}