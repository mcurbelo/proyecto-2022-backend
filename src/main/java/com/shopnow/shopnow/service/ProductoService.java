package com.shopnow.shopnow.service;

import com.shopnow.shopnow.controller.responsetypes.Excepcion;
import com.shopnow.shopnow.model.*;
import com.shopnow.shopnow.model.datatypes.DtAltaProducto;
import com.shopnow.shopnow.model.datatypes.DtEventoInfo;
import com.shopnow.shopnow.model.datatypes.DtFiltros;
import com.shopnow.shopnow.model.datatypes.DtProductoSlim;
import com.shopnow.shopnow.model.enumerados.EstadoProducto;
import com.shopnow.shopnow.model.enumerados.EstadoSolicitud;
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

    public void agregarProducto(DtAltaProducto datosProducto, MultipartFile[] imagenes) throws Excepcion, IOException {
        //TODO Para testear
        categoriaRepository.save(Categoria.builder().nombre("Tecnologia").build());

        if (datosProducto.getFechaFin() != null && datosProducto.getFechaFin().before(new Date())) {
            throw new Excepcion("La fecha de fin es invalida");
        }
        Optional<Usuario> resultado = usuarioRepository.findByCorreo(datosProducto.getEmailVendedor());
        Generico usuario;
        if (resultado.isEmpty()) {
            throw new Excepcion("El usuario no existe");
        } else {
            usuario = (Generico) resultado.get();
        }

        if (!datosProducto.getEsSolicitud() && usuario.getDatosVendedor() == null) {
            throw new Excepcion("Funcionalidad no disponible para este usuario.");
        }

        if (!datosProducto.getEsSolicitud() && usuario.getDatosVendedor().getEstadoSolicitud() != EstadoSolicitud.Aceptado)
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
                .estado((datosProducto.getEsSolicitud()) ? EstadoProducto.Pausado : EstadoProducto.Activo)
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

        if (!sortBy.matches("nombre|fechaInicio|precio|permiteEnvio")) {
            throw new Excepcion("Atributo de ordenamiento invalido");
        }

        EventoPromocional evento = new EventoPromocional();
        if (filtros.getIdEventoPromocional() != null) {
            Optional<EventoPromocional> resevento = eventoPromocionalRepository.findById(filtros.getIdEventoPromocional());
            if (resevento.isEmpty())
                throw new Excepcion("El id del evento no existe");
            else
                evento = resevento.get();
            if (evento.getFechaFin().before(new Date()) || evento.getFechaInicio().after(new Date())) { //Si ya termino o si aun no comenzo
                throw new Excepcion("El evento no esta disponible");
            }
        }
        List<UUID> productosIdEnCategoria = new ArrayList<>();
        if (filtros.getCategorias() != null) {
            List<Categoria> categorias = categoriaRepository.findAllById(filtros.getCategorias());
            List<UUID> productosFiltro = new ArrayList<>();
            for (Categoria categoria : categorias) {
                productosFiltro.addAll(categoria.getProductos().keySet());
            }
            productosIdEnCategoria = productosFiltro.stream().distinct().collect(Collectors.toList()); //Quitamos repetidos :)
        }

        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();

        // create Pageable instance
        Page<Producto> productos;
        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);

        List<UUID> productosValidos = productoRepository.productosValidosParaListar(); //Obtengo todos los id's de productos validos
        List<UUID> productosCumplenFiltro;

        if (filtros.getCategorias() != null && filtros.getNombre() == null && filtros.getIdEventoPromocional() == null) { // 1 0 0
            productosCumplenFiltro = productosIdEnCategoria;

        } else if (filtros.getCategorias() == null && filtros.getNombre() != null && filtros.getIdEventoPromocional() == null) { // 0 1 0
            productosCumplenFiltro = productoRepository.findByNombreContaining(filtros.getNombre());

        } else if (filtros.getCategorias() == null && filtros.getNombre() == null && filtros.getIdEventoPromocional() != null) { // 0 0 1
            productosCumplenFiltro = evento.getProductos().keySet().stream().toList();
        } else if (filtros.getCategorias() != null && filtros.getNombre() != null && filtros.getIdEventoPromocional() == null) { // 1 1 0
            List<UUID> productosFiltro2 = productoRepository.findByNombreContaining(filtros.getNombre());
            productosCumplenFiltro = productosIdEnCategoria.stream()
                    .distinct()
                    .filter(productosFiltro2::contains)
                    .collect(Collectors.toList());
        } else if (filtros.getCategorias() == null && filtros.getNombre() != null && filtros.getIdEventoPromocional() != null) { // 0 1 1
            productosCumplenFiltro = productoRepository.buscarProductoEnEventoYporNombre(filtros.getIdEventoPromocional(), filtros.getNombre());
        } else if (filtros.getCategorias() != null && filtros.getNombre() == null && filtros.getIdEventoPromocional() != null) { // 1 0 1
            List<UUID> productosFiltro2 = evento.getProductos().keySet().stream().toList();
            productosCumplenFiltro = productosIdEnCategoria.stream()
                    .distinct()
                    .filter(productosFiltro2::contains)
                    .collect(Collectors.toList());
        } else if (filtros.getCategorias() != null && filtros.getNombre() != null && filtros.getIdEventoPromocional() != null) { // 1 1 1
            List<UUID> productosFiltro2 = productoRepository.buscarProductoEnEventoYporNombre(filtros.getIdEventoPromocional(), filtros.getNombre());
            productosCumplenFiltro = productosIdEnCategoria.stream()
                    .distinct()
                    .filter(productosFiltro2::contains)
                    .collect(Collectors.toList());
        } else { // 0 0 0
            productosCumplenFiltro = productosValidos;
        }
        if (productosCumplenFiltro != productosValidos) {
            List<UUID> productosIDParaMostar = productosValidos.stream()
                    .distinct()
                    .filter(productosCumplenFiltro::contains).toList();
            productos = productoRepository.findByIdIn(productosIDParaMostar, pageable);
        } else {
            productos = productoRepository.findByIdIn(productosValidos, pageable);
        }


        List<Producto> listaDeProductos = productos.getContent();

        List<DtProductoSlim> content = listaDeProductos.stream().map(this::generarDtProductoSlim).collect(Collectors.toList());


        Map<String, Object> response = new LinkedHashMap<>();
        response.put("productos", content);
        response.put("currentPage", productos.getNumber());
        response.put("totalItems", productos.getTotalElements());
        response.put("totalPages", productos.getTotalPages());

        //Si se quiere obtener info de evento activo
        if (filtros.getRecibirInfoEventoActivo()) {
            Optional<EventoPromocional> eventoActivo = eventoPromocionalRepository.eventoActivo();
            if (eventoActivo.isPresent()) {
                EventoPromocional eventoInfo = eventoActivo.get();
                response.put("EventoPromocionalActivo", generarDtEventoInfo(eventoInfo));
            } else {
                response.put("EventoPromocionalActivo", null);
            }

        }

        return response;

    }

    private DtProductoSlim generarDtProductoSlim(Producto producto) {
        return new DtProductoSlim(producto.getId(), producto.getNombre(), producto.getImagenesURL().get(0).getUrl(), producto.getPrecio());
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

