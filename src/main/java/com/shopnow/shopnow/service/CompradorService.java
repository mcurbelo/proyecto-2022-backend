package com.shopnow.shopnow.service;

import com.shopnow.shopnow.controller.responsetypes.Excepcion;
import com.shopnow.shopnow.model.*;
import com.shopnow.shopnow.model.datatypes.*;
import com.shopnow.shopnow.model.enumerados.EstadoSolicitud;
import com.shopnow.shopnow.model.enumerados.EstadoUsuario;
import com.shopnow.shopnow.repository.CompraRepository;
import com.shopnow.shopnow.repository.DatosVendedorRepository;
import com.shopnow.shopnow.repository.DireccionRepository;
import com.shopnow.shopnow.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CompradorService {

    @Autowired
    UsuarioRepository usuarioRepository;

    @Autowired
    ProductoService productoService;

    @Autowired
    DireccionRepository direccionRepository;

    @Autowired
    GoogleSMTP googleSMTP;

    @Autowired
    CompraRepository compraRepository;

    @Autowired
    DatosVendedorRepository datosVendedorRepository;

    @Autowired
    FirebaseStorageService firebaseStorageService;

    public void agregarDireccion(DtDireccion datos, String correoUsuario) {
        Optional<Usuario> usuario = usuarioRepository.findByCorreoAndEstado(correoUsuario, EstadoUsuario.Activo);

        if (usuario.isEmpty()) throw new Excepcion("Algo ha salido mal");
        Generico usuarioCasteado = (Generico) usuario.get();
        Direccion direccion = Direccion.builder()
                .calle(datos.getCalle())
                .numero(datos.getNumero())
                .localidad(datos.getLocalidad())
                .departamento(datos.getDepartamento())
                .notas(datos.getNotas())
                .build();

        if (datos.getEsLocal()) {
            if (usuarioCasteado.getDatosVendedor() == null)
                throw new Excepcion("El usuario no puede agregar direcciones de retiro");
            usuarioCasteado.getDatosVendedor()
                    .getLocales().values().forEach(address -> validarDireccion(direccion, address));
            direccionRepository.save(direccion);
            usuarioCasteado.getDatosVendedor().getLocales().put(direccion.getId(), direccion);
            usuarioRepository.save(usuarioCasteado);
        } else {
            usuarioCasteado.getDireccionesEnvio().values().forEach(address -> validarDireccion(direccion, address));
            direccionRepository.save(direccion);
            usuarioCasteado.getDireccionesEnvio().put(direccion.getId(), direccion);
            usuarioRepository.save(usuarioCasteado);
        }
    }

    public List<DtDireccion> obtenerDirecciones(String correoUsuario){
        Optional<Usuario> usuario = usuarioRepository.findByCorreoAndEstado(correoUsuario, EstadoUsuario.Activo);

        if (usuario.isEmpty()) throw new Excepcion("Algo ha salido mal");
        Generico usuarioCasteado = (Generico) usuario.get();
        List<DtDireccion> direcciones =  new ArrayList<DtDireccion>();

        Boolean esVendedor = usuarioCasteado.getDatosVendedor() != null;

        for (Direccion direccion : usuarioCasteado.getDireccionesEnvio().values()) {
            DtDireccion d = DtDireccion.builder()
                    .calle(direccion.getCalle())
                    .localidad(direccion.getLocalidad())
                    .id(direccion.getId())
                    .numero(direccion.getNumero())
                    .notas(direccion.getNotas())
                    .departamento(direccion.getDepartamento()).build();
            if(esVendedor){
                DatosVendedor datosVendedor = usuarioCasteado.getDatosVendedor();
                for (Direccion direccionLocal : datosVendedor.getLocales().values()) {
                    DtDireccion dLocal = DtDireccion.builder()
                            .calle(direccionLocal.getCalle())
                            .localidad(direccionLocal.getLocalidad())
                            .id(direccionLocal.getId())
                            .numero(direccionLocal.getNumero())
                            .notas(direccionLocal.getNotas())
                            .departamento(direccionLocal.getDepartamento()).esLocal(true).build();
                    direcciones.add(dLocal);
                }
            }
            direcciones.add(d);
        }
        return direcciones;

    }

    public void editarDireccion (DtDireccion nuevaDireccion){
        Optional<Direccion> resultado = direccionRepository.findById(nuevaDireccion.getId());
        Direccion direccion = (Direccion) resultado.get();

        if(direccion.equals(null)){
            throw new Excepcion("No existe la direccion");
        }

        direccion.setCalle(nuevaDireccion.getCalle());
        direccion.setNumero(nuevaDireccion.getNumero());
        direccion.setDepartamento(nuevaDireccion.getDepartamento());
        direccion.setLocalidad(nuevaDireccion.getLocalidad());
        direccion.setNotas(nuevaDireccion.getNotas());

        direccionRepository.save(direccion);
    }

    private void validarDireccion(Direccion toAdd, Direccion existingAddress) {
        if (Objects.equals(toAdd.getCalle(), existingAddress.getCalle()) &&
                Objects.equals(toAdd.getNumero(), existingAddress.getNumero()) &&
                Objects.equals(toAdd.getDepartamento(), existingAddress.getDepartamento()) &&
                Objects.equals(toAdd.getLocalidad(), existingAddress.getLocalidad()))
            throw new Excepcion("Dirección ya existente");
    }

    public void crearSolicitud(DtSolicitud datos, MultipartFile[] imagenes, String email) throws IOException {
        boolean esEmpresa = contieneDatosEmpresa(datos.getNombreEmpresa(), datos.getRut(), datos.getTelefonoEmpresa());
        if (esEmpresa && !datosEmpresaValidos(datos.getNombreEmpresa(), datos.getRut(), datos.getTelefonoEmpresa())) {
            throw new Excepcion("Los datos de la empresa no estan completos");
        }
        if (esEmpresa && datosVendedorRepository.existsByRutOrNombreEmpresaOrTelefonoEmpresa(datos.getRut(), datos.getNombreEmpresa(), datos.getTelefonoEmpresa()))
            throw new Excepcion("Ya existen los datos ingresados de la empresa");


        //TODO
/*                            Validaciones de largo y que sean numericos

        if (esEmpresa && datos.getRut().length() != 12 || datos.getRut().matches("[+-]?\\d*(\\.\\d+)?")) {
            throw new Excepcion("Valor RUT invalido");
          }
        if (esEmpresa && datos.getTelefonoEmpresa().length() >= 8 || datos.getTelefonoEmpresa().matches("[+-]?\\d*(\\.\\d+)?")) {
            throw new Excepcion("Valor RUT invalido");
        }
 */
        Optional<Usuario> resultado = usuarioRepository.findByCorreo(email);
        Generico usuario;
        if (resultado.isEmpty()) {
            throw new Excepcion("El usuario no existe");
        } else {
            usuario = (Generico) resultado.get();
        }

        if (usuario.getDatosVendedor() != null) {
            if (usuario.getDatosVendedor().getEstadoSolicitud() == EstadoSolicitud.Aceptado) {
                throw new Excepcion("No puedes utilizar esta funcionalidad");
            }

            if (usuario.getDatosVendedor().getEstadoSolicitud() == EstadoSolicitud.Pendiente) {
                throw new Excepcion("No puedes utilizar esta funcionalidad, cuando tienes una solicitud pendiente");
            }
        }

        productoService.agregarProducto(datos.getProducto(), imagenes, email, true);

        DtDireccion infoLocal = datos.getLocal();
        Integer idDireccion = datos.getIdDireccion();

        if (infoLocal == null && idDireccion == null)
            throw new Excepcion("Se debe ingresar una direccion valida");

        if (infoLocal != null && idDireccion != null) {
            throw new Excepcion("Se debe ingresar una sola direccion");
        }
        Direccion local;
        if (infoLocal != null) {
            local = new Direccion(null, infoLocal.getCalle(), infoLocal.getNumero(), infoLocal.getDepartamento(), infoLocal.getLocalidad(), infoLocal.getNotas());
            direccionRepository.saveAndFlush(local);
        } else {
            local = direccionRepository.findById(idDireccion).orElseThrow(() -> new Excepcion("El identificador de direccion no existe"));
            if (direccionRepository.yaPerteneceAUnaEmpresa(local.getId())) {
                throw new Excepcion("Esa direccion ya pertenece a un vendedor");
            }
        }
        Map<Integer, Direccion> locales = new HashMap<>();
        locales.put(local.getId(), local);
        DatosVendedor solicitud;
        if (esEmpresa)
            solicitud = new DatosVendedor(null, datos.getNombreEmpresa(), datos.getRut(), datos.getTelefonoEmpresa(), EstadoSolicitud.Pendiente, locales);
        else {
            solicitud = new DatosVendedor(null, null, null, null, EstadoSolicitud.Pendiente, locales);
        }
        usuario.setDatosVendedor(solicitud);
        usuarioRepository.save(usuario);
        googleSMTP.enviarCorreo("proyecto.tecnologo.2022@gmail.com", "Hay una nueva solicitud pendiente para ser vendedor (" + usuario.getNombre() + " " + usuario.getApellido() + ").", "Solicitud rol vendedor");
    }


    public Map<String, Object> historialDeCompras(int pageNo, int pageSize, String sortBy, String sortDir, DtFiltrosCompras filtros, UUID id) {
        if (!sortBy.matches("fecha|estado")) {
            throw new Excepcion("Atributo de ordenamiento invalido");
        }

        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();

        // create Pageable instance
        Page<Compra> compras;
        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);

        List<UUID> comprasCumplenFiltro;

        if (filtros != null) {
            List<UUID> comprasIdConFecha = null;
            if (filtros.getFecha() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                String fecha = sdf.format(filtros.getFecha());
                comprasIdConFecha = compraRepository.comprasPorFechaYIdusuario(fecha, id);
            }
            List<UUID> comprasIdConEstado = null;
            if (filtros.getEstado() != null) {
                comprasIdConEstado = compraRepository.comprasPorEstadoYIdusuario(filtros.getEstado().name(), id);
            }
            List<UUID> comprasIdConNombreVendedor = null;
            if (filtros.getNombreVendedor() != null) {
                comprasIdConNombreVendedor = compraRepository.comprasPorIdUsuarioYNombreVendedor(id, filtros.getNombreVendedor());
            }
            List<UUID> comprasIdConNombreProducto = null;
            if (filtros.getNombreVendedor() != null) {
                comprasIdConNombreProducto = compraRepository.comprasPorIdUsuarioYNombreProducto(id, filtros.getNombreProducto());
            }
            comprasCumplenFiltro = UtilService.encontrarInterseccion(new HashSet<>(), comprasIdConEstado, comprasIdConFecha, comprasIdConNombreProducto, comprasIdConNombreVendedor).stream().toList();
            compras = compraRepository.findByIdIn(comprasCumplenFiltro, pageable);
        } else
            compras = compraRepository.comprasPorIdUsuario(id, pageable);


        List<Compra> listaDeCompras = compras.getContent();

        List<DtCompraSlimComprador> content = listaDeCompras.stream().map(this::generarDtCompraSlimComprador).collect(Collectors.toList());

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("compras", content);
        response.put("currentPage", compras.getNumber());
        response.put("totalItems", compras.getTotalElements());
        response.put("totalPages", compras.getTotalPages());

        return response;
    }


    private boolean datosEmpresaValidos(String nombre, String rut, String telefono) {
        List<String> datos = new ArrayList<>(Arrays.asList(nombre, rut, telefono));
        return !datos.contains(null) && !datos.contains("");
    }

    private boolean contieneDatosEmpresa(String nombre, String rut, String telefono) {
        if (nombre != null && !nombre.isEmpty())
            return true;
        if (rut != null && !rut.isEmpty())
            return true;
        return telefono != null && !telefono.isEmpty();
    }

    private DtCompraSlimComprador generarDtCompraSlimComprador(Compra compra) {
        Generico vendedor = (Generico) compraRepository.obtenerVendedor(compra.getId());
        String nombreProducto = compra.getInfoEntrega().getProducto().getNombre();
        String nombreParaMostrar = (vendedor.getDatosVendedor().getNombreEmpresa() != null) ? vendedor.getDatosVendedor().getNombreEmpresa() : vendedor.getNombre() + " " + vendedor.getApellido();

        return new DtCompraSlimComprador(compra.getId(), vendedor.getId(), nombreParaMostrar, nombreProducto, compra.getInfoEntrega().getCantidad(), compra.getFecha(), compra.getEstado(), compra.getInfoEntrega().getPrecioTotal(), compra.getInfoEntrega().getPrecioUnitario());
    }
}
