package com.shopnow.shopnow.controller;


import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.shopnow.shopnow.model.datatypes.DtConfirmarCompra;
import com.shopnow.shopnow.model.datatypes.DtFiltosMisProductos;
import com.shopnow.shopnow.model.datatypes.DtFiltroReclamo;
import com.shopnow.shopnow.model.datatypes.DtFiltrosVentas;
import com.shopnow.shopnow.model.enumerados.EstadoCompra;
import com.shopnow.shopnow.model.enumerados.EstadoProducto;
import com.shopnow.shopnow.model.enumerados.TipoReclamo;
import com.shopnow.shopnow.model.enumerados.TipoResolucion;
import com.shopnow.shopnow.service.CompraService;
import com.shopnow.shopnow.service.ProductoService;
import com.shopnow.shopnow.service.ReclamoService;
import com.shopnow.shopnow.service.VendedorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
        DtFiltosMisProductos filtros;
        if (categorias == null && estado == null && fecha == null && nombre == null) {
            filtros = null;
        } else {
            filtros = new DtFiltosMisProductos(fecha, nombre, categorias, estado);
        }
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
        DtFiltrosVentas filtros;
        if (estado == null && fecha == null && nombre == null) {
            filtros = null;
        } else
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
            @RequestParam(value = "estado", required = false) TipoReclamo tipo,
            @RequestParam(value = "estado", required = false) TipoResolucion resolucion,
            @RequestParam(value = "fecha", required = false) @DateTimeFormat(pattern = "dd/MM/yyyy") Date fecha,
            @RequestParam(value = "nombre", required = false) String nombreProducto,
            @RequestParam(value = "nombre", required = false) String nombreUsuario,
            @PathVariable(value = "id") UUID id) {
        DtFiltroReclamo filtros;
        if (tipo == null && resolucion == null && fecha == null && nombreProducto == null && nombreUsuario == null)
            filtros = null;
        else
            filtros = new DtFiltroReclamo(fecha, nombreProducto, nombreUsuario, tipo, resolucion);
        return reclamoService.listarMisReclamosRecibidos(pageNo, pageSize, sortBy, sortDir, filtros, id);
    }
}
