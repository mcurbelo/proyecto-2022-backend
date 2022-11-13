package com.shopnow.shopnow.service;

import com.shopnow.shopnow.model.Compra;
import com.shopnow.shopnow.model.Generico;
import com.shopnow.shopnow.model.Producto;
import com.shopnow.shopnow.model.URLimagen;
import com.shopnow.shopnow.model.datatypes.DtMiProducto;
import com.shopnow.shopnow.model.enumerados.EstadoCompra;
import com.shopnow.shopnow.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
public class UtilService {

    @Autowired
    ProductoRepository productoRepository;

    @SafeVarargs
    public static <T, C extends Collection<T>> C encontrarInterseccion(C newCollection, Collection<T>... collections) {
        boolean first = true;
        for (Collection<T> collection : collections) {
            if (collection == null)
                continue;
            if (collection.isEmpty()) {
                newCollection.clear();
                break;
            }
            if (first) {
                newCollection.addAll(collection);
                first = false;
            } else
                newCollection.retainAll(collection);
        }
        return newCollection;

    }

    public String detallesCompra(Compra compra, Generico vendedor, Generico comprador, Producto producto, Boolean porEnvio) {
        return "\n" +
                "Identificador: " + compra.getId() + ".\n" +
                "Producto: " + producto.getNombre() + ".\n" +
                "Vendedor: " + vendedor.getNombre() + " " + vendedor.getApellido() + ".\n" +
                "Comprador: " + comprador.getNombre() + " " + comprador.getApellido() + ".\n" +
                "Cantidad: " + compra.getInfoEntrega().getCantidad() + ".\n" +
                "Precio unitario: $" + compra.getInfoEntrega().getPrecioUnitario() + ".\n" +
                "Precio total: $" + compra.getInfoEntrega().getCantidad() + ".\n" +
                "Por envio: " + ((porEnvio) ? "Sí" : "No") + ".\n" +
                ((porEnvio) ? "Direccion envio: " : "Direccion retiro: ") + "" + compra.getInfoEntrega().getDireccionEnvioORetiro().toString() + "\n" +
                "Estado actual: " + parseEstado(compra.getEstado()) + ".";
    }

    private String parseEstado(EstadoCompra estadoCompra) {
        if (estadoCompra == EstadoCompra.EsperandoConfirmacion)
            return "Esperando confirmación";
        else
            return estadoCompra.toString();
    }

    DtMiProducto generarDtMiProductos(Producto producto) {
        List<String> urlImagenes = new ArrayList<>();
        for (URLimagen url : producto.getImagenesURL()) {
            urlImagenes.add(url.getUrl());
        }
        List<String> categorias = productoRepository.categoriasDelProducto(producto.getId());
        return new DtMiProducto(producto.getId(), producto.getNombre(), urlImagenes, producto.getFechaInicio(), producto.getFechaFin(), categorias, producto.getPrecio(), producto.getStock(), producto.getEstado(), producto.getDescripcion(), producto.getPermiteEnvio(), producto.getDiasGarantia());
    }
}
