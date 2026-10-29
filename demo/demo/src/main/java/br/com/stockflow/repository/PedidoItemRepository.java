package br.com.stockflow.repository;

import br.com.stockflow.entity.PedidoItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PedidoItemRepository extends JpaRepository<PedidoItem, Long> {

    List<PedidoItem> findByPedido_Id(Long pedidoId);

    Optional<PedidoItem> findByPedido_IdAndProduto_Id(Long pedidoId, Long produtoId);
}
