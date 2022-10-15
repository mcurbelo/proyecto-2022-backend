package com.shopnow.shopnow.service;

import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.shopnow.shopnow.controller.responsetypes.Excepcion;
import com.shopnow.shopnow.model.*;
import com.shopnow.shopnow.model.datatypes.DtCompra;
import com.shopnow.shopnow.model.enumerados.EstadoProducto;
import com.shopnow.shopnow.model.enumerados.EstadoSolicitud;
import com.shopnow.shopnow.model.enumerados.EstadoUsuario;
import com.shopnow.shopnow.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
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

    @Autowired
    EventoPromocionalRepository eventoPromocionalRepository;

    @Autowired
    FirebaseMessagingService firebaseMessagingService;

    @Autowired
    GoogleSMTP googleSMTP;

    public void nuevaCompra(DtCompra datosCompra) throws FirebaseMessagingException, FirebaseAuthException {
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

        Producto producto = resProducto.get();
        Generico comprador = (Generico) resComprador.get();
        Tarjeta tarjeta = resTarjeta.get();


        Integer idDireccion;
        if (datosCompra.getEsParaEnvio()) { //
            if (!comprador.getDireccionesEnvio().containsKey(datosCompra.getIdDireccionEnvio()))
                throw new Excepcion("La direccion de envio no pertenece al comprador");
            else
                idDireccion = datosCompra.getIdDireccionEnvio();
        } else if (!vendedor.getDatosVendedor().getLocales().containsKey(datosCompra.getIdDireccionEnvio()))
            throw new Excepcion("La direccion de retiro no pertenece al vendedor");
        else
            idDireccion = datosCompra.getIdDireccionLocal();

        Optional<Direccion> resDire = direccionRepository.findById(idDireccion);
        if (resDire.isEmpty()) {
            throw new Excepcion("La direccion no existe");
        }
        Direccion direccion = resDire.get();

        //TODO Revisar si hay evento promocional activo
        //TODO Falso todo los eventos no tienen descuento, nos falto eso :(((


        float precio = producto.getPrecio();
        DecimalFormat df = new DecimalFormat("0.00");
        if (cupon != null) {
            precio = (float) (precio - (precio * (cupon.getDescuento() / 100.00))); //Esta dudoso esto
        }
        df.format(precio);

        //TODO PAGO TARJETA :DDDD

        CompraProducto infoEntrega = new CompraProducto(null, null, null, datosCompra.getEsParaEnvio(), direccion, precio, datosCompra.getCantidad(), precio * datosCompra.getCantidad(), producto, null);


        Compra compra = Compra.builder()
                .id(null)
                .fecha(new Date())
                .cuponAplicado(cupon)
                .tarjetaPago(tarjeta)
                .infoEntrega(infoEntrega)
                .build();
        compraRepository.saveAndFlush(compra);
        comprador.getCompras().put(compra.getId(), compra);
        vendedor.getVentas().put(compra.getId(), compra);
        usuarioRepository.save(comprador);
        usuarioRepository.save(vendedor);

        Note notificacionVendedor = new Note("Nueva venta registrada", "Se realizó una venta de uno de sus producto. Dirigase a 'Mis ventas' para realizar acciones.", null, null, null);

        firebaseMessagingService.enviarNotificacion(notificacionVendedor, vendedor.getWebToken());

        googleSMTP.enviarCorreo(vendedor.getCorreo(), "Hola, " + vendedor.getNombre() + " " + vendedor.getApellido() + ".\nSe realizó una venta de uno de sus producto. Dirigase a 'Mis ventas' para realizar acciones.\n Detalles de la venta: \n" + detallesCompra(compra, vendedor, comprador, producto, datosCompra.getEsParaEnvio()), "Nueva venta");
        googleSMTP.enviarCorreo(comprador.getCorreo(), "Hola, " + comprador.getNombre() + " " + comprador.getApellido() + ".\nRealizó una compra de un producto, la confirmacion puede demorar hasta 72hrs despues de haber recibido este correo.\n Detalles de la compra: \n" + detallesCompra(compra, vendedor, comprador, producto, datosCompra.getEsParaEnvio()), "Compra realizada");
    }

    private String detallesCompra(Compra compra, Generico vendedor, Generico comprador, Producto producto, Boolean porEnvio) {

        return "\n" +
                "Producto: " + producto.getNombre() + ".\n" +
                "Vendedor: " + vendedor.getNombre() + " " + vendedor.getApellido() + ".\n" +
                "Comprador: " + comprador.getNombre() + " " + comprador.getApellido() + ".\n" +
                "Cantidad: " + compra.getInfoEntrega().getCantidad() + "." +
                "Precio unitario: " + compra.getInfoEntrega().getPrecioUnitario() + ".\n" +
                "Precio total: " + compra.getInfoEntrega().getCantidad() + ".\n" +
                "Por envio: " + ((porEnvio) ? "Sí" : "No") + ".\n" +
                ((porEnvio) ? "Direccion envio: " : "Direccion retiro: ") + "" + compra.getInfoEntrega().getDireccionEnvioORetiro().toString() + "\n" +
                "Estado actual: Esperando confirmacion";
    }
}
