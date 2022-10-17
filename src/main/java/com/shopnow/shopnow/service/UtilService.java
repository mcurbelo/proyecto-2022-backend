package com.shopnow.shopnow.service;

import com.shopnow.shopnow.model.Compra;
import com.shopnow.shopnow.model.Generico;
import com.shopnow.shopnow.model.Producto;
import com.shopnow.shopnow.model.enumerados.EstadoCompra;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
public class UtilService {


    @SafeVarargs
    public static <T, C extends Collection<T>> C encontrarInterseccion(C newCollection, Collection<T>... collections) {
        boolean first = true;
        for (Collection<T> collection : collections) {
            if (collection == null)
                continue;
            if (collection.isEmpty()) {
                newCollection.clear();
                return newCollection;
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
                "Cantidad: " + compra.getInfoEntrega().getCantidad() + "." +
                "Precio unitario: " + compra.getInfoEntrega().getPrecioUnitario() + ".\n" +
                "Precio total: " + compra.getInfoEntrega().getCantidad() + ".\n" +
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
}
