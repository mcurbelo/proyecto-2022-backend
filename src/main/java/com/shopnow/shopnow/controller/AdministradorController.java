package com.shopnow.shopnow.controller;

import com.shopnow.shopnow.model.datatypes.DtMotivo;
import com.shopnow.shopnow.model.datatypes.DtUsuarioSlim;
import com.shopnow.shopnow.model.enumerados.EstadoUsuario;
import com.shopnow.shopnow.service.AdministradorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.security.NoSuchAlgorithmException;
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
    public ResponseEntity<String> revisarSolicitudNuevoVendedor(@PathVariable(value = "id") UUID id, @RequestParam(value = "aceptar") Boolean accion, @RequestBody DtMotivo motivo) {
        administradorService.respuestaSolicitud(id, accion, motivo.getMotivo());
        return new ResponseEntity<>("Accion realizada con exito!!!", HttpStatus.OK);
    }

    @PostMapping()
    public ResponseEntity<String> nuevoAdministrador(@Valid @RequestBody DtUsuarioSlim datos) throws NoSuchAlgorithmException {
        administradorService.crearAdministrador(datos);
        return new ResponseEntity<>("Accion realizada con exito!!!", HttpStatus.OK);
    }
}
