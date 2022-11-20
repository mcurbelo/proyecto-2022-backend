package com.shopnow.shopnow.service;

import com.braintreegateway.Result;
import com.braintreegateway.Transaction;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.shopnow.shopnow.controller.responsetypes.Excepcion;
import com.shopnow.shopnow.model.*;
import com.shopnow.shopnow.model.datatypes.DtChat;
import com.shopnow.shopnow.model.datatypes.DtCompra;
import com.shopnow.shopnow.model.datatypes.DtCompraDeshacer;
import com.shopnow.shopnow.model.datatypes.DtConfirmarCompra;
import com.shopnow.shopnow.model.enumerados.*;
import com.shopnow.shopnow.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.text.DecimalFormat;
import java.util.*;

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

    @Autowired
    UtilService utilService;

    @Autowired
    BraintreeUtils braintreeUtils;

    @Autowired
    ReclamoRepository reclamoRepository;

    public Map<String, String> nuevaCompra(DtCompra datosCompra, UUID idComprador) throws FirebaseMessagingException, FirebaseAuthException {
        //Validaciones RNE

        if (idComprador.compareTo(datosCompra.getIdVendedor()) == 0) {
            throw new Excepcion("No se puede comprar un producto a usted mismo");
        }
        Optional<Usuario> resComprador = usuarioRepository.findByIdAndEstado(idComprador, EstadoUsuario.Activo);
        if (resComprador.isEmpty()) {
            throw new Excepcion("El usuario comprador no esta habilitado para realizar esta compra");
        }

        Optional<Usuario> resVendedor = usuarioRepository.findByIdAndEstado(datosCompra.getIdVendedor(), EstadoUsuario.Activo);
        if (resVendedor.isEmpty()) {
            throw new Excepcion("El usuario vendedor no esta habilitado para realizar esta compra");
        }

        Optional<Producto> resProducto = productoRepository.findByIdAndEstado(datosCompra.getIdProducto(), EstadoProducto.Activo);
        if (resProducto.isEmpty()) {
            throw new Excepcion("El producto no esta disponible en estos momentos");
        }

        if (resVendedor.get() instanceof Administrador || resComprador.get() instanceof Administrador) {
            throw new Excepcion("Usuario invalidos para esta funcionalidad");
        }

        if (datosCompra.getIdDireccionLocal() == null && datosCompra.getIdDireccionEnvio() == null) {
            throw new Excepcion("Debe haber almenos una direccion");
        }

        Generico vendedor = (Generico) resVendedor.get();

        //Validaciones vendedor

        if (vendedor.getDatosVendedor().getEstadoSolicitud() != EstadoSolicitud.Aceptado) { //No deberia pasar pero bueno
            throw new Excepcion("Usuario vendedor invalido");
        }
        if (!vendedor.getProductos().containsKey(datosCompra.getIdProducto())) {
            throw new Excepcion("El producto ingresado no pertenece al vendedor");
        }

        Producto producto = resProducto.get();

        //Validaciones producto

        if (producto.getStock() == 0 || producto.getStock() < datosCompra.getCantidad()) {
            throw new Excepcion("El producto no tiene stock suficiente");
        }

        //Valiaciones comprador

        Generico comprador = (Generico) resComprador.get();

        Optional<Tarjeta> resTarjeta = tarjetaRepository.findById(datosCompra.getIdTarjeta());
        if (resTarjeta.isEmpty()) {
            throw new Excepcion("Tarjeta no registrada en el sistema");
        }

        if (!comprador.getTarjetas().containsKey(datosCompra.getIdTarjeta())) {
            throw new Excepcion("La tarjeta debe pertenecer al usuario comprador");
        }
        Tarjeta tarjeta = resTarjeta.get();


        //Validacion cupon

        Cupon cupon = null;
        if (datosCompra.getCodigoCanje() != null) {
            Optional<Cupon> resCupon = cuponRepository.findByCodigoCanje(datosCompra.getCodigoCanje());
            if (resCupon.isEmpty()) {
                throw new Excepcion("El cupon ingresado no es valido");
            } else
                cupon = resCupon.get();
        }

        //Validacion de entrega y retiro direcciones

        Integer idDireccion;
        if (datosCompra.getEsParaEnvio()) { //
            if (!comprador.getDireccionesEnvio().containsKey(datosCompra.getIdDireccionEnvio()))
                throw new Excepcion("La direccion de envio no pertenece al comprador");
            else
                idDireccion = datosCompra.getIdDireccionEnvio();
        } else {
            if (!vendedor.getDatosVendedor().getLocales().containsKey(datosCompra.getIdDireccionLocal()))
                throw new Excepcion("La direccion de retiro no pertenece al vendedor");
            else
                idDireccion = datosCompra.getIdDireccionLocal();
        }
        Optional<Direccion> resDire = direccionRepository.findById(idDireccion);
        if (resDire.isEmpty()) {
            throw new Excepcion("La direccion no existe");
        }
        Direccion direccion = resDire.get();

        //TODO Revisar si hay evento promocional activo
        //TODO Falso todo los eventos no tienen descuento, nos falto eso :(((


        //Logica del CU

        float precio = producto.getPrecio();
        DecimalFormat df = new DecimalFormat("0.00");
        if (cupon != null) {
            precio = (float) (precio - (precio * (cupon.getDescuento() / 100.00))); //Esta dudoso esto
        }
        df.format(precio);

        //PAGO TARJETA
        String transaccionId;
        Map<String, String> respuesta = new LinkedHashMap<>();
        Result<Transaction> resultado = braintreeUtils.hacerPago(comprador.getBraintreeCustomerId(), tarjeta.getToken(), String.valueOf(precio));
        if (resultado.isSuccess()) {
            transaccionId = resultado.getTarget().getId();
        } else if (resultado.getTransaction() != null) {
            Transaction transaction = resultado.getTransaction();
            respuesta.put("Failed!", transaction.getId());
            respuesta.put("Status", transaction.getStatus().toString());
            respuesta.put("Code", transaction.getProcessorResponseCode());
            respuesta.put("Text", transaction.getProcessorResponseText());
            return respuesta;
        } else
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, resultado.getMessage());

        CompraProducto infoEntrega = new CompraProducto(null, null, null, datosCompra.getEsParaEnvio(), direccion, precio, datosCompra.getCantidad(), precio * datosCompra.getCantidad(), producto, new ArrayList<>());

        Compra compra = Compra.builder()
                .id(null)
                .fecha(new Date())
                .cuponAplicado(cupon)
                .tarjetaPago(tarjeta)
                .infoEntrega(infoEntrega)
                .idTransaccion(transaccionId)
                .build();
        compraRepository.saveAndFlush(compra);
        comprador.getCompras().put(compra.getId(), compra);
        vendedor.getVentas().put(compra.getId(), compra);
        usuarioRepository.save(comprador);
        usuarioRepository.save(vendedor);
        producto.setStock(producto.getStock() - datosCompra.getCantidad());
        productoRepository.save(producto);


        if (!vendedor.getWebToken().equals("")) {
            Note notificacionVendedor = new Note("Nueva venta registrada", "Se realizó una venta de uno de sus producto. Dirigase a 'Mis ventas' para realizar acciones.", new HashMap<>(), null);
            firebaseMessagingService.enviarNotificacion(notificacionVendedor, vendedor.getWebToken());
        }
        googleSMTP.enviarCorreo(vendedor.getCorreo(), "Hola, " + vendedor.getNombre() + " " + vendedor.getApellido() + ".\nSe realizó una venta de uno de sus producto. Dirigase a 'Mis ventas' para realizar acciones.\nDetalles de la venta: \n" + utilService.detallesCompra(compra, vendedor, comprador, producto, datosCompra.getEsParaEnvio()), "Nueva venta");
        googleSMTP.enviarCorreo(comprador.getCorreo(), "Hola, " + comprador.getNombre() + " " + comprador.getApellido() + ".\nRealizó una compra de un producto, la confirmacion puede demorar hasta 72hrs despues de haber recibido este correo.\nDetalles de la compra: \n" + utilService.detallesCompra(compra, vendedor, comprador, producto, datosCompra.getEsParaEnvio()), "Compra realizada");

        respuesta.put("success", "200");
        return respuesta;
    }

    public void cambiarEstadoVenta(UUID idVendedor, UUID idVenta, EstadoCompra nuevoEstado, DtConfirmarCompra datosEntregaRetiro) throws FirebaseMessagingException, FirebaseAuthException {
        Optional<Usuario> resUsu = usuarioRepository.findByIdAndEstado(idVendedor, EstadoUsuario.Activo);
        if (resUsu.isEmpty()) {
            throw new Excepcion("El usuario no esta habilitado");
        }
        Optional<Compra> resCompra = compraRepository.findById(idVenta);
        if (resCompra.isEmpty()) {
            throw new Excepcion("La venta no existe");
        }
        Generico vendedor = (Generico) resUsu.get();
        Compra venta = resCompra.get();

        if (!vendedor.getVentas().containsKey(idVenta)) {
            throw new Excepcion("Esta venta no pertenece a este vendedor");
        }

        if (venta.getInfoEntrega().getEsEnvio() && nuevoEstado == EstadoCompra.Completada) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Esta funcionalidad no es para completar ventas de tipo envio");
        }
        if (venta.getEstado() == nuevoEstado) {
            throw new Excepcion("La venta ya se encuentra en ese estado");
        }

        if (nuevoEstado == EstadoCompra.EsperandoConfirmacion) {
            throw new Excepcion("No se puede volver a ese estado");
        }

        if (venta.getEstado() == EstadoCompra.Completada) {
            throw new Excepcion("No se puede cambiar el estado de una venta completada");
        }

        if (venta.getEstado() == EstadoCompra.Confirmada && nuevoEstado != EstadoCompra.Completada || venta.getEstado() == EstadoCompra.EsperandoConfirmacion && nuevoEstado == EstadoCompra.Completada || venta.getEstado() == EstadoCompra.Cancelada) {
            throw new Excepcion("No se puede modificar el estado de esta venta");
        }

        if (nuevoEstado == EstadoCompra.Confirmada && venta.getInfoEntrega().getEsEnvio() && datosEntregaRetiro.getFechayHoraEntrega() == null) {
            throw new Excepcion("Información insuficiente para completar el envío");
        }

        if (nuevoEstado == EstadoCompra.Confirmada && !venta.getInfoEntrega().getEsEnvio() && datosEntregaRetiro.getFechayHoraRetiro() == null) {
            throw new Excepcion("Información insuficiente para completar el retiro");
        }
        if (nuevoEstado == EstadoCompra.Cancelada && datosEntregaRetiro.getMotivo() == null) {
            throw new Excepcion("Información insuficiente para cancelar la compra");
        }

        if ((datosEntregaRetiro.getFechayHoraEntrega() != null && datosEntregaRetiro.getFechayHoraEntrega().before(new Date())) || (datosEntregaRetiro.getFechayHoraRetiro() != null && datosEntregaRetiro.getFechayHoraRetiro().before(new Date()))) {
            throw new Excepcion("Fecha invalida");
        }

        //Logica
        if (nuevoEstado == EstadoCompra.Confirmada) {
            if (venta.getInfoEntrega().getEsEnvio())  //Es un envio
                venta.getInfoEntrega().setTiempoEstimadoEnvio(datosEntregaRetiro.getFechayHoraEntrega());
            else
                venta.getInfoEntrega().setHorarioRetiroLocal(datosEntregaRetiro.getFechayHoraRetiro());
        }
        if (nuevoEstado == EstadoCompra.Cancelada) {
            boolean success = braintreeUtils.devolverDinero(venta.getIdTransaccion());
            if (!success) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "No se puede realizar la devolución del dinero");
            }
        }

        venta.setEstado(nuevoEstado);
        Generico comprador = compraRepository.obtenerComprador(idVenta);
        compraRepository.save(venta);
        String nombreParaMostrar;
        if (vendedor.getDatosVendedor().getNombreEmpresa().isBlank())
            nombreParaMostrar = vendedor.getDatosVendedor().getNombreEmpresa();
        else
            nombreParaMostrar = vendedor.getNombre() + " " + vendedor.getApellido();

        Note noteComprador;
        String mensaje, asunto;
        if (nuevoEstado == EstadoCompra.Confirmada) {
            noteComprador = new Note("Compra confirmada", "La compra hecha a " + nombreParaMostrar + " a sido confirmada!!! Ve hacia 'Historial de compras' para obtener más información de la entrega/retiro o iniciar chat con vendedor.", new HashMap<>(), null);
            mensaje = "La compra hecha a " + nombreParaMostrar + " a sido confirmada (Identificador: " + venta.getId() + ")!!! Ve hacia 'Historial de compras' en la pagina web o en tu dispositivo movil para obtener más información de la entrega/retiro.";
            asunto = "Estado de compra actualizado";
        } else if (nuevoEstado == EstadoCompra.Cancelada) {
            noteComprador = new Note("Compra cancelada", "La compra hecha a " + nombreParaMostrar + " a sido cancelada!!! Revisa tu correo para conocer el motivo." + vendedor.getCorreo() + "", new HashMap<>(), null);
            mensaje = "La compra hecha a " + nombreParaMostrar + " a sido cancelada (Identificador: " + venta.getId() + ")!!!\n Motivo:\n" + datosEntregaRetiro.getMotivo() + "\n Para mas información ponerse en contacto con el vendedor:\n Correo: " + vendedor.getCorreo() + ".";
            asunto = "Estado de compra actualizado";
        } else {
            noteComprador = new Note("Compra completada", "La compra hecha a " + nombreParaMostrar + " a sido completada!!! Ve hacia 'Historial de compras' para calificar al vendedor o realizar reclamos.", new HashMap<>(), null);
            mensaje = "La compra hecha a " + nombreParaMostrar + " a sido completada (Identificador: +" + venta.getId() + ")!!! Ve hacia 'Historial de compras' para calificar al vendedor o realizar reclamos.\n Detalles de la compra:\n" + utilService.detallesCompra(venta, vendedor, comprador, venta.getInfoEntrega().getProducto(), venta.getInfoEntrega().getEsEnvio()) + "";
            asunto = "Compra completada";
        }
        if (!comprador.getWebToken().equals(""))
            firebaseMessagingService.enviarNotificacion(noteComprador, comprador.getWebToken());
        if (!comprador.getMobileToken().equals(""))
            firebaseMessagingService.enviarNotificacion(noteComprador, comprador.getMobileToken());
        googleSMTP.enviarCorreo(comprador.getCorreo(), mensaje, asunto);
    }

    public void confirmarEntregaoReciboProducto(UUID idCompra) throws FirebaseMessagingException, FirebaseAuthException {
        Compra compra = compraRepository.findById(idCompra).orElseThrow();

        if (!compra.getInfoEntrega().getEsEnvio()) {
            throw new Excepcion("Esta compra no es del tipo envio");
        }

        if (compra.getEstado() != EstadoCompra.Confirmada) {
            throw new Excepcion("Esta compra esta en un estado no valido para esta funcionalidad");
        }

        if (compra.getInfoEntrega().getTiempoEstimadoEnvio().after(new Date())) {
            throw new Excepcion("Solo se puede colocar la compra como completada cuando supere la fecha estipulada para ser entregada");
        }

        Generico comprador = compraRepository.obtenerComprador(compra.getId());
        Generico vendedor = compraRepository.obtenerVendedor(compra.getId());
        String nombreParaMostrar;
        if (vendedor.getDatosVendedor().getNombreEmpresa().isBlank())
            nombreParaMostrar = vendedor.getDatosVendedor().getNombreEmpresa();
        else
            nombreParaMostrar = vendedor.getNombre() + " " + vendedor.getApellido();

        compra.setEstado(EstadoCompra.Completada);
        compraRepository.save(compra);
        Note noteComprador = new Note("Compra completada", "La compra hecha a " + nombreParaMostrar + " a sido completada!!! Ve hacia 'Historial de compras' para calificar al vendedor o realizar reclamos.", new HashMap<>(), null);
        String mensaje = "La compra hecha a " + nombreParaMostrar + " a sido completada (Identificador: +" + compra.getId() + ")!!! Ve hacia 'Historial de compras' para calificar al vendedor o realizar reclamos.\n Detalles de la compra:\n" + utilService.detallesCompra(compra, vendedor, comprador, compra.getInfoEntrega().getProducto(), compra.getInfoEntrega().getEsEnvio()) + "";
        String asunto = "Compra completada";
        if (!comprador.getWebToken().equals(""))
            firebaseMessagingService.enviarNotificacion(noteComprador, comprador.getWebToken());
        if (!comprador.getMobileToken().equals(""))
            firebaseMessagingService.enviarNotificacion(noteComprador, comprador.getMobileToken());
        googleSMTP.enviarCorreo(comprador.getCorreo(), mensaje, asunto);
    }

    public void crearChat(DtChat datosChat, String emailUsuario) throws Excepcion {
//        Optional<Usuario> usuarioBaseDatos = usuarioRepository.findByCorreoAndEstado(emailUsuario, EstadoUsuario.Activo);
//        if (usuarioBaseDatos.isEmpty()) {
//            throw new Excepcion("El usuario no esta habilitado");
//        }
//        Generico usuario = (Generico) usuarioBaseDatos.get();

        Compra compra = (Compra) compraRepository.findById(UUID.fromString(datosChat.getIdCompra())).get();
        if (compra == null) {
            throw new Excepcion("La compra no exite");
        }
        compra.setIdChat(datosChat.getIdChat());
        compraRepository.save(compra);

    }

    public String obtenerChat(String idCompra) {
        Compra compra = (Compra) compraRepository.findById(UUID.fromString(idCompra)).get();
        if (compra == null) {
            throw new Excepcion("La compra no exite");
        } else {
            return compra.getIdChat();
        }

    }

    public DtCompraDeshacer infoCompraParaReembolso(UUID idCompra) {
        Compra compra = compraRepository.findById(idCompra).orElseThrow(() -> new Excepcion("La compra/venta no existe."));
        Generico comprador = compraRepository.obtenerComprador(idCompra);
        Generico vendedor = compraRepository.obtenerVendedor(idCompra);
        CompraProducto infoCompra = compra.getInfoEntrega();

        String nombreVendedor;
        if (vendedor.getDatosVendedor().getNombreEmpresa() == null) {
            nombreVendedor = vendedor.getNombre() + " " + vendedor.getApellido();
        } else {
            nombreVendedor = vendedor.getDatosVendedor().getNombreEmpresa();
        }
        boolean reclamoNoResuelto = reclamoRepository.existsByCompraAndResuelto(compra, TipoResolucion.NoResuelto);

        boolean garantiaActiva = compra.getEstado() == EstadoCompra.Completada;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(infoCompra.getHorarioRetiroLocal());
        calendar.add(Calendar.DATE, infoCompra.getProducto().getDiasGarantia());
        if (new Date().after(calendar.getTime()))
            garantiaActiva = false;

        return new DtCompraDeshacer(compra.getId(), comprador.getNombre() + " " + comprador.getApellido(), nombreVendedor, infoCompra.getProducto().getNombre(),
                infoCompra.getCantidad(), compra.getFecha(), compra.getEstado(), infoCompra.getPrecioTotal(), infoCompra.getPrecioUnitario(), infoCompra.getEsEnvio(),
                reclamoNoResuelto, infoCompra.getHorarioRetiroLocal(), infoCompra.getDireccionEnvioORetiro().toString(), garantiaActiva);

    }


}
