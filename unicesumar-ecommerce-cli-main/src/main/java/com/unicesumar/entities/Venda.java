package com.unicesumar.entities;

import java.util.List;
import java.util.UUID;

public class Venda {
    private UUID uuid;
    private User usuario;
    private List<Product> produtos;
    private String formaDePagamento;
    private double valorTotal;

    public Venda(User usuario, List<Product> produtos, String formaDePagamento) {
        this.uuid = UUID.randomUUID();
        this.usuario = usuario;
        this.produtos = produtos;
        this.formaDePagamento = formaDePagamento;
        calcularTotal();
    }

    private void calcularTotal() {
        this.valorTotal = produtos.stream()
                .mapToDouble(Product::getPrice)
                .sum();
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public User getUsuario() {
        return usuario;
    }

    public void setUsuario(User usuario) {
        this.usuario = usuario;
    }

    public List<Product> getProdutos() {
        return produtos;
    }

    public void setProdutos(List<Product> produtos) {
        this.produtos = produtos;
        calcularTotal();
    }

    public String getFormaDePagamento() {
        return formaDePagamento;
    }

    public void setFormaDePagamento(String formaDePagamento) {
        this.formaDePagamento = formaDePagamento;
    }

    public double getValorTotal() {
        return valorTotal;
    }

    @Override
    public String toString() {
        return "Venda{" +
                "uuid=" + uuid +
                ", usuario=" + usuario.getName() +
                ", produtos=" + produtos.size() +
                ", formaDePagamento='" + formaDePagamento + '\'' +
                ", valorTotal=" + valorTotal +
                '}';
    }
}