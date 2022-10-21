package com.shopnow.shopnow.controller;


import com.braintreegateway.BraintreeGateway;
import com.shopnow.shopnow.controller.responsetypes.CreditCardRef;
import com.shopnow.shopnow.model.datatypes.*;
import com.shopnow.shopnow.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;


@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    UsuarioService usuarioService;
    @Autowired
    BraintreeGateway gateway;

    @GetMapping("/obtenerInfoUsuario/{correo}")
    @ResponseBody
    public DtUsuario obtenerInfoUsuario(@PathVariable String correo) {
        DtUsuario usuario = usuarioService.infoUsuario(correo);
        return usuario;
    }

    @PutMapping("/{id}/perfil")
    public ResponseEntity<String> modificarPerfil(@PathVariable(value = "id") UUID id, @RequestPart DtModificarUsuario datos, @RequestPart(required = false) MultipartFile imagen) throws IOException {
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        //TODO Para cuando utilicemos 100% los token
        //   if(!email.equals(correo)){
        //      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        //}
        usuarioService.modificarDatosUsuario(id, datos, imagen);
        return new ResponseEntity<>("Perfil editado con exito!!!", HttpStatus.OK);
    }

    @PostMapping("/{id}/tarjetas")
    public ResponseEntity<String> agregarTarjeta(@PathVariable(value = "id") UUID id, @RequestBody DtTarjeta tarjeta) {
        usuarioService.agregarTarjeta(tarjeta, id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/tarjetas")
    public ResponseEntity<List<CreditCardRef>> fetchTarjetas(@PathVariable(value = "id") UUID id) {
        return ResponseEntity.ok().body(usuarioService.getTarjetas(id));
    }

    @GetMapping()
    public Map<String, Object> busquedaDeUsuarios(
            @RequestParam(value = "pageNo", defaultValue = "0", required = false) int pageNo,
            @RequestParam(value = "pageSize", defaultValue = "20", required = false) int pageSize,
            @RequestParam(value = "sortBy", defaultValue = "apellido", required = false) String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "asc", required = false) String sortDir,
            @RequestBody(required = false) DtFiltrosUsuario filtros) {
        return usuarioService.listadoDeUsuarios(pageNo, pageSize, sortBy, sortDir, filtros);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> eliminarCuenta(@PathVariable(value = "id") UUID id) {
        usuarioService.eliminarMiCuenta(id);
        return new ResponseEntity<>("Cuenta eliminada con exito!!!", HttpStatus.OK);
    }
}
