package com.app.taskjects;

import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.app.taskjects.pojos.Tarea;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

public class AniadirTareaActivity extends AppCompatActivity {

    private final String EMPLEADOS = "empleados";
    private final String TAREAS = "tareas";

    //Componentes pantalla
    ChipGroup cgPrioridades;
    TextInputEditText etTarea;
    AutoCompleteTextView atvEmpleados;
    MaterialButton btnCrearTarea;
    TextInputLayout outlinedTextFieldTarea;

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
        outlinedTextFieldTarea = findViewById(R.id.outlinedTextFieldTarea);

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
        getSupportActionBar().setTitle(getString(R.string.crearTarea));
        //Captura el click de volver atrás
        toolbar.setNavigationOnClickListener(view -> mostrarDialogoSalida());

        cargaEmpleadosEmpresa();
    }

    //Metodo que carga el nombre y su id de todos los empleados de la empresa del proyecto
    private void cargaEmpleadosEmpresa() {

        db.collection(EMPLEADOS)
                .whereArrayContains("uidProyectos", uidProyecto)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (!task.getResult().isEmpty()) {
                            mapEmpleados.put("-- Sin asignar --", "noasignado");
                            for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                                mapEmpleados.put(documentSnapshot.getString("nombre").concat(" ").concat(documentSnapshot.getString("apellidos")), documentSnapshot.getId());
                            }
                            ArrayList<String>nombreEmpleados = new ArrayList<>(mapEmpleados.keySet());
                            //La posicion 0 es el texto de -- Sin asignar -- que va a ser el valor por defecto
                            atvEmpleados.setText(nombreEmpleados.get(0));
                            atvEmpleados.setAdapter(new ArrayAdapter<>(AniadirTareaActivity.this, R.layout.lista_empleados_empresa, nombreEmpleados));

                        } else {
                            //Si task.isEmpty() devuelve true entonces no se han encontrado registros, se lo indico al usuario
                            Toast.makeText(AniadirTareaActivity.this, getString(R.string.errorGeneral), Toast.LENGTH_LONG).show();
                            Log.d("AniadirTareaActivity", "No se han encontrado empleados");
                        }
                    } else {
                        Log.d("AniadirTareaActivity", "Error en BD al leer empleados");
                        //Si hay algun problema al recuperar datos de la base de datos le muestro al usuario que hay un problema
                        Toast.makeText(AniadirTareaActivity.this, getString(R.string.errorAccesoBD), Toast.LENGTH_LONG).show();
                    }
                });
    }

    //Metodo que verifica el formulario de la tarea
    private boolean verificarDatos() {
        outlinedTextFieldTarea.setErrorEnabled(false);
        if (etTarea.getText().toString().trim().isEmpty()) {
            outlinedTextFieldTarea.setErrorEnabled(true);
            outlinedTextFieldTarea.setError(getString(R.string.faltaTarea));
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
            String uidTarea =  mDatabase.child(TAREAS).child(uidEmpresa).child(uidProyecto).child(uidEmpleadoATV).push().getKey();
            mDatabase.child(TAREAS)
                    .child(uidProyecto)
                    .child(uidTarea)
                    .setValue(new Tarea(estado, etTarea.getText().toString().trim(), mapEmpleados.get(atvEmpleados.getText().toString()), prioridad, uidProyecto, uidTarea))
                    .addOnSuccessListener(aVoid -> {
                        AlertDialog.Builder alertaCreacionTareaCorrecta = new AlertDialog.Builder(AniadirTareaActivity.this);
                        alertaCreacionTareaCorrecta.setMessage(getString(R.string.creacionTareaCorrecta))
                                .setPositiveButton(getString(R.string.ok), (dialogInterface, i) -> {
                                    btnCrearTarea.setEnabled(true);
                                    finish();
                                }).show();
                    }).addOnFailureListener(e -> {
                        Log.d("taskjectsdebug","Error al crear la tarea en bbdd: " + e.getMessage());
                        Toast.makeText(AniadirTareaActivity.this, getString(R.string.errorAccesoBD), Toast.LENGTH_LONG).show();
                        findViewById(R.id.btnCrearProyecto).setEnabled(true);
                    });
        } else {
            btnCrearTarea.setEnabled(true);
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

        AlertDialog.Builder alertaSalidaCreacion = new AlertDialog.Builder(AniadirTareaActivity.this);
        //Si pulsa en cancelar no salgo de la activity
        alertaSalidaCreacion.setMessage(getString(R.string.confirmSalidaCreacionTarea))
                .setNeutralButton(getString(R.string.cancelar), (dialogInterface, i) -> { })
                .setPositiveButton(getString(R.string.aceptar), (dialogInterface, i) -> finish())
                .show();
    }
}