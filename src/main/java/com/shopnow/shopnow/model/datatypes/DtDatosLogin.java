package com.shopnow.shopnow.model.datatypes;


import lombok.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class DtDatosLogin {
    private String correo;
    private String password;
    private String tokenWeb;
    private String tokenMobile;
}