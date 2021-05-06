package com.app.taskjects.pojos;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Tarea {

    public String tarea;
    public String uidEmpleado;
    public String prioridad;
    public String uidProyecto;

    public Tarea () {}

    public Tarea(String tarea, String uidEmpleado, String prioridad,String uidProyecto) {
        this.tarea = tarea;
        this.uidEmpleado = uidEmpleado;
        this.prioridad = prioridad;
        this.uidProyecto = uidProyecto;
    }
}
