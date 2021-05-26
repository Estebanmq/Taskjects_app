package com.app.taskjects.pojos;

import com.google.firebase.firestore.DocumentId;

public class Categoria {

    @DocumentId
    private String uid; // uid del registro en BD

    private String descripcion;
    private Boolean marca;

    public Categoria() { }

    public Categoria(String descripcion, boolean marca) {
        this.descripcion = descripcion;
        this.marca = marca;
    }

    // GETTERS & SETTERS

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Boolean getMarca() {
        return marca;
    }

    public void setMarca(Boolean marca) {
        this.marca = marca;
    }
}