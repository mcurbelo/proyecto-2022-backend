package com.shopnow.shopnow.service;

import com.shopnow.shopnow.controller.responsetypes.CreditCardRef;
import com.shopnow.shopnow.controller.responsetypes.Excepcion;
import com.shopnow.shopnow.model.Generico;
import com.shopnow.shopnow.model.Tarjeta;
import com.shopnow.shopnow.model.Usuario;
import com.shopnow.shopnow.model.datatypes.*;
import com.shopnow.shopnow.model.enumerados.EstadoSolicitud;
import com.shopnow.shopnow.model.enumerados.EstadoUsuario;
import com.shopnow.shopnow.repository.DatosVendedorRepository;
import com.shopnow.shopnow.repository.TarjetasRepository;
import com.shopnow.shopnow.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@Service
public class UsuarioService {

    @Autowired
    UsuarioRepository usuarioRepository;
    @Autowired
    DatosVendedorRepository datosVendedorRepository;
    @Autowired
    TarjetasRepository tarjetasRepository;
    @Autowired
    FirebaseStorageService firebaseStorageService;
    @Autowired
    BraintreeUtils braintreeUtils;

    public DtUsuario infoUsuario(String correo) {

        Optional<Usuario> usuarioBaseDatos = usuarioRepository.findByCorreo(correo);

        Usuario usuario = usuarioBaseDatos.get();

        DtUsuario usuarioReturn = DtUsuario.builder()
                .nombre(usuario.getNombre())
                .apellido(usuario.getApellido())
                .correo(usuario.getCorreo())
                .imagen(DtImagen.builder().data(usuario.getImagen()).build())
                .telefono(usuario.getTelefono()).build();

        return usuarioReturn;
    }

    public void modificarDatosUsuario(UUID id, DtModificarUsuario datos, MultipartFile imagen) throws IOException {
        Generico usuario;
        Optional<Usuario> res = usuarioRepository.findByIdAndEstado(id, EstadoUsuario.Activo);
        if (res.isEmpty()) {
            throw new Excepcion("El usuario no existe");
        } else {
            usuario = (Generico) res.get();
        }
        if (datos.getCorreo() != null) {
            if (usuarioRepository.existsByCorreoAndEstado(datos.getCorreo(), EstadoUsuario.Activo)) {
                throw new Excepcion("El correo nuevo ya existe.");
            } else
                usuario.setCorreo(datos.getCorreo());
        }

        if (imagen != null && !imagen.isEmpty()) { //Solo se cambia el link
            String idImagen = UUID.randomUUID().toString();
            String link = firebaseStorageService.uploadFile(imagen, idImagen + "-UsuarioImg");
            usuario.setImagen(link);
        }

        if (datos.getContrasenaVieja() != null && datos.getContrasenaNueva() != null) {
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            if (encoder.matches(datos.getContrasenaVieja(), usuario.getPassword())) {
                String nuevaContrasena = encoder.encode(datos.getContrasenaNueva());
                usuario.setPassword(nuevaContrasena);
            } else {
                throw new Excepcion("Contraseña antigua incorrecta. No se realizó la modificacion");
            }
        }
        if (datos.getTelefonoContacto() != null)
            usuario.setTelefono(datos.getTelefonoContacto());

        if (usuario.getDatosVendedor() != null && usuario.getDatosVendedor().getEstadoSolicitud() == EstadoSolicitud.Aceptado) {
            //Esta parte de la funcionalidad deberia estar solo para vendedores aceptados, igualmente valido

            /* TODO Validacion del telefono :)
            if (datos.getTelefonoEmpresa().length() >= 8 || datos.getTelefonoEmpresa().matches("[+-]?\\d*(\\.\\d+)?")) {
                throw new Excepcion("El numero de telefono es invalido");
            }
        */
            if (datos.getNombreEmpresa() != null && datosVendedorRepository.existsByNombreEmpresa(datos.getNombreEmpresa()))
                throw new Excepcion("El nombre de la empresa ya existe");
            else
                usuario.getDatosVendedor().setNombreEmpresa(datos.getNombreEmpresa());

            if (datos.getTelefonoEmpresa() != null && datosVendedorRepository.existsByTelefonoEmpresa(datos.getNombreEmpresa()))
                throw new Excepcion("El numero de telefono de la empresa ya esta en uso.");
            else
                usuario.getDatosVendedor().setTelefonoEmpresa(datos.getTelefonoEmpresa());
        }

        usuarioRepository.save(usuario);

    }

    //TODO Verificar tarjeta duplicada para el usuario y verificar tarjeta existente en el sistema en general
    public void agregarTarjeta(DtTarjeta dtTarjeta, UUID userId) {
        Optional<Usuario> user = usuarioRepository.findByIdAndEstado(userId, EstadoUsuario.Activo);
        if (user.isPresent() && user.get() instanceof Generico usuarioGenerico) {
            if (usuarioGenerico.getBraintreeCustomerId() == null) {
                String braintreeId = braintreeUtils.generateCustomerId(usuarioGenerico);
                if (braintreeId == null) throw new Excepcion("Ha ocurrido un error inesperado");
                usuarioGenerico.setBraintreeCustomerId(braintreeId);
                usuarioRepository.save(usuarioGenerico);
            }
            Tarjeta nuevaTarjeta = braintreeUtils.agregarTarjeta(dtTarjeta, usuarioGenerico.getBraintreeCustomerId());
            if (nuevaTarjeta == null) throw new Excepcion("Ha ocurrido un error al agregar la tarjeta");
            tarjetasRepository.save(nuevaTarjeta);
            usuarioGenerico.getTarjetas().put(nuevaTarjeta.getIdTarjeta(), nuevaTarjeta);
            usuarioRepository.save(usuarioGenerico);
        } else {
            throw new Excepcion("Ha ocurrido un error inesperado");
        }
    }

    public List<CreditCardRef> getTarjetas(UUID id) {
        Optional<Usuario> user = usuarioRepository.findByIdAndEstado(id, EstadoUsuario.Activo);
        ArrayList<CreditCardRef> list = new ArrayList<>();
        if (user.isPresent() && user.get() instanceof Generico usuarioGenerico) {
            usuarioGenerico.getTarjetas().values().forEach(tarjeta -> {
                CreditCardRef ref = new CreditCardRef(
                        tarjeta.getIdTarjeta(),
                        tarjeta.getLast4(),
                        tarjeta.getImageUrl(),
                        tarjeta.getVencimiento());
                list.add(ref);
            });
        }
        return list;
    }

    public Map<String, Object> listadoDeUsuarios(int pageNo, int pageSize, String sortBy, String sortDir, DtFiltrosUsuario filtros) {

        if (!sortBy.matches("nombre|apellido|correo")) {
            throw new Excepcion("Atributo de ordenamiento invalido");
        }
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();

        // create Pageable instance
        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);

        Page<Usuario> usuarios;

        List<UUID> usuariosCumplenFiltro = new ArrayList<>();
        boolean isEmpty = false;

        if (filtros != null) {
            List<UUID> usuariosIdConNombre = null;
            if (filtros.getNombre() != null) {
                usuariosIdConNombre = usuarioRepository.usuariosConNombre(filtros.getNombre());
                if (usuariosIdConNombre.isEmpty()) isEmpty = true;
            }
            List<UUID> usuariosIdConApellido = null;
            if (filtros.getApellido() != null && !isEmpty) {
                usuariosIdConApellido = usuarioRepository.usuariosConApellido(filtros.getApellido());
                if (usuariosIdConApellido.isEmpty()) isEmpty = true;
            }
            List<UUID> usuariosIdConCorreo = null;
            if (filtros.getCorreo() != null && !isEmpty) {
                usuariosIdConCorreo = usuarioRepository.usuariosConCorreo(filtros.getCorreo());
                if (usuariosIdConCorreo.isEmpty()) isEmpty = true;
            }
            List<UUID> usuariosIdConEstado = null;
            if (filtros.getEstado() != null && !isEmpty) {
                usuariosIdConEstado = usuarioRepository.usuariosConEstado(filtros.getEstado().toString());
                if (usuariosIdConEstado.isEmpty()) isEmpty = true;
            }
            if (!isEmpty)
                usuariosCumplenFiltro = UtilService.encontrarInterseccion(new HashSet<>(), usuariosIdConNombre, usuariosIdConApellido, usuariosIdConCorreo, usuariosIdConEstado).stream().toList();
            usuarios = usuarioRepository.findByIdIn(usuariosCumplenFiltro, pageable);
        } else
            usuarios = usuarioRepository.todosLosUsuarios(pageable);

        List<Usuario> listaDeUsuarios = usuarios.getContent();

        List<DtUsuarioSlim> content = listaDeUsuarios.stream().map(this::getDtUsuarioSlim).toList();

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("usuarios", content);
        response.put("currentPage", usuarios.getNumber());
        response.put("totalItems", usuarios.getTotalElements());
        response.put("totalPages", usuarios.getTotalPages());

        return response;
    }

    private DtUsuarioSlim getDtUsuarioSlim(Usuario usuario) {
        return new DtUsuarioSlim(usuario.getId(), usuario.getCorreo(), usuario.getNombre(), usuario.getApellido(), usuario.getEstado());

    }
}
