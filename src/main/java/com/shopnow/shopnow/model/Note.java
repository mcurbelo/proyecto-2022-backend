package com.shopnow.shopnow.model;

import lombok.*;

import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Note {
    private String asunto;
    private String mensaje;
    private String token;
    private Map<String, String> data;
    private String img;
}