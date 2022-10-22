package com.shopnow.shopnow.controller.responsetypes;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class CreditCardRef {
    String id, last4, imageUrl, expiration;
}
