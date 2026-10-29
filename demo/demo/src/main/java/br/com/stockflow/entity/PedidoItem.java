package br.com.stockflow.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;

@Entity
@Table(
        name = "pedido_itens",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_pedido_item_produto",
                        columnNames = {"pedido_id", "produto_id"}
                )
        }
)
public class PedidoItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pedido_id", nullable = false)
    private Pedido pedido;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

    @Column(name = "quantidade", nullable = false)
    private Integer quantidade;

    @Column(
            name = "preco_unitario",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal precoUnitario;

    protected PedidoItem() {
    }

    public PedidoItem(
            Pedido pedido,
            Produto produto,
            Integer quantidade,
            BigDecimal precoUnitario
    ) {
        this.pedido = pedido;
        this.produto = produto;
        this.quantidade = quantidade;
        this.precoUnitario = precoUnitario;
    }

    @PrePersist
    protected void prePersist() {
        if (quantidade == null || quantidade <= 0) {
            throw new IllegalArgumentException(
                    "A quantidade deve ser maior que zero."
            );
        }

        if (precoUnitario == null
                || precoUnitario.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "O preço unitário não pode ser negativo."
            );
        }
    }

    public Long getId() {
        return id;
    }

    public Pedido getPedido() {
        return pedido;
    }

    public void setPedido(Pedido pedido) {
        this.pedido = pedido;
    }

    public Produto getProduto() {
        return produto;
    }

    public void setProduto(Produto produto) {
        this.produto = produto;
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
    }

    public BigDecimal getPrecoUnitario() {
        return precoUnitario;
    }

    public void setPrecoUnitario(BigDecimal precoUnitario) {
        this.precoUnitario = precoUnitario;
    }

    public BigDecimal calcularSubtotal() {
        if (quantidade == null || precoUnitario == null) {
            return BigDecimal.ZERO;
        }

        return precoUnitario.multiply(
                BigDecimal.valueOf(quantidade)
        );
    }
}
