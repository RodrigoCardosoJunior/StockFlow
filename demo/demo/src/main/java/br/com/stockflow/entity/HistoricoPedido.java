package br.com.stockflow.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "historico_pedidos")
public class HistoricoPedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pedido_id", nullable = false)
    private Pedido pedido;

    @Column(name = "tipo_evento", nullable = false, length = 40)
    private String tipoEvento;

    @Column(name = "descricao", nullable = false, length = 500)
    private String descricao;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "dados_anteriores", columnDefinition = "jsonb")
    private String dadosAnteriores;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "dados_novos", columnDefinition = "jsonb")
    private String dadosNovos;

    @Column(name = "usuario_responsavel", length = 100)
    private String usuarioResponsavel;

    @Column(name = "data_evento", nullable = false, updatable = false)
    private LocalDateTime dataEvento;

    protected HistoricoPedido() {
    }

    public HistoricoPedido(
            Pedido pedido,
            String tipoEvento,
            String descricao,
            String dadosAnteriores,
            String dadosNovos,
            String usuarioResponsavel) {
        this.pedido = pedido;
        this.tipoEvento = tipoEvento;
        this.descricao = descricao;
        this.dadosAnteriores = dadosAnteriores;
        this.dadosNovos = dadosNovos;
        this.usuarioResponsavel = usuarioResponsavel;
    }

    @PrePersist
    protected void prePersist() {
        if (dataEvento == null) {
            dataEvento = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public Pedido getPedido() {
        return pedido;
    }

    public String getTipoEvento() {
        return tipoEvento;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getDadosAnteriores() {
        return dadosAnteriores;
    }

    public String getDadosNovos() {
        return dadosNovos;
    }

    public String getUsuarioResponsavel() {
        return usuarioResponsavel;
    }

    public LocalDateTime getDataEvento() {
        return dataEvento;
    }
}
