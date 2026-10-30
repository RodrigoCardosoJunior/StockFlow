/*
@Mock	        Cria uma simulação da dependência.
@InjectMocks	Cria o PedidoService usando essas simulações.
when()	        Define como uma simulação deve responder.
assertThrows()	Verifica se uma exceção foi lançada.
verify()	    Confirma se determinado método foi chamado ou não.
*/
package br.com.stockflow.service;

import br.com.stockflow.dto.PedidoCadastroDTO;
import br.com.stockflow.repository.ClienteRepository;
import br.com.stockflow.repository.PedidoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private HistoricoPedidoService historicoPedidoService;

    @InjectMocks
    private PedidoService pedidoService;

    @Test
    void deveImpedirCriacaoQuandoClienteNaoExiste() {

        // Arrange: preparar o cenário
        Long clienteId = 999L;

        PedidoCadastroDTO dto = new PedidoCadastroDTO(clienteId);

        when(clienteRepository.findById(clienteId))
                .thenReturn(Optional.empty());

        // Act + Assert: executar e verificar o resultado
        assertThrows(
                EntityNotFoundException.class,
                () -> pedidoService.criar(dto)
        );

        // Verificar que nada foi salvo
        verify(pedidoRepository, never()).save(any());

        verify(historicoPedidoService, never()).registrar(
                any(), anyString(), anyString(),
                any(), any(), any()
        );
    }
}
