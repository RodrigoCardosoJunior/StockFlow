package br.com.stockflow.repository;

import br.com.stockflow.entity.HistoricoPedido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistoricoPedidoRepository
        extends JpaRepository<HistoricoPedido, Long> {

    List<HistoricoPedido> findByPedido_IdOrderByDataEventoDesc(Long pedidoId);
}
