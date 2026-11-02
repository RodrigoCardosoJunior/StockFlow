/*
@Mock	        Cria uma simulação da dependência.
@InjectMocks	Cria o PedidoService usando essas simulações.
when()	        Define como uma simulação deve responder.
assertThrows()	Verifica se uma exceção foi lançada.
verify()	    Confirma se determinado método foi chamado ou não.
*/
package br.com.stockflow.service;

import br.com.stockflow.dto.PedidoCadastroDTO;
import br.com.stockflow.entity.Cliente;
import br.com.stockflow.repository.ClienteRepository;
import br.com.stockflow.repository.PedidoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import br.com.stockflow.entity.Pedido;
import br.com.stockflow.entity.StatusPedido;
import br.com.stockflow.dto.PedidoRespostaDTO;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;

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

    @Test
    void deveImpedirCriacaoQuandoClienteEstaInativo() {

        // Arrange
        Long clienteId = 10L;

        Cliente cliente = mock(Cliente.class);

        when(clienteRepository.findById(clienteId))
                .thenReturn(Optional.of(cliente));

        when(cliente.isAtivo()).thenReturn(false);

        PedidoCadastroDTO dto = new PedidoCadastroDTO(clienteId);

        // Act + Assert
        assertThrows(
                IllegalStateException.class,
                () -> pedidoService.criar(dto)
        );

        // Verificar que nada foi salvo
        verify(pedidoRepository, never()).save(any());

        verify(historicoPedidoService, never()).registrar(
                any(), anyString(), anyString(),
                any(), any(), any()
        );
    }

    @Test
    void deveCriarPedidoQuandoClienteEstaAtivo() {

        // Arrange
        Cliente cliente = new Cliente(
                "João da Silva",
                "joao@email.com",
                "44999998888"
        );

        when(clienteRepository.findById(10L))
                .thenReturn(Optional.of(cliente));

        when(pedidoRepository.save(any(Pedido.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PedidoCadastroDTO dto = new PedidoCadastroDTO(10L);

        // Act
        PedidoRespostaDTO resposta = pedidoService.criar(dto);

        // Assert
        assertNotNull(resposta);
        assertEquals(StatusPedido.PENDENTE, resposta.status());
        assertEquals(BigDecimal.ZERO, resposta.valorTotal());

        verify(pedidoRepository, times(1)).save(any(Pedido.class));

        verify(historicoPedidoService, times(1)).registrar(
                any(Pedido.class),
                eq("PEDIDO_CRIADO"),
                anyString(),
                isNull(),
                anyString(),
                isNull()
        );
    }

    @Test
    void deveImpedirTransicaoInvalidaDeStatus() {

        // Arrange
        Cliente cliente = new Cliente(
                "João da Silva",
                "joao@email.com",
                "44999998888"
        );

        Pedido pedido = new Pedido(cliente);

        when(pedidoRepository.findById(1L))
                .thenReturn(Optional.of(pedido));

        // Act + Assert
        assertThrows(
                IllegalStateException.class,
                () -> pedidoService.alterarStatus(
                        1L,
                        StatusPedido.CONCLUIDO
                )
        );

        // Verificar que o pedido não foi alterado ou salvo
        assertEquals(StatusPedido.PENDENTE, pedido.getStatus());

        verify(pedidoRepository, never()).save(any(Pedido.class));

        verify(historicoPedidoService, never()).registrar(
                any(), anyString(), anyString(),
                any(), any(), any()
        );
    }

    @Test
    void deveAlterarStatusQuandoTransicaoForValida() {

        // Arrange
        Cliente cliente = new Cliente(
                "João da Silva",
                "joao@email.com",
                "44999998888"
        );

        Pedido pedido = new Pedido(cliente);

        when(pedidoRepository.findById(1L))
                .thenReturn(Optional.of(pedido));

        when(pedidoRepository.save(any(Pedido.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        PedidoRespostaDTO resposta = pedidoService.alterarStatus(
                1L,
                StatusPedido.CONFIRMADO
        );

        // Assert
        assertEquals(StatusPedido.CONFIRMADO, resposta.status());
        assertEquals(StatusPedido.CONFIRMADO, pedido.getStatus());

        verify(pedidoRepository).save(pedido);

        verify(historicoPedidoService).registrar(
                eq(pedido),
                eq("STATUS_ALTERADO"),
                anyString(),
                eq("{\"status\":\"PENDENTE\"}"),
                eq("{\"status\":\"CONFIRMADO\"}"),
                isNull()
        );
    }

}
