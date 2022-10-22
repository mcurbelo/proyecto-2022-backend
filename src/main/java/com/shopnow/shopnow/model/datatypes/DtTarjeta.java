package com.shopnow.shopnow.model.datatypes;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Getter
@Setter
public class DtTarjeta {
    @NonNull
    private String cardNumber, cardCvv, cardExpiration;
}


