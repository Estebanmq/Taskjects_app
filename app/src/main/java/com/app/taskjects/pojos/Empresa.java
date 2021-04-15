package com.app.taskjects.pojos;

public class Empresa {

    public String cif;
    public String nombre;
    public String password;
    public String ubicacion;
    public String email;

    public Empresa() { }

    public Empresa(String cif, String nombre, String password, String ubicacion, String email) {
        this.cif = cif;
        this.nombre = nombre;
        this.password = password;
        this.ubicacion = ubicacion;
        this.email = email;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    public String getEmail() { return email; }

    public void setEmail(String email) { this.email = email; }
}
