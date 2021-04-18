package com.app.taskjects.pojos;

public class Empresa {

    public String cif;
    public String nombre;
    public String direccion;
    public String email;
    public String password;
    public String uid;

    public Empresa() { }

    public Empresa(String cif, String nombre, String direccion, String email, String password, String uid) {
        this.cif = cif;
        this.nombre = nombre;
        this.direccion = direccion;
        this.email = email;
        this.password = password;
        this.uid = uid;
    }

    public String getCif() {
        return cif;
    }

    public void setCif(String cif) {
        this.cif = cif;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getEmail() { return email; }

    public void setEmail(String email) { this.email = email; }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
