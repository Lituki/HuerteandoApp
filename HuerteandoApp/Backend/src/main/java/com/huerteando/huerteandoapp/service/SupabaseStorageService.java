package com.huerteando.huerteandoapp.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
public class SupabaseStorageService {

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.key}")
    private String apiKey;

    @Value("${supabase.bucket}")
    private String bucket;

    // Método genérico: sube un fichero a una carpeta del bucket y devuelve la URL pública.
    private String subirEnCarpeta(MultipartFile file, String carpeta, String prefijoNombre) {
        try {
            // Nombre único para evitar colisiones.
            String fileName = prefijoNombre + UUID.randomUUID() + "_" + file.getOriginalFilename();

            // Normalizamos carpeta para que siempre sea "/carpeta/".
            String folder = carpeta;
            if (!folder.startsWith("/")) folder = "/" + folder;
            if (!folder.endsWith("/")) folder = folder + "/";

            // URL de subida a Supabase Storage.
            String uploadUrl = supabaseUrl
                    + "/storage/v1/object/"
                    + bucket
                    + folder
                    + fileName;

            // Cabeceras: usamos el MIME real si viene informado.
            HttpHeaders headers = new HttpHeaders();
            if (file.getContentType() != null && !file.getContentType().isBlank()) {
                headers.setContentType(MediaType.parseMediaType(file.getContentType()));
            } else {
                headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            }
            headers.set("apikey", apiKey);
            headers.setBearerAuth(apiKey);

            HttpEntity<byte[]> request = new HttpEntity<>(file.getBytes(), headers);

            RestTemplate restTemplate = new RestTemplate();
            restTemplate.exchange(uploadUrl, HttpMethod.POST, request, String.class);

            // URL pública para guardar en BD.
            return supabaseUrl
                    + "/storage/v1/object/public/"
                    + bucket
                    + folder
                    + fileName;

        } catch (Exception e) {
            throw new RuntimeException("Error subiendo archivo", e);
        }
    }

    public String subirImagen(MultipartFile file) {
        // Imágenes de observaciones -> carpeta "observaciones/"
        return subirEnCarpeta(file, "observaciones", "");
    }

    public String subirAvatar(MultipartFile file) {
        // Avatares -> carpeta "avatars/"
        return subirEnCarpeta(file, "avatars", "avatar_");
    }
}