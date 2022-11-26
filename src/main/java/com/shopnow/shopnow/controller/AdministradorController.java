package com.shopnow.shopnow.controller;

import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.shopnow.shopnow.model.datatypes.DtMotivo;
import com.shopnow.shopnow.model.datatypes.DtUsuarioSlim;
import com.shopnow.shopnow.model.enumerados.EstAdm;
import com.shopnow.shopnow.model.enumerados.EstadoUsuario;
import com.shopnow.shopnow.service.AdministradorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/administradores")
public class AdministradorController {

    @Autowired
    AdministradorService administradorService;


    @PutMapping("/usuarios/{id}")
    public ResponseEntity<String> cambiarEstadoUsuario(@PathVariable(value = "id") UUID id, @Valid @RequestBody DtMotivo motivo, @RequestParam(value = "operacion") EstadoUsuario nuevoEstado) {
        if (nuevoEstado == EstadoUsuario.Bloqueado)
            administradorService.bloquearUsuario(id, motivo.getMotivo());
        else if (nuevoEstado == EstadoUsuario.Activo)
            administradorService.desbloquearUsuario(id);
        else
            administradorService.eliminarUsuario(id, motivo.getMotivo());
        return new ResponseEntity<>("Usuario cambiado de estado con exito!!!", HttpStatus.OK);
    }

    @PutMapping("/usuarios/{id}/solicitudes")
    public ResponseEntity<String> revisarSolicitudNuevoVendedor(@PathVariable(value = "id") UUID id, @RequestParam(value = "aceptar") Boolean accion, @RequestBody(required = false) DtMotivo motivo) {
        administradorService.respuestaSolicitud(id, accion, (motivo != null) ? motivo.getMotivo() : null);
        return new ResponseEntity<>("Accion realizada con exito!!!", HttpStatus.OK);
    }

    @PostMapping()
    public ResponseEntity<String> nuevoAdministrador(@Valid @RequestBody DtUsuarioSlim datos) throws NoSuchAlgorithmException {
        administradorService.crearAdministrador(datos);
        return new ResponseEntity<>("Accion realizada con exito!!!", HttpStatus.OK);
    }

    @GetMapping("/solicitudes")
    public Map<String, Object> solicitudesVendedor(
            @RequestParam(value = "pageNo", defaultValue = "0", required = false) int pageNo,
            @RequestParam(value = "pageSize", defaultValue = "20", required = false) int pageSize,
            @RequestParam(value = "sortBy", defaultValue = "id", required = false) String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "dsc", required = false) String sortDir) {
        return administradorService.listadoSolicitudes(pageNo, pageSize, sortBy, sortDir);
    }

    @GetMapping("/estadisticas/{opcion}")
    public Map<String, Object> estadisticasAdm(@PathVariable(value = "opcion") EstAdm opcion,
                                               @RequestParam(value = "fechaInicio", required = false) @DateTimeFormat(pattern = "dd/MM/yyyy") Date fechaInicio,
                                               @RequestParam(value = "fechaFin", required = false) @DateTimeFormat(pattern = "dd/MM/yyyy") Date fechaFin) {
        Map<String, Object> response = new LinkedHashMap<>();
        boolean esHistorico = fechaInicio == null && fechaFin == null;

        if (opcion == EstAdm.Todas || opcion == EstAdm.Ventas)
            response.put("ventas", administradorService.estaditicasVentas(fechaInicio, fechaFin, esHistorico));

        if (opcion == EstAdm.Todas || opcion == EstAdm.Reclamos)
            response.put("reclamos", administradorService.estadisticasReclamos(fechaInicio, fechaFin, esHistorico));

        if (opcion == EstAdm.Todas || opcion == EstAdm.Usuarios)
            response.put("usuarios", administradorService.estadisticaUsuarios(fechaInicio, fechaFin, esHistorico));
        return response;
    }

    @PutMapping("/reembolsos/{idCompra}")
    public ResponseEntity<String> reembolsarCompra(@PathVariable(value = "idCompra") UUID idCompra) throws FirebaseMessagingException, FirebaseAuthException {
        administradorService.deshacerCompra(idCompra);
        return new ResponseEntity<>("Accion realizada con exito!!!", HttpStatus.OK);
    }

}
