package com.shopnow.shopnow.service;

import com.braintreegateway.*;
import com.shopnow.shopnow.model.Generico;
import com.shopnow.shopnow.model.Tarjeta;
import com.shopnow.shopnow.model.datatypes.DtTarjeta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BraintreeUtils {
    @Autowired
    BraintreeGateway gateway;
    public String generateCustomerId(Generico usuario) {
        CustomerRequest request = new CustomerRequest()
                .firstName(usuario.getNombre())
                .lastName(usuario.getApellido())
                .email(usuario.getCorreo());
        Result<Customer> result = gateway.customer().create(request);
        if(result.isSuccess()) {
            return result.getTarget().getId();
        }
        return null;
    }

    public Tarjeta agregarTarjeta(DtTarjeta tarjeta, String customerId) {
        CreditCardRequest request = new CreditCardRequest()
                .customerId(customerId)
                .cvv(tarjeta.getCardCvv())
                .number(tarjeta.getCardNumber())
                .expirationDate(tarjeta.getCardExpiration());
        Result<CreditCard> result = gateway.creditCard().create(request);
        if(result.isSuccess()) {
            return Tarjeta.builder().token(result.getTarget().getToken())
                    .last4(result.getTarget().getLast4())
                    .vencimiento(result.getTarget().getExpirationDate())
                    .imageUrl(result.getTarget().getImageUrl())
                    .build();
        }
        return null;
    }
}
