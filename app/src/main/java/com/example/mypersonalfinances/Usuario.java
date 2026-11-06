package com.example.mypersonalfinances;

public class Usuario {
    private String idDocumento;
    private String nombre;
    private String correo;
    private String rol;

    // CONSTRUCTOR PÚBLICO VACÍO (Obligatorio para Firestore)
    public Usuario() {}

    // Constructor con parámetros
    public Usuario(String idDocumento, String nombre, String correo, String rol) {
        this.idDocumento = idDocumento;
        this.nombre = nombre;
        this.correo = correo;
        this.rol = rol;
    }

    // Getters
    public String getIdDocumento() { return idDocumento; }
    public String getNombre() { return nombre; }
    public String getCorreo() { return correo; }
    public String getRol() { return rol; }

    // Setters
    public void setIdDocumento(String idDocumento) { this.idDocumento = idDocumento; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setCorreo(String correo) { this.correo = correo; }
    public void setRol(String rol) { this.rol = rol; }
}