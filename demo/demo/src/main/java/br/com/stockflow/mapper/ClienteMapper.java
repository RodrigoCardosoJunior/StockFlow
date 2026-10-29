package br.com.stockflow.mapper;

import br.com.stockflow.dto.ClienteCadastroDTO;
import br.com.stockflow.dto.ClienteRespostaDTO;
import br.com.stockflow.entity.Cliente;

public class ClienteMapper {

    private ClienteMapper() {
    }

    public static Cliente paraEntidade(ClienteCadastroDTO dto) {
        return new Cliente(
                dto.nome(),
                dto.email(),
                dto.telefone()
        );
    }

    public static ClienteRespostaDTO paraResposta(Cliente cliente) {
        return new ClienteRespostaDTO(
                cliente.getId(),
                cliente.getNome(),
                cliente.getEmail(),
                cliente.getTelefone(),
                cliente.isAtivo(),
                cliente.getDataCriacao()
        );
    }
}
