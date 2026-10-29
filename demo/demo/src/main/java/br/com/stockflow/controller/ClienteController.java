package br.com.stockflow.controller;

import br.com.stockflow.dto.ClienteCadastroDTO;
import br.com.stockflow.dto.ClienteRespostaDTO;
import br.com.stockflow.service.ClienteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ClienteRespostaDTO cadastrar(
            @Valid @RequestBody ClienteCadastroDTO dto) {
        return clienteService.cadastrar(dto);
    }

    @GetMapping
    public List<ClienteRespostaDTO> listarTodos() {
        return clienteService.listarTodos();
    }

    @GetMapping("/{id}")
    public ClienteRespostaDTO buscarPorId(@PathVariable Long id) {
        return clienteService.buscarPorId(id);
    }

    @PatchMapping("/{id}/desativar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void desativar(@PathVariable Long id) {
        clienteService.desativar(id);
    }

    @PatchMapping("/{id}/ativar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void ativar(@PathVariable Long id) {
        clienteService.ativar(id);
    }
}
