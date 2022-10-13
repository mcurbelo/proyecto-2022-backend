package com.shopnow.shopnow.service;

import com.shopnow.shopnow.controller.responsetypes.Excepcion;
import com.shopnow.shopnow.model.Categoria;
import com.shopnow.shopnow.model.Generico;
import com.shopnow.shopnow.model.Producto;
import com.shopnow.shopnow.model.Usuario;
import com.shopnow.shopnow.model.datatypes.DtAltaProducto;
import com.shopnow.shopnow.model.enumerados.EstadoProducto;
import com.shopnow.shopnow.repository.CategoriaRepository;
import com.shopnow.shopnow.repository.ProductoRepository;
import com.shopnow.shopnow.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@Service
public class ProductoService {

    @Autowired
    UsuarioRepository usuarioRepository;

    @Autowired
    CategoriaRepository categoriaRepository;

    @Autowired
    FirebaseStorageService firebaseStorageService;

    @Autowired
    ProductoRepository productoRepository;

    public void agregarProducto(DtAltaProducto datosProducto, MultipartFile[] imagenes) throws Excepcion, IOException {
        //Para testear
        categoriaRepository.save(Categoria.builder().nombre("Tecnologia").build());

        if (datosProducto.getFechaFin() != null && datosProducto.getFechaFin().before(new Date())) {
            throw new Excepcion("La fecha de fin es invalida");
        }
        Optional<Usuario> resultado = usuarioRepository.findByCorreo(datosProducto.getEmailVendedor());
        Generico usuario;
        if (resultado.isEmpty()) {
            throw new Excepcion("El usuario no existe");
        } else {
            usuario = (Generico) resultado.get();
        }

     //   if (!datosProducto.getEsSolicitud() && usuario.getDatosVendedor().getEstadoSolicitud() != EstadoSolicitud.Aceptado)
       //     throw new Excepcion("El usuario con el correo ingresado no esta habilitado para agregar mas productos.");

        datosProducto.getCategorias().forEach(categoria -> {
            if(!categoriaRepository.existsById((categoria)))
                throw new Excepcion("Una o mas categorias no son validas");
        });

        List<String> linkImagenes = new ArrayList<>();
        String idImagen = UUID.randomUUID().toString();
        int i = 0;
        for (MultipartFile imagen : imagenes) {
            linkImagenes.add(firebaseStorageService.uploadFile(imagen, idImagen + "--img" + i));
            i++;
        }
        Producto producto = Producto.builder()
                                    .nombre(datosProducto.getNombreProducto())
                                    .stock(datosProducto.getStock())
                                    .imagenesURL(linkImagenes)
                                    .descripcion(datosProducto.getDescripcion())
                                    .fechaInicio(new Date())
                                    .fechaFin(datosProducto.getFechaFin())
                                    .estado((datosProducto.getEsSolicitud()) ? EstadoProducto.Pausado : EstadoProducto.Activo)
                                    .precio(datosProducto.getPrecio())
                                    .diasGarantia(datosProducto.getDiasGarantia())
                                    .permiteEnvio(datosProducto.getPermiteEnvio())
                                    .build();
        productoRepository.saveAndFlush(producto);
        List<Categoria> categorias = categoriaRepository.findAllById(datosProducto.getCategorias());
        for (Categoria categoria : categorias) {
            categoria.getProductos().put(producto.getId(), producto);
        }
        categoriaRepository.saveAll(categorias);
        usuario.getProductos().put(producto.getId(), producto);
        usuarioRepository.save(usuario);
    }
}
