package br.com.stockflow.service;

import br.com.stockflow.dto.ProdutoCadastroDTO;
import br.com.stockflow.mapper.ProdutoMapper;
import br.com.stockflow.dto.ProdutoRespostaDTO;
import br.com.stockflow.entity.Produto;
import br.com.stockflow.repository.ProdutoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProdutoService {

    private final ProdutoRepository produtoRepository;

    public ProdutoService(ProdutoRepository produtoRepository) {
        this.produtoRepository = produtoRepository;
    }

    @Transactional
    public ProdutoRespostaDTO cadastrar(ProdutoCadastroDTO dto) {
        Produto produto = ProdutoMapper.paraEntidade(dto);
        Produto produtoSalvo = produtoRepository.save(produto);

        return ProdutoMapper.paraResposta(produtoSalvo);
    }

    @Transactional(readOnly = true)
    public List<ProdutoRespostaDTO> listarTodos() {
        return produtoRepository.findAll()
                .stream()
                .map(ProdutoMapper::paraResposta)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProdutoRespostaDTO buscarPorId(Long id) {
        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Produto não encontrado com o ID: " + id
                ));

        return ProdutoMapper.paraResposta(produto);
    }

    @Transactional
    public void desativar(Long id) {
        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Produto não encontrado com o ID: " + id
                ));

        produto.setAtivo(false);
    }

    @Transactional
    public void ativar(Long id) {
        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Produto não encontrado com o ID: " + id
                ));

        produto.setAtivo(true);
    }
}
