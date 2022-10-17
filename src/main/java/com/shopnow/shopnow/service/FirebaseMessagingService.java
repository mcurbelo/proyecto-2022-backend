package com.shopnow.shopnow.service;


import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.shopnow.shopnow.model.Note;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FirebaseMessagingService {
    @Autowired
    private FirebaseMessaging firebaseMessaging;

    public void enviarNotificacion(Note note, String token) throws FirebaseMessagingException, FirebaseAuthException {

        Notification notification = Notification
                .builder()
                .setTitle(note.getAsunto())
                .setBody(note.getMensaje())
                .build();


        Message message = Message
                .builder()
                .setToken(token)
                .setNotification(notification)
                .putAllData(note.getData())
                .build();

        firebaseMessaging.send(message);
    }

}