package br.com.julio.pi.backoffice_users.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.julio.pi.backoffice_users.dto.ProdutoCreateDTO;
import br.com.julio.pi.backoffice_users.dto.ProdutoResponseDTO;
import br.com.julio.pi.backoffice_users.model.ImagemProduto;
import br.com.julio.pi.backoffice_users.model.Produto;
import br.com.julio.pi.backoffice_users.repository.ImagemProdutoRepository;
import br.com.julio.pi.backoffice_users.repository.ProdutoRepository;
import br.com.julio.pi.backoffice_users.service.ProdutoService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/produtos")
public class ProdutoController {

    private final ProdutoService produtoService;
    private final ProdutoRepository produtoRepository;
    private final ImagemProdutoRepository imagemRepository;

    public ProdutoController(ProdutoService produtoService,
                             ProdutoRepository produtoRepository,
                             ImagemProdutoRepository imagemRepository) {
        this.produtoService = produtoService;
        this.produtoRepository = produtoRepository;
        this.imagemRepository = imagemRepository;
    }

    /* =================== PRODUTOS =================== */

    @GetMapping
    public List<ProdutoResponseDTO> listar() {
        return produtoRepository.findAllByOrderByIdDesc().stream().map(p -> {
            String imgRaw = imagemRepository.findFirstByProdutoIdAndPrincipalTrue(p.getId())
                .map(ImagemProduto::getCaminhoDestino)
                .orElseGet(() -> imagemRepository.findFirstByProdutoIdOrderByIdAsc(p.getId())
                    .map(ImagemProduto::getCaminhoDestino)
                    .orElse(null));
            String img = normalizeImg(p.getId(), imgRaw);
            return ProdutoResponseDTO.of(p, img);
        }).toList();
    }

    @GetMapping("/{id}")
    public ProdutoResponseDTO buscar(@PathVariable Long id) {
        Produto p = produtoService.buscarPorId(id);
        String imgRaw = imagemRepository.findFirstByProdutoIdAndPrincipalTrue(id)
            .map(ImagemProduto::getCaminhoDestino)
            .orElse(null);
        String img = normalizeImg(id, imgRaw);
        return ProdutoResponseDTO.of(p, img);
    }

    @PostMapping
    public ResponseEntity<ProdutoResponseDTO> criar(@Valid @RequestBody ProdutoCreateDTO dto) {
        Produto salvo = produtoService.incluir(dto.toEntity());
        return ResponseEntity.created(URI.create("/api/produtos/" + salvo.getId()))
            .body(ProdutoResponseDTO.of(salvo, null));
    }

    public static record NovaImagemDTO(String caminhoOrigem, boolean principal) {}

    public static record ImagemDTO(Long id, String url, boolean principal) {}

    @GetMapping("/{id}/imagens")
    public List<ImagemDTO> listarImagens(@PathVariable Long id) {
        return imagemRepository.findByProdutoIdOrderByPrincipalDescIdAsc(id).stream()
            .map(i -> new ImagemDTO(i.getId(), normalizeImg(id, i.getCaminhoDestino()), i.isPrincipal()))
            .toList();
    }

    @PostMapping("/{id}/imagens")
    public ImagemProduto adicionarImagem(@PathVariable Long id, @RequestBody NovaImagemDTO req) {
        return produtoService.adicionarImagemFromOrigem(id, req.caminhoOrigem(), req.principal());
    }

    @DeleteMapping("/{id}/imagens/{imgId}")
    public ResponseEntity<Void> removerImagem(@PathVariable Long id, @PathVariable Long imgId) {
        boolean ok = produtoService.removerImagem(id, imgId);
        return ok ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    private static String normalizeImg(Long produtoId, String path) {
        if (path == null || path.isBlank()) return null;
        String p = path.replace('\\', '/');
        if (p.startsWith("/")) p = p.substring(1); 
        if (!p.contains("/")) {
            p = "imagens/" + produtoId + "/" + p;
        }
        return "/" + p; 
    }
}
