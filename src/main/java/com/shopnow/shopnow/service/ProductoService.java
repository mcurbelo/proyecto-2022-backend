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
                    if (producto.getEstado() == EstadoProducto.Activo)
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
                    if (producto.getEstado() == EstadoProducto.Activo) {
                        soloProductosValidos.add(producto.getId());
                    }
                }
                productosIdEnCategoria = soloProductosValidos.stream().distinct().collect(Collectors.toList()); //Quitamos repetidos :)
            }
            List<UUID> productosIdConNombre = null;
            if (filtros.getNombre() != null) {
                List<Producto> productosConNombre = productoRepository.findByNombreContainingIgnoreCaseAndEstado(filtros.getNombre(), EstadoProducto.Activo); //Se puede optimizar a una sola
                productosIdConNombre = new ArrayList<>();
                for (Producto producto : productosConNombre) {
                    productosIdConNombre.add(producto.getId());
                }
            }
            {
                productosCumplenFiltro = UtilService.encontrarInterseccion(new HashSet<>(), productosIdEnCategoria, productosIdConNombre, productosIdEnEventoPromocional).stream().toList();
            }

        } else
            productosCumplenFiltro = productoRepository.productosValidosParaListar();

        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();

        // create Pageable instance
        Page<Producto> productos;
        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);

        productos = productoRepository.findByIdIn(productosCumplenFiltro, pageable);

        List<Producto> listaDeProductos = productos.getContent();

        List<DtProductoSlim> content = listaDeProductos.stream().map(this::generarDtProductoSlim).collect(Collectors.toList());


        Map<String, Object> response = new LinkedHashMap<>();
        response.put("productos", content);
        response.put("currentPage", productos.getNumber());
        response.put("totalItems", productos.getTotalElements());
        response.put("totalPages", productos.getTotalPages());

        //Si se quiere obtener info de evento activo
        if (filtros != null && filtros.getRecibirInfoEventoActivo()) {
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


    public DtProducto obtenerProducto(UUID id) {
        Optional<Producto> resultado = productoRepository.findById(id);
        Producto producto;
        if (resultado.isEmpty()) {
            throw new Excepcion("El producto no existe");
        } else {
            producto = resultado.get();
        }

        UUID idVendedor = productoRepository.vendedorProducto(id);
        Optional<Usuario> res = usuarioRepository.findById(idVendedor);
        Generico usuario;
        if (res.isEmpty()) {
            throw new Excepcion("El usuario no existe");
        } else {
            usuario = (Generico) res.get();
        }
        if (producto.getEstado() != EstadoProducto.Activo || usuario.getEstado() != EstadoUsuario.Activo) { //Verifico que el producto se pueda mostrar y ese usuario este activo
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
            for (Compra venta : ventas.values()) {
                sumaCalificacion += venta.getInfoEntrega().getCalificacion().getPuntuacion();
            }
            calificacion = sumaCalificacion / ventas.size();
        }
        //TODO Descontar el precio si esta en un evento promocional
        return new DtProducto(id, idVendedor, linksImagenes, producto.getNombre(), producto.getDescripcion(), producto.getPrecio(), producto.getPermiteEnvio(), producto.getComentarios(), nombreVendedor, calificacion, usuario.getImagen(), datosVendedor.getLocales());

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

