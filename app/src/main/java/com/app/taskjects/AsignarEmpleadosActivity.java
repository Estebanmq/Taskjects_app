package com.app.taskjects;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.taskjects.adaptadores.AdaptadorEmpleadosRV;
import com.app.taskjects.pojos.Categoria;
import com.app.taskjects.pojos.Empleado;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class AsignarEmpleadosActivity extends AppCompatActivity {

    private final String EMPLEADOS = "empleados";
    private final String CATEGORIAS = "categorias";

    //Componentes
    RecyclerView rvEmpleados;

    //Variables para manejar la bbdd y sus datos
    FirebaseFirestore db;

    //Variables de control de la clase
    String uidProyecto;
    String uidJefeProyecto;
    String uidEmpresa;
    Map<String, Categoria> mapCategorias;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.asignar_empleados_layout);

        //Recoge datos del intent
        uidProyecto = getIntent().getStringExtra("uidProyecto");
        uidJefeProyecto = getIntent().getStringExtra("uidJefeProyecto");
        uidEmpresa = getIntent().getStringExtra("uidEmpresa");

        //Recoge la instancia de la BD
        db = FirebaseFirestore.getInstance();

        //Map para almacenar las categorías
        mapCategorias = new TreeMap<>();

        //Inicializa los componentes
        rvEmpleados = findViewById(R.id.rvEmpleados);
        rvEmpleados.setHasFixedSize(true);
        rvEmpleados.setLayoutManager(new LinearLayoutManager(AsignarEmpleadosActivity.this));

        //Inicializo la toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.asignarEmpleados));

        //Captura el click de volver atrás
        toolbar.setNavigationOnClickListener(view -> finish());

        recuperarCategorias();
    }

    private void recuperarCategorias() {

        db.collection(CATEGORIAS).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            mapCategorias.put(document.getId(), document.toObject(Categoria.class));
                        }
                        recuperarEmpleados();
                    } else {
                        Log.d("taskjectsdebug", "error en BD al recuperar categorias");
                        Toast.makeText(AsignarEmpleadosActivity.this, getString(R.string.errorAccesoBD), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void recuperarEmpleados() {

        List<Empleado> listEmpleados = new ArrayList<>();
        db.collection(EMPLEADOS)
                .whereEqualTo("uidEmpresa",uidEmpresa)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null || snapshot==null || snapshot.isEmpty()) {
                        Log.d("taskjectsdebug", "error al recuperar empleados");
                        Toast.makeText(AsignarEmpleadosActivity.this, getString(R.string.errorAccesoBD), Toast.LENGTH_LONG).show();
                        return;
                    }
                    listEmpleados.clear();
                    for (QueryDocumentSnapshot documentSnapshot : snapshot) {
                        Empleado empleado = documentSnapshot.toObject(Empleado.class);
                        listEmpleados.add(empleado);
                    }
                    rvEmpleados.setAdapter(new AdaptadorEmpleadosRV(listEmpleados, mapCategorias, uidProyecto, uidJefeProyecto));
                });
    }
}