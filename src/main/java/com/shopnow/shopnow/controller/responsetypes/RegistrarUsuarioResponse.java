package com.shopnow.shopnow.controller.responsetypes;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class RegistrarUsuarioResponse {
    boolean success;
    String token, errorMessage, uuid;
}
