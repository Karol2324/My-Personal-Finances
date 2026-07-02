package com.example.mypersonalfinances.model;

/**
 * Modelo de datos que representa una transacción financiera.
 * Corresponde a un registro de la tabla 'transacciones' en SQLite.
 */
public class Transaccion {

    /** Identificador único generado automáticamente por la base de datos. */
    private int id;

    /** Descripción de la transacción (ej. "Salario", "Supermercado"). */
    private String concepto;

    /** Cantidad de dinero asociada a la transacción. */
    private double monto;

    /** Tipo de movimiento: "Ingreso" o "Gasto". */
    private String tipo;

    /**
     * Constructor vacío requerido al reconstruir objetos desde un Cursor de SQLite.
     */
    public Transaccion() {
    }

    /**
     * Constructor para crear una transacción nueva (sin id, se asigna al insertar).
     */
    public Transaccion(String concepto, double monto, String tipo) {
        this.concepto = concepto;
        this.monto = monto;
        this.tipo = tipo;
    }

    /**
     * Constructor completo usado al leer o actualizar registros existentes.
     */
    public Transaccion(int id, String concepto, double monto, String tipo) {
        this.id = id;
        this.concepto = concepto;
        this.monto = monto;
        this.tipo = tipo;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
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

    /** Indica si la transacción es un ingreso de dinero. */
    public boolean esIngreso() {
        return "Ingreso".equals(tipo);
    }

    /** Indica si la transacción es un gasto. */
    public boolean esGasto() {
        return "Gasto".equals(tipo);
    }
}
