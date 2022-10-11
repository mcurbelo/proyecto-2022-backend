package com.shopnow.shopnow.model.datatypes;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;
import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class DtAltaProducto {
    @NotBlank
    private String emailVendedor;
    @NotBlank
    private String nombreProducto;
    @NotNull
    @Min(1)
    private Integer stock;
    @NotBlank
    private String descripcion;

    @JsonFormat(pattern = "DD/MM/yyyy")
    private Date fechaFin;
    @NotNull
    @Min(1)
    private Float precio;
    @NotNull
    @Min(0)
    private Integer diasGarantia;
    @NotNull
    private Boolean permiteEnvio;
    @Size(min=1, max =5)
    private List<String> categorias;
    @NotNull
    private Boolean esSolicitud;
}
