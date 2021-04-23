package com.app.taskjects;

import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
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
        atvJefeEmpleado = findViewById(R.id.atvJefeEmpleado);
        db = FirebaseFirestore.getInstance();
        uidEmpresa = getIntent().getStringExtra("uidEmpresa");

        //Aqui almacenare los empleados jefe
        mapJefes = new HashMap<>();

        //Inicializo la toolbar
        Toolbar toolbarAniadirProyecto = findViewById(R.id.toolbarAniadirProyecto);
        setSupportActionBar(toolbarAniadirProyecto);
        toolbarAniadirProyecto.setTitle(getString(R.string.aniadirProyecto));
        toolbarAniadirProyecto.inflateMenu(R.menu.menu_crear_proyecto);

        //Muestro el boton de la flecha para volver atras
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbarAniadirProyecto.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Si hace click en el icono de la flecha para salir de la creacion de proyecto le muestro un pop up de confirmacion
                AlertDialog.Builder alertaSalidaCreacion = new AlertDialog.Builder(AniadirProyectoActivity.this);
                alertaSalidaCreacion.setMessage(getString(R.string.confirmSalidaCreacionProyecto))
                        //Si pulsa en cancelar no salgo de la activity
                        .setNeutralButton(getString(R.string.cancelar), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Log.d("AniadirProyectoActivity","Salgo de la creacion de proyecto");
                            }
                        }).setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                            //Si pulsa en de acuerdo cierro la activity
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                finish();
                            }
                        }).show();
            }
        });


        cargarEmpleadosJefe();
    }


    public void crearProyecto(View view) {
        //Todo: depurar e informar al usuario
        Log.d("Debug añadir tarea","entro dentro");
        boolean creoProyecto = true;
        String jefeProyecto;

        if (TextUtils.isEmpty(etNombreProyecto.getText().toString())) {
            etNombreProyecto.setError(getString(R.string.faltaNombreProyecto));
            creoProyecto = false;
        }

        if (TextUtils.isEmpty(etDescripcionProyecto.getText().toString())) {
            etDescripcionProyecto.setError(getString(R.string.faltaDescripcion));
            creoProyecto = false;
        }
        if (TextUtils.isEmpty(atvJefeEmpleado.getText().toString())) {
            atvJefeEmpleado.setError(getString(R.string.faltaJefeProyecto));
            creoProyecto = false;
        }

        if (creoProyecto) {
            //Aqui almaceno los datos del proyecto
            Proyecto proyecto = new Proyecto(uidEmpresa,etNombreProyecto.getText().toString(),etDescripcionProyecto.getText().toString(),mapJefes.get(atvJefeEmpleado.getText().toString()));
            db.collection("proyectos")
                    .add(proyecto)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            Log.d("AniadirProyectoActivity","Proyecto añadido correctamente");
                            AlertDialog.Builder alertaCreacionProyectoCorrecta = new AlertDialog.Builder(AniadirProyectoActivity.this);
                            alertaCreacionProyectoCorrecta.setMessage(getString(R.string.creacionProyectoCorrecta))
                                    .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            finish();
                                        }
                                    }).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            AlertDialog.Builder alertaErrorAccesoBBDD = new AlertDialog.Builder(AniadirProyectoActivity.this);
                            alertaErrorAccesoBBDD.setMessage(getString(R.string.errorAccesoBD))
                                    .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            Log.d("AniadirProyectoActivity","Error al subir proyecto a bbdd");
                                        }
                                    }).show();
                        }
                    });
        }

    }

    private void cargarEmpleadosJefe() {
        db.collection("empleados")
                .whereEqualTo("uidEmpresa",uidEmpresa)
                .whereEqualTo("categoria","1")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if (!task.getResult().isEmpty()) {
                                //Me recorro todos los datos que ha devuelto la query
                                for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                                    //Por cada empleado jefe que encuentra lo añade al map
                                    mapJefes.put(documentSnapshot.getString("nombre") + " " + documentSnapshot.getString("apellidos"), documentSnapshot.getId());
                                }
                            } else {
                                //Si task.isEmpty() devuelve true entonces no se han encontrado registros, se lo indico al usuario
                                atvJefeEmpleado.setError(getString(R.string.noSeEncuentranJefes));
                                Log.d("AniadirProyectoActivity","No se han encontrado datos");
                            }
                            //Le añado un adaptador al listado que mostrara los empelados jefe
                            atvJefeEmpleado.setAdapter(new ArrayAdapter<String>(AniadirProyectoActivity.this,R.layout.lista_jefes_proyecto,new ArrayList<>(mapJefes.keySet())));
                        } else {
                            //Si hay algun problema al recuperar datos de la base de datos le muestro al usuario que hay un problema
                            AlertDialog.Builder alertaNoDatosBBDD = new AlertDialog.Builder(AniadirProyectoActivity.this);
                            alertaNoDatosBBDD.setMessage(getString(R.string.errorAccesoBD))
                                    .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            Log.d("AniadirProyectoActivity","Error al recuperar datos de la bbdd");
                                        }
                                    }).show();
                        }
                    }
                });
    }

}