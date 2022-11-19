package com.shopnow.shopnow.service;

import com.shopnow.shopnow.controller.responsetypes.Excepcion;
import com.shopnow.shopnow.model.*;
import com.shopnow.shopnow.model.datatypes.*;
import com.shopnow.shopnow.model.enumerados.EstadoCompra;
import com.shopnow.shopnow.model.enumerados.EstadoProducto;
import com.shopnow.shopnow.model.enumerados.EstadoSolicitud;
import com.shopnow.shopnow.model.enumerados.EstadoUsuario;
import com.shopnow.shopnow.repository.*;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

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
        Optional<Producto> resultado = productoRepository.findById(idProducto);
        Producto producto;
        if (resultado.isEmpty()) {
            throw new Excepcion("El producto no existe");
        } else {
            producto = resultado.get();
        }
        if (producto.getEstado() == nuevoEstado) {
            throw new Excepcion("El producto ya se encuentra en ese estado");
        }
        Optional<Usuario> res = usuarioRepository.findByIdAndEstado(id, EstadoUsuario.Activo);
        Generico usuario;
        if (res.isEmpty()) {
            throw new Excepcion("El usuario no existe");
        } else {
            usuario = (Generico) res.get();
        }
        if (usuario.getDatosVendedor() == null || usuario.getDatosVendedor().getEstadoSolicitud() != EstadoSolicitud.Aceptado) {
            throw new Excepcion("Usuario no habilitado para esta funcionalidad");
        }

        if (nuevoEstado == EstadoProducto.BloqueadoADM || !usuario.getProductos().containsKey(idProducto)) {
            throw new Excepcion("Este usuario no puede ejecutar esta accion");
        }
        producto.setEstado(nuevoEstado);
        productoRepository.save(producto);
    }

    public Map<String, Object> historialVentas(int pageNo, int pageSize, String sortBy, String sortDir, DtFiltrosVentas filtros, UUID id) throws ParseException {

        if (!sortBy.matches("fecha|estado")) {
            throw new Excepcion("Atributo de ordenamiento invalido");
        }

        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();

        // create Pageable instance
        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);

        Page<Compra> ventas;

        List<UUID> ventasCumplenFiltro;

        if (filtros != null) {
            List<UUID> ventasIdConEstado = null;
            if (filtros.getEstado() != null) {
                ventasIdConEstado = compraRepository.ventasPorEstadoYIdusuario(filtros.getEstado().name(), id);
            }

            List<UUID> ventasIdConFecha = null;
            if (filtros.getFecha() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                String fecha = sdf.format(filtros.getFecha());
                ventasIdConFecha = compraRepository.ventasPorFechaYIdusuario(fecha, id);
            }

            List<UUID> ventasIdConNombreComprador = null;
            if (filtros.getNombre() != null) {
                ventasIdConNombreComprador = compraRepository.ventasPorIdUsuarioYNombreComprador(id, filtros.getNombre());
            }
            ventasCumplenFiltro = UtilService.encontrarInterseccion(new HashSet<>(), ventasIdConEstado, ventasIdConFecha, ventasIdConNombreComprador).stream().toList();
            ventas = compraRepository.findByIdIn(ventasCumplenFiltro, pageable);
        } else {
            ventas = compraRepository.ventasPorIdUsuario(id, pageable);
        }

        List<Compra> listaDeVentas = ventas.getContent();

        List<DtCompraSlimVendedor> content = listaDeVentas.stream().map(venta -> getDtCompraSlim(venta, id)).toList();

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("ventas", content);
        response.put("currentPage", ventas.getNumber());
        response.put("totalItems", ventas.getTotalElements());
        response.put("totalPages", ventas.getTotalPages());

        return response;
    }

    public DtBalance balanceVendedor(UUID idUsuario, Date fechaInicio, Date fechaFin, Boolean historico) {
        Generico usuario = (Generico) usuarioRepository.findByIdAndEstado(idUsuario, EstadoUsuario.Activo).orElseThrow(() -> new Excepcion("El usuario no esta disponible para utilizar esta funcionaldiad"));
        float totalGanado = 0.00f, ganadoPorEnvio = 0.00f, ganadoPorRetiro = 0.00f, perdidoPorComision = 0.00f;
        int cantidadPorEnvio = 0, cantidadPorRetiro = 0;
        float comisionShopNow = 0.05f;
        List<Compra> ventas;
        if (historico) {
            ventas = usuarioRepository.ventasTotalesCompletadas(usuario.getId());
        } else {
            ventas = usuarioRepository.ventasPorFechaCompletadas(usuario.getId(), fechaInicio, fechaFin);
        }
        for (Compra venta : ventas) {
            float comision = venta.getInfoEntrega().getPrecioTotal() * comisionShopNow;
            perdidoPorComision += comision;
            totalGanado += venta.getInfoEntrega().getPrecioTotal() - comision;

            if (venta.getInfoEntrega().getEsEnvio()) {
                ganadoPorEnvio += venta.getInfoEntrega().getPrecioTotal() - comision;
                cantidadPorEnvio++;
            } else {
                ganadoPorRetiro += venta.getInfoEntrega().getPrecioTotal() - comision;
                cantidadPorRetiro++;
            }
        }
        return new DtBalance(totalGanado, ganadoPorEnvio, ganadoPorRetiro, perdidoPorComision, cantidadPorEnvio, cantidadPorRetiro);
    }

    public List<DtTopProductosVendidos> topTeenProductosVendidos(UUID idUsuario, Date fechaInicio, Date fechaFin, Boolean historico) {
        Generico usuario = (Generico) usuarioRepository.findByIdAndEstado(idUsuario, EstadoUsuario.Activo).orElseThrow(() -> new Excepcion("El usuario no esta disponible para utilizar esta funcionaldiad"));
        if (historico) {
            return usuarioRepository.topteenProductosVendidos(usuario.getId()).stream().map(tupla -> new DtTopProductosVendidos(
                            tupla.get(0, String.class),
                            tupla.get(1, Integer.class)
                    ))
                    .collect(Collectors.toList());
        } else {
            return usuarioRepository.topteenProductosVendidosEntreFecha(usuario.getId(), fechaInicio, fechaFin).stream().map(tupla -> new DtTopProductosVendidos(
                            tupla.get(0, String.class),
                            tupla.get(1, Integer.class)
                    ))
                    .collect(Collectors.toList());
        }
    }

    public List<DtProductosMejoresCalificados> productosMejoresCalificados(UUID idUsuario, Date fechaInicio, Date fechaFin, Boolean historico) {
        Generico usuario = (Generico) usuarioRepository.findByIdAndEstado(idUsuario, EstadoUsuario.Activo).orElseThrow(() -> new Excepcion("El usuario no esta disponible para utilizar esta funcionaldiad"));
        if (historico) {
            return usuarioRepository.promedioCalificacionPorProducto(usuario.getId()).stream().map(tupla -> new DtProductosMejoresCalificados(
                            tupla.get(0, String.class),
                            tupla.get(1, Float.class),
                            tupla.get(2, Integer.class)
                    ))
                    .collect(Collectors.toList());
        } else {
            return usuarioRepository.promedioCalificacionPorProductoEntreFecha(usuario.getId(), fechaInicio, fechaFin).stream().map(tupla -> new DtProductosMejoresCalificados(
                            tupla.get(0, String.class),
                            tupla.get(1, Float.class),
                            tupla.get(2, Integer.class)
                    ))
                    .collect(Collectors.toList());
        }
    }


    private DtCompraSlimVendedor getDtCompraSlim(Compra compra, UUID idVendedor) {
        Generico comprador = compraRepository.obtenerComprador(compra.getId());
        Producto producto = compra.getInfoEntrega().getProducto();

        String imagen = producto.getImagenesURL().get(0).getUrl();
        CompraProducto infoEntrega = compra.getInfoEntrega();

        boolean puedeCalificar = compra.getEstado() == EstadoCompra.Completada;
        if (puedeCalificar) {
            for (Calificacion calficacion : infoEntrega.getCalificaciones()) {
                if (calficacion.getAutor().getId().equals(idVendedor)) {
                    puedeCalificar = false;
                    break;
                }
            }
        }

        Date fechaEntrega = ObjectUtils.firstNonNull(infoEntrega.getHorarioRetiroLocal(), infoEntrega.getTiempoEstimadoEnvio());
        boolean puedeCompletar = compra.getEstado() != EstadoCompra.Completada && compra.getEstado() != EstadoCompra.Devolucion && fechaEntrega != null && fechaEntrega.before(new Date());

        Map<UUID, Compra> compras = comprador.getCompras();
        float sumaCalificacion = 0, calificacion = 0;
        if (compras.size() != 0) {
            int ventasCalificacion = 0;
            for (Compra compraRealizada : compras.values()) {
                if (compraRealizada.getInfoEntrega().getCalificaciones().isEmpty()) {
                    continue;
                }
                for (Calificacion calificacionItem : compraRealizada.getInfoEntrega().getCalificaciones()) {
                    if (calificacionItem.getAutor().getId().compareTo(comprador.getId()) != 0) {
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


        return new DtCompraSlimVendedor(compra.getId(), comprador.getId(), comprador.getNombre() + " " + comprador.getApellido(),
                compra.getInfoEntrega().getProducto().getNombre(),
                compra.getInfoEntrega().getCantidad(), compra.getFecha(),
                compra.getEstado(), compra.getInfoEntrega().getPrecioTotal(), compra.getInfoEntrega().getPrecioUnitario(), imagen, fechaEntrega, puedeCalificar, puedeCompletar, infoEntrega.getEsEnvio(), infoEntrega.getDireccionEnvioORetiro().toString(), calificacion);
    }
}


