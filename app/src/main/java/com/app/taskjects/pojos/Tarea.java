package com.app.taskjects.pojos;

import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.firestore.DocumentId;

@IgnoreExtraProperties
public class Tarea {

    private String estado;
    private String tarea;
    private String uidEmpleado;
    private String prioridad;
    private String uidProyecto;

    //Todo: Anotacion para que no te lo meta como campo en el "documento"
    public String uidTarea;

    public Tarea () {}

    public Tarea(String estado, String tarea, String uidEmpleado, String prioridad,String uidProyecto,String uidTarea) {
        this.estado = estado;
        this.tarea = tarea;
        this.uidEmpleado = uidEmpleado;
        this.prioridad = prioridad;
        this.uidProyecto = uidProyecto;
        this.uidTarea = uidTarea;
    }

    public String getEstado() { return estado; }

    public void setEstado(String estado) { this.estado = estado; }

    public String getTarea() {
        return tarea;
    }

    public void setTarea(String tarea) {
        this.tarea = tarea;
    }

    public String getUidEmpleado() {
        return uidEmpleado;
    }

    public void setUidEmpleado(String uidEmpleado) {
        this.uidEmpleado = uidEmpleado;
    }

    public String getPrioridad() {
        return prioridad;
    }

    public void setPrioridad(String prioridad) {
        this.prioridad = prioridad;
    }

    public String getUidProyecto() {
        return uidProyecto;
    }

    public void setUidProyecto(String uidProyecto) {
        this.uidProyecto = uidProyecto;
    }

    public String getUidTarea() { return uidTarea; }

    public void setUidTarea(String uidTarea) { this.uidTarea = uidTarea; }
}
