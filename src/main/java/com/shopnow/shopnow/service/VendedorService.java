package com.shopnow.shopnow.service;

import com.shopnow.shopnow.controller.responsetypes.Excepcion;
import com.shopnow.shopnow.model.Compra;
import com.shopnow.shopnow.model.Generico;
import com.shopnow.shopnow.model.Producto;
import com.shopnow.shopnow.model.Usuario;
import com.shopnow.shopnow.model.datatypes.DtCompraSlim;
import com.shopnow.shopnow.model.datatypes.DtFiltrosVentas;
import com.shopnow.shopnow.model.enumerados.EstadoProducto;
import com.shopnow.shopnow.model.enumerados.EstadoUsuario;
import com.shopnow.shopnow.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class VendedorService {

    @Autowired
    UsuarioRepository usuarioRepository;

    @Autowired
    ProductoService productoService;

    @Autowired
    DireccionRepository direccionRepository;

    @Autowired
    GoogleSMTP googleSMTP;

    @Autowired
    ProductoRepository productoRepository;

    @Autowired
    DatosVendedorRepository datosVendedorRepository;

    @Autowired
    FirebaseStorageService firebaseStorageService;

    @Autowired
    CompraRepository compraRepository;

    @SafeVarargs
    private static <T, C extends Collection<T>> C encontrarInterseccion(C newCollection, Collection<T>... collections) {
        boolean first = true;
        for (Collection<T> collection : collections) {
            if (collection.isEmpty())
                continue;
            if (first) {
                newCollection.addAll(collection);
                first = false;
            } else {
                newCollection.retainAll(collection);
            }
        }
        return newCollection;
    }

    public void cambiarEstadoProducto(UUID idProducto, UUID id, EstadoProducto nuevoEstado) {
        //No valido que realmente sea un vendedor, porque teniendo el token solo los que tengan el rol vendedor van a poder utilizar esta funcionalidad
        Optional<Producto> resultado = productoRepository.findById(idProducto);
        Producto producto;
        if (resultado.isEmpty()) {
            throw new Excepcion("El producto no existe");
        } else {
            producto = resultado.get();
        }
        if (producto.getEstado() == nuevoEstado) {
            throw new Excepcion("Ya el producto ya se encuentra en ese estado");
        }
        Optional<Usuario> res = usuarioRepository.findByIdAndEstado(id, EstadoUsuario.Activo);
        Generico usuario;
        if (res.isEmpty()) {
            throw new Excepcion("El usuario no existe");
        } else {
            usuario = (Generico) res.get();
        }
        if (nuevoEstado == EstadoProducto.BloqueadoADM || !usuario.getProductos().containsKey(idProducto)) {
            throw new Excepcion("Este usuario no puede ejecutar esta accion");
        }
        producto.setEstado(nuevoEstado);
        productoRepository.save(producto);
    }

    public Map<String, Object> historialVentas(int pageNo, int pageSize, String sortBy, String sortDir, DtFiltrosVentas filtros, UUID id) throws ParseException {

        if (!sortBy.matches("nombreComprador|fecha|estado")) {
            throw new Excepcion("Atributo de ordenamiento invalido");
        }

        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();

        // create Pageable instance
        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);

        List<UUID> ventasCumplenFiltro = new ArrayList<>();

        if (filtros != null) {
            List<UUID> ventasIdConEstado = new ArrayList<>();
            if (filtros.getEstado() != null) {
                ventasIdConEstado = compraRepository.ventasPorEstadoYIdusuario(filtros.getEstado().name(), id);
            }
            
            List<UUID> ventasIdConFecha = new ArrayList<>();
            if (filtros.getFecha() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                String fecha = sdf.format(filtros.getFecha());
                ventasIdConFecha = compraRepository.ventasPorFechaYIdusuario(fecha, id);
            }

            List<UUID> ventasIdConNombreComprador = new ArrayList<>();
            if (filtros.getNombre() != null) {
                ventasIdConNombreComprador = compraRepository.ventasPorIdUsuarioYNombreComprador(id, filtros.getNombre());
            }
            ventasCumplenFiltro = encontrarInterseccion(new HashSet<>(), ventasIdConEstado, ventasIdConFecha, ventasIdConNombreComprador).stream().toList();
        } else {
            ventasCumplenFiltro = compraRepository.ventasPorIdUsuario(id);
        }

        Page<Compra> ventas = compraRepository.findByIdIn(ventasCumplenFiltro, pageable);

        List<Compra> listaDeVentas = ventas.getContent();

        List<DtCompraSlim> content = listaDeVentas.stream().map(this::getDtCompraSlim).toList();

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("ventas", content);
        response.put("currentPage", ventas.getNumber());
        response.put("totalItems", ventas.getTotalElements());
        response.put("totalPages", ventas.getTotalPages());

        return response;
    }

    private DtCompraSlim getDtCompraSlim(Compra compra) {
        UUID idComprador = compraRepository.obtenerComprador(compra.getId());
        Optional<Usuario> resUsu = usuarioRepository.findById(idComprador);
        Usuario comprador = new Generico();
        if (resUsu.isPresent()) comprador = resUsu.get();
        return new DtCompraSlim(compra.getId(), comprador.getId(), comprador.getNombre() + " " + comprador.getApellido(), compra.getFecha(), compra.getEstado(), compra.getInfoEntrega().getPrecioTotal());
    }

}


