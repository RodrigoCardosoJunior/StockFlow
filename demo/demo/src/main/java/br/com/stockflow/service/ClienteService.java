package br.com.stockflow.service;

import br.com.stockflow.dto.ClienteCadastroDTO;
import br.com.stockflow.mapper.ClienteMapper;
import br.com.stockflow.dto.ClienteRespostaDTO;
import br.com.stockflow.entity.Cliente;
import br.com.stockflow.repository.ClienteRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;

    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    @Transactional
    public ClienteRespostaDTO cadastrar(ClienteCadastroDTO dto) {
        if (clienteRepository.existsByEmail(dto.email())) {
            throw new IllegalArgumentException(
                    "Já existe um cliente cadastrado com esse e-mail."
            );
        }

        Cliente cliente = ClienteMapper.paraEntidade(dto);
        Cliente clienteSalvo = clienteRepository.save(cliente);

        return ClienteMapper.paraResposta(clienteSalvo);
    }

    @Transactional(readOnly = true)
    public List<ClienteRespostaDTO> listarTodos() {
        return clienteRepository.findAll()
                .stream()
                .map(ClienteMapper::paraResposta)
                .toList();
    }

    @Transactional(readOnly = true)
    public ClienteRespostaDTO buscarPorId(Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Cliente não encontrado com o ID: " + id
                ));

        return ClienteMapper.paraResposta(cliente);
    }

    @Transactional
    public void desativar(Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Cliente não encontrado com o ID: " + id
                ));

        cliente.setAtivo(false);
    }

    @Transactional
    public void ativar(Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Cliente não encontrado com o ID: " + id
                ));

        cliente.setAtivo(true);
    }
}
