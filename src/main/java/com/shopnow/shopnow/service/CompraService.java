package com.shopnow.shopnow.service;

import com.shopnow.shopnow.controller.responsetypes.Excepcion;
import com.shopnow.shopnow.model.*;
import com.shopnow.shopnow.model.datatypes.DtCompra;
import com.shopnow.shopnow.model.enumerados.EstadoProducto;
import com.shopnow.shopnow.model.enumerados.EstadoSolicitud;
import com.shopnow.shopnow.model.enumerados.EstadoUsuario;
import com.shopnow.shopnow.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

@Service
public class CompraService {

    @Autowired
    CompraRepository compraRepository;

    @Autowired
    UsuarioRepository usuarioRepository;

    @Autowired
    ProductoRepository productoRepository;

    @Autowired
    TarjetaRepository tarjetaRepository;

    @Autowired
    CuponRepository cuponRepository;

    @Autowired
    DireccionRepository direccionRepository;

    public void nuevaCompra(DtCompra datosCompra) {
        if (datosCompra.getIdComprador().equals(datosCompra.getIdVendedor())) {
            throw new Excepcion("No se puede comprar un producto a usted mismo");
        }

        Optional<Usuario> resComprador = usuarioRepository.findByIdAndEstado(datosCompra.getIdComprador(), EstadoUsuario.Activo);
        if (resComprador.isEmpty()) {
            throw new Excepcion("El usuario comprador no esta habilitado para realizar esta compra");
        }

        Optional<Usuario> resVendedor = usuarioRepository.findByIdAndEstado(datosCompra.getIdVendedor(), EstadoUsuario.Activo);
        if (resVendedor.isEmpty()) {
            throw new Excepcion("El usuario vendedor no esta habilitado para realizar esta compra");
        }

        if (resVendedor.get() instanceof Administrador || resComprador.get() instanceof Administrador) {
            throw new Excepcion("Usuario invalidos para esta funcionalidad");
        }

        Optional<Producto> resProducto = productoRepository.findByIdAndEstado(datosCompra.getIdProducto(), EstadoProducto.Activo);
        if (resProducto.isEmpty()) {
            throw new Excepcion("El producto no esta disponible en estos momentos");
        }

        if (resProducto.get().getStock() == 0 || resProducto.get().getStock() < datosCompra.getCantidad()) {
            throw new Excepcion("El producto no tiene stock suficiente");
        }

        Optional<Tarjeta> resTarjeta = tarjetaRepository.findById(datosCompra.getIdTarjeta());
        if (resTarjeta.isEmpty()) {
            throw new Excepcion("Tarjeta no registrada en el sistema");
        }

        Generico vendedor = (Generico) resVendedor.get();
        if (vendedor.getDatosVendedor().getEstadoSolicitud() != EstadoSolicitud.Aceptado) { //No deberia pasar pero bueno
            throw new Excepcion("Usuario vendedor invalido");
        }
        if (!vendedor.getProductos().containsKey(datosCompra.getIdProducto())) {
            throw new Excepcion("El producto ingresado no pertenece al vendedor");
        }
        Cupon cupon = null;
        if (datosCompra.getCodigoCanje() != null) {
            Optional<Cupon> resCupon = cuponRepository.findByCodigoCanje(datosCompra.getCodigoCanje());
            if (resCupon.isEmpty()) {
                throw new Excepcion("El cupon ingresado no es valido");
            } else
                cupon = resCupon.get();
        }
        //TODO Crear eventoPromocional enviar descuento, Verificar que sea envio, Validar direccion pertenezcan

        Producto producto = resProducto.get();
        Generico comprador = (Generico) resComprador.get();
        Tarjeta tarjeta = resTarjeta.get();

        Integer idDireccion;
        if(datosCompra.getEsParaEnvio()) { //
            if () {
                throw new Excepcion("Direccion invalida, no pertenece al vendedor");
            } else
                idDireccion = datosCompra.getIdDireccionLocal();

            if (datosCompra.getEsParaEnvio() && !comprador.getDireccionesEnvio().containsKey(datosCompra.getIdDireccionEnvio())) {
                throw new Excepcion("Direccion invalida, no pertenece al comprador");
            } else
                idDireccion = datosCompra.getIdDireccionEnvio();

        }

        float precio = producto.getPrecio();

        //TODO Revisar si hay evento promocional activo y descontar

        if (cupon != null) {
            precio = (float) (precio - (precio * (cupon.getDescuento() / 100.00))); //Esta dudoso esto
        }

        CompraProducto infoEntrga = new CompraProducto(null, null, null, (datosCompra.getEsParaEnvio()) ? datosCompra.getIdDireccionEnvio() : datosCompra.getIdDireccionLocal(), precio, datosCompra.getCantidad(), precio * datosCompra.getCantidad(), producto, null);


        Compra compra = Compra.builder()
                .id(null)
                .fecha(new Date())
                .cuponAplicado(cupon)
                .tarjetaPago()
                .infoEntrega(null)
                .build();


    }
}
