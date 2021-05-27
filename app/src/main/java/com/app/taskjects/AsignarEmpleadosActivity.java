package com.app.taskjects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.app.taskjects.adaptadores.AdaptadorEmpleadosRV;
import com.app.taskjects.pojos.Categoria;
import com.app.taskjects.pojos.Empleado;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

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
    FirebaseAuth mAuth;
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

        //Captura el click de volver atrás
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        recuperarCategorias();
    }

    private void recuperarCategorias() {

        db.collection(CATEGORIAS).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                mapCategorias.put(document.getId(), document.toObject(Categoria.class));
                            }
                            recuperarEmpleados();
                        } else {
                            Toast.makeText(AsignarEmpleadosActivity.this, getString(R.string.errorAccesoBD), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void recuperarEmpleados() {

        Log.d("taskjectsdebug", "entra a recuperar empleados");
        List<Empleado> listEmpleados = new ArrayList<>();
        db.collection(EMPLEADOS)
                .whereEqualTo("uidEmpresa",uidEmpresa)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshot, @Nullable FirebaseFirestoreException error) {
                        if (error != null || snapshot.isEmpty()) {
                            Toast.makeText(AsignarEmpleadosActivity.this, getString(R.string.errorAccesoBD), Toast.LENGTH_LONG).show();
                            return;
                        }
                        listEmpleados.clear();
                        Log.d("taskjectsdebug", "lee empleados");
                        for (QueryDocumentSnapshot documentSnapshot : snapshot) {
                            Empleado empleado = documentSnapshot.toObject(Empleado.class);
                            Log.d("taskjectsdebug", "carga empleado " + empleado.getNombre());
                            listEmpleados.add(empleado);
                        }
                        rvEmpleados.setAdapter(new AdaptadorEmpleadosRV(listEmpleados, mapCategorias, uidProyecto, uidJefeProyecto));
                    }
                });
    }
}