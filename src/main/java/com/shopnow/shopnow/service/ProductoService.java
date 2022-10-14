package com.shopnow.shopnow.service;

import com.shopnow.shopnow.controller.responsetypes.Excepcion;
import com.shopnow.shopnow.model.*;
import com.shopnow.shopnow.model.datatypes.DtAltaProducto;
import com.shopnow.shopnow.model.datatypes.DtProducto;
import com.shopnow.shopnow.model.enumerados.EstadoProducto;
import com.shopnow.shopnow.model.enumerados.EstadoSolicitud;
import com.shopnow.shopnow.model.enumerados.EstadoUsuario;
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
        //TODO Para testear
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

        if (!datosProducto.getEsSolicitud() && usuario.getDatosVendedor() == null) {
            throw new Excepcion("Funcionalidad no disponible para este usuario.");
        }

        if (!datosProducto.getEsSolicitud() && usuario.getDatosVendedor().getEstadoSolicitud() != EstadoSolicitud.Aceptado)
            throw new Excepcion("El usuario con el correo ingresado no esta habilitado para agregar productos.");

        datosProducto.getCategorias().forEach(categoria -> {
            if (!categoriaRepository.existsById((categoria)))
                throw new Excepcion("Una o mas categorias no son validas");
        });

        List<URLimagen> linkImagenes = new ArrayList<>();
        String idImagen = UUID.randomUUID().toString();
        int i = 0;
        for (MultipartFile imagen : imagenes) {
            linkImagenes.add(new URLimagen(firebaseStorageService.uploadFile(imagen, idImagen + "--img" + i)));
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

    public DtProducto obtenerProducto(UUID id) {
        Optional<Producto> resultado = productoRepository.findById(id);
        Producto producto;
        if (resultado.isEmpty()) {
            throw new Excepcion("El producto no existe");
        } else {
            producto = resultado.get();
        }

        UUID idVendedor = productoRepository.vendedorProducto(id);
        Optional<Usuario> res = usuarioRepository.findById(idVendedor);
        Generico usuario;
        if (res.isEmpty()) {
            throw new Excepcion("El usuario no existe");
        } else {
            usuario = (Generico) res.get();
        }
        if (producto.getEstado() != EstadoProducto.Activo || usuario.getEstado() != EstadoUsuario.Activo) { //Verifico que el producto se pueda mostrar y ese usuario este activo
            throw new Excepcion("Este producto no se puede visualizar en este momento");
        }

        List<String> linksImagenes = new ArrayList<>();
        for (URLimagen url : producto.getImagenesURL()) { //Obtengo links de imagenes del producto
            linksImagenes.add(url.getUrl());
        }
        String nombreVendedor;
        DatosVendedor datosVendedor = usuario.getDatosVendedor(); //Veo que nombre mandar del vendedor
        if (datosVendedor.getNombreEmpresa() == null) {
            nombreVendedor = usuario.getNombre() + " " + usuario.getApellido();
        } else {
            nombreVendedor = datosVendedor.getNombreEmpresa();
        }
        Map<UUID, Compra> ventas = usuario.getVentas();  //Calculo la calificacion :)
        float sumaCalificacion = 0, calificacion = 0;
        if (ventas.size() != 0) {
            for (Compra venta : ventas.values()) {
                sumaCalificacion += venta.getInfoEntrega().getCalificacion().getPuntuacion();
            }
            calificacion = sumaCalificacion / ventas.size();
        }
        return new DtProducto(id, idVendedor, linksImagenes, producto.getNombre(), producto.getDescripcion(), producto.getPrecio(), producto.getPermiteEnvio(), producto.getComentarios(), nombreVendedor, calificacion, usuario.getImagen(), datosVendedor.getLocales());
    }

}

