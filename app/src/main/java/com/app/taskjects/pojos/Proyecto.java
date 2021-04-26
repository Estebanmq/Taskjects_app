package com.app.taskjects.pojos;

public class Proyecto {

    public String uidEmpresa;
    public String nombre;
    public String descripcion;
    public String uidEmpleadoJefe;

    public Proyecto() {}

    public Proyecto(String uidEmpresa, String nombre, String descripcion, String uidEmpleadoJefe) {
        this.uidEmpresa = uidEmpresa;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.uidEmpleadoJefe = uidEmpleadoJefe;
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
