package com.shopnow.shopnow.service;

import com.shopnow.shopnow.controller.responsetypes.Excepcion;
import com.shopnow.shopnow.model.Generico;
import com.shopnow.shopnow.model.Producto;
import com.shopnow.shopnow.model.Usuario;
import com.shopnow.shopnow.model.enumerados.EstadoProducto;
import com.shopnow.shopnow.model.enumerados.EstadoUsuario;
import com.shopnow.shopnow.repository.DatosVendedorRepository;
import com.shopnow.shopnow.repository.DireccionRepository;
import com.shopnow.shopnow.repository.ProductoRepository;
import com.shopnow.shopnow.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    public void cambiarEstadoProducto(UUID idProducto, String correo, EstadoProducto nuevoEstado) {
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
        Optional<Usuario> res = usuarioRepository.findByCorreoAndEstado(correo, EstadoUsuario.Activo);
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
}
