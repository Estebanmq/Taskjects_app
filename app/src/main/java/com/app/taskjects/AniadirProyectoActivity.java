package com.app.taskjects;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.app.taskjects.pojos.Empleado;
import com.app.taskjects.pojos.Proyecto;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AniadirProyectoActivity extends AppCompatActivity {

    private final String PROYECTOS = "proyectos";
    private final String EMPLEADOS = "empleados";

    //Componentes
    TextInputEditText etNombreProyecto;
    TextInputEditText etDescripcionProyecto;
    AutoCompleteTextView atvJefeEmpleado;
    TextInputLayout outlinedTextFieldNombreProyecto;
    TextInputLayout outlinedTextFieldDescripcionProyecto;
    TextInputLayout outlinedTextFieldEmpleadosJefe;
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
        outlinedTextFieldNombreProyecto = findViewById(R.id.outlinedTextFieldNombreProyecto);
        outlinedTextFieldDescripcionProyecto = findViewById(R.id.outlinedTextFieldDescripcionProyecto);
        outlinedTextFieldEmpleadosJefe = findViewById(R.id.outlinedTextFieldEmpleadosJefe);

        //Inicializacion de la BD
        db = FirebaseFirestore.getInstance();

        //Recupera del intent el uid de la empresa donde dar de alta el proyecto
        uidEmpresa = getIntent().getStringExtra("uidEmpresa");

        //Aqui almacenare los empleados jefe
        mapJefes = new HashMap<>();

        //Inicializo la toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.crearProyecto));

        //Captura el click de volver atrás
        toolbar.setNavigationOnClickListener(view -> mostrarDialogoSalida());

        cargarEmpleadosJefe();
    }

    private void cargarEmpleadosJefe() {
        db.collection(EMPLEADOS)
                .whereEqualTo("uidEmpresa", uidEmpresa)
                .whereEqualTo("categoria","1")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (!task.getResult().isEmpty()) {
                            //Me recorro todos los datos que ha devuelto la query
                            for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                                //Por cada empleado jefe que encuentra lo añade al map
                                mapJefes.put(documentSnapshot.getString("nombre").concat(" ").concat(documentSnapshot.getString("apellidos")) , documentSnapshot.getId());
                            }
                            //Le añado un adaptador al listado que mostrara los empleados jefe
                            atvJefeEmpleado.setAdapter(new ArrayAdapter<>(AniadirProyectoActivity.this,R.layout.lista_jefes_proyecto,new ArrayList<>(mapJefes.keySet())));
                        } else {
                            //Si task.isEmpty() devuelve true entonces no se han encontrado registros, se lo indico al usuario
                            Toast.makeText(AniadirProyectoActivity.this, getString(R.string.errorGeneral), Toast.LENGTH_LONG).show();
                            Log.d("AniadirProyectoActivity","No se han encontrado empleados jefe");
                            findViewById(R.id.btnCrearProyecto).setEnabled(false);
                        }
                    } else {
                        Log.d("AniadirProyectoActivity","error al acceder a BD para recuperar jefes de proyecto");
                        //Si hay algun problema al recuperar datos de la base de datos le muestro al usuario que hay un problema
                        Toast.makeText(AniadirProyectoActivity.this, getString(R.string.errorAccesoBD), Toast.LENGTH_LONG).show();
                        findViewById(R.id.btnCrearProyecto).setEnabled(false);
                    }
                });
    }

    public void crearProyecto(View view) {
        outlinedTextFieldNombreProyecto.setErrorEnabled(false);
        outlinedTextFieldDescripcionProyecto.setErrorEnabled(false);
        outlinedTextFieldEmpleadosJefe.setErrorEnabled(false);

        findViewById(R.id.btnCrearProyecto).setEnabled(false);
        boolean creoProyecto = true;

        if (TextUtils.isEmpty(etNombreProyecto.getText().toString().trim())) {
            outlinedTextFieldNombreProyecto.setErrorEnabled(true);
            outlinedTextFieldNombreProyecto.setError(getString(R.string.faltaNombreProyecto));
            creoProyecto = false;
        }

        if (TextUtils.isEmpty(etDescripcionProyecto.getText().toString().trim())) {
            outlinedTextFieldDescripcionProyecto.setErrorEnabled(true);
            outlinedTextFieldDescripcionProyecto.setError(getString(R.string.faltaDescripcion));
            creoProyecto = false;
        }
        if (TextUtils.isEmpty(atvJefeEmpleado.getText().toString().trim())) {
            outlinedTextFieldEmpleadosJefe.setErrorEnabled(true);
            outlinedTextFieldEmpleadosJefe.setError(getString(R.string.faltaJefeProyecto));
            creoProyecto = false;
        }

        if (creoProyecto) {

            //Aqui almaceno los datos del proyecto
            Proyecto proyecto = new Proyecto(uidEmpresa, etNombreProyecto.getText().toString().trim(), etDescripcionProyecto.getText().toString().trim(), mapJefes.get(atvJefeEmpleado.getText().toString()));
            db.collection(PROYECTOS)
                    .add(proyecto)
                    .addOnSuccessListener(documentReference -> actualizarEmpleadoJefe(mapJefes.get(atvJefeEmpleado.getText().toString()), documentReference.getId()))
                    .addOnFailureListener(e -> {
                        Log.d("taskjectsdebug","Error al subir proyecto a bbdd: " + e.getMessage());
                        findViewById(R.id.btnCrearProyecto).setEnabled(true);
                        Toast.makeText(AniadirProyectoActivity.this, getString(R.string.errorAccesoBD), Toast.LENGTH_LONG).show();
                    });
        } else {
            findViewById(R.id.btnCrearProyecto).setEnabled(true);
        }

    }

    private void actualizarEmpleadoJefe(String uidEmpleadoJefe, String uidProyecto) {

        DocumentReference docRef = db.collection(EMPLEADOS).document(uidEmpleadoJefe);
        docRef.get()
                .addOnSuccessListener(documentSnapshot -> {

                    Empleado empleado = documentSnapshot.toObject(Empleado.class);
                    empleado.getUidProyectos().add(uidProyecto);
                    db.collection(EMPLEADOS).document(uidEmpleadoJefe)
                            .set(empleado)
                            .addOnSuccessListener(aVoid -> {
                                findViewById(R.id.btnCrearProyecto).setEnabled(true);

                                AlertDialog.Builder alertaCreacionProyectoCorrecta = new AlertDialog.Builder(AniadirProyectoActivity.this);
                                alertaCreacionProyectoCorrecta.setMessage(getString(R.string.creacionProyectoCorrecta))
                                        .setPositiveButton(getString(R.string.ok), (dialogInterface, i) -> finish()).show();
                            })
                            .addOnFailureListener(e -> {
                                Log.d("taskjectsdebug","Error al actualizar el empleado en bbdd: " + e.getMessage());
                                Toast.makeText(AniadirProyectoActivity.this, getString(R.string.errorAccesoBD), Toast.LENGTH_LONG).show();
                                findViewById(R.id.btnCrearProyecto).setEnabled(true);
                            });
                })
                .addOnFailureListener(e -> {
                    Log.d("taskjectsdebug","Error al leer el empleado de la BD: " + e.getMessage());
                    Toast.makeText(AniadirProyectoActivity.this, getString(R.string.errorAccesoBD), Toast.LENGTH_LONG).show();
                    findViewById(R.id.btnCrearProyecto).setEnabled(true);
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_crear, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /*
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
    */

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