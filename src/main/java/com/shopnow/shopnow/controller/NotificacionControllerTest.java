package com.shopnow.shopnow.controller;



import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.shopnow.shopnow.model.Note;
import com.shopnow.shopnow.service.FirebaseMessagingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test/notificacion")
public class NotificacionControllerTest {
    @Autowired
    private FirebaseMessagingService firebaseService;

    @PostMapping
    @ResponseBody
    public String enviarNotificacion(@RequestBody Note note) throws FirebaseMessagingException, FirebaseAuthException {
        return firebaseService.enviarNotificacion(note, note.getToken());
    }
}