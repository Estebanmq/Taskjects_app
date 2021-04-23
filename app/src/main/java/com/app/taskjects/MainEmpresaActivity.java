package com.app.taskjects;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.app.taskjects.pojos.Empresa;
import com.app.taskjects.pojos.Proyecto;
import com.app.taskjects.utils.AdaptadorProyectosRV;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.w3c.dom.Document;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class MainEmpresaActivity extends AppCompatActivity {

    //Componentes
    TextView etInfoNoProyectos;
    RecyclerView rvProyectos;

    //Variables para gestionar el usuario de firebase
    FirebaseAuth mAuth;
    FirebaseUser user;
    FirebaseFirestore db;
    String uidEmpresa;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_empresa_layout);

        rvProyectos = findViewById(R.id.rvProyectos);
        rvProyectos.setHasFixedSize(true);

        LinearLayoutManager llm = new LinearLayoutManager(MainEmpresaActivity.this);
        rvProyectos.setLayoutManager(llm);

        //Inicio componentes
        etInfoNoProyectos = findViewById(R.id.tvInfoNoProyectos);

        //Inicio variables
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();

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
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if (!task.getResult().isEmpty() ) {
                                etInfoNoProyectos.setVisibility(TextView.INVISIBLE);
                                for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                                    //Todo: Guardar en la base de datos EL UID DEL EMPLEADO!!!! cambiar el nombre URGENTE
                                    listProyectos.add(new Proyecto(documentSnapshot.getString("uidEmpresa"),
                                            documentSnapshot.getString("nombre"),
                                            documentSnapshot.getString("descripcion"),
                                            documentSnapshot.getString("cifEmpleadoJefe")));

                                            Log.d("MainEmpresaActivityDebug","Proyecto encontrado" + documentSnapshot.getString("nombre")+
                                            documentSnapshot.getString("uidEmpresa"));
                                }
                                AdaptadorProyectosRV adaptadorProyectosRV = new AdaptadorProyectosRV(listProyectos,MainEmpresaActivity.this);
                                rvProyectos.setAdapter(adaptadorProyectosRV);
                            } else {
                                //Si no he recuperado ningun dato le muestro al usuario un texto de que no hay proyectos
                                etInfoNoProyectos.setVisibility(TextView.VISIBLE);
                                Log.d("MainEmpresaAcitivityDebug","No se han recuperado datos de proyectos");

                            }
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("MainEmpresaActivityDebug","Error al acceder a la bbdd");
                    }
                });
    }
}