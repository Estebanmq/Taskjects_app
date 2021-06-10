package com.app.taskjects;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.taskjects.adaptadores.AdaptadorProyectosRV;
import com.app.taskjects.pojos.Empresa;
import com.app.taskjects.pojos.Proyecto;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainEmpresaActivity extends MenuToolbarActivity {

    private final String EMPRESAS = "empresas";
    private final String CATEGORIAS = "categorias";
    private final String EMPLEADOS = "empleados";
    private final String PROYECTOS = "proyectos";

    //Componentes
    TextView tvInfoNoProyectos;
    RecyclerView rvProyectos;
    BottomAppBar bottomAppBar;
    FloatingActionButton fabCrearProyecto;

    //Variables para gestionar el usuario de firebase
    FirebaseAuth mAuth;
    FirebaseUser user;
    FirebaseFirestore db;
    String uidEmpresa;

    // Variables de la clase
    List<String> categoriasJefe;
    Map<String,String> mapJefes;
    Empresa empresa;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_empresa_layout);

        //Inicializa los componentes
        rvProyectos = findViewById(R.id.rvProyectos);
        rvProyectos.setHasFixedSize(true);

        LinearLayoutManager llm = new LinearLayoutManager(MainEmpresaActivity.this);
        rvProyectos.setLayoutManager(llm);

        //Inicialización de componentes
        tvInfoNoProyectos = findViewById(R.id.tvInfoNoProyectos);
        bottomAppBar = findViewById(R.id.bottomAppBar);
        setSupportActionBar(bottomAppBar);
        fabCrearProyecto = findViewById(R.id.fabCrearProyecto);

        //Inicialización de variables de acceso a BD
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();

        //Inicialización de variables de clase
        categoriasJefe = new ArrayList<>();
        mapJefes = new HashMap<>();
        empresa = new Empresa();

        //Recupera todas las categorías de tipo Jefe de Proyecto (campo marca es true)
        recuperarCategoriasTipoJefe();

    }

    @Override
    protected void onResume() {
        super.onResume();
        fabCrearProyecto.setEnabled(true);
    }

    private void recuperarCategoriasTipoJefe() {

        db.collection(CATEGORIAS)
                .whereEqualTo("marca", true)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            categoriasJefe.add(document.getId());
                        }
                        //Cargo el usuario empresa actual
                        cargarUsuarioEmpresa();
                    } else {
                        Log.d("taskjectsdebug","error en BD al recuperar categorias tipo jefe");
                        Toast.makeText(MainEmpresaActivity.this, getString(R.string.errorAccesoBD), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void cargarUsuarioEmpresa() {

        db.collection(EMPRESAS)
                .whereEqualTo("uidAuth", user.getUid())
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null) {
                        Log.d("taskjectsdebug","error en BD al recuperar el usuario Empresa");
                        Toast.makeText(MainEmpresaActivity.this, getString(R.string.errorAccesoBD), Toast.LENGTH_LONG).show();
                        return;
                    }
                    empresa = snapshot.getDocuments().get(0).toObject(Empresa.class);
                    uidEmpresa = empresa.getUid();
                    //Carga las SharedPreferences de la empresa
                    cargarSharedPreferences(empresa);
                    //Una vez que ya he recuperado el usuario cargo sus empleados Jefe
                    cargarEmpleadosJefe();
                });
    }

    private void cargarEmpleadosJefe() {
        db.collection(EMPLEADOS)
                .whereEqualTo("uidEmpresa", uidEmpresa)
                .whereIn("categoria", categoriasJefe)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (!task.getResult().isEmpty()) {
                            //Me recorro todos los datos que ha devuelto la query
                            for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                                //Por cada empleado jefe que encuentra lo añade al map
                                mapJefes.put(documentSnapshot.getId(), documentSnapshot.getString("nombre").concat(" ").concat(documentSnapshot.getString("apellidos")));
                            }
                        } else {
                            //Si task.isEmpty() devuelve true entonces no se han encontrado registros
                            Toast.makeText(MainEmpresaActivity.this, getString(R.string.noSeEncuentranJefes), Toast.LENGTH_LONG).show();
                            fabCrearProyecto.setVisibility(View.GONE);
                        }
                        //Una vez que ya he recuperados los Jefe de proyecto se cargan cargo los proyectos (Promesas de firebase)
                        cargarProyectos();
                    } else {
                        //Si hay algun problema al recuperar datos de la base de datos le muestro al usuario que hay un problema
                        Toast.makeText(MainEmpresaActivity.this, getString(R.string.errorAccesoBD), Toast.LENGTH_LONG).show();
                        findViewById(R.id.floating_action_button).setVisibility(View.INVISIBLE);
                        Log.d("taskjectsdebug","error en BD al recuperar empleados jefe");
                    }
                });
    }

    private void cargarProyectos() {
        ArrayList<Proyecto>listProyectos = new ArrayList<>();
        db.collection(PROYECTOS)
                .whereEqualTo("uidEmpresa",uidEmpresa)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        Log.d("taskjectsdebug","error en BD al recuperar los proyectos: " + error.getMessage());
                        Toast.makeText(MainEmpresaActivity.this, getString(R.string.errorAccesoBD), Toast.LENGTH_LONG).show();
                        findViewById(R.id.floating_action_button).setVisibility(View.INVISIBLE);
                        return;
                    }
                    listProyectos.clear();
                    if (snapshot == null || snapshot.isEmpty()) {
                        tvInfoNoProyectos.setVisibility(TextView.VISIBLE);
                    } else {
                        tvInfoNoProyectos.setVisibility(TextView.INVISIBLE);
                        for (QueryDocumentSnapshot documentSnapshot : snapshot) {

                            Proyecto proyecto = documentSnapshot.toObject(Proyecto.class);

                            String nombreJefeProyecto = ":: Sin jefe de proyecto ::";
                            if (mapJefes.get(documentSnapshot.getString("uidEmpleadoJefe")) != null) {
                                nombreJefeProyecto = ":: ".concat(mapJefes.get(documentSnapshot.getString("uidEmpleadoJefe"))).concat(" ::");
                            }
                            String descripcion = documentSnapshot.getString("descripcion");
                            if (descripcion.length() > 100) {
                                descripcion = descripcion.substring(0, 100).concat("...");
                            }
                            String nombre = documentSnapshot.getString("nombre");
                            if (nombre.length() > 33) {
                                nombre = nombre.substring(0,33).concat("...");
                            }
                            proyecto.setDescripcion(descripcion);
                            proyecto.setNombre(nombre);
                            proyecto.setNombreEmpleadoJefe(nombreJefeProyecto);
                            listProyectos.add(proyecto);
                        }

                    }
                    AdaptadorProyectosRV adaptadorProyectosRV = new AdaptadorProyectosRV(listProyectos,MainEmpresaActivity.this);
                    rvProyectos.setAdapter(adaptadorProyectosRV);
                });
    }

    //Metodo que muestra el fragment para añadir un proyecto
    public void aniadirProyecto(View view) {
        fabCrearProyecto.setEnabled(false);
        Intent pantallaAniadirProyecto = new Intent(MainEmpresaActivity.this,AniadirProyectoActivity.class);
        pantallaAniadirProyecto.putExtra("uidEmpresa", uidEmpresa);
        startActivity(pantallaAniadirProyecto);
    }

    private void cargarSharedPreferences(Empresa empresa) {

        SharedPreferences.Editor editor = getSharedPreferences(empresa.getUidAuth(), Context.MODE_PRIVATE).edit();
        editor.putString("tipoLogin", "E");
        editor.putString("uidEmpresa", uidEmpresa);
        editor.putString("cif", empresa.getCif());
        editor.putString("nombre", empresa.getNombre());
        editor.putString("direccion", empresa.getDireccion());
        editor.putString("email", empresa.getEmail());
        editor.putString("password", empresa.getPassword());
        editor.putString("uidAuth", empresa.getUidAuth());
        editor.apply();

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            AlertDialog.Builder alertaSalidaAplicacion = new AlertDialog.Builder(this);
            alertaSalidaAplicacion.setMessage(getString(R.string.confirmSalidaAplicacion))
                    //Si pulsa en cancelar no hace nada
                    .setNeutralButton(getString(R.string.cancelar), (dialogInterface, i) -> {
                    })
                    //Si pulsa aceptar borra los sharedPreferences y cierra la aplicación
                    .setPositiveButton(getString(R.string.aceptar), (dialogInterface, i) -> {
                        borrarSharedPreferences();
                        finishAffinity();
                    }).show();
        }
        return true;
    }

}