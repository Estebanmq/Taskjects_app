package com.app.taskjects;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import com.app.taskjects.pojos.Empleado;
import com.app.taskjects.pojos.Proyecto;
import com.app.taskjects.utils.Conversor;
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
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ModificarProyectoActivity extends AppCompatActivity {

    private final String EMPLEADOS = "empleados";
    private final String PROYECTOS = "proyectos";
    private final String CATEGORIAS = "categorias";

    //Componentes
    TextInputEditText etNombreProyecto;
    TextInputEditText etDescripcionProyecto;
    AutoCompleteTextView atvJefeEmpleado;
    TextView tvFechaHoraCreacion;

    //Variables para manejar la BBDD
    FirebaseFirestore db;
    DocumentSnapshot document;

    //Variables de control de la clase
    List<String> categoriasJefe;
    boolean modoEdit;
    Map<String,String> mapJefes;
    String nombreJefe;
    String uidProyecto;
    String uidEmpresa;
    boolean cambioJefe;
    String uidJefeProyectoAnterior;

    //Datos del proyecto antes de la actualización
    Proyecto proyecto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.modificar_proyecto_layout);

        //Inicializacion de componentes y variables
        etNombreProyecto = findViewById(R.id.etNombreProyecto);
        etDescripcionProyecto = findViewById(R.id.etDescripcionProyecto);
        atvJefeEmpleado = findViewById(R.id.atvJefeEmpleado);
        tvFechaHoraCreacion = findViewById(R.id.tvFechaHoraCreacion);

        //Recoge datos del intent
        uidProyecto = getIntent().getStringExtra("uidProyecto");
        uidEmpresa = getIntent().getStringExtra("uidEmpresa");

        //Recoge la instancia de la BD
        db = FirebaseFirestore.getInstance();

        //Inicialización de las variables de la clase
        categoriasJefe = new ArrayList<>();
        mapJefes = new HashMap<>();

        //Inicializo la toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Captura el click de volver atrás
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Si está en modo edición...
                if (modoEdit) {
                    mostrarDialogoSalida();
                } else {
                    finish();
                }

            }
        });

        recuperarDatos();
    }

    private void recuperarDatos() {

        DocumentReference docRef = db.collection(PROYECTOS).document(uidProyecto);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    document = task.getResult();
                    if (document.exists()) {
                        Log.d("taskjectsdebug", "Encuentra el documento");
                        recuperarCategoriasTipoJefe();
                    } else {
                        Log.d("taskjectsdebug", "Error! no encuentra el documento");
                        Toast.makeText(ModificarProyectoActivity.this, R.string.errorGeneral, Toast.LENGTH_LONG).show();
                        finish();
                    }
                } else {
                    Log.d("taskjectsdebug", "Error! la tarea de buscar el proyecto no ha ido bien");
                    Toast.makeText(ModificarProyectoActivity.this, R.string.errorAccesoBD, Toast.LENGTH_LONG).show();
                    finish();
                }
            }
        });
    }

    private void recuperarCategoriasTipoJefe() {

        db.collection(CATEGORIAS)
                .whereEqualTo("marca", true)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                categoriasJefe.add(document.getId());
                                Log.d("taskjectsdebug", "Recupera categoría Jefe: " + document.getId());
                            }
                            //Recupera los empleados que son Jefe de Proyecto
                            cargarEmpleadosJefe();
                        } else {
                            Toast.makeText(ModificarProyectoActivity.this, getString(R.string.errorAccesoBD), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void cargarEmpleadosJefe() {

        db.collection(EMPLEADOS)
                .whereEqualTo("uidEmpresa",uidEmpresa)
                .whereIn("categoria",categoriasJefe)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if (!task.getResult().isEmpty()) {
                                Log.d("taskjectsdebug", "encuentra jefes de proyecto");
                                //Me recorro todos los datos que ha devuelto la query
                                for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                                    //Por cada empleado jefe que encuentra lo añade al map
                                    mapJefes.put(documentSnapshot.getString("nombre").concat(" ").concat(documentSnapshot.getString("apellidos")) , documentSnapshot.getId());
                                    if (documentSnapshot.getId().equals(document.getString("uidEmpleadoJefe"))) {
                                        nombreJefe = documentSnapshot.getString("nombre").concat(" ").concat(documentSnapshot.getString("apellidos"));
                                    }
                                }
                                cargarDatosPantalla();
                            } else {
                                //Si task.isEmpty() devuelve true entonces no se han encontrado registros, se lo indico al usuario
                                atvJefeEmpleado.setError(getString(R.string.noSeEncuentranJefes));
                                Log.d("taskjectsdebug","No se han encontrado empleados jefe");
                            }
                        } else {
                            //Si hay algun problema al recuperar datos de la base de datos le muestro al usuario que hay un problema
                            Toast.makeText(ModificarProyectoActivity.this, getString(R.string.errorAccesoBD), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void cargarDatosPantalla() {
        //Obtiene el proyecto desde el DocumentSnapshot. Se quedan guardados sus datos para comprobar posteriormente si se han producido cambios
        proyecto = document.toObject(Proyecto.class);

        etNombreProyecto.setText(proyecto.getNombre());
        etDescripcionProyecto.setText(proyecto.getDescripcion());
        atvJefeEmpleado.setText(nombreJefe);
        tvFechaHoraCreacion.setText(getString(R.string.creadoEl).concat(" ").concat(Conversor.timestampToString(Locale.getDefault(), proyecto.getFechaHoraCreacion())));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_modificar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.itemEditar:
                editar();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void editar() {
        modoEdit = true;
        etNombreProyecto.setEnabled(true);
        etDescripcionProyecto.setEnabled(true);
        //Le añado un adaptador al listado que mostrara los empleados jefe
        atvJefeEmpleado.setAdapter(new ArrayAdapter<>(ModificarProyectoActivity.this,R.layout.lista_jefes_proyecto,new ArrayList<>(mapJefes.keySet())));
        atvJefeEmpleado.setEnabled(true);
        findViewById(R.id.btnModifProyecto).setVisibility(View.VISIBLE);
    }

    public void modificarProyecto(View view) {

        findViewById(R.id.btnModifProyecto).setEnabled(false);

        if (validarDatos()) {

            proyecto.setNombre(etNombreProyecto.getText().toString());
            proyecto.setDescripcion(etDescripcionProyecto.getText().toString());
            proyecto.setUidEmpleadoJefe(mapJefes.get(atvJefeEmpleado.getText().toString()));

            DocumentReference proyectoUpdate = db.collection(PROYECTOS).document(uidProyecto);
            proyectoUpdate.set(proyecto).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Log.d("taskjectsdebug","Proyecto modificado correctamente");

                        if (cambioJefe) {
                            actualizarEmpleadoJefe(uidJefeProyectoAnterior, proyecto.getUid(), true);

                            actualizarEmpleadoJefe(mapJefes.get(atvJefeEmpleado.getText().toString()), proyecto.getUid(), false);
                        } else {
                            AlertDialog.Builder alertaModificacionProyectoCorrecta = new AlertDialog.Builder(ModificarProyectoActivity.this);
                            alertaModificacionProyectoCorrecta.setMessage(getString(R.string.modifProyectoCorrecta))
                                    .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            finish();
                                        }
                                    }).show();
                        }

                    } else {
                        AlertDialog.Builder alertaErrorAccesoBBDD = new AlertDialog.Builder(ModificarProyectoActivity.this);
                        alertaErrorAccesoBBDD.setMessage(getString(R.string.errorAccesoBD))
                                .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        findViewById(R.id.btnModifProyecto).setEnabled(true);
                                        Log.d("taskjectsdebug","Error al subir proyecto a bbdd");
                                    }
                                }).show();
                    }
                }
            });
        } else {
            findViewById(R.id.btnModifProyecto).setEnabled(true);
        }
    }

    private boolean validarDatos() {
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

        //Comprueba si se han producido cambios...
        if (etNombreProyecto.getText().toString().equals(proyecto.getNombre()) &&
                etDescripcionProyecto.getText().toString().equals(proyecto.getDescripcion()) &&
                mapJefes.get(atvJefeEmpleado.getText().toString()).equals(proyecto.getUidEmpleadoJefe())) {
            //Si no se han producido cambios se muestra un Toast y no permite continuar
            Toast.makeText(ModificarProyectoActivity.this, getString(R.string.noHayCambios), Toast.LENGTH_LONG).show();
            creoProyecto = false;
        }

        if (!mapJefes.get(atvJefeEmpleado.getText().toString()).equals(proyecto.getUidEmpleadoJefe())) {
            cambioJefe = true;
            uidJefeProyectoAnterior = proyecto.getUidEmpleadoJefe();
        }

        return creoProyecto;
    }

    private void actualizarEmpleadoJefe(String uidEmpleadoJefe, String uidProyecto, boolean quitar) {

        Log.d("taskjectsdebug","Actualiza los proyectos del empleado jefe: " + uidEmpleadoJefe);
        DocumentReference docRef = db.collection(EMPLEADOS).document(uidEmpleadoJefe);
        docRef.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        Empleado empleado = documentSnapshot.toObject(Empleado.class);
                        if (quitar) {
                            empleado.getUidProyectos().remove(uidProyecto);
                        } else {
                            empleado.getUidProyectos().add(uidProyecto);
                        }
                        db.collection(EMPLEADOS).document(uidEmpleadoJefe)
                                .set(empleado)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        if (!quitar) {
                                            AlertDialog.Builder alertaCreacionProyectoCorrecta = new AlertDialog.Builder(ModificarProyectoActivity.this);
                                            alertaCreacionProyectoCorrecta.setMessage(getString(R.string.creacionProyectoCorrecta))
                                                    .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialogInterface, int i) {
                                                            finish();
                                                        }
                                                    }).show();
                                        }
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        findViewById(R.id.btnModifProyecto).setEnabled(true);
                                        Log.d("taskjectsdebug","Error al subir el empleado a bbdd");
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        findViewById(R.id.btnModifProyecto).setEnabled(true);
                        Log.d("taskjectsdebug","Error al subir el empleado a bbdd");
                    }
                });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (modoEdit) {
                mostrarDialogoSalida();
            } else {
                finish();
            }
        }
        return true;
    }

    private void mostrarDialogoSalida() {

        AlertDialog.Builder alertaSalidaCreacion = new AlertDialog.Builder(ModificarProyectoActivity.this);
        //Si pulsa en cancelar no salgo de la activity
        alertaSalidaCreacion.setMessage(getString(R.string.confirmSalidaCreacionProyecto))
                .setNeutralButton(getString(R.string.cancelar), (dialogInterface, i) -> { })
                .setPositiveButton(getString(R.string.aceptar), (dialogInterface, i) -> finish())
                .show();
    }
}