package com.app.taskjects.pojos;

import com.google.firebase.firestore.DocumentId;

public class Empresa {

    @DocumentId
    private String uid; // uid del registro en BD

    private String cif;
    private String nombre;
    private String direccion;
    private String email;
    private String password;
    private String uidAuth;

    public Empresa() { }

    public Empresa(String cif, String nombre, String direccion, String email, String password, String uidAuth) {
        this.cif = cif;
        this.nombre = nombre;
        this.direccion = direccion;
        this.email = email;
        this.password = password;
        this.uidAuth = uidAuth;
    }

    // GETTERS & SETTERS
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
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

    public String getUidAuth() {
        return uidAuth;
    }

    public void setUidAuth(String uidAuth) {
        this.uidAuth = uidAuth;
    }
}
