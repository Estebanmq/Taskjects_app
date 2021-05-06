package com.app.taskjects;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import com.app.taskjects.pojos.Tarea;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.chip.ChipGroup;
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

public class AniadirTareaActivity extends AppCompatActivity {

    //Componentes pantalla
    ChipGroup cgPrioridades;
    TextInputEditText etTarea;
    AutoCompleteTextView atvEmpleados;

    //Variables para manejar la bbdd y sus datos
    FirebaseAuth mAuth;
    FirebaseFirestore db;
    DatabaseReference mDatabase;
    String uidEmpresa;
    String uidProyecto;

    Map<String,String> mapEmpleados;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.aniadir_tarea_layout);

        //Inicializacion componentes
        cgPrioridades = findViewById(R.id.chipGroupPrioridades);
        etTarea = findViewById(R.id.etTarea);
        atvEmpleados = findViewById(R.id.atvEmpleados);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        sharedPreferences = getSharedPreferences(mAuth.getUid(), Context.MODE_PRIVATE);

        uidEmpresa = sharedPreferences.getString("uidEmpresa","");
        uidProyecto = getIntent().getStringExtra("uidProyecto");

        mapEmpleados = new HashMap<>();

        cargaEmpleadosEmpresa();
    }

    private void cargaEmpleadosEmpresa() {
        db.collection("empleados")
                .whereEqualTo("uidEmpresa",uidEmpresa)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if (!task.getResult().isEmpty()) {
                                mapEmpleados.put("-- Sin asignar --", "noasignado");
                                for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                                    Log.d("AniadirTareaActivity", documentSnapshot.getString("nombre"));
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

    private boolean verificarDatos() {
        if (etTarea.getText().toString().isEmpty()) {
            etTarea.setError(getString(R.string.faltaTarea));
            return false;
        }
        return true;
    }

    public void crearTarea(View view) {
        if (verificarDatos()) {
            //Todo: Prio desde 0 no?
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

            mDatabase.child("tareas").child(uidEmpresa).child(uidProyecto).push().setValue(new Tarea(etTarea.getText().toString(),mapEmpleados.get(atvEmpleados.getText().toString()),prioridad,uidProyecto))
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            AlertDialog.Builder alertaCreacionTareaCorrecta = new AlertDialog.Builder(AniadirTareaActivity.this);
                            alertaCreacionTareaCorrecta.setMessage(getString(R.string.creacionTareaCorrecta))
                                    .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
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


        }

    }

}