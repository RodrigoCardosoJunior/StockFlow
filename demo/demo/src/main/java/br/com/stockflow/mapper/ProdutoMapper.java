package br.com.stockflow.mapper;

import br.com.stockflow.dto.ProdutoCadastroDTO;
import br.com.stockflow.dto.ProdutoRespostaDTO;
import br.com.stockflow.entity.Produto;

public class ProdutoMapper {

    private ProdutoMapper() {
    }

    public static Produto paraEntidade(ProdutoCadastroDTO dto) {
        return new Produto(
                dto.nome(),
                dto.descricao(),
                dto.preco()
        );
    }

    public static ProdutoRespostaDTO paraResposta(Produto produto) {
        return new ProdutoRespostaDTO(
                produto.getId(),
                produto.getNome(),
                produto.getDescricao(),
                produto.getPreco(),
                produto.isAtivo(),
                produto.getDataCriacao()
        );
    }
}
