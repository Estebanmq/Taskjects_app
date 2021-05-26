package com.app.taskjects;

import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import com.app.taskjects.pojos.Empleado;
import com.app.taskjects.pojos.Proyecto;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
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

    Map<String,String> mapJefes;

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

        //Inicializacion de la BD
        db = FirebaseFirestore.getInstance();

        //Recupera del intent el uid de la empresa donde dar de alta el proyecto
        uidEmpresa = getIntent().getStringExtra("uidEmpresa");

        //Aqui almacenare los empleados jefe
        mapJefes = new HashMap<>();

        //Inicializo la toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Captura el click de volver atrás
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mostrarDialogoSalida();
            }
        });

        cargarEmpleadosJefe();
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
                                    mapJefes.put(documentSnapshot.getString("nombre").concat(" ".concat(documentSnapshot.getString("apellidos"))) , documentSnapshot.getId());
                                }
                                //Le añado un adaptador al listado que mostrara los empleados jefe
                                atvJefeEmpleado.setAdapter(new ArrayAdapter<String>(AniadirProyectoActivity.this,R.layout.lista_jefes_proyecto,new ArrayList<>(mapJefes.keySet())));
                            } else {
                                //Si task.isEmpty() devuelve true entonces no se han encontrado registros, se lo indico al usuario
                                Toast.makeText(AniadirProyectoActivity.this, getString(R.string.errorGeneral), Toast.LENGTH_LONG).show();
                                Log.d("AniadirProyectoActivity","No se han encontrado empleados jefe");
                            }
                        } else {
                            //Si hay algun problema al recuperar datos de la base de datos le muestro al usuario que hay un problema
                            Toast.makeText(AniadirProyectoActivity.this, getString(R.string.errorAccesoBD), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    public void crearProyecto(View view) {

        findViewById(R.id.btnCrearProyecto).setEnabled(false);
        boolean creoProyecto = true;

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
            Proyecto proyecto = new Proyecto(uidEmpresa, etNombreProyecto.getText().toString(), etDescripcionProyecto.getText().toString(), mapJefes.get(atvJefeEmpleado.getText().toString()));
            db.collection("proyectos")
                    .add(proyecto)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            actualizarEmpleadoJefe(mapJefes.get(atvJefeEmpleado.getText().toString()), documentReference.getId());

                            Log.d("taskjectsdebug","Proyecto añadido correctamente");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            findViewById(R.id.btnCrearProyecto).setEnabled(true);

                            AlertDialog.Builder alertaErrorAccesoBBDD = new AlertDialog.Builder(AniadirProyectoActivity.this);
                            alertaErrorAccesoBBDD.setMessage(getString(R.string.errorAccesoBD))
                                    .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            Log.d("taskjectsdebug","Error al subir proyecto a bbdd");
                                        }
                                    }).show();
                        }
                    });
        } else {
            findViewById(R.id.btnCrearProyecto).setEnabled(true);
        }

    }

    private void actualizarEmpleadoJefe(String uidEmpleadoJefe, String uidProyecto) {

        Log.d("taskjectsdebug","Actualiza los proyectos del empleado jefe: " + uidEmpleadoJefe);
        DocumentReference docRef = db.collection("empleados").document(uidEmpleadoJefe);
        docRef.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {

                        Empleado empleado = documentSnapshot.toObject(Empleado.class);
                        empleado.getUidProyectos().add(uidProyecto);
                        db.collection("empleados").document(uidEmpleadoJefe)
                                .set(empleado)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        findViewById(R.id.btnCrearProyecto).setEnabled(true);

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
                                        findViewById(R.id.btnCrearProyecto).setEnabled(true);

                                        Log.d("taskjectsdebug","Error al subir el empleado a bbdd");
                                    }
                                });
                    }})
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        findViewById(R.id.btnCrearProyecto).setEnabled(true);
                        Log.d("taskjectsdebug","Error al leer el empleado de la BD");
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_crear, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
//            case R.id. : // Aquí la opción pulsada
//                .        // Aquí lo que haya que hacer
//                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            mostrarDialogoSalida();
        }
        return true;
    }

    private void mostrarDialogoSalida() {

        AlertDialog.Builder alertaSalidaCreacion = new AlertDialog.Builder(AniadirProyectoActivity.this);
        //Si pulsa en cancelar no salgo de la activity
        alertaSalidaCreacion.setMessage(getString(R.string.confirmSalidaCreacionProyecto))
                .setNeutralButton(getString(R.string.cancelar), (dialogInterface, i) -> { })
                .setPositiveButton(getString(R.string.aceptar), (dialogInterface, i) -> finish())
                .show();
    }
}