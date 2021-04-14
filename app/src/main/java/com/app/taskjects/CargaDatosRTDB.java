package com.app.taskjects;

import android.view.View;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class CargaDatosRTDB {

    public void borrarDatos() {

    }

    public void cargaDatos() {
        DatabaseReference raizRTDB = FirebaseDatabase.getInstance().getReference();
        /*
        Empresa[]empresas = {};
        Empleado[]empleados = {};
        Proyecto[]proyectos = {};
        Tarea[]tareas = {};
        */

        //Carga de empresas
        DatabaseReference empresasRaiz = raizRTDB.child("empresas");

        //Carga de empleados
        DatabaseReference empleadosRaiz = raizRTDB.child("empleados");

        //Carga de proyectos
        DatabaseReference proyectosRaiz = raizRTDB.child("proyectos");

        //Carga de tareas
        DatabaseReference tareasRaiz = raizRTDB.child("tareas");

        //Carga de relaciones entre empleados y proyectos
        DatabaseReference empleadosProyectosRaiz = raizRTDB.child("empleadosProyectos");

        //Carga de datos categorias de empleados
        DatabaseReference categoriasRaiz = raizRTDB.child("categorias");

        //Carga de datos prioridades de tareas
        DatabaseReference prioridadesRaiz = raizRTDB.child("prioridades");

    }


    //Metodo de prueba sin utilizar para hacer un push en la base de datos
    public void hacerPush(View view) {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child("Empresas");
        DatabaseReference newPostRef = mDatabase.push();
        //newPostRef.setValue(new Empresa("Prueba 1","Prueba ubia"));
    }

}
