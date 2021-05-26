package com.app.taskjects.pojos;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.ArrayList;
import java.util.List;

public class Empleado {

    @DocumentId
    private String uid; // uid del registro en BD

    @ServerTimestamp
    private Timestamp fechaHoraCreacion; // Fecha y hora a la que se crea el registro

    private String nif;
    private String nombre;
    private String apellidos;
    private String email;
    private String password;
    private String uidEmpresa;  // uid de la empresa del empleado
    private String categoria;
    private String uidAuth; // uid del empleado en Authentication
    private List<String> uidProyectos;

    public Empleado() { }

    public Empleado(String nif, String nombre, String apellidos, String email, String password, String uidEmpresa, String categoria, String uidAuth) {
        this.nif = nif;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.email = email;
        this.password = password;
        this.uidEmpresa = uidEmpresa;
        this.categoria = categoria;
        this.uidAuth = uidAuth;
        uidProyectos = new ArrayList<String>();
    }

    public String getNombreApellidos () {
        return this.nombre.concat(" ").concat(this.apellidos);
    }

    // GETTERS & SETTERS
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public Timestamp getFechaHoraCreacion() {
        return fechaHoraCreacion;
    }

    public void setFechaHoraCreacion(Timestamp fechaHoraCreacion) { this.fechaHoraCreacion = fechaHoraCreacion; }

    public String getNif() { return nif; }

    public void setNif(String nif) { this.nif = nif; }

    public String getNombre() { return nombre; }

    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellidos() { return apellidos; }

    public void setApellidos(String apellidos) { this.apellidos = apellidos; }

    public String getEmail() { return email; }

    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }

    public void setPassword(String password) { this.password = password; }

    public String getUidEmpresa() { return uidEmpresa; }

    public void setUidEmpresa(String uidEmpresa) { this.uidEmpresa = uidEmpresa; }

    public String getCategoria() { return categoria; }

    public void setCategoria(String categoria) { this.categoria = categoria; }

    public String getUidAuth() { return uidAuth; }

    public void setUidAuth(String uidAuth) { this.uidAuth = uidAuth; }

    public List<String> getUidProyectos() { return uidProyectos; }

    public void setUidProyectos(List<String> uidProyectos) { this.uidProyectos = uidProyectos; }
}