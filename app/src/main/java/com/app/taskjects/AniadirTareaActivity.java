package com.app.taskjects;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import com.app.taskjects.pojos.Tarea;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

public class AniadirTareaActivity extends AppCompatActivity {

    //Componentes pantalla
    ChipGroup cgPrioridades;
    TextInputEditText etTarea;
    AutoCompleteTextView atvEmpleados;
    MaterialButton btnCrearTarea;

    //Variables para manejar la bbdd y sus datos
    DatabaseReference mDatabase;
    FirebaseFirestore db;

    String uidEmpresa;
    String uidProyecto;

    Map<String,String> mapEmpleados;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.aniadir_tarea_layout);

        //Inicializacion componentes
        cgPrioridades = findViewById(R.id.chipGroupPrioridades);
        cgPrioridades.setSelectionRequired(true);
        etTarea = findViewById(R.id.etTarea);
        atvEmpleados = findViewById(R.id.atvEmpleados);
        btnCrearTarea = findViewById(R.id.btnCrearTarea);

        //Inicializo las variables de manejo de la BD
        db = FirebaseFirestore.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        //Inicializo las variables de la clase
        uidEmpresa = getIntent().getStringExtra("uidEmpresa");
        uidProyecto = getIntent().getStringExtra("uidProyecto");
        mapEmpleados = new TreeMap<>();

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

        cargaEmpleadosEmpresa();
    }

    //Metodo que carga el nombre y su id de todos los empleados de la empresa del proyecto
    private void cargaEmpleadosEmpresa() {

        db.collection("empleados")
                .whereArrayContains("uidProyectos", uidProyecto)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if (!task.getResult().isEmpty()) {
                                mapEmpleados.put("-- Sin asignar --", "noasignado");
                                for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                                    Log.d("taskjectsdebug", documentSnapshot.getString("nombre"));
                                    mapEmpleados.put(documentSnapshot.getString("nombre").concat(" ".concat(documentSnapshot.getString("apellidos"))), documentSnapshot.getId());

                                }
                                ArrayList<String>nombreEmpleados = new ArrayList<>(mapEmpleados.keySet());
                                //La posicion 0 es el texto de -- Sin asignar -- que va a ser el valor por defecto
                                atvEmpleados.setText(nombreEmpleados.get(0));
                                atvEmpleados.setAdapter(new ArrayAdapter<String>(AniadirTareaActivity.this, R.layout.lista_empleados_empresa, nombreEmpleados));

                            } else {
                                //Si task.isEmpty() devuelve true entonces no se han encontrado registros, se lo indico al usuario
                                Toast.makeText(AniadirTareaActivity.this, getString(R.string.errorGeneral), Toast.LENGTH_LONG).show();
                                Log.d("AniadirTareaActivity", "No se han encontrado empleados");
                            }
                        } else {
                            //Si hay algun problema al recuperar datos de la base de datos le muestro al usuario que hay un problema
                            Toast.makeText(AniadirTareaActivity.this, getString(R.string.errorAccesoBD), Toast.LENGTH_LONG).show();
                        }
                    }
                });

    }

    //Metodo que verifica el formulario de la tarea
    private boolean verificarDatos() {
        if (etTarea.getText().toString().isEmpty()) {
            etTarea.setError(getString(R.string.faltaTarea));
            return false;
        }
        return true;
    }

    //Metodo que crea tarea en la RTDB
    public void crearTarea(View view) {
        btnCrearTarea.setEnabled(false);
        //Si todos los datos ok creo tarea
        if (verificarDatos()) {
            String prioridad = "0";
            //Todo: el case da warning, ver otra forma
            switch (cgPrioridades.getCheckedChipId()) {
                case R.id.chipPrioMedia:
                    prioridad = "1";
                    break;
                case R.id.chipPrioAlta:
                    prioridad = "2";
                    break;
            }
            String estado = "0";
            if (!atvEmpleados.getText().toString().equals("-- Sin asignar --"))
                estado = "1";

            String uidEmpleadoATV = mapEmpleados.get(atvEmpleados.getText().toString());
            String uidTarea =  mDatabase.child("tareas").child(uidEmpresa).child(uidProyecto).child(uidEmpleadoATV).push().getKey();
            mDatabase.child("tareas")
                    .child(uidProyecto)
                    .child(uidTarea)
                    .setValue(new Tarea(estado,etTarea.getText().toString(),mapEmpleados.get(atvEmpleados.getText().toString()),prioridad,uidProyecto,uidTarea))
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            AlertDialog.Builder alertaCreacionTareaCorrecta = new AlertDialog.Builder(AniadirTareaActivity.this);
                            alertaCreacionTareaCorrecta.setMessage(getString(R.string.creacionTareaCorrecta))
                                    .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            btnCrearTarea.setEnabled(true);
                                            finish();
                                        }
                                    }).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            AlertDialog.Builder alertaErrorAccesoBBDD = new AlertDialog.Builder(AniadirTareaActivity.this);
                            alertaErrorAccesoBBDD.setMessage(getString(R.string.errorAccesoBD))
                                    .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            Log.d("AniadirTareaActivity","Error al subir la tarea a bbdd");
                                        }
                                    }).show();
                        }
                    });
        } else
            btnCrearTarea.setEnabled(true);

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            mostrarDialogoSalida();
        }
        return true;
    }

    private void mostrarDialogoSalida() {

        AlertDialog.Builder alertaSalidaCreacion = new AlertDialog.Builder(AniadirTareaActivity.this);
        //Si pulsa en cancelar no salgo de la activity
        alertaSalidaCreacion.setMessage(getString(R.string.confirmSalidaCreacionTarea))
                .setNeutralButton(getString(R.string.cancelar), (dialogInterface, i) -> { })
                .setPositiveButton(getString(R.string.aceptar), (dialogInterface, i) -> finish())
                .show();
    }
}