package com.shopnow.shopnow.service;

import com.shopnow.shopnow.controller.responsetypes.Excepcion;
import com.shopnow.shopnow.model.Categoria;
import com.shopnow.shopnow.repository.CategoriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CategoriaService {

    @Autowired
    CategoriaRepository categoriaRepository;

    public void agregarCategoria(String nombre) {
        if (categoriaRepository.existsById(nombre)) {
            throw new Excepcion("El nombre de esa categoria ya existe");
        }
        categoriaRepository.save(Categoria.builder().nombre(nombre).build());
    }


    public List<String> listaCategorias() {
        List<Categoria> categorias = categoriaRepository.findAll();
        List<String> categoriasRet = new ArrayList<>();
        for (Categoria categoria : categorias) {
            categoriasRet.add(categoria.getNombre());
        }
        return categoriasRet;
    }
}
