package br.com.julio.pi.backoffice_users.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class FileStorageService {

    /**
     * Base pública para servir imagens via HTTP.
     * Se for "imagens", com a config do application.properties (ver seção 4),
     * os arquivos ficarão em ./imagens e serão servidos em /imagens/**.
     */
    private final Path baseDir;

    public FileStorageService(@Value("${app.images.base-dir:imagens}") String baseDir) {
        this.baseDir = Path.of(baseDir).toAbsolutePath().normalize();
    }

    /** Garante que a pasta exista. */
    private void ensureDir(Path dir) throws IOException {
        if (!Files.exists(dir)) Files.createDirectories(dir);
    }

    /** Slug simples pro nome do arquivo (sem acentos/espaços). */
    private String slug(String s) {
        if (!StringUtils.hasText(s)) return "img";
        String n = Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        n = n.replaceAll("[^a-zA-Z0-9._-]+", "-").replaceAll("[-]{2,}", "-");
        return n.toLowerCase();
    }

    /**
     * Copia o arquivo de origem (caminho local) para a pasta pública do produto:
     *  ./imagens/{produtoId}/arquivo-YYYYMMDDHHmmss.ext
     * @return caminho relativo público ex.: "imagens/{produtoId}/arquivo-...ext"
     */
    public String copyToProductDir(long produtoId, String caminhoOrigem) throws IOException {
        if (!StringUtils.hasText(caminhoOrigem)) {
            throw new IOException("Caminho de origem vazio");
        }
        Path src = Path.of(caminhoOrigem);
        if (!Files.exists(src) || !Files.isRegularFile(src)) {
            throw new IOException("Arquivo de origem não encontrado: " + caminhoOrigem);
        }

        String original = src.getFileName().toString();
        String baseName;
        String ext = "";
        int dot = original.lastIndexOf('.');
        if (dot > 0) {
            baseName = original.substring(0, dot);
            ext = original.substring(dot);
        } else {
            baseName = original;
        }

        String stamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String fileName = slug(baseName) + "-" + stamp + ext;

        Path productDir = baseDir.resolve(String.valueOf(produtoId));
        ensureDir(productDir);

        Path dest = productDir.resolve(fileName);
        Files.copy(src, dest, StandardCopyOption.REPLACE_EXISTING);

        // caminho relativo público
        return "imagens/" + produtoId + "/" + fileName;
    }

    /** Tenta deletar o arquivo físico relativo à base (silencioso em caso de erro). */
    public void tryDeleteRelative(String relativePath) {
        if (!StringUtils.hasText(relativePath)) return;
        Path rel = Path.of(relativePath.replace("\\", "/"));
        // tira / inicial, se existir
        String norm = rel.toString().startsWith("/") ? rel.toString().substring(1) : rel.toString();
        Path file = baseDir.getParent() != null
                ? baseDir.getParent().resolve(norm) // caso baseDir seja ./imagens (base é .)
                : Path.of(norm);
        try {
            Files.deleteIfExists(file.toAbsolutePath().normalize());
        } catch (Exception ignored) {}
    }
}
