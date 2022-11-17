package com.shopnow.shopnow.controller.responsetypes;

import com.shopnow.shopnow.model.enumerados.Rol;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class RegistrarUsuarioResponse {
    boolean success;
    String token, errorMessage, uuid;
    Rol rol;
}
