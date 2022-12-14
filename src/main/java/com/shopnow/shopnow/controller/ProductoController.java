package com.shopnow.shopnow.controller;


import com.shopnow.shopnow.controller.responsetypes.Excepcion;
import com.shopnow.shopnow.model.datatypes.DtAltaProducto;
import com.shopnow.shopnow.model.datatypes.DtFiltros;
import com.shopnow.shopnow.model.datatypes.DtProducto;
import com.shopnow.shopnow.service.ProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    @Autowired
    ProductoService productoService;

    @PostMapping()
    public ResponseEntity<String> nuevoProducto(@Valid @RequestPart DtAltaProducto datos, @RequestPart final MultipartFile[] imagenes) throws IOException {
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (imagenes.length == 0 || imagenes.length > 5) {
            throw new Excepcion("Cantidad de imagenes incorrecta");
        }
        productoService.agregarProducto(datos, imagenes, email, false);
        return new ResponseEntity<>("Producto agregado con exito!!!", HttpStatus.OK);
    }

    @GetMapping()
    public Map<String, Object> busquedaDeProductos(
            @RequestParam(value = "pageNo", defaultValue = "0", required = false) int pageNo,
            @RequestParam(value = "pageSize", defaultValue = "20", required = false) int pageSize,
            @RequestParam(value = "sortBy", defaultValue = "nombre", required = false) String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "asc", required = false) String sortDir,
            @RequestParam(value = "categorias", required = false) List<String> categorias,
            @RequestParam(value = "infoEventoActivo", required = false) Boolean infoEventoActivo,
            @RequestParam(value = "nombre", required = false) String nombre,
            @RequestParam(value = "idEventoPromocional", required = false) UUID idEventoPromocional
    ) {
        DtFiltros filtros = null;
        if (categorias != null || infoEventoActivo != null || nombre != null || idEventoPromocional != null)
            filtros = new DtFiltros(nombre, categorias, idEventoPromocional, infoEventoActivo);
        return productoService.busquedaDeProductos(pageNo, pageSize, sortBy, sortDir, filtros);
    }

    @GetMapping("/{id}")
    public DtProducto informacionProducto(@PathVariable(value = "id") UUID id) {
        return productoService.obtenerProducto(id);
    }
}
