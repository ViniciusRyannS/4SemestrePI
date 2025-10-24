package br.com.julio.pi.backoffice_users.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ImageStorageUtil {

    private final Path baseDir;
    private final String baseDirName; // ex.: "imagens"

    public ImageStorageUtil(@Value("${app.images.base-dir:imagens}") String baseDir) {
        this.baseDir = Paths.get(baseDir).toAbsolutePath().normalize();
        this.baseDirName = Paths.get(baseDir).getFileName().toString().replace('\\','/'); // "imagens"
    }

    /**
     * Copia origem -> <baseDir>/<produtoId>/<nomeUnico.ext>
     * e retorna o CAMINHO WEB começando com "/imagens/...".
     * Ex.: "/imagens/5/123abc.jpg"
     */
    public String copyForProduct(Long produtoId, String origemAbsoluta) throws IOException {
        if (produtoId == null) throw new IllegalArgumentException("produtoId é obrigatório");
        if (origemAbsoluta == null || origemAbsoluta.isBlank()) throw new IllegalArgumentException("Caminho de origem é obrigatório");

        Path src = Paths.get(origemAbsoluta);
        if (!Files.exists(src) || !Files.isReadable(src)) {
            throw new IOException("Arquivo origem não encontrado ou sem permissão: " + src.toAbsolutePath());
        }

        String ext = guessExtension(src.getFileName().toString());
        String nomeUnico = gerarNomeUnico(ext);

        Path pastaProduto = baseDir.resolve(String.valueOf(produtoId));
        Files.createDirectories(pastaProduto);

        Path destino = pastaProduto.resolve(nomeUnico);
        int tentativa = 0;
        while (Files.exists(destino) && tentativa < 3) {
            nomeUnico = gerarNomeUnico(ext);
            destino = pastaProduto.resolve(nomeUnico);
            tentativa++;
        }

        Files.copy(src, destino, StandardCopyOption.REPLACE_EXISTING);

        // CAMINHO WEB (com barra inicial) -> "/imagens/{id}/{arquivo}"
        String webPath = "/" + baseDirName + "/" + produtoId + "/" + destino.getFileName().toString();
        return webPath.replace('\\', '/');
    }

    private String gerarNomeUnico(String ext) {
        String base = Instant.now().toEpochMilli() + "-" + UUID.randomUUID().toString().substring(0, 8);
        return (ext == null || ext.isBlank()) ? base : base + "." + ext.toLowerCase(Locale.ROOT);
    }

    private String guessExtension(String filename) {
        int i = filename.lastIndexOf('.');
        return (i > 0 && i < filename.length() - 1) ? filename.substring(i + 1) : "";
    }
}
