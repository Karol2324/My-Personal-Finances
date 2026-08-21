package com.example.mypersonalfinances.model;

public class Transaccion {

    private String id; // ID de documento Firestore (String)
    private String concepto;
    private double monto;
    private String tipo; // "Ingreso" o "Gasto"

    // Constructor vacío obligatorio para Firestore
    public Transaccion() {
    }

    // Constructor para crear nuevas transacciones
    public Transaccion(String concepto, double monto, String tipo) {
        this.concepto = concepto;
        this.monto = monto;
        this.tipo = tipo;
    }

    // Constructor completo
    public Transaccion(String id, String concepto, double monto, String tipo) {
        this.id = id;
        this.concepto = concepto;
        this.monto = monto;
        this.tipo = tipo;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getConcepto() {
        return concepto;
    }

    public void setConcepto(String concepto) {
        this.concepto = concepto;
    }

    public double getMonto() {
        return monto;
    }

    public void setMonto(double monto) {
        this.monto = monto;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public boolean esIngreso() {
        return "Ingreso".equalsIgnoreCase(tipo);
    }

    public boolean esGasto() {
        return "Gasto".equalsIgnoreCase(tipo);
    }
}