package com.shopnow.shopnow.service;

import com.shopnow.shopnow.controller.responsetypes.Excepcion;
import com.shopnow.shopnow.model.Compra;
import com.shopnow.shopnow.model.Generico;
import com.shopnow.shopnow.model.Producto;
import com.shopnow.shopnow.model.Usuario;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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

    public void historialVentas(int pageNo, int pageSize, String sortBy, String sortDir, DtFiltrosVentas filtros, UUID id) {

        if (!sortBy.matches("nombreComprador|fecha|estado")) {
            throw new Excepcion("Atributo de ordenamiento invalido");
        }
        List<UUID> ventasIdConEstado = new ArrayList<>();
        if (filtros.getEstado() != null) {
            ventasIdConEstado = compraRepository.ventasPorEstadoYIdusuario(filtros.getEstado(), id);
        }
        List<UUID> ventasIdConFecha = new ArrayList<>();
        if (filtros.getFecha() != null) {
            ventasIdConFecha = compraRepository.ventasPorFechaYIdusuario(filtros.getFecha(), id);
        }
        List<UUID> ventasIdConNombreComprador = new ArrayList<>();
        if (filtros.getNombre() != null) {
            ventasIdConNombreComprador = compraRepository.ventasPorIdUsuarioYNombreComprador(id, filtros.getNombre());
        }

        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();

        // create Pageable instance
        Page<Compra> compras;
        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);

        List<UUID> ventasCumplenFiltro;

        if (filtros.getEstado() != null && filtros.getFecha() == null && filtros.getNombre() == null) { //1 0 0
            ventasCumplenFiltro = ventasIdConEstado;
        }

        if (filtros.getEstado() == null && filtros.getFecha() != null && filtros.getNombre() == null) { //1 0 0
            ventasCumplenFiltro = ventasIdConFecha;
        }

        if (filtros.getEstado() == null && filtros.getFecha() == null && filtros.getNombre() != null) { // 0 0 1
            ventasCumplenFiltro = ventasIdConNombreComprador;
        }


    }
}
