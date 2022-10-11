package com.shopnow.shopnow.controller;


import com.shopnow.shopnow.controller.responsetypes.Excepcion;
import com.shopnow.shopnow.model.datatypes.DtProducto;
import com.shopnow.shopnow.service.ProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    @Autowired
    ProductoService productoService;

    @PostMapping()
    public ResponseEntity<String> nuevoProducto(@Valid @RequestPart DtProducto datos, @RequestPart final MultipartFile[] imagenes) throws IOException {
        if (imagenes.length==0 || imagenes.length>5){
                throw new Excepcion("Cantidad de imagenes incorrecta");
            }
            productoService.agregarProducto(datos, imagenes);
            return new ResponseEntity<>("Producto agregado con exito!!!", HttpStatus.OK);
    }
}
