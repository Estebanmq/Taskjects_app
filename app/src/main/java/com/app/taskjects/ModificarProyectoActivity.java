package com.app.taskjects;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import com.app.taskjects.pojos.Proyecto;
import com.app.taskjects.utils.Conversor;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ModificarProyectoActivity extends AppCompatActivity {

    //Componentes
    TextInputEditText etNombreProyecto;
    TextInputEditText etDescripcionProyecto;
    AutoCompleteTextView atvJefeEmpleado;
    TextView tvFechaHoraCreacion;

    //Variables para manejar la BBDD
    FirebaseFirestore db;
    DocumentSnapshot document;

    //Variables de control de la clase
    boolean modoEdit;
    Map<String,String> mapJefesXNombre;
    Map<String,String> mapJefesXUid;
    String uidProyecto;
    String uidEmpresa;

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

        //Aqui almacenare los empleados jefe
        mapJefesXNombre = new HashMap<>();
        mapJefesXUid = new HashMap<>();

        //Inicializo la toolbar
        Toolbar toolbarModificarProyecto = findViewById(R.id.toolbarModificarProyecto);
        setSupportActionBar(toolbarModificarProyecto);
        toolbarModificarProyecto.setTitle(getString(R.string.modifProyecto));
        toolbarModificarProyecto.inflateMenu(R.menu.menu_modificar_proyecto);

        //Muestro el boton de la flecha para volver atras
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbarModificarProyecto.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Si está en modo edición...
                if (modoEdit) {
                    //Si hace click en el icono de la flecha para salir de la creacion de proyecto le muestro un pop up de confirmacion
                    AlertDialog.Builder alertaSalidaCreacion = new AlertDialog.Builder(ModificarProyectoActivity.this);
                    alertaSalidaCreacion.setMessage(getString(R.string.confirmSalidaModifProyecto))
                            //Si pulsa en cancelar no salgo de la activity
                            .setNeutralButton(getString(R.string.cancelar), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Log.d("taskjectsdebug","Salgo de la modificación de proyecto");
                                }
                            }).setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                        //Si pulsa en de acuerdo cierro la activity
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            finish();
                        }
                    }).show();
                } else {
                    finish();
                }

            }
        });

        recuperarDatos();
    }

    private void recuperarDatos() {

        DocumentReference docRef = db.collection("proyectos").document(uidProyecto);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    document = task.getResult();
                    if (document.exists()) {
                        Log.d("taskjectsdebug", "Encuentra el documento");
                        cargarEmpleadosJefe();
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
                                Log.d("taskjectsdebug", "encuentra jefes de proyecto");
                                //Me recorro todos los datos que ha devuelto la query
                                for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                                    //Por cada empleado jefe que encuentra lo añade al map
                                    mapJefesXNombre.put(documentSnapshot.getString("nombre").concat(" ".concat(documentSnapshot.getString("apellidos"))) , documentSnapshot.getId());
                                    mapJefesXUid.put(documentSnapshot.getId(), documentSnapshot.getString("nombre").concat(" ".concat(documentSnapshot.getString("apellidos"))));
                                }
                                //Le añado un adaptador al listado que mostrara los empleados jefe
                                atvJefeEmpleado.setAdapter(new ArrayAdapter<String>(ModificarProyectoActivity.this,R.layout.lista_jefes_proyecto,new ArrayList<>(mapJefesXNombre.keySet())));
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
        Proyecto proyecto = document.toObject(Proyecto.class);
        etNombreProyecto.setText(proyecto.getNombre());
        etDescripcionProyecto.setText(proyecto.getDescripcion());
        atvJefeEmpleado.setText(mapJefesXUid.get(proyecto.getUidEmpleadoJefe()));
        tvFechaHoraCreacion.setText(getString(R.string.creadoEl).concat(" ").concat(Conversor.timestampToString(Locale.getDefault(), proyecto.getFechaHoraCreacion())));
    }
}