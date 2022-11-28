package com.shopnow.shopnow.service;

import com.shopnow.shopnow.controller.responsetypes.Excepcion;
import com.shopnow.shopnow.model.*;
import com.shopnow.shopnow.model.datatypes.DtFiltros;
import com.shopnow.shopnow.model.enumerados.EstadoProducto;
import com.shopnow.shopnow.model.enumerados.EstadoSolicitud;
import com.shopnow.shopnow.repository.CategoriaRepository;
import com.shopnow.shopnow.repository.ProductoRepository;
import org.hibernate.service.spi.InjectService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.*;
import org.springframework.util.Assert;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class ProductoServiceTest {

    @Mock
    ProductoRepository productoRepository;

    @Mock
    CategoriaRepository categoriaRepository;

    @InjectMocks
    ProductoService productoService;
    private Usuario comprador;

    private Usuario vendedor;

    private Tarjeta tarjeta;

    private Producto producto1, producto2;

    private Map<UUID, Producto> productos = new HashMap<>();

    private List<Producto> productosLista = new ArrayList<>();

    private Direccion direccion1, direccion;

    private Categoria categoria;

    private DatosVendedor datosVendedor;

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
        datosVendedor = new DatosVendedor(0, "Prueba", "123123", "222", EstadoSolicitud.Aceptado, new HashMap<>());

        List<URLimagen> imagenes = new ArrayList<URLimagen>();
        imagenes.add(new URLimagen("urldeimagen"));
        producto1 =  Producto.builder()
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
        producto2 =  Producto.builder()
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

        vendedor = Generico.builder().id(UUID.fromString("d652bd18-0d70-4f73-b72f-6627620bc5c5")).fechaNac(new Date())
                .reclamos(new HashMap<>())
                .ventas(new HashMap<>())
                .productos(productos)
                .webToken("")
                .compras(new HashMap<>())
                .calificaciones(new HashMap<>())
                .direccionesEnvio(new HashMap<>())
                .tarjetas(new HashMap<>()).datosVendedor(datosVendedor).build();
        categoria = Categoria.builder().productos(productos).nombre("Tecnologia").build();
    }

    @Test
    void obtenerProducto() {
        Page<Producto> productosFiltrados = new PageImpl<>(productosLista);;
        when(productoRepository.productosValidosParaListar(any())).thenReturn(productosFiltrados);
        Sort sort = Sort.by("nombre").descending();
        Pageable pageable = PageRequest.of(0, 10, sort);
        assertTrue(!productoRepository.productosValidosParaListar(pageable).isEmpty());
        assertThrows(Excepcion.class, () -> productoService.busquedaDeProductos(0, 10, "a", "", null));
        List<String> categorias = new ArrayList<>();
        categorias.add("Tecnologia");
        DtFiltros filtros = new DtFiltros("Televison", categorias, null, false);

        List<UUID> productoNombre = new ArrayList<UUID>();
        productoNombre.add(producto1.getId());
        when(productoRepository.productosContenganNombre(any(String.class))).thenReturn(productoNombre);

        List<Categoria> categoriasReturn = new ArrayList<Categoria>();
        categoriasReturn.add(categoria);
        when(categoriaRepository.findAllById(any())).thenReturn(categoriasReturn);

        List<UUID> productoUUIDs = new ArrayList<UUID>();
        productoUUIDs.add(producto1.getId());

        when(productoRepository.buscarEstosProductos(productoUUIDs, pageable)).thenReturn(productosFiltrados);
        assertNotNull(productoService.busquedaDeProductos(0, 10, "nombre", "", null));
        assertNotNull(productoService.busquedaDeProductos(0, 10, "nombre", "", filtros));
    }
}