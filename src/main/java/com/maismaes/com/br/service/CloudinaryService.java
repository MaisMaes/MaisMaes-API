package com.maismaes.com.br.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    public String subirImagem(MultipartFile arquivo) throws IOException {
        Map envioResultado = cloudinary.uploader().upload(arquivo.getBytes(), ObjectUtils.asMap(
                "folder", "fotos-perfil",
                "resource_type", "image",
                "type", "upload"  //upload
        ));
        return (String) envioResultado.get("public_id");
    }


    public String getImagemUrl(String publicId) {
        return cloudinary.url().secure(true).generate(publicId);
    }


    public void deletarImagem(String publicId) throws IOException {
        Map<String, Object> opcoes = ObjectUtils.asMap("invalidate", true);
        cloudinary.uploader().destroy(publicId, opcoes);
    }
}