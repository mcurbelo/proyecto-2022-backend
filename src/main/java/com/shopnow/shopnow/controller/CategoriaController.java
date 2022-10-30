package com.shopnow.shopnow.controller;

import com.shopnow.shopnow.model.datatypes.DtCategoria;
import com.shopnow.shopnow.service.CategoriaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {

    @Autowired
    CategoriaService categoriaService;

    @PostMapping()
    public ResponseEntity<String> nuevaCategoria(@Valid @RequestBody DtCategoria categoria) {
        categoriaService.agregarCategoria(categoria.getNombre());
        return new ResponseEntity<>("Categoria agregada con exito!!!", HttpStatus.OK);
    }

    @GetMapping()
    public List<DtCategoria> listarCategorias() {
        return categoriaService.listaCategorias().stream().map(DtCategoria::new).collect(Collectors.toList());
    }

    @PostMapping("/cargar")
    public void carga() {
        categoriaService.agregarCategoria("Tecnologia");
        categoriaService.agregarCategoria("Cocina");
        categoriaService.agregarCategoria("Inmuebles");
        categoriaService.agregarCategoria("Deportes");
    }

}
