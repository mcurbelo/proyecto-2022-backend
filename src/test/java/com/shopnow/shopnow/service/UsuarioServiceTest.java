package com.shopnow.shopnow.service;

import com.shopnow.shopnow.model.*;
import com.shopnow.shopnow.model.datatypes.*;
import com.shopnow.shopnow.model.enumerados.*;
import com.shopnow.shopnow.repository.DatosVendedorRepository;
import com.shopnow.shopnow.repository.TarjetasRepository;
import com.shopnow.shopnow.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UsuarioServiceTest {

    @InjectMocks
    UsuarioService usuarioService;
    @Mock
    UsuarioRepository usuarioRepository;
    @Mock
    DatosVendedorRepository datosVendedorRepository;
    @Mock
    TarjetasRepository tarjetasRepository;
    @Mock
    FirebaseStorageService firebaseStorageService;
    @Mock
    BraintreeUtils braintreeUtils;
    @Mock
    GoogleSMTP googleSMTP;

    private Generico comprador;

    private Generico vendedor;

    private Administrador administrador;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        Direccion direccion1 = Direccion.builder().id(100).calle("Bulevar").numero("100").localidad("Montevideo").departamento("Montevideo").notas("").build();
        Map<Integer, Direccion> direccionesEnvio = new HashMap<>();
        direccionesEnvio.put(direccion1.getId(), direccion1);

        List<URLimagen> imagenes = new ArrayList<URLimagen>();
        imagenes.add(new URLimagen("urldeimagen"));
        Producto producto1 = Producto.builder()
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
        Producto producto2 = Producto.builder()
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


        comprador = Generico.builder().id(UUID.fromString("37894b62-ae0a-475f-a425-ffd489effbc1")).fechaNac(new Date())
                .nombre("Comprador")
                .reclamos(new HashMap<>())
                .correo("ss")
                .ventas(new HashMap<>())
                .productos(new HashMap<>())
                .estado(EstadoUsuario.Activo)
                .compras(new HashMap<>())
                .braintreeCustomerId(null)
                .calificaciones(new HashMap<>())
                .direccionesEnvio(direccionesEnvio)
                .tarjetas(new HashMap<>()).build();

        DatosVendedor datosVendedor = new DatosVendedor(0, "Prueba", "123123", "222", EstadoSolicitud.Aceptado, new HashMap<>());
        vendedor = Generico.builder().id(UUID.fromString("d652bd18-0d70-4f73-b72f-6627620bc5c5")).fechaNac(new Date())
                .nombre("Vendedor")
                .reclamos(new HashMap<>())
                .ventas(new HashMap<>())
                .productos(productos)
                .webToken("")
                .estado(EstadoUsuario.Activo)
                .compras(new HashMap<>())
                .calificaciones(new HashMap<>())
                .direccionesEnvio(new HashMap<>())
                .tarjetas(new HashMap<>())
                .datosVendedor(datosVendedor).build();

        administrador = Administrador.builder()
                .id(UUID.fromString("99850ddc-24db-4657-8fc6-3848ecf60fed"))
                .correo("correoAdm@shopnow.com")
                .nombre("Admin1")
                .apellido("ShopNow")
                .password("passwordEndcodeada")
                .estado(EstadoUsuario.Activo)
                .imagen("")
                .fechaRegistro(new Date())
                .build();
    }


    @Test
    void infoUsuarioComprador() {
        when(usuarioRepository.findByIdAndEstado(any(), any())).thenReturn(Optional.of(comprador));
        DtUsuario retorno = usuarioService.infoUsuario(comprador.getId().toString());
        assertTrue(Objects.equals(retorno.getCorreo(), comprador.getCorreo()) && retorno.getDatosVendedor() == null);
    }

    @Test
    void infoUsuarioVendedorPendiente() {
        vendedor.getDatosVendedor().setEstadoSolicitud(EstadoSolicitud.Pendiente);
        when(usuarioRepository.findByIdAndEstado(any(), any())).thenReturn(Optional.of(vendedor));
        DtUsuario retorno = usuarioService.infoUsuario(vendedor.getId().toString());
        assertTrue(Objects.equals(retorno.getCorreo(), vendedor.getCorreo()) && retorno.getDatosVendedor() == null);
    }

    @Test
    void infoUsuarioVendedorAceptado() {
        when(usuarioRepository.findByIdAndEstado(any(), any())).thenReturn(Optional.of(vendedor));
        DtUsuario retorno = usuarioService.infoUsuario(vendedor.getId().toString());
        assertTrue(Objects.equals(retorno.getCorreo(), vendedor.getCorreo()) && retorno.getDatosVendedor() != null);
    }

    @Test
    void infoUsuarioAdministrador() {
        when(usuarioRepository.findByIdAndEstado(any(), any())).thenReturn(Optional.of(administrador));
        DtUsuario retorno = usuarioService.infoUsuario(administrador.getId().toString());
        assertTrue(Objects.equals(retorno.getNombre(), administrador.getNombre()) && retorno.getRol() == Rol.ADM);
    }

    @Test
    void modificarInfoBasica() {
        Generico generico = mock(Generico.class);
        when(usuarioRepository.findByIdAndEstado(any(), any())).thenReturn(Optional.of(generico));
        when(usuarioRepository.save(any())).thenReturn(null);
        when(usuarioRepository.existsByCorreoAndEstado("nuevoCorreo@shopnow.com", EstadoUsuario.Activo)).thenReturn(false);
        when(usuarioRepository.existsByCorreoAndEstado("nuevoCorreo@shopnow.com", EstadoUsuario.Bloqueado)).thenReturn(false);
        DtUsuario modificar = new DtUsuario(null, null, null, "nuevoCorreo@shopnow.com", null, "NuevoNombre", "NuevoApellido", "123456789", null, null, null, null);
        usuarioService.modificarInfoBasica(comprador.getId(), modificar);
        verify(generico).setCorreo("nuevoCorreo@shopnow.com");
        verify(generico).setNombre("NuevoNombre");
        verify(generico).setApellido("NuevoApellido");
        verify(generico).setTelefono("123456789");
        verify(usuarioRepository).save(any());
    }

    @Test
    void modificarInfoBasicaCorreoExiste1() {
        Generico generico = mock(Generico.class);
        when(usuarioRepository.findByIdAndEstado(any(), any())).thenReturn(Optional.of(generico));
        when(usuarioRepository.save(any())).thenReturn(null);
        when(usuarioRepository.existsByCorreoAndEstado("nuevoCorreo@shopnow.com", EstadoUsuario.Activo)).thenReturn(false);
        when(usuarioRepository.existsByCorreoAndEstado("nuevoCorreo@shopnow.com", EstadoUsuario.Bloqueado)).thenReturn(true);
        DtUsuario modificar = new DtUsuario(null, null, null, "nuevoCorreo@shopnow.com", null, "NuevoNombre", "NuevoApellido", "123456789", null, null, null, null);
        assertThrowsExactly(com.shopnow.shopnow.controller.responsetypes.Excepcion.class, () -> usuarioService.modificarInfoBasica(comprador.getId(), modificar));
    }

    @Test
    void modificarInfoBasicaCorreoExiste2() {
        Generico generico = mock(Generico.class);
        when(usuarioRepository.findByIdAndEstado(any(), any())).thenReturn(Optional.of(generico));
        when(usuarioRepository.save(any())).thenReturn(null);
        when(usuarioRepository.existsByCorreoAndEstado("nuevoCorreo@shopnow.com", EstadoUsuario.Activo)).thenReturn(true);
        when(usuarioRepository.existsByCorreoAndEstado("nuevoCorreo@shopnow.com", EstadoUsuario.Bloqueado)).thenReturn(false);
        DtUsuario modificar = new DtUsuario(null, null, null, "nuevoCorreo@shopnow.com", null, "NuevoNombre", "NuevoApellido", "123456789", null, null, null, null);
        assertThrowsExactly(com.shopnow.shopnow.controller.responsetypes.Excepcion.class, () -> usuarioService.modificarInfoBasica(comprador.getId(), modificar));
    }

    @Test
    void modificarInfoBasicaCorreoExiste3() {
        Generico generico = mock(Generico.class);
        when(usuarioRepository.findByIdAndEstado(any(), any())).thenReturn(Optional.of(generico));
        when(usuarioRepository.save(any())).thenReturn(null);
        when(usuarioRepository.existsByCorreoAndEstado("nuevoCorreo@shopnow.com", EstadoUsuario.Activo)).thenReturn(true);
        when(usuarioRepository.existsByCorreoAndEstado("nuevoCorreo@shopnow.com", EstadoUsuario.Bloqueado)).thenReturn(true);
        DtUsuario modificar = new DtUsuario(null, null, null, "nuevoCorreo@shopnow.com", null, "NuevoNombre", "NuevoApellido", "123456789", null, null, null, null);
        assertThrowsExactly(com.shopnow.shopnow.controller.responsetypes.Excepcion.class, () -> usuarioService.modificarInfoBasica(comprador.getId(), modificar));
    }

    @Test
    void modificarImagen() throws IOException {
        when(usuarioRepository.findByIdAndEstado(any(), any())).thenReturn(Optional.of(comprador));
        when(firebaseStorageService.uploadFile(any(), any())).thenReturn("urlNuevaImagen");
        when(usuarioRepository.save(any())).thenReturn(null);
        MultipartFile imagen = new MockMultipartFile("Imagen1", "imagen.png", MediaType.IMAGE_JPEG_VALUE,
                "Imagen del usuario nueva".getBytes());
        usuarioService.modificarImagen(comprador.getId(), imagen);
        verify(usuarioRepository).save(any());
    }

    @Test
    void modificarDatosUsuario() {
        Generico genericoVendedor = mock(Generico.class);
        DatosVendedor datosVendedor = new DatosVendedor(762, "EmpresaDePrueba", "123456789012", "123456789", EstadoSolicitud.Aceptado, new HashMap<>());
        when(genericoVendedor.getDatosVendedor()).thenReturn(datosVendedor);
        DtModificarUsuario modificar = new DtModificarUsuario(null, "ContraNueva", "ContraVieja", null, "NuevoNombreEmpresa", "78978945615263");
        when(usuarioRepository.findByIdAndEstado(any(), any())).thenReturn(Optional.of(genericoVendedor));
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        when(genericoVendedor.getPassword()).thenReturn(encoder.encode("ContraVieja"));
        when(datosVendedorRepository.existsByNombreEmpresa(any())).thenReturn(false);
        when(datosVendedorRepository.existsByTelefonoEmpresa(any())).thenReturn(false);
        when(usuarioRepository.save(any())).thenReturn(null);
        usuarioService.modificarDatosUsuario(UUID.fromString("ca75331c-194a-4d34-852f-b59cbdcb73c4"), modificar);
        verify(usuarioRepository).save(genericoVendedor);
    }

    @Test
    void modificarDatosUsuarioEmpresaRepetido() {
        Generico genericoVendedor = mock(Generico.class);
        DatosVendedor datosVendedor = new DatosVendedor(762, "EmpresaDePrueba", "123456789012", "123456789", EstadoSolicitud.Aceptado, new HashMap<>());
        when(genericoVendedor.getDatosVendedor()).thenReturn(datosVendedor);
        DtModificarUsuario modificar = new DtModificarUsuario(null, "ContraNueva", "ContraVieja", null, "NuevoNombreEmpresa", "78978945615263");
        when(usuarioRepository.findByIdAndEstado(any(), any())).thenReturn(Optional.of(genericoVendedor));
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        when(genericoVendedor.getPassword()).thenReturn(encoder.encode("ContraVieja"));
        when(datosVendedorRepository.existsByNombreEmpresa(any())).thenReturn(true);
        when(datosVendedorRepository.existsByTelefonoEmpresa(any())).thenReturn(false);
        assertThrowsExactly(com.shopnow.shopnow.controller.responsetypes.Excepcion.class, () -> usuarioService.modificarDatosUsuario(comprador.getId(), modificar));
    }

    @Test
    void modificarDatosUsuarioEmpresaTelefonoRepetida() {
        Generico genericoVendedor = mock(Generico.class);
        DatosVendedor datosVendedor = new DatosVendedor(762, "EmpresaDePrueba", "123456789012", "123456789", EstadoSolicitud.Aceptado, new HashMap<>());
        when(genericoVendedor.getDatosVendedor()).thenReturn(datosVendedor);
        DtModificarUsuario modificar = new DtModificarUsuario(null, "ContraNueva", "ContraVieja", null, "NuevoNombreEmpresa", "78978945615263");
        when(usuarioRepository.findByIdAndEstado(any(), any())).thenReturn(Optional.of(genericoVendedor));
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        when(genericoVendedor.getPassword()).thenReturn(encoder.encode("ContraVieja"));
        when(datosVendedorRepository.existsByNombreEmpresa(any())).thenReturn(false);
        when(datosVendedorRepository.existsByTelefonoEmpresa(any())).thenReturn(true);
        assertThrowsExactly(com.shopnow.shopnow.controller.responsetypes.Excepcion.class, () -> usuarioService.modificarDatosUsuario(comprador.getId(), modificar));
    }


    @Test
    void agregarTarjeta() {
        when(usuarioRepository.findByIdAndEstado(any(), any())).thenReturn(Optional.of(comprador));
        when(braintreeUtils.generateCustomerId(comprador)).thenReturn("IDBraintree");
        when(usuarioRepository.save(any())).thenReturn(null);
        Tarjeta tarjeta = new Tarjeta("64abfff8-edbc-48e9-ae37-cfd66851376d", "12/24", "UrlImagen", "123", "tokenGenerado");
        when(braintreeUtils.agregarTarjeta(any(), any())).thenReturn(tarjeta);
        when(tarjetasRepository.save(any())).thenReturn(null);
        when(usuarioRepository.save(any())).thenReturn(null);
        DtTarjeta dtTarjeta = new DtTarjeta("655644566545645123", "153", "12/24");
        usuarioService.agregarTarjeta(dtTarjeta, comprador.getId());
        verify(usuarioRepository, times(2)).save(any());
    }


    @Test
    void listadoDeUsuarios() {
        DtFiltrosUsuario filtros = new DtFiltrosUsuario("Comprador", "ShopNow", "comprador@shopnow.com", EstadoUsuario.Activo);
        List<UUID> result = new ArrayList<>();
        result.add(UUID.fromString("37894b62-ae0a-475f-a425-ffd489effbc1"));
        when(usuarioRepository.usuarioNombreApellido(any())).thenReturn(result);
        when(usuarioRepository.usuariosConApellido(any())).thenReturn(result);
        when(usuarioRepository.usuariosConCorreo(any())).thenReturn(result);
        when(usuarioRepository.usuariosConEstado(any())).thenReturn(result);

        List<Usuario> usuarios = new ArrayList<>();
        usuarios.add(comprador);

        Page<Usuario> pageResponse = new PageImpl(usuarios);
        when(usuarioRepository.findByIdIn(any(), any())).thenReturn(pageResponse);
        Map<String, Object> historial = usuarioService.listadoDeUsuarios(0, 20, "nombre", "asc", filtros);

        List<DtUsuarioSlim> usuariosList = (List<DtUsuarioSlim>) historial.get("usuarios");
        assertEquals(1, usuariosList.size());
    }

    @Test
    void listadoDeUsuariosSinFiltros() {
        List<UUID> result = new ArrayList<>();
        result.add(UUID.fromString("37894b62-ae0a-475f-a425-ffd489effbc1"));

        List<Usuario> usuarios = new ArrayList<>();
        usuarios.add(comprador);
        usuarios.add(vendedor);
        usuarios.add(administrador);

        Page<Usuario> pageResponse = new PageImpl(usuarios);
        when(usuarioRepository.todosLosUsuarios(any())).thenReturn(pageResponse);
        Map<String, Object> historial = usuarioService.listadoDeUsuarios(0, 20, "nombre", "asc", null);

        List<DtUsuarioSlim> usuariosList = (List<DtUsuarioSlim>) historial.get("usuarios");
        assertEquals(3, usuariosList.size());
    }

    @Test
    void eliminarMiCuenta() {
        when(usuarioRepository.findByIdAndEstado(any(), any())).thenReturn(Optional.of(comprador));
        when(usuarioRepository.save(any())).thenReturn(null);
        doNothing().when(googleSMTP).enviarCorreo(any(), any(), any());
        usuarioService.eliminarMiCuenta(comprador.getId());
        verify(googleSMTP).enviarCorreo(any(), any(), any());
    }

    @Test
    void eliminarMiCuentaComprasActivas() {
        Compra compra = new Compra();
        compra.setId(UUID.fromString("62bc1158-3219-4bb6-849b-708b1190349d"));
        compra.setEstado(EstadoCompra.EsperandoConfirmacion);
        comprador.getCompras().put(compra.getId(), compra);
        when(usuarioRepository.findByIdAndEstado(any(), any())).thenReturn(Optional.of(comprador));
        when(usuarioRepository.save(any())).thenReturn(null);
        doNothing().when(googleSMTP).enviarCorreo(any(), any(), any());
        assertThrowsExactly(com.shopnow.shopnow.controller.responsetypes.Excepcion.class, () -> usuarioService.eliminarMiCuenta(comprador.getId()));
    }

    @Test
    void eliminarMiCuentaProductosActivos() {
        when(usuarioRepository.findByIdAndEstado(any(), any())).thenReturn(Optional.of(vendedor));
        when(usuarioRepository.save(any())).thenReturn(null);
        doNothing().when(googleSMTP).enviarCorreo(any(), any(), any());
        assertThrowsExactly(com.shopnow.shopnow.controller.responsetypes.Excepcion.class, () -> usuarioService.eliminarMiCuenta(comprador.getId()));
    }

    @Test
    void eliminarMiCuentaVentasActivas() {
        Compra venta = new Compra();
        venta.setId(UUID.fromString("e3ce665d-0036-4b14-a0c8-2d1fc0657fad"));
        venta.setEstado(EstadoCompra.EsperandoConfirmacion);
        comprador.getVentas().put(venta.getId(), venta);
        when(usuarioRepository.findByIdAndEstado(any(), any())).thenReturn(Optional.of(vendedor));
        when(usuarioRepository.save(any())).thenReturn(null);
        doNothing().when(googleSMTP).enviarCorreo(any(), any(), any());
        assertThrowsExactly(com.shopnow.shopnow.controller.responsetypes.Excepcion.class, () -> usuarioService.eliminarMiCuenta(vendedor.getId()));
    }
}