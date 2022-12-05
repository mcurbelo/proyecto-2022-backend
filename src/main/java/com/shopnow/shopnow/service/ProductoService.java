package com.shopnow.shopnow.service;

import com.shopnow.shopnow.controller.responsetypes.Excepcion;
import com.shopnow.shopnow.model.*;
import com.shopnow.shopnow.model.datatypes.*;
import com.shopnow.shopnow.model.enumerados.EstadoProducto;
import com.shopnow.shopnow.model.enumerados.EstadoSolicitud;
import com.shopnow.shopnow.model.enumerados.EstadoUsuario;
import com.shopnow.shopnow.repository.CategoriaRepository;
import com.shopnow.shopnow.repository.EventoPromocionalRepository;
import com.shopnow.shopnow.repository.ProductoRepository;
import com.shopnow.shopnow.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductoService {

    @Autowired
    UsuarioRepository usuarioRepository;

    @Autowired
    CategoriaRepository categoriaRepository;

    @Autowired
    FirebaseStorageService firebaseStorageService;

    @Autowired
    ProductoRepository productoRepository;

    @Autowired
    EventoPromocionalRepository eventoPromocionalRepository;

    @Autowired
    UtilService utilService;

    public void agregarProducto(DtAltaProducto datosProducto, MultipartFile[] imagenes, String email, boolean esSolicitud) throws Excepcion, IOException {
        if (datosProducto.getFechaFin() != null && datosProducto.getFechaFin().before(new Date())) {
            throw new Excepcion("La fecha de fin es invalida");
        }
        Optional<Usuario> resultado = usuarioRepository.findByCorreo(email);
        Generico usuario;
        if (resultado.isEmpty()) {
            throw new Excepcion("El usuario no existe");
        } else {
            usuario = (Generico) resultado.get();
        }

        if (!esSolicitud && usuario.getDatosVendedor() == null) {
            throw new Excepcion("Funcionalidad no disponible para este usuario.");
        }

        if (!esSolicitud && usuario.getDatosVendedor().getEstadoSolicitud() != EstadoSolicitud.Aceptado)
            throw new Excepcion("El usuario con el correo ingresado no esta habilitado para agregar productos.");

        datosProducto.getCategorias().forEach(categoria -> {
            if (!categoriaRepository.existsById((categoria)))
                throw new Excepcion("Una o mas categorias no son validas");
        });

        List<URLimagen> linkImagenes = new ArrayList<>();
        String idImagen = UUID.randomUUID().toString();
        int i = 0;
        for (MultipartFile imagen : imagenes) {
            linkImagenes.add(new URLimagen(firebaseStorageService.uploadFile(imagen, idImagen + "--img" + i)));
            i++;
        }
        Producto producto = Producto.builder()
                .nombre(datosProducto.getNombreProducto())
                .stock(datosProducto.getStock())
                .imagenesURL(linkImagenes)
                .descripcion(datosProducto.getDescripcion())
                .fechaInicio(new Date())
                .fechaFin(datosProducto.getFechaFin())
                .estado((esSolicitud) ? EstadoProducto.Pausado : EstadoProducto.Activo)
                .precio(datosProducto.getPrecio())
                .diasGarantia(datosProducto.getDiasGarantia())
                .permiteEnvio(datosProducto.getPermiteEnvio())
                .build();
        productoRepository.saveAndFlush(producto);
        List<Categoria> categorias = categoriaRepository.findAllById(datosProducto.getCategorias());
        for (Categoria categoria : categorias) {
            categoria.getProductos().put(producto.getId(), producto);
        }
        categoriaRepository.saveAll(categorias);
        usuario.getProductos().put(producto.getId(), producto);
        usuarioRepository.save(usuario);
    }


    public Map<String, Object> busquedaDeProductos(int pageNo, int pageSize, String sortBy, String sortDir, DtFiltros filtros) {

        if (!sortBy.matches("nombre|fecha_inicio|precio|permite_envio")) {
            throw new Excepcion("Atributo de ordenamiento invalido");
        }
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Page<Producto> productos;
        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);
        List<UUID> productosCumplenFiltro;

        EventoPromocional evento;

        if (filtros != null) {
            List<UUID> productosIdEnEventoPromocional = null;
            if (filtros.getIdEventoPromocional() != null) {
                Optional<EventoPromocional> resevento = eventoPromocionalRepository.findById(filtros.getIdEventoPromocional());
                if (resevento.isEmpty())
                    throw new Excepcion("El id del evento no existe");
                else
                    evento = resevento.get();
                if (evento.getFechaFin().before(new Date()) || evento.getFechaInicio().after(new Date())) { //Si ya termino o si aun no comenzo
                    throw new Excepcion("El evento no esta disponible");
                }
                List<UUID> soloProductosValidos = new ArrayList<>();
                for (Producto producto : evento.getProductos().values()) {
                    if (producto.getEstado() == EstadoProducto.Activo && producto.getFechaFin().after(new Date()))
                        soloProductosValidos.add(producto.getId());
                }
                productosIdEnEventoPromocional = soloProductosValidos;
            }

            List<UUID> productosIdEnCategoria = null;
            if (filtros.getCategorias() != null) {
                List<Categoria> categorias = categoriaRepository.findAllById(filtros.getCategorias());
                List<Producto> productosFiltro = new ArrayList<>();
                for (Categoria categoria : categorias) {
                    productosFiltro.addAll(categoria.getProductos().values().stream().toList());
                }
                List<UUID> soloProductosValidos = new ArrayList<>();
                for (Producto producto : productosFiltro) {
                    if (producto.getEstado() == EstadoProducto.Activo && (producto.getFechaFin() == null || producto.getFechaFin().after(new Date())) && producto.getStock() > 0) {
                        soloProductosValidos.add(producto.getId());
                    }
                }
                productosIdEnCategoria = soloProductosValidos.stream().distinct().collect(Collectors.toList()); //Quitamos repetidos :)
            }
            List<UUID> productosIdConNombre = null;
            if (filtros.getNombre() != null) {
                productosIdConNombre = productoRepository.productosContenganNombre(filtros.getNombre());
            }
            productosCumplenFiltro = UtilService.encontrarInterseccion(new HashSet<>(), productosIdEnCategoria, productosIdConNombre, productosIdEnEventoPromocional).stream().toList();
            productos = productoRepository.buscarEstosProductos(productosCumplenFiltro, pageable);

        } else
            productos = productoRepository.productosValidosParaListar(pageable);

        List<Producto> listaDeProductos = productos.getContent();

        List<DtProductoSlim> content = listaDeProductos.stream().map(this::generarDtProductoSlim).collect(Collectors.toList());


        Map<String, Object> response = new LinkedHashMap<>();
        response.put("productos", content);
        response.put("currentPage", productos.getNumber());
        response.put("totalItems", productos.getTotalElements());
        response.put("totalPages", productos.getTotalPages());

        //Si se quiere obtener info de evento activo
//        if (filtros != null && filtros.getRecibirInfoEventoActivo() != null && filtros.getRecibirInfoEventoActivo()) {
//            Optional<EventoPromocional> eventoActivo = eventoPromocionalRepository.eventoActivo();
//            if (eventoActivo.isPresent()) {
//                EventoPromocional eventoInfo = eventoActivo.get();
//                response.put("EventoPromocionalActivo", generarDtEventoInfo(eventoInfo));
//            } else {
//                response.put("EventoPromocionalActivo", null);
//            }
//
//        }

        return response;

    }


    public DtProducto obtenerProducto(UUID id) {
        Optional<Producto> resultado = productoRepository.findById(id);
        Producto producto;
        if (resultado.isEmpty()) {
            throw new Excepcion("El producto no existe");
        } else {
            producto = resultado.get();
        }

        Generico usuario = productoRepository.vendedorProducto(id);
        if (producto.getEstado() != EstadoProducto.Activo || usuario.getEstado() != EstadoUsuario.Activo || (producto.getFechaFin() != null && producto.getFechaFin().before(new Date()))) { //Verifico que el producto se pueda mostrar y ese usuario este activo
            throw new Excepcion("Este producto no se puede visualizar en este momento");
        }

        List<String> linksImagenes = new ArrayList<>();
        for (URLimagen url : producto.getImagenesURL()) { //Obtengo links de imagenes del producto
            linksImagenes.add(url.getUrl());
        }
        String nombreVendedor;
        DatosVendedor datosVendedor = usuario.getDatosVendedor(); //Veo que nombre mandar del vendedor
        if (datosVendedor.getNombreEmpresa() == null) {
            nombreVendedor = usuario.getNombre() + " " + usuario.getApellido();
        } else {
            nombreVendedor = datosVendedor.getNombreEmpresa();
        }
        Map<UUID, Compra> ventas = usuario.getVentas();  //Calculo la calificacion :)
        float sumaCalificacion = 0, calificacion = 0;
        if (ventas.size() != 0) {
            int ventasCalificacion = 0;
            for (Compra venta : ventas.values()) {
                if (venta.getInfoEntrega().getCalificaciones().isEmpty()) {
                    continue;
                }
                for (Calificacion calificacionItem : venta.getInfoEntrega().getCalificaciones()) {
                    if (calificacionItem.getAutor().getId().compareTo(usuario.getId()) != 0) {
                        sumaCalificacion += calificacionItem.getPuntuacion();
                        ventasCalificacion++;
                    }
                }

            }
            if (ventasCalificacion == 0)
                calificacion = 0;
            else
                calificacion = sumaCalificacion / ventasCalificacion;
        }
        //TODO Descontar el precio si esta en un evento promocional
        return new DtProducto(id, usuario.getId(), linksImagenes, producto.getNombre(), producto.getDescripcion(), producto.getPrecio(), producto.getPermiteEnvio(), producto.getComentarios().values().stream().toList(), nombreVendedor, calificacion, usuario.getImagen(), datosVendedor.getLocales().values().stream().toList(), producto.getStock(), producto.getDiasGarantia());
    }


    public Map<String, Object> listarMisProductos(int pageNo, int pageSize, String sortBy, String sortDir, DtFiltosMisProductos filtros, UUID id) {
        if (!sortBy.matches("nombre|fecha_inicio|precio|permite_envio")) {
            throw new Excepcion("Atributo de ordenamiento invalido");
        }
        List<UUID> productosCumplenFiltro;

        // create Pageable instance
        Page<Producto> productos;
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);


        if (filtros != null) {
            List<UUID> productosIdConFecha = null;
            if (filtros.getFecha() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                String fecha = sdf.format(filtros.getFecha());
                productosIdConFecha = productoRepository.misProductosPorFecha(id, fecha);
            }
            List<UUID> productosIdConEstado = null;
            if (filtros.getEstado() != null) {
                productosIdConEstado = productoRepository.misProductosPorEstado(id, filtros.getEstado().name());
            }
            List<UUID> productosIdConNombre = null;
            if (filtros.getNombre() != null) {
                productosIdConNombre = productoRepository.misProductosConNombre(id, filtros.getNombre());
            }
            List<UUID> productosEnCategorias = null;
            if (filtros.getCategorias() != null) {
                productosEnCategorias = new ArrayList<>();
                for (String categoria : filtros.getCategorias()) {
                    productosEnCategorias.addAll(productoRepository.misProductosEnCategoria(id, categoria));
                }
            }
            productosCumplenFiltro = UtilService.encontrarInterseccion(new HashSet<>(), productosEnCategorias, productosIdConFecha, productosIdConEstado, productosIdConNombre).stream().toList();
            productos = productoRepository.buscarEstosProductos(productosCumplenFiltro, pageable);
        } else
            productos = productoRepository.misProductos(id, pageable);


        List<Producto> listaDeProductos = productos.getContent();

        List<DtMiProducto> content = listaDeProductos.stream().map((producto) -> utilService.generarDtMiProductos(producto)).collect(Collectors.toList());

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("misProductos", content);
        response.put("currentPage", productos.getNumber());
        response.put("totalItems", productos.getTotalElements());
        response.put("totalPages", productos.getTotalPages());
        return response;
    }

    public void editarProducto(UUID idProducto, UUID idVendedor, DtModificarProducto nuevosDatos, MultipartFile[] imagenes) throws IOException {
        Producto producto = productoRepository.findById(idProducto).orElseThrow(() -> new Excepcion("El producto no existe"));
        Generico vendedor = (Generico) usuarioRepository.findByIdAndEstado(idVendedor, EstadoUsuario.Activo).orElseThrow(() -> new Excepcion("El vendedor no esta habilitado"));

        if (!vendedor.getProductos().containsKey(idProducto))
            throw new Excepcion("El vendedor no contiene este producto");

        if (nuevosDatos.getDescripcion() != null)
            producto.setDescripcion(nuevosDatos.getDescripcion());

        if (nuevosDatos.getFechaFin() != null)
            producto.setFechaFin(nuevosDatos.getFechaFin());

        if (nuevosDatos.getStock() != null) {
            producto.setStock(nuevosDatos.getStock());
        }

        if (nuevosDatos.getPrecio() != null) {
            producto.setPrecio(nuevosDatos.getPrecio());
        }

        if (nuevosDatos.getImagenesQuitar() != null && nuevosDatos.getImagenesQuitar().size() > 0) {
            List<URLimagen> imagenesEliminar = new ArrayList<>();

            for (Integer indice : nuevosDatos.getImagenesQuitar()) {
                imagenesEliminar.add(producto.getImagenesURL().get(indice));
            }
            producto.getImagenesURL().removeAll(imagenesEliminar);

        }

        String idImagen = UUID.randomUUID().toString();
        int i = 0;
        for (MultipartFile imagen : imagenes) {
            if (imagen.getSize() > 0 && !imagen.getName().equals(""))
                producto.getImagenesURL().add(i, new URLimagen(firebaseStorageService.uploadFile(imagen, idImagen + "--img" + i)));
            i++;
        }


        if (nuevosDatos.getPermiteEnvio() != null) {
            producto.setPermiteEnvio(nuevosDatos.getPermiteEnvio());
        }
        productoRepository.save(producto);
    }

    private DtProductoSlim generarDtProductoSlim(Producto producto) {
        return new DtProductoSlim(producto.getId(), producto.getNombre(), producto.getImagenesURL().get(0).getUrl(), producto.getPrecio(), producto.getStock(), producto.getPermiteEnvio());
    }


    private DtEventoInfo generarDtEventoInfo(EventoPromocional evento) {
        List<DtProductoSlim> infoProductos = new ArrayList<>();
        Map<UUID, Producto> enEvento = evento.getProductos();
        if (enEvento.size() > 20) { //La idea no es enviar todos sino un par para hacer una preview de que el evento esta activo
            List<Producto> prod = new ArrayList<>(enEvento.values());
            for (int i = 0; i <= 20; i++) {
                infoProductos.add(generarDtProductoSlim(prod.get(i)));
            }
        } else {
            enEvento.forEach((k, v) -> infoProductos.add(generarDtProductoSlim(v)));
        }

        return new DtEventoInfo(infoProductos, evento.getId(), evento.getNombre(), evento.getFechaFin(), evento.getCategorias().keySet().stream().toList());
    }
}

