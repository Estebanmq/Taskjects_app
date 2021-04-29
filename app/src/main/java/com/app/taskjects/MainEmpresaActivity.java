package com.app.taskjects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.app.taskjects.pojos.Proyecto;
import com.app.taskjects.adaptadores.AdaptadorProyectosRV;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainEmpresaActivity extends AppCompatActivity {

    //Todo: Controlar el tamaño de los campos, se salen de los componentes
    //Componentes
    TextView tvInfoNoProyectos;
    RecyclerView rvProyectos;
    BottomAppBar bottomAppBar;

    //Variables para gestionar el usuario de firebase
    FirebaseAuth mAuth;
    FirebaseUser user;
    FirebaseFirestore db;
    String uidEmpresa;

    // Variables de la clase
    Map<String,String> mapJefes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("MainEmpresaActivity","Entro en el onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_empresa_layout);

        rvProyectos = findViewById(R.id.rvProyectos);
        rvProyectos.setHasFixedSize(true);

        LinearLayoutManager llm = new LinearLayoutManager(MainEmpresaActivity.this);
        rvProyectos.setLayoutManager(llm);

        //Inicio componentes
        tvInfoNoProyectos = findViewById(R.id.tvInfoNoProyectos);
        bottomAppBar = findViewById(R.id.bottomAppBar);

        //Inicio variables
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();

        mapJefes = new HashMap<String, String>();

        //Cargo el usuario empresa actual
        cargarUsuarioEmpresa();
    }

    //Metodo que muestra el fragment para añadir un proyecto
    public void aniadirProyecto(View view) {
        Intent pantallaAniadirProyecto = new Intent(MainEmpresaActivity.this,AniadirProyectoActivity.class);
        pantallaAniadirProyecto.putExtra("uidEmpresa",uidEmpresa);
        startActivity(pantallaAniadirProyecto);
    }

    //Cargo el UID de la empresa que ha iniciado sesion en la app y si ok llamo a cargarProyectos()
    private void cargarUsuarioEmpresa() {
        db.collection("empresas")
                .whereEqualTo("uid",user.getUid())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                                uidEmpresa = documentSnapshot.getId();
                                Log.d("MainEmpresaActivityDebug","Empresa actual: "+uidEmpresa);
                                //Una vez que ya he recuperado el usuario cargo sus empleados Jefe
                                cargarEmpleadosJefe();
                            }

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
                                //Me recorro todos los datos que ha devuelto la query
                                for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                                    //Por cada empleado jefe que encuentra lo añade al map
                                    mapJefes.put(documentSnapshot.getId(), documentSnapshot.getString("nombre").concat(" ".concat(documentSnapshot.getString("apellidos"))));
                                }
                                //Una vez que ya he recuperados los usuarios Jefe cargo los proyectos (Promesas de firebase)
                                cargarProyectos();
                            } else {
                                //Si task.isEmpty() devuelve true entonces no se han encontrado registros
                                Log.d("MainEmpresaActivity","No se han encontrado empleados jefe");
                            }
                        } else {
                            //Si hay algun problema al recuperar datos de la base de datos le muestro al usuario que hay un problema
                            AlertDialog.Builder alertaNoDatosBBDD = new AlertDialog.Builder(MainEmpresaActivity.this);
                            alertaNoDatosBBDD.setMessage(getString(R.string.errorAccesoBD))
                                    .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            Log.d("MainEmpresaActivity","Error al recuperar empleados jefe de la bbdd");
                                        }
                                    }).show();
                        }
                    }
                });
    }

    private void cargarProyectos() {
        ArrayList<Proyecto>listProyectos = new ArrayList<>();
        Log.d("MainEmpresaActivityDebug","Empresa en cargarProyecto: "+uidEmpresa);
        db.collection("proyectos")
                .whereEqualTo("uidEmpresa",uidEmpresa)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshot, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            Log.w("MainEmpresaActivityDebug", "Error en el listen");
                            return;
                        }
                        listProyectos.clear();
                        if (snapshot.isEmpty()) {
                            tvInfoNoProyectos.setVisibility(TextView.VISIBLE);
                        } else {
                            tvInfoNoProyectos.setVisibility(TextView.INVISIBLE);
                            for (QueryDocumentSnapshot documentSnapshot : snapshot) {
                                String nombreJefeProyecto = ":: ".concat(mapJefes.get(documentSnapshot.getString("uidEmpleadoJefe"))).concat(" ::");
                                String descripcion = documentSnapshot.getString("descripcion");
                                if (descripcion.length() > 130) {
                                    descripcion = descripcion.substring(0, 130).concat("...");
                                }
                                listProyectos.add(new Proyecto(documentSnapshot.getId(), documentSnapshot.getString("uidEmpresa"),
                                        documentSnapshot.getString("nombre"),
                                        descripcion,
                                        nombreJefeProyecto));
                                Log.d("MainEmpresaActivityDebug", "Proyecto encontrado " + documentSnapshot.getString("nombre") +
                                        documentSnapshot.getString("uidEmpresa"));
                            }

                        }
                        AdaptadorProyectosRV adaptadorProyectosRV = new AdaptadorProyectosRV(listProyectos,MainEmpresaActivity.this);
                        rvProyectos.setAdapter(adaptadorProyectosRV);
                    }
                });
    }


}