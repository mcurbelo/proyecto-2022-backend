package com.shopnow.shopnow.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class GoogleSMTP {

//    @Autowired
//    JavaMailSender sender;


    public void enviarCorreo(String destino, String mensaje, String asunto) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(destino);
        message.setSubject(asunto);
        message.setText(mensaje);
//        sender.send(message);
    }

}
