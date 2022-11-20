package com.shopnow.shopnow.controller;


import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.shopnow.shopnow.model.datatypes.*;
import com.shopnow.shopnow.model.enumerados.*;
import com.shopnow.shopnow.service.CompraService;
import com.shopnow.shopnow.service.ProductoService;
import com.shopnow.shopnow.service.ReclamoService;
import com.shopnow.shopnow.service.VendedorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.text.ParseException;
import java.util.*;

@RestController
@RequestMapping("/api/vendedores")
public class VendedorController {

    @Autowired
    VendedorService vendedorService;

    @Autowired
    ProductoService productoService;

    @Autowired
    CompraService compraService;

    @Autowired
    ReclamoService reclamoService;

    @PutMapping("/{idUsuario}/productos/{id}/estado")
    public ResponseEntity<String> cambiarEstado(@PathVariable(value = "idUsuario") UUID id, @PathVariable(value = "id") UUID idProducto, @RequestParam(value = "nuevoEstado") EstadoProducto nuevoEstado) {
        /*TODO Utilizar cuando se utilicen al 100% los token
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!email.equals(correo)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        */
        vendedorService.cambiarEstadoProducto(idProducto, id, nuevoEstado);
        return new ResponseEntity<>("Producto cambiado de estado con exito", HttpStatus.OK);
    }

    @GetMapping("/{id}/productos")
    public Map<String, Object> listarMisProductos(
            @RequestParam(value = "pageNo", defaultValue = "0", required = false) int pageNo,
            @RequestParam(value = "pageSize", defaultValue = "20", required = false) int pageSize,
            @RequestParam(value = "sortBy", defaultValue = "nombre", required = false) String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "asc", required = false) String sortDir,
            @RequestParam(value = "categorias", required = false) List<String> categorias,
            @RequestParam(value = "estado", required = false) EstadoProducto estado,
            @RequestParam(value = "fecha", required = false) @DateTimeFormat(pattern = "dd/MM/yyyy") Date fecha,
            @RequestParam(value = "nombre", required = false) String nombre,
            @PathVariable(value = "id") UUID id) {
        DtFiltosMisProductos filtros = null;
        if (categorias != null || estado != null || fecha != null || nombre != null)
            filtros = new DtFiltosMisProductos(fecha, nombre, categorias, estado);
        return productoService.listarMisProductos(pageNo, pageSize, sortBy, sortDir, filtros, id);
    }


    @PutMapping("/{idUsuario}/ventas/{id}/estado")
    public ResponseEntity<String> cambiarEstadoVenta(@PathVariable(value = "idUsuario") UUID id, @PathVariable(value = "id") UUID idVenta, @RequestParam(value = "nuevoEstado") EstadoCompra nuevoEstado, @RequestBody DtConfirmarCompra info) throws FirebaseMessagingException, FirebaseAuthException {
        compraService.cambiarEstadoVenta(id, idVenta, nuevoEstado, info);
        return new ResponseEntity<>("Estado de venta cambiado con exito!!!", HttpStatus.OK);
    }


    @GetMapping("/{id}/ventas")
    public Map<String, Object> busquedaDeventas(
            @RequestParam(value = "pageNo", defaultValue = "0", required = false) int pageNo,
            @RequestParam(value = "pageSize", defaultValue = "20", required = false) int pageSize,
            @RequestParam(value = "sortBy", defaultValue = "fecha", required = false) String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "asc", required = false) String sortDir,
            @RequestParam(value = "estado", required = false) EstadoCompra estado,
            @RequestParam(value = "fecha", required = false) @DateTimeFormat(pattern = "dd/MM/yyyy") Date fecha,
            @RequestParam(value = "nombre", required = false) String nombre,
            @PathVariable(value = "id") UUID id) throws ParseException {
        DtFiltrosVentas filtros = null;
        if (estado != null || fecha != null || nombre != null)
            filtros = new DtFiltrosVentas(nombre, fecha, estado);
        return vendedorService.historialVentas(pageNo, pageSize, sortBy, sortDir, filtros, id);
    }

    @PutMapping("/{id}/ventas/{idVenta}/reclamos/{idReclamo}")
    public ResponseEntity<String> gestionarReclamo(@PathVariable(value = "id") UUID idVendedor, @PathVariable(value = "idVenta") UUID idVenta,
                                                   @PathVariable(value = "idReclamo") UUID idReclamo, @RequestParam(value = "accion") TipoResolucion accion) throws FirebaseMessagingException, FirebaseAuthException {
        reclamoService.gestionReclamo(idVenta, idReclamo, idVendedor, accion);
        return new ResponseEntity<>("Accion del reclamo realizada con exito!!!", HttpStatus.OK);
    }

    @GetMapping("/{id}/ventas/reclamos")
    public Map<String, Object> obtenerReclamos(
            @RequestParam(value = "pageNo", defaultValue = "0", required = false) int pageNo,
            @RequestParam(value = "pageSize", defaultValue = "20", required = false) int pageSize,
            @RequestParam(value = "sortBy", defaultValue = "fecha", required = false) String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "asc", required = false) String sortDir,
            @RequestParam(value = "tipo", required = false) TipoReclamo tipo,
            @RequestParam(value = "resolucion", required = false) TipoResolucion resolucion,
            @RequestParam(value = "fecha", required = false) @DateTimeFormat(pattern = "dd/MM/yyyy") Date fecha,
            @RequestParam(value = "nombreProducto", required = false) String nombreProducto,
            @RequestParam(value = "nombreUsuario", required = false) String nombreUsuario,
            @PathVariable(value = "id") UUID id) {
        DtFiltroReclamo filtros = null;
        if (tipo != null || resolucion != null || fecha != null || nombreProducto != null || nombreUsuario != null)
            filtros = new DtFiltroReclamo(fecha, nombreProducto, nombreUsuario, tipo, resolucion);
        return reclamoService.listarMisReclamosRecibidos(pageNo, pageSize, sortBy, sortDir, filtros, id);
    }

    @PutMapping("/{id}/productos/{idProducto}")
    public ResponseEntity<String> modificarProducto(@PathVariable(value = "id") UUID idVendedor,
                                                    @PathVariable(value = "idProducto") UUID idProducto,
                                                    @RequestPart DtModificarProducto datos,
                                                    @RequestPart MultipartFile[] imagenes) throws IOException {
        productoService.editarProducto(idProducto, idVendedor, datos, imagenes);
        return new ResponseEntity<>("Producto modificado con éxito!!!", HttpStatus.OK);
    }

    @GetMapping("/{id}/estadisticas/{opcion}")
    public Map<String, Object> estadisticasVendedor(@PathVariable(value = "id") UUID idVendedor,
                                                    @PathVariable(value = "opcion") EstVendedor opcion,
                                                    @RequestParam(value = "fechaInicio", required = false) @DateTimeFormat(pattern = "dd/MM/yyyy") Date fechaInicio,
                                                    @RequestParam(value = "fechaFin", required = false) @DateTimeFormat(pattern = "dd/MM/yyyy") Date fechaFin) {
        Map<String, Object> response = new LinkedHashMap<>();
        boolean esHistorico = fechaInicio == null && fechaFin == null;

        if (opcion == EstVendedor.Todas || opcion == EstVendedor.Balance)
            response.put("balance", vendedorService.balanceVendedor(idVendedor, fechaInicio, fechaFin, esHistorico));

        if (opcion == EstVendedor.Todas || opcion == EstVendedor.Top10ProdVendidos)
            response.put("top10", vendedorService.topTeenProductosVendidos(idVendedor, fechaInicio, fechaFin, esHistorico));

        if (opcion == EstVendedor.Todas || opcion == EstVendedor.Top10ProdCalificados)
            response.put("mejoresCalificados", vendedorService.productosMejoresCalificados(idVendedor, fechaInicio, fechaFin, esHistorico));
        return response;
    }
}
