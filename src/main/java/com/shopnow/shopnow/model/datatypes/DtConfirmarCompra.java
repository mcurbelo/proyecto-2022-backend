package com.shopnow.shopnow.model.datatypes;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Date;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class DtConfirmarCompra {

    @JsonFormat(pattern = "dd/MM/yyyy hh:mm")
    private Date fechayHoraRetiro;


    @JsonFormat(pattern = "dd/MM/yyyy hh:mm")
    private Date fechayHoraEntrega;

    private String motivo;
}
