 package com.maismaes.com.br.controller;

import com.mongodb.client.gridfs.model.GridFSFile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.core.io.Resource;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/arquivo")
@RequiredArgsConstructor
public class ArquivoController {

    private final GridFsTemplate gridFsTemplate;

    /**
     * Armazena um arquivo no MongoDB GridFS e retorna seu ID.
     * O ID retornado deve ser enviado como {@code fileId} na mensagem WebSocket.
     *
     * <p>POST /arquivo/upload
     */
    @PostMapping("/upload")
    public ResponseEntity<String> upload(@RequestParam("file") MultipartFile file) throws IOException {
        log.info("[Arquivo] Upload iniciado | Nome: {} | Tipo: {} | Tamanho: {} bytes",
                file.getOriginalFilename(), file.getContentType(), file.getSize());

        ObjectId id = gridFsTemplate.store(
                file.getInputStream(),
                file.getOriginalFilename(),
                file.getContentType()
        );

        log.info("[Arquivo] Upload concluído | GridFS ID: {}", id);
        return ResponseEntity.ok(id.toString());
    }

    /**
     * Serve o arquivo armazenado no GridFS pelo seu ID.
     * O Content-Type original é preservado para que o browser possa renderizá-lo inline
     * (imagens, áudios, etc).
     *
     * <p>GET /arquivo/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Resource> download(@PathVariable String id) throws IOException {
        log.info("[Arquivo] Download solicitado | ID: {}", id);

        GridFSFile gridFSFile = gridFsTemplate.findOne(
                new Query(Criteria.where("_id").is(new ObjectId(id)))
        );

        if (gridFSFile == null) {
            log.warn("[Arquivo] Arquivo não encontrado | ID: {}", id);
            return ResponseEntity.notFound().build();
        }

        GridFsResource resource = gridFsTemplate.getResource(gridFSFile);

        String contentType = resource.getContentType();
        if (contentType == null || contentType.isBlank()) {
            contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }

        log.info("[Arquivo] Servindo arquivo | ID: {} | Nome: {} | ContentType: {}",
                id, gridFSFile.getFilename(), contentType);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                // "inline" faz o browser renderizar diretamente (imagem/áudio); troca por
                // "attachment" se quiser forçar download
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + gridFSFile.getFilename() + "\"")
                .body(resource);
    }
}

