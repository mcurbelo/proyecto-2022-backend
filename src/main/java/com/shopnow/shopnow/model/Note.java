package com.shopnow.shopnow.model;

import lombok.Data;

import java.util.Map;

@Data
public class Note {
    private String asunto;
    private String mensaje;
    private String token;
    private Map<String, String> data;
    private String img;
}