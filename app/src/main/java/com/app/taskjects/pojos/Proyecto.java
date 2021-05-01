package com.app.taskjects.pojos;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;

public class Proyecto {

    @DocumentId
    private String uid; // uid del registro en BD

    @ServerTimestamp
    private Timestamp fechaHoraCreacion; // Fecha y hora a la que se crea el registro

    private String uidEmpresa;
    private String nombre;
    private String descripcion;
    private String uidEmpleadoJefe;

    public Proyecto() {}

    public Proyecto(String uid, String uidEmpresa, String nombre, String descripcion, String uidEmpleadoJefe) {
        this.uid = uid;
        this.uidEmpresa = uidEmpresa;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.uidEmpleadoJefe = uidEmpleadoJefe;
    }

    public Proyecto(String uidEmpresa, String nombre, String descripcion, String uidEmpleadoJefe) {
        this.uidEmpresa = uidEmpresa;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.uidEmpleadoJefe = uidEmpleadoJefe;
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

    public void setFechaHoraCreacion(Timestamp fechaHoraCreacion) {
        this.fechaHoraCreacion = fechaHoraCreacion;
    }

    public String getUidEmpresa() { return uidEmpresa; }

    public void setUidEmpresa(String uidEmpresa) { this.uidEmpresa = uidEmpresa; }

    public String getNombre() { return nombre; }

    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }

    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getUidEmpleadoJefe() { return uidEmpleadoJefe; }

    public void setUidEmpleadoJefe(String uidEmpleadoJefe) { this.uidEmpleadoJefe = uidEmpleadoJefe; }

}
