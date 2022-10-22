package com.shopnow.shopnow.service;

import com.braintreegateway.*;
import com.shopnow.shopnow.model.Generico;
import com.shopnow.shopnow.model.Tarjeta;
import com.shopnow.shopnow.model.datatypes.DtTarjeta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

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
        if (result.isSuccess()) {
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
        if (result.isSuccess()) {
            return Tarjeta.builder().token(result.getTarget().getToken())
                    .last4(result.getTarget().getLast4())
                    .vencimiento(result.getTarget().getExpirationDate())
                    .imageUrl(result.getTarget().getImageUrl())
                    .build();
        }
        return null;
    }

    public Result<Transaction> hacerPago(String customerId, String tokenTarjeta, String precio) {
        TransactionRequest request = new TransactionRequest()
                .customerId(customerId)
                .paymentMethodToken(tokenTarjeta)
                .amount(new BigDecimal(precio))
                .options()
                .submitForSettlement(true)
                .done();
        return gateway.transaction().sale(request);
    }

    public boolean devolverDinero(String idTransaccion) {
        Transaction.Status status = gateway.transaction().find(idTransaccion).getStatus();
        Result<Transaction> result;
        if (status == Transaction.Status.SETTLED) {
            result = gateway.transaction().refund(idTransaccion);
        } else {
            result = gateway.transaction().voidTransaction(idTransaccion);
        }
        return result.isSuccess();
    }

}
