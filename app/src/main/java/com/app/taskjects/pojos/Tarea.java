package com.app.taskjects.pojos;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Tarea {

    private String estado;
    private String tarea;
    private String uidEmpleado;
    private String prioridad;
    private String uidProyecto;
    @Exclude private String uidTarea;

    public Tarea () {}

    public Tarea(String estado, String tarea, String uidEmpleado, String prioridad,String uidProyecto,String uidTarea) {
        this.estado = estado;
        this.tarea = tarea;
        this.uidEmpleado = uidEmpleado;
        this.prioridad = prioridad;
        this.uidProyecto = uidProyecto;
        this.uidTarea = uidTarea;
    }

    //Metodo que cambia los datos de la tarea con los nuevos datos de una tarea pasada como parametro
    public void setNuevosDatos(Tarea tarea) {
        if (!this.tarea.equals(tarea.getTarea()))
            setTarea(tarea.getTarea());
        if (!this.uidEmpleado.equals(tarea.getUidEmpleado()))
            setUidEmpleado(tarea.getUidEmpleado());
        if (!this.prioridad.equals(tarea.getPrioridad()))
            setPrioridad(tarea.getPrioridad());
    }

    @Override
    public String toString() {
        return "Tarea{" +
                "estado='" + estado + '\'' +
                ", tarea='" + tarea + '\'' +
                ", uidEmpleado='" + uidEmpleado + '\'' +
                ", prioridad='" + prioridad + '\'' +
                ", uidProyecto='" + uidProyecto + '\'' +
                ", uidTarea='" + uidTarea + '\'' +
                '}';
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

    @Exclude
    public String getUidTarea() { return uidTarea; }

    public void setUidTarea(String uidTarea) { this.uidTarea = uidTarea; }


}
