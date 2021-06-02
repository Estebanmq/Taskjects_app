package com.app.taskjects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.widget.TextView;
import android.widget.Toast;

import com.app.taskjects.adaptadores.AdaptadorProyectosRV;
import com.app.taskjects.pojos.Empleado;
import com.app.taskjects.pojos.Proyecto;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class MainEmpleadoActivity extends MenuToolbarActivity {

    //Constantes
    private final String EMPLEADOS = "empleados";
    private final String PROYECTOS = "proyectos";
    private final String CATEGORIAS = "categorias";


    //Componentes
    RecyclerView rvProyectosEmpleados;
    TextView tvInfoNoProyectos;
    BottomAppBar bottomAppBar;

    //Variables para manejar la bbdd y sus datos
    FirebaseAuth mAuth;
    FirebaseUser user;
    FirebaseFirestore db;

    // Variables de la clase
    List<String> categoriasJefe;
    Map<String,String> mapJefes;
    Empleado empleado;
    String uidEmpresa;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_empleado_layout);

        //Inicializacion de componentes
        tvInfoNoProyectos = findViewById(R.id.tvInfoNoProyectos);
        tvInfoNoProyectos.setVisibility(TextView.INVISIBLE);

        rvProyectosEmpleados = findViewById(R.id.rvProyectosEmpleados);
        rvProyectosEmpleados.setHasFixedSize(true);

        LinearLayoutManager llm = new LinearLayoutManager(MainEmpleadoActivity.this);
        rvProyectosEmpleados.setLayoutManager(llm);

        //Inicialización de la toolbar
        bottomAppBar = findViewById(R.id.bottomAppBar);
        setSupportActionBar(bottomAppBar);

        //Inicialización de variables de acceso a BD
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();

        //Inicialización de variables de clase
        categoriasJefe = new ArrayList<>();
        mapJefes = new TreeMap<String, String>();
        empleado = new Empleado();

        //Recoge el uid de la empresa del empleado logueado
        uidEmpresa = getIntent().getStringExtra("uidEmpresa");

        //Recupera todas las categorías de tipo Jefe de Proyecto (campo marca es true)
        recuperarCategoriasTipoJefe();

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
                            //Carga los Jefes de Proyecto de la empresa a la que pertenece el empleado
                            cargarEmpleadosJefe();
                        } else {
                            Toast.makeText(MainEmpleadoActivity.this, getString(R.string.errorAccesoBD), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void cargarEmpleadosJefe() {
        db.collection(EMPLEADOS)
                .whereEqualTo("uidEmpresa", uidEmpresa)
                .whereIn("categoria", categoriasJefe)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if (!task.getResult().isEmpty()) {
                                //Me recorro todos los datos que ha devuelto la query
                                for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                                    //Por cada empleado jefe que encuentra lo añade al map
                                    mapJefes.put(documentSnapshot.getId(), documentSnapshot.getString("nombre").concat(" ".concat(documentSnapshot.getString("apellidos"))));
                                    Log.d("taskjectsdebug","añade el siguiente empleado Jefe: " + documentSnapshot.getString("nombre"));
                                }
                            }
                            //Una vez recuperados los empleados jefe de la empresa se cargan los datos del empleado que se ha logeado
                            cargarUsuarioEmpleado();
                        } else {
                            //Si hay algun problema al recuperar datos de la base de datos le muestro al usuario que hay un problema
                            Toast.makeText(MainEmpleadoActivity.this, getString(R.string.errorAccesoBD), Toast.LENGTH_LONG).show();
                            Log.d("taskjectsdebug","ha habido algún error en la recuperación de empleados jefe");
                        }
                    }
                });
    }

    private void cargarUsuarioEmpleado() {
        db.collection(EMPLEADOS)
                .whereEqualTo("uidAuth", user.getUid())
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshot, @Nullable FirebaseFirestoreException error) {
                        if (error == null) {
                            empleado = snapshot.getDocuments().get(0).toObject(Empleado.class);
                            Log.d("taskjectsdebug", "Empleado actual -> " + empleado.getUid());
                            //Carga las SharedPreferences del empleado
                            cargarSharedPreferences(empleado);
                            //Una vez que recuperados los datos del empleado se cargan sus proyectos (Promesas de firebase)
                            cargarProyectos();
                        } else {
                            Toast.makeText(MainEmpleadoActivity.this, getString(R.string.errorAccesoBD), Toast.LENGTH_LONG).show();
                            Log.d("taskjectsdebug","ha habido algún error en la recuperación del empleado");
                        }
                    }
                });
    }

    private void cargarProyectos() {

        if (!empleado.getUidProyectos().isEmpty()) {
            tvInfoNoProyectos.setVisibility(TextView.INVISIBLE);
            db.collection(PROYECTOS)
                    .whereIn(FieldPath.documentId(), empleado.getUidProyectos())
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot snapshot, @Nullable FirebaseFirestoreException error) {
                            if (error == null) {
                                if (snapshot != null && !snapshot.isEmpty()) {
                                    List<Proyecto> listProyectos = new ArrayList<>();
                                    for (QueryDocumentSnapshot doc : snapshot) {
                                        Proyecto proyecto = doc.toObject(Proyecto.class);

                                        String nombreJefeProyecto = ":: Sin jefe de proyecto ::";
                                        if (mapJefes.get(proyecto.getUidEmpleadoJefe()) != null) {
                                            nombreJefeProyecto = ":: ".concat(mapJefes.get(proyecto.getUidEmpleadoJefe())).concat(" ::");
                                        }
                                        String descripcion = proyecto.getDescripcion();
                                        if (descripcion.length() > 100) {
                                            descripcion = descripcion.substring(0, 100).concat("...");
                                        }

                                        String nombre = proyecto.getNombre();
                                        if (nombre.length() > 33) {
                                            nombre = nombre.substring(0,33).concat("...");
                                        }

                                        proyecto.setDescripcion(descripcion);
                                        proyecto.setNombre(nombre);
                                        proyecto.setNombreEmpleadoJefe(nombreJefeProyecto);
                                        listProyectos.add(proyecto);
                                    }
                                    AdaptadorProyectosRV adaptadorProyectosRV = new AdaptadorProyectosRV(listProyectos,MainEmpleadoActivity.this);
                                    rvProyectosEmpleados.setAdapter(adaptadorProyectosRV);

                                } else {
                                    Toast.makeText(MainEmpleadoActivity.this, getString(R.string.errorGeneral), Toast.LENGTH_LONG).show();
                                    Log.d("taskjectsdebug","no recupera los proyectos del empleado");
                                }
                            } else {
                                Toast.makeText(MainEmpleadoActivity.this, getString(R.string.errorAccesoBD), Toast.LENGTH_LONG).show();
                                Log.d("taskjectsdebug","ha habido algún error en la recuperación de los proyectos");
                            }
                        }
                    });
        } else {
            tvInfoNoProyectos.setVisibility(TextView.VISIBLE);
            rvProyectosEmpleados.setAdapter(null);
        }

    }

    private void cargarSharedPreferences(Empleado empleado) {

        SharedPreferences.Editor editor = getSharedPreferences(empleado.getUidAuth(), Context.MODE_PRIVATE).edit();
        editor.putString("tipoLogin","C");
        editor.putString("uidEmpleado", empleado.getUid());
        editor.putString("nif", empleado.getNif());
        editor.putString("nombre", empleado.getNombre());
        editor.putString("apellidos", empleado.getApellidos());
        editor.putString("email", empleado.getEmail());
        editor.putString("password", empleado.getPassword());
        editor.putString("uidAuth", empleado.getUidAuth());
        editor.putString("uidEmpresa", empleado.getUidEmpresa());
        editor.putString("categoria", empleado.getCategoria());
        editor.apply();
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Log.d("taskjectsdebug","Pulsado botón atrás en la activity");
            AlertDialog.Builder alertaSalidaAplicacion = new AlertDialog.Builder(this);
            alertaSalidaAplicacion.setMessage(getString(R.string.confirmSalidaAplicacion))
                    //Si pulsa en cancelar no hace nada
                    .setNeutralButton(getString(R.string.cancelar), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Log.d("taskjectsdebug","Ha pulsado cancelar, no se hace nada");
                        }})
                    .setPositiveButton(getString(R.string.aceptar), new DialogInterface.OnClickListener() {
                        //Si pulsa en de acuerdo cierra la aplicación
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            borrarSharedPreferences();
                            finishAffinity();
                        }}).show();
        }

        return true;
    }

}