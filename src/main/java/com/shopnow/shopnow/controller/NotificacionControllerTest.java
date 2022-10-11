package com.shopnow.shopnow.controller;



import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.shopnow.shopnow.model.Note;
import com.shopnow.shopnow.service.FirebaseMessagingService;
import com.shopnow.shopnow.service.FirebaseStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/test/")
public class NotificacionControllerTest {
    @Autowired
    private FirebaseMessagingService firebaseService;

    @Autowired
    private FirebaseStorageService firebaseStorageService;

    @PostMapping("/notificacion")
    @ResponseBody
    public String enviarNotificacion(@RequestBody Note note) throws FirebaseMessagingException, FirebaseAuthException {
         return firebaseService.enviarNotificacion(note, note.getToken());
    }
    @PostMapping("/subirArchivo")
    @ResponseBody
    public String guardarArchivo(@RequestParam("file") MultipartFile multipartFile) throws IOException {
        return firebaseStorageService.uploadFile(multipartFile, "testSubirArchivo");
    }



}