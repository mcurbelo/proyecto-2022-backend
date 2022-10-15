package com.shopnow.shopnow.controller;


import com.shopnow.shopnow.model.datatypes.DtFiltros;
import com.shopnow.shopnow.model.datatypes.DtModificarProducto;
import com.shopnow.shopnow.service.VendedorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/vendedores")
public class VendedorController {

    @Autowired
    VendedorService vendedorService;


    @PutMapping("/{idUsuario}/productos/{id}/estado")
    public ResponseEntity<String> cambiarEstado(@PathVariable(value = "idUsuario") UUID id, @PathVariable(value = "id") UUID idProducto, @RequestBody DtModificarProducto datos) {
        //Ese Dt se deberia utilizar tambien para editar producto

        /*TODO Utilizar cuando se utilicen al 100% los token
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!email.equals(correo)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        */
        if (datos.getNuevoEstadoProducto() == null) { // Cuando se usen tokens se suma al if de arriba
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        vendedorService.cambiarEstadoProducto(idProducto, id, datos.getNuevoEstadoProducto());
        return new ResponseEntity<>("Producto cambiado de estado con exito", HttpStatus.OK);
    }

    @GetMapping("/{id}/ventas")
    public Map<String, Object> busquedaDeProductos(
            @RequestParam(value = "pageNo", defaultValue = "0", required = false) int pageNo,
            @RequestParam(value = "pageSize", defaultValue = "20", required = false) int pageSize,
            @RequestParam(value = "sortBy", defaultValue = "nombre", required = false) String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "asc", required = false) String sortDir,
            @RequestBody DtFiltros filtros) {
        return vendedorService.historialVentas(pageNo, pageSize, sortBy, sortDir, DtFiltrosVentas);
    }
}
