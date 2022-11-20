package com.shopnow.shopnow.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.*;
import com.shopnow.shopnow.controller.responsetypes.Excepcion;
import com.shopnow.shopnow.controller.responsetypes.RegistrarUsuarioResponse;
import com.shopnow.shopnow.model.Administrador;
import com.shopnow.shopnow.model.Generico;
import com.shopnow.shopnow.model.Usuario;
import com.shopnow.shopnow.model.datatypes.DtUsuario;
import com.shopnow.shopnow.model.enumerados.EstadoUsuario;
import com.shopnow.shopnow.model.enumerados.Rol;
import com.shopnow.shopnow.repository.UsuarioRepository;
import com.shopnow.shopnow.security.JWTUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.TimeUnit;


@Service
public class AuthService {
    @Autowired
    GoogleSMTP googleSMTP;
    @Autowired
    private UsuarioRepository usuarioRepo;
    @Autowired
    private JWTUtil jwtUtil;
    @Autowired
    private AuthenticationManager authManager;
    @Autowired
    private PasswordEncoder passwordEncoder;
    private String bucketName;
    private String projectId;
    private StorageOptions storageOptions;

    @PostConstruct
    private void initializeFirebase() throws Exception {
        bucketName = "shopnowproyecto2022.appspot.com";
        projectId = "shopnowproyecto2022";

        this.storageOptions = StorageOptions.newBuilder().setCredentials(GoogleCredentials.fromStream(new ClassPathResource("firebase-service-account.json").getInputStream()))
                .setProjectId(projectId).build();
    }

    public RegistrarUsuarioResponse registrarUsuario(DtUsuario datosUsuario) {
        //validaciones
        if (usuarioRepo.findByCorreoAndEstado(datosUsuario.getCorreo(), EstadoUsuario.Activo).isPresent()) {
            return new RegistrarUsuarioResponse(false, "", "Usuario ya existente", "", null);
        }


        String urlImagen = "";
        if (datosUsuario.getImagen() != null) {

            try {
                urlImagen = this.cargarImagen(datosUsuario);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }


        String encodedPass = passwordEncoder.encode(datosUsuario.getPassword());
        Generico usuario = Generico.builder()
                .fechaNac(new Date())
                .nombre(datosUsuario.getNombre())
                .apellido(datosUsuario.getApellido()).correo(datosUsuario.getCorreo())
                .estado(EstadoUsuario.Activo)
                .imagen(urlImagen).mobileToken("")
                .webToken("")
                .password(encodedPass)
                .telefono(datosUsuario.getTelefono())
                .fechaRegistro(new Date())
                .fechaNac(datosUsuario.getFechaNac())
                .build();
        usuarioRepo.save(usuario);
        String token = jwtUtil.generateToken(usuario.getCorreo(), usuario.getId().toString());
        return new RegistrarUsuarioResponse(true, token, "", usuario.getId().toString(), Rol.Usuario);
    }

    public Map<String, String> iniciarSesion(String correo, String password, String tokenWeb, String tokenMobile) {
        if (usuarioRepo.findByCorreoAndEstado(correo, EstadoUsuario.Activo).isEmpty()) {
            throw new RuntimeException("Credenciales invalidas");
        }

        try {
            UsernamePasswordAuthenticationToken authInputToken = new UsernamePasswordAuthenticationToken(correo, password);
            authManager.authenticate(authInputToken);
            Map<String, String> response = new HashMap<>(Collections.emptyMap());
            Usuario usuario = usuarioRepo.findByCorreoAndEstado(correo, EstadoUsuario.Activo).orElseThrow();
            response.put("uuid", usuario.getId().toString());
            String token = jwtUtil.generateToken(correo, response.get("uuid"));
            response.put("jwt-token", token);
            if (tokenWeb != null) {
                usuarioRepo.quitarTokenWeb(tokenWeb);
                usuario.setWebToken(tokenWeb);
                usuarioRepo.save(usuario);
            }
            if (tokenMobile != null) {
                usuarioRepo.quitarTokenMobile(tokenMobile);
                usuario.setMobileToken(tokenMobile);
                usuarioRepo.save(usuario);

            }
            response.put("rol", String.valueOf((usuario instanceof Administrador) ? Rol.ADM : Rol.Usuario));
            return response;
        } catch (AuthenticationException | NoSuchElementException authExc) {
            throw new RuntimeException("Credenciales invalidas");
        }
    }


    public void recuperarContrasena(String correo) throws NoSuchAlgorithmException {
        Usuario usuario = usuarioRepo.findByCorreoAndEstado(correo, EstadoUsuario.Activo).orElse(null);
        if (usuario != null) {
            String chrs = "0123456789abcdefghijklmnopqrstuvwxyz-_ABCDEFGHIJKLMNOPQRSTUVWXYZ";
            SecureRandom secureRandom = SecureRandom.getInstanceStrong();
            String token = secureRandom.ints(9, 0, chrs.length()).mapToObj(chrs::charAt)
                    .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append).toString();
            usuario.setResetPasswordToken(token);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            calendar.add(Calendar.HOUR, 1);
            usuario.setExpiracionPasswordToken(calendar.getTime());
            usuarioRepo.save(usuario);
            googleSMTP.enviarCorreo(correo, "Para reiniciar tu contraseña en el sitio ShopNow, utiliza el siguiente codigo: " + usuario.getResetPasswordToken() + " ingresandolo en el campo solicitado.\nEste código tiene validez de 1 hora.", "Solicitud de reinicio de contraseña");
        }
    }

    public void reiniciarContrasena(String token, String nuevaContra) {
        Usuario usuario = usuarioRepo.findByResetPasswordToken(token).orElseThrow(() -> new Excepcion("Token incorrecto"));
        if (usuario.getExpiracionPasswordToken().before(new Date())) {
            throw new Excepcion("Tiempo de validez del token finalizado");
        }
        usuario.setPassword(passwordEncoder.encode(nuevaContra));
        usuario.setResetPasswordToken(null);
        usuario.setResetPasswordToken(null);
        usuarioRepo.save(usuario);

    }

    public boolean verificarCodigo (String codigo){
        Usuario usuario = usuarioRepo.findByResetPasswordToken(codigo).orElseThrow(() -> new Excepcion("Token incorrecto"));
        if (usuario.getExpiracionPasswordToken().before(new Date())) {
            throw new Excepcion("Tiempo de validez del token finalizado");
        }else{
            return true;
        }
    }


    /*Funciones auxiliares*/

    public String cargarImagen(DtUsuario datosUsuario) throws IOException {
        File file = this.getFile(datosUsuario.getImagen().getData());
        Path filePath = file.toPath();
        String objectName = datosUsuario.getImagen().getNombre();

        Storage storage = storageOptions.getService();

        BlobId blobId = BlobId.of(bucketName, objectName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
        Blob blob = storage.create(blobInfo, Files.readAllBytes(filePath));
        URL url = blob.signUrl(15, TimeUnit.MINUTES, Storage.SignUrlOption.withV4Signature());

        String tempurl = String.format("https://firebasestorage.googleapis.com/v0/b/shopnowproyecto2022.appspot.com/o/%s?alt=media", URLEncoder.encode(objectName, StandardCharsets.UTF_8));

        Files.delete(filePath); //Borro la imagen de el directorio del equipo
        return tempurl;
    }

    private File getFile(String base64) throws IOException {
        File file = new File("imagen");
        byte[] data = Base64.getDecoder().decode(base64);
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(data);
        fos.close();
        return file;
    }
}