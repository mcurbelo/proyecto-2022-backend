package com.shopnow.shopnow.controller;

import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.shopnow.shopnow.controller.responsetypes.Excepcion;
import com.shopnow.shopnow.model.datatypes.DtCalificacion;
import com.shopnow.shopnow.model.datatypes.DtChat;
import com.shopnow.shopnow.service.CalificacionService;
import com.shopnow.shopnow.service.CompraService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.UUID;

@RestController
@RequestMapping("/api/compras")
public class ComprasController {

    @Autowired
    CompraService compraService;

    @Autowired
    CalificacionService calificacionService;


    @PutMapping("/enviadas/{id}")
    public ResponseEntity<String> completarCompraEnvio(@PathVariable(value = "id") UUID idCompra) throws FirebaseMessagingException, FirebaseAuthException {
        //TODO Validar que el UUID que hace la solicitud es uno de los dos involucradors (vendedor o comprador)
        compraService.confirmarEntregaoReciboProducto(idCompra);
        return new ResponseEntity<>("Compra cambiada de estado con exito!!!", HttpStatus.OK);
    }

    @PostMapping("/calificaciones/{id}")
    public ResponseEntity<String> realizarCalificacion(@Valid @PathVariable(value = "id") UUID idcompra, @RequestBody DtCalificacion datos) {
        calificacionService.agregarCalificacion(idcompra, datos);
        return new ResponseEntity<>("Calificacion hecha con exito!!!", HttpStatus.OK);
    }

    @PostMapping("/iniciarChat")
    public ResponseEntity<String> iniciarChat (@RequestBody DtChat datosChat){
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        try{
            compraService.crearChat(datosChat, email);
        }catch (Excepcion e){
            return new ResponseEntity<>("Error al inicar el chat", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>("Chat Iniciado", HttpStatus.OK);
    }

    @GetMapping("/chat/{idcompra}")
    public ResponseEntity<String> obtenerChat (@PathVariable(value = "idcompra") String idcompra){
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(email.isEmpty()){
            return new ResponseEntity<>("Usuario desconocido", HttpStatus.FORBIDDEN);
        }
        try{
            String idChat = compraService.obtenerChat(idcompra);
            return new ResponseEntity<>(idChat, HttpStatus.OK);
        }catch (Excepcion e){
            return new ResponseEntity<>("Error al obtener el chat", HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }


}
