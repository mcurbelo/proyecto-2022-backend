package com.shopnow.shopnow.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.*;
import com.google.firebase.cloud.StorageClient;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

@Service
public class FirebaseStorageService {

    private String bucketName;
    private StorageOptions storageOptions;


    @PostConstruct
    private void initializeFirebase() throws Exception {
        bucketName = ("shopnowproyecto2022.appspot.com");

        GoogleCredentials googleCredentials = GoogleCredentials
                .fromStream(new ClassPathResource("firebase-service-account.json").getInputStream());

        this.storageOptions = StorageOptions.newBuilder()
                .setCredentials(googleCredentials).build();

    }

    public String uploadFile(MultipartFile multipartFile, String id) throws IOException {
        File file = convertMultiPartToFile(multipartFile);
        Path filePath = file.toPath();
        String objectName = file.getName();

        Storage storage = storageOptions.getService();
        BlobId blobId = BlobId.of(bucketName, id);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType("image/jpeg").build();
        Blob blob = storage.create(blobInfo, Files.readAllBytes(filePath));

        String url = String.format("https://firebasestorage.googleapis.com/v0/b/shopnowproyecto2022.appspot.com/o/%s?alt=media", URLEncoder.encode(id, StandardCharsets.UTF_8));

        Files.delete(filePath);
        return url;
    }


    private File convertMultiPartToFile(MultipartFile file) throws IOException {
        //  File convertedFile = new File("/tmp/" + Objects.requireNonNull(file.getOriginalFilename()));
        File convertedFile = new File(Objects.requireNonNull(file.getOriginalFilename()));
        FileOutputStream fos = new FileOutputStream(convertedFile);
        fos.write(file.getBytes());
        fos.close();
        return convertedFile;
    }

    public void deleteFile(String nombre) {
        Bucket bucket = StorageClient.getInstance().bucket(bucketName);
        bucket.get(nombre).delete();
    }


}
