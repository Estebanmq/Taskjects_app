package com.app.taskjects;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import com.app.taskjects.pojos.Empresa;
import com.app.taskjects.pojos.Proyecto;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AniadirProyectoActivity extends AppCompatActivity {

    //Todo: Avisar a usuario de que le ha dado al boton de volver
    //Componentes
    TextInputEditText etNombreProyecto;
    TextInputEditText etDescripcionProyecto;
    AutoCompleteTextView atvJefeEmpleado;

    Map<String,String>mapJefes;

    //Variables para manejar la BBDD
    FirebaseFirestore db;
    String uidEmpresa;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.aniadir_proyecto_layout);

        //Inicializacion de componentes y variables
        etNombreProyecto = findViewById(R.id.etNombreProyecto);
        etDescripcionProyecto = findViewById(R.id.etDescripcionProyecto);
        atvJefeEmpleado = findViewById(R.id.empleadosJefe);
        db = FirebaseFirestore.getInstance();
        uidEmpresa = getIntent().getStringExtra("uidEmpresa");

        mapJefes = new HashMap<>();

        //Inicializo la toolbar
        Toolbar toolbar = findViewById(R.id.toolbarAniadirProyecto);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getString(R.string.aniadirProyecto));
        toolbar.inflateMenu(R.menu.menu_crear_proyecto);

        //Muestro el boton de la flecha para volver atras
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


        cargarEmpleadosJefe();
    }


    public void crearProyecto(View view) {
        //Todo: depurar
        Log.d("Debug añadir tarea","entro dentro");
        boolean creoProyecto = true;
        String jefeProyecto;

        if (TextUtils.isEmpty(etNombreProyecto.getText().toString())) {
            etNombreProyecto.setError(getString(R.string.faltaNombreProyecto));
            creoProyecto = false;
        }

        if (TextUtils.isEmpty(atvJefeEmpleado.getText().toString())) {
            atvJefeEmpleado.setError(getString(R.string.faltaJefeProyecto));
            creoProyecto = false;
        }

        if (creoProyecto) {

            Proyecto proyecto = new Proyecto(uidEmpresa,etNombreProyecto.getText().toString(),etDescripcionProyecto.getText().toString(),mapJefes.get(atvJefeEmpleado.getText().toString()));

            db.collection("proyectos")
                    .add(proyecto)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            Log.d("Agregado","El proyecto");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w("Error al subir tarea",e);
                        }
                    });
        }

    }

    private void cargarEmpleadosJefe() {
        //Aqui almacenare los empleados jefe

        db.collection("empleados")
                .whereEqualTo("uidEmpresa",uidEmpresa)
                .whereEqualTo("categoria","1")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            //Me recorro todos los datos que ha devuelto la query
                            for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                                //Por cada empleado jefe que encuentra lo añade al map
                                mapJefes.put(documentSnapshot.getString("nombre")+" "+documentSnapshot.getString("apellidos"),documentSnapshot.getId());
                            }

                            //Le añado un adaptador al listado que mostrara los empelados jefe
                            atvJefeEmpleado.setAdapter(new ArrayAdapter<String>(AniadirProyectoActivity.this,R.layout.lista_jefes_proyecto,new ArrayList<>(mapJefes.keySet())));
                        } else {
                            //Todo: informar al usuario de que no se han recuperado datos
                            Log.d("cargaEmpleadosJefe","No hay datos");
                        }
                    }
                });
    }

}