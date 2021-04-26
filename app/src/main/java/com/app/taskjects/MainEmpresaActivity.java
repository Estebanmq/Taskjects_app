package com.app.taskjects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.app.taskjects.pojos.Proyecto;
import com.app.taskjects.adaptadores.AdaptadorProyectosRV;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class MainEmpresaActivity extends AppCompatActivity {

    //Todo: Si borro el ultimo proyecto de una empresa no se quita de la interfaz SOLUCIONAR!!
    //Todo: Controlar el tamaño de los campos, se salen de los componentes
    //Componentes
    TextView tvInfoNoProyectos;
    RecyclerView rvProyectos;

    //Variables para gestionar el usuario de firebase
    FirebaseAuth mAuth;
    FirebaseUser user;
    FirebaseFirestore db;
    String uidEmpresa;

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

        //Inicio variables
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();

        //Cargo el usuario empresa actual
        cargarUsuarioEmpresa();
    }


    @Override
    protected void onResume() {
        super.onResume();
        //Llamo a cargar proyectos
        cargarProyectos();
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
                                //Una vez que ya he recuperado el usuario cargo los proyectos (Promesas de firebase)
                                cargarProyectos();
                            }

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
                                listProyectos.add(new Proyecto(documentSnapshot.getString("uidEmpresa"),
                                        documentSnapshot.getString("nombre"),
                                        documentSnapshot.getString("descripcion"),
                                        documentSnapshot.getString("uidEmpleadoJefe")));

                                Log.d("MainEmpresaActivityDebug", "Proyecto encontrado" + documentSnapshot.getString("nombre") +
                                        documentSnapshot.getString("uidEmpresa"));
                            }

                        }
                        AdaptadorProyectosRV adaptadorProyectosRV = new AdaptadorProyectosRV(listProyectos,MainEmpresaActivity.this);
                        rvProyectos.setAdapter(adaptadorProyectosRV);
                    }
                });
    }


}