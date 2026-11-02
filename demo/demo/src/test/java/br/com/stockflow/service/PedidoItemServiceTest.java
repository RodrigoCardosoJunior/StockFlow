package br.com.stockflow.service;

import br.com.stockflow.dto.PedidoItemRespostaDTO;
import br.com.stockflow.entity.*;
import br.com.stockflow.repository.PedidoItemRepository;
import br.com.stockflow.repository.PedidoRepository;
import br.com.stockflow.repository.ProdutoRepository;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import br.com.stockflow.dto.PedidoItemCadastroDTO;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PedidoItemServiceTest {

    @Mock
    private PedidoItemRepository pedidoItemRepository;

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private ProdutoRepository produtoRepository;

    @Mock
    private HistoricoPedidoService historicoPedidoService;

    @InjectMocks
    private PedidoItemService pedidoItemService;

    @Test
    void deveImpedirAdicaoQuandoPedidoNaoExiste() {

        // Arrange
        when(pedidoRepository.findById(1L))
                .thenReturn(Optional.empty());

        PedidoItemCadastroDTO dto =
                new PedidoItemCadastroDTO(1L, 2);

        // Act + Assert
        assertThrows(
                EntityNotFoundException.class,
                () -> pedidoItemService.adicionar(1L, dto)
        );

        verify(produtoRepository, never()).findById(anyLong());

        verify(pedidoItemRepository, never()).save(any(PedidoItem.class));

        verify(historicoPedidoService, never()).registrar(
                any(), anyString(), anyString(),
                any(), any(), any()
        );
    }

    @Test
    void deveImpedirAdicaoQuandoPedidoEstaCancelado() {

        //Arrange
        Cliente cliente = new Cliente(
                "João da Silva",
                "joao@emai.com",
                "44999440044"
        );

        Pedido pedido = new Pedido(cliente);
        pedido.setStatus(StatusPedido.CANCELADO);

        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));

        PedidoItemCadastroDTO dto = new PedidoItemCadastroDTO(1L, 2);

        //Act + Assert
        assertThrows(IllegalStateException.class, () -> pedidoItemService.adicionar(1l, dto));

        verify(produtoRepository, never()).findById(anyLong());
        verify(pedidoItemRepository, never()).save(any(PedidoItem.class));

        verify(historicoPedidoService, never()).registrar(
                any(), anyString(), anyString(),
                any(), any(), any()
        );
    }

    ;

    @Test
    void deveImpedirAdicaoQuandoProdutoNaoExiste() {

        //Arrange
        Cliente cliente = new Cliente(
                "João da Silva",
                "joao@emai.com",
                "44999440044"
        );

        Pedido pedido = new Pedido(cliente);

        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        when(produtoRepository.findById(1l)).thenReturn(Optional.empty());

        PedidoItemCadastroDTO dto = new PedidoItemCadastroDTO(1L, 2);

        //Act + Assert
        assertThrows(EntityNotFoundException.class,
                () -> pedidoItemService.adicionar(1l, dto));

        verify(pedidoItemRepository, never()).save(any(PedidoItem.class));

        verify(historicoPedidoService, never()).registrar(
                any(), anyString(), anyString(),
                any(), any(), any()
        );
    }

    ;

    @Test
    void deveImpedirAdicaoQuandoProdutoEstaInativo() {

        //Arrange
        Cliente cliente = new Cliente(
                "João da Silva",
                "joao@emai.com",
                "44999440044"
        );

        Pedido pedido = new Pedido(cliente);

        Produto produto = new Produto(
                "Teclado",
                "Teclado mecânico",
                new BigDecimal("250.00")
        );

        produto.setAtivo(false);

        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        when(produtoRepository.findById(1l)).thenReturn(Optional.of(produto));

        PedidoItemCadastroDTO dto = new PedidoItemCadastroDTO(1L, 2);

        //Act + Assert
        assertThrows(IllegalStateException.class, () -> pedidoItemService.adicionar(1l, dto));

        verify(pedidoItemRepository, never()).save(any(PedidoItem.class));

        verify(historicoPedidoService, never()).registrar(
                any(), anyString(), anyString(),
                any(), any(), any()
        );
    }

    @Test
    void deveAdicionarProdutoQuandoPedidoEProdutoSaoValidos() {
        //Arrange
        Cliente cliente = new Cliente(
                "João da Silva",
                "joao@emai.com",
                "44999440044"
        );

        Pedido pedido = new Pedido(cliente);

        Produto produto = new Produto(
                "Teclado",
                "Teclado mecânico",
                new BigDecimal("250.00")
        );

        List<PedidoItem> itens = new ArrayList<>();

        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));
        when(pedidoItemRepository.findByPedido_IdAndProduto_Id(1L, 1L)).thenReturn(Optional.empty());
        when(pedidoItemRepository.save(any(PedidoItem.class)))
                .thenAnswer(invocation -> {
                    PedidoItem item = invocation.getArgument(0);
                    itens.add(item);
                    return item;
                });

        when(pedidoItemRepository.findByPedido_Id(any())).thenAnswer(invocation -> itens);

        PedidoItemCadastroDTO dto = new PedidoItemCadastroDTO(1L, 2);

        //Act
        PedidoItemRespostaDTO respostaDTO = pedidoItemService.adicionar(1L, dto);

        //Assert
        assertEquals(2, respostaDTO.quantidade());
        assertEquals(0, new BigDecimal("500.00").compareTo(pedido.getValorTotal()));
        assertEquals(0, new BigDecimal("250.00").compareTo(respostaDTO.precoUnitario()));
        assertEquals(1, itens.size());

        verify(pedidoRepository).save(pedido);

        verify(historicoPedidoService).registrar(
                eq(pedido),
                eq("ITEM_ADICIONADO"),
                anyString(),
                anyString(),
                anyString(),
                isNull()
        );
    }

    @Test
    void deveSomarQuantidadeQuandoProdutoJaExisteNoPedido() {
        // Arrange
        Cliente cliente = new Cliente(
                "João da Silva",
                "joao@email.com",
                "44999998888"
        );

        Pedido pedido = new Pedido(cliente);

        Produto produto = new Produto(
                "Teclado",
                "Teclado mecânico",
                new BigDecimal("250.00")
        );

        PedidoItem itemExistente = new PedidoItem(
                pedido,
                produto,
                2,
                new BigDecimal("250.00")
        );

        List<PedidoItem> itens = new ArrayList<>();
        itens.add(itemExistente);

        pedido.setValorTotal(new BigDecimal("500.00"));

        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));
        when(pedidoItemRepository.findByPedido_IdAndProduto_Id(1L,1L)).thenReturn(Optional.of(itemExistente));
        when(pedidoItemRepository.save(any(PedidoItem.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(pedidoItemRepository.findByPedido_Id(isNull()))
                .thenReturn(itens);

        PedidoItemCadastroDTO dto = new PedidoItemCadastroDTO(1L, 3);

        //Act
        PedidoItemRespostaDTO respostaDTO = pedidoItemService.adicionar(1L, dto);

        //Assert
        assertEquals(5, respostaDTO.quantidade());
        assertEquals(0, new BigDecimal("1250.00").compareTo(pedido.getValorTotal()));
        assertEquals(0, new BigDecimal("250.00").compareTo(respostaDTO.precoUnitario()));
        assertEquals(1, itens.size());

        verify(pedidoItemRepository).save(itemExistente);
        verify(pedidoRepository).save(pedido);

        verify(historicoPedidoService).registrar(
                eq(pedido),
                eq("QUANTIDADE_ALTERADA"),
                anyString(),
                anyString(),
                anyString(),
                isNull()
        );
    }

    @Test
    void deveImpedirRemocaoQuandoPedidoNaoExiste() {
        //Arange
        when(pedidoRepository.findById(1L)).thenReturn(Optional.empty());

        //Act + Assert
        assertThrows(EntityNotFoundException.class,
                () -> pedidoItemService.remover(1L, 1L));

        verify(pedidoRepository).findById(1L);

        verifyNoInteractions(pedidoItemRepository,produtoRepository,historicoPedidoService);
    }

    @Test
    void deveImpedirRemocaoQuandoPedidoCancelado(){
        //Arrange
        Cliente cliente = new Cliente(
                "João da Silva",
                "joao@emai.com",
                "44999440044"
        );

        Pedido pedido = new Pedido(cliente);
        pedido.setStatus(StatusPedido.CANCELADO);

        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));

        //Act + Assert
        assertThrows(IllegalStateException.class, () -> pedidoItemService.remover(1L, 1L));

        verify(pedidoRepository).findById(1L);

        verifyNoInteractions(pedidoItemRepository,produtoRepository,historicoPedidoService);
    }

    @Test
    void deveImpedirRemocaoQuandoPedidoConcluido(){
        //Arrange
        Cliente cliente = new Cliente(
                "João da Silva",
                "joao@emai.com",
                "44999440044"
        );

        Pedido pedido = new Pedido(cliente);
        pedido.setStatus(StatusPedido.CONCLUIDO);

        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));

        //Act + Assert
        assertThrows(IllegalStateException.class, () -> pedidoItemService.remover(1L, 1L));

        verify(pedidoRepository).findById(1L);

        verifyNoInteractions(pedidoItemRepository,produtoRepository,historicoPedidoService);
    }

    @Test
    void deveImpedirRemocaoQuandoPItemNaoExisteNoPedido(){
        //Arrange
        Cliente cliente = new Cliente(
                "João da Silva",
                "joao@emai.com",
                "44999440044"
        );

        Pedido pedido = new Pedido(cliente);

        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        when(pedidoItemRepository.findByPedido_IdAndProduto_Id(1L,1L)).thenReturn(Optional.empty());

        //Act + Assert
            assertThrows(EntityNotFoundException.class, () -> pedidoItemService.remover(1L, 1L));

        verify(pedidoRepository).findById(1L);
        verify(pedidoItemRepository).findByPedido_IdAndProduto_Id(1L,1L);

        verifyNoInteractions(produtoRepository,historicoPedidoService);
    }

    @Test
    void deveRemoverItemERecalcularTotalDoPedido() {
        // Arrange
        Cliente cliente = new Cliente(
                "João da Silva",
                "joao@email.com",
                "44999998888"
        );

        Pedido pedido = new Pedido(cliente);

        Produto produto = new Produto(
                "Teclado",
                "Teclado mecânico",
                new BigDecimal("250.00")
        );

        PedidoItem item = new PedidoItem(
                pedido,
                produto,
                2,
                new BigDecimal("250.00")
        );

        List<PedidoItem> itens = new ArrayList<>();

        pedido.setValorTotal(new BigDecimal("500.00"));

        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        when(pedidoItemRepository.findByPedido_IdAndProduto_Id(1L,1L)).thenReturn(Optional.of(item));
        when(pedidoItemRepository.findByPedido_Id(isNull())).thenReturn(itens);

        //Act
        pedidoItemService.remover(1L, 1L);

        //Assert
        assertEquals(0, BigDecimal.ZERO.compareTo(pedido.getValorTotal()));

        verify(pedidoItemRepository).delete(item);
        verify(pedidoItemRepository).flush();
        verify(pedidoRepository).save(pedido);

        verify(historicoPedidoService).registrar(
                eq(pedido),
                eq("ITEM_REMOVIDO"),
                anyString(),
                anyString(),
                anyString(),
                isNull()
        );
    }

    @Test
    void deveImpedirListagemQuandoPedidoNaoExiste() {
        // Arrange
        when(pedidoRepository.existsById(1L))
                .thenReturn(false);

        // Act + Assert
        assertThrows(EntityNotFoundException.class, () ->
                pedidoItemService.listarPorPedido(1L)
        );

        verify(pedidoRepository).existsById(1L);

        verifyNoInteractions(
                pedidoItemRepository,
                produtoRepository,
                historicoPedidoService
        );
    }

    @Test
    void deveListarItensQuandoPedidoExiste() {
        // Arrange
        Cliente cliente = new Cliente(
                "João da Silva",
                "joao@email.com",
                "44999998888"
        );

        Pedido pedido = new Pedido(cliente);

        Produto produto = new Produto(
                "Teclado",
                "Teclado mecânico",
                new BigDecimal("250.00")
        );

        PedidoItem item = new PedidoItem(
                pedido,
                produto,
                2,
                new BigDecimal("250.00")
        );

        List<PedidoItem> itens = List.of(item);

        when(pedidoRepository.existsById(1L)).thenReturn(true);
        when(pedidoItemRepository.findByPedido_Id(1L)).thenReturn(itens);

        //Act
        List<PedidoItemRespostaDTO> respostaDTO = pedidoItemService.listarPorPedido(1L);

        //Assert
        assertNotNull(respostaDTO);
        assertEquals(1, respostaDTO.size());
        assertEquals(2, respostaDTO.get(0).quantidade());

        verify(pedidoRepository).existsById(1L);
        verify(pedidoItemRepository).findByPedido_Id(1L);
    }

    @Test
    void deveRetornarListaVaziaQuandoPedidoNaoPossuiItens() {
        // Arrange
        when(pedidoRepository.existsById(1L))
                .thenReturn(true);

        when(pedidoItemRepository.findByPedido_Id(1L))
                .thenReturn(List.of());

        // Act
        List<PedidoItemRespostaDTO> respostaDTO =
                pedidoItemService.listarPorPedido(1L);

        // Assert
        assertNotNull(respostaDTO);
        assertTrue(respostaDTO.isEmpty());

        verify(pedidoRepository).existsById(1L);
        verify(pedidoItemRepository).findByPedido_Id(1L);
    }

    @Test
    void deveManterTotalDosItensRestantesAposRemocao() {
        // Arrange
        Cliente cliente = new Cliente(
                "João da Silva",
                "joao@email.com",
                "44999998888"
        );

        Pedido pedido = new Pedido(cliente);

        Produto teclado = new Produto(
                "Teclado",
                "Teclado mecânico",
                new BigDecimal("250.00")
        );

        Produto mouse = new Produto(
                "Mouse",
                "Mouse sem fio",
                new BigDecimal("100.00")
        );

        PedidoItem itemTeclado = new PedidoItem(
                pedido,
                teclado,
                2,
                new BigDecimal("250.00")
        );

        PedidoItem itemMouse = new PedidoItem(
                pedido,
                mouse,
                1,
                new BigDecimal("100.00")
        );

        List<PedidoItem> itensRestantes = List.of(itemMouse);

        pedido.setValorTotal(new BigDecimal("600.00"));

        when(pedidoRepository.findById(1L))
                .thenReturn(Optional.of(pedido));

        when(pedidoItemRepository.findByPedido_IdAndProduto_Id(1L, 1L))
                .thenReturn(Optional.of(itemTeclado));

        when(pedidoItemRepository.findByPedido_Id(isNull()))
                .thenReturn(itensRestantes);

        // Act
        pedidoItemService.remover(1L, 1L);

        // Assert
        assertEquals(
                0,
                new BigDecimal("100.00").compareTo(pedido.getValorTotal())
        );

        verify(pedidoItemRepository).delete(itemTeclado);
        verify(pedidoItemRepository).flush();
        verify(pedidoRepository).save(pedido);
    }

    @Test
    void devePreservarPrecoUnitarioAoAdicionarQuantidadeDeProdutoExistente() {
        // Arrange
        Cliente cliente = new Cliente(
                "João da Silva",
                "joao@email.com",
                "44999998888"
        );

        Pedido pedido = new Pedido(cliente);

        Produto produto = new Produto(
                "Teclado",
                "Teclado mecânico",
                new BigDecimal("300.00")
        );

        PedidoItem itemExistente = new PedidoItem(
                pedido,
                produto,
                1,
                new BigDecimal("250.00")
        );

        List<PedidoItem> itens = new ArrayList<>();
        itens.add(itemExistente);

        pedido.setValorTotal(new BigDecimal("250.00"));

        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));
        when(pedidoItemRepository.findByPedido_IdAndProduto_Id(1L, 1L)).thenReturn(Optional.of(itemExistente));
        when(pedidoItemRepository.save(any(PedidoItem.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(pedidoItemRepository.findByPedido_Id(isNull())).thenReturn(itens);

        PedidoItemCadastroDTO dto = new PedidoItemCadastroDTO(1L, 2);

        //Act
        PedidoItemRespostaDTO respostaDTO = pedidoItemService.adicionar(1L, dto);

        //Assert
        assertEquals(3, respostaDTO.quantidade());
        assertEquals(0, new BigDecimal("250.00").compareTo(respostaDTO.precoUnitario()));
        assertEquals(0, new BigDecimal("750.00").compareTo(pedido.getValorTotal()));
    }

    @Test
    void deveImpedirEstouroNaSomaDaQuantidade() {
        // Arrange
        Cliente cliente = new Cliente(
                "João da Silva",
                "joao@email.com",
                "44999998888"
        );

        Pedido pedido = new Pedido(cliente);

        Produto produto = new Produto(
                "Teclado",
                "Teclado mecânico",
                new BigDecimal("250.00")
        );

        PedidoItem itemExistente = new PedidoItem(
                pedido,
                produto,
                Integer.MAX_VALUE,
                new BigDecimal("250.00")
        );

        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));
        when(pedidoItemRepository.findByPedido_IdAndProduto_Id(1L, 1L)).thenReturn(Optional.of(itemExistente));

        PedidoItemCadastroDTO dto = new PedidoItemCadastroDTO(1L, 1);

        // Act + Assert
        assertThrows(ArithmeticException.class, () -> pedidoItemService.adicionar(1L, dto));
        verify(pedidoItemRepository, never()).save(any());
    }

    @Test
    void deveRecalcularTotalAoAdicionarProdutoEmPedidoComVariosItens() {
        // Arrange
        Cliente cliente = new Cliente(
                "João da Silva",
                "joao@email.com",
                "44999998888"
        );

        Pedido pedido = new Pedido(cliente);

        Produto teclado = new Produto(
                "Teclado",
                "Teclado mecânico",
                new BigDecimal("250.00")
        );

        Produto mouse = new Produto(
                "Mouse",
                "Mouse sem fio",
                new BigDecimal("100.00")
        );

        Produto monitor = new Produto(
                "Monitor",
                "Monitor 24 polegadas",
                new BigDecimal("800.00")
        );

        PedidoItem itemTeclado = new PedidoItem(
                pedido, teclado, 2, new BigDecimal("250.00")
        );

        PedidoItem itemMouse = new PedidoItem(
                pedido, mouse, 1, new BigDecimal("100.00")
        );

        List<PedidoItem> itens = new ArrayList<>();
        itens.add(itemTeclado);
        itens.add(itemMouse);

        pedido.setValorTotal(new BigDecimal("600.00"));

        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        when(produtoRepository.findById(3L)).thenReturn(Optional.of(monitor));
        when(pedidoItemRepository.findByPedido_IdAndProduto_Id(1L, 3L)).thenReturn(Optional.empty());
        when(pedidoItemRepository.save(any(PedidoItem.class)))
                .thenAnswer(invocation -> {
                    PedidoItem novoItem = invocation.getArgument(0);
                    itens.add(novoItem);
                    return novoItem;
                });

        when(pedidoItemRepository.findByPedido_Id(isNull())).thenReturn(itens);

        PedidoItemCadastroDTO dto = new PedidoItemCadastroDTO(3L, 1);

        // Act
        PedidoItemRespostaDTO respostaDTO = pedidoItemService.adicionar(1L, dto);

        // Assert
        assertNotNull(respostaDTO);
        assertEquals(3, itens.size());

        assertEquals(0, new BigDecimal("1400.00").compareTo(pedido.getValorTotal()));

        verify(pedidoItemRepository).save(any(PedidoItem.class));
        verify(pedidoRepository).save(pedido);
    }
}