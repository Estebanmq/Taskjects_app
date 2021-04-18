package com.app.taskjects.pojos;

public class Empleado {

    public String nif;
    public String nombre;
    public String apellidos;
    public String email;
    public String password;
    public String uidEmpresa;
    public String categoria;
    public String uid;

    public Empleado() { }

    public Empleado(String nif, String nombre, String apellidos, String email, String password, String uidEmpresa, String categoria, String uid) {
        this.nif = nif;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.email = email;
        this.password = password;
        this.uidEmpresa = uidEmpresa;
        this.categoria = categoria;
        this.uid = uid;
    }

    public String getNif() {
        return nif;
    }

    public void setNif(String nif) {
        this.nif = nif;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) { this.apellidos = apellidos; }

    public String getEmail() { return email; }

    public void setEmail(String email) { this.email = email; }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUidEmpresa() {
        return uidEmpresa;
    }

    public void setUidEmpresa(String uidEmpresa) {
        this.uidEmpresa = uidEmpresa;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}

