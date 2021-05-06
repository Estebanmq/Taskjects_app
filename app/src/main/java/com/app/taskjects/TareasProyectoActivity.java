package com.app.taskjects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.app.taskjects.adaptadores.AdaptadorProyectosRV;
import com.app.taskjects.adaptadores.AdaptadorTareasRV;
import com.app.taskjects.pojos.Proyecto;
import com.app.taskjects.pojos.Tarea;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashSet;

public class TareasProyectoActivity extends MenuToolbarActivity {

    RecyclerView rvTareasIniciales;
    FloatingActionButton fABTareas;
    BottomAppBar bottomAppBar;

    AdaptadorTareasRV adaptadorTareasRV;
    ArrayList<Tarea> listTareas;
    String uidProyecto;
    String uidEmpresa;

    //Variables para manejar la bbdd y sus datos
    FirebaseAuth mAuth;
    DatabaseReference mDatabase;

    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tareas_proyecto_layout);

        //Inicializacion de variables
        bottomAppBar = findViewById(R.id.bottomAppBar);
        setSupportActionBar(bottomAppBar);
        fABTareas = findViewById(R.id.fABTareas);

        listTareas = new ArrayList<>();
        rvTareasIniciales = findViewById(R.id.rvTareasIniciales);
        rvTareasIniciales.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(TareasProyectoActivity.this);
        rvTareasIniciales.setLayoutManager(llm);
        adaptadorTareasRV = new AdaptadorTareasRV(listTareas,TareasProyectoActivity.this);
        rvTareasIniciales.setAdapter(adaptadorTareasRV);


        uidProyecto = getIntent().getStringExtra("uidProyecto");
        uidEmpresa = getIntent().getStringExtra("uidEmpresa");

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        sharedPreferences = getSharedPreferences(mAuth.getUid(), Context.MODE_PRIVATE);

        if (sharedPreferences.getString("categoria", "").equals("1")) {
            findViewById(R.id.fABTareas).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.fABTareas).setVisibility(View.INVISIBLE);
        }
        cargarTareasProyecto();

    }

    private void cargarTareasProyecto() {
        mDatabase.child("tareas").child(uidEmpresa).child(uidProyecto).addChildEventListener(new ChildEventListener() {
            //El onChildAdded viaja por todos los hijos, el nombre puede confundir
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                listTareas.add(snapshot.getValue(Tarea.class));
                adaptadorTareasRV.notifyDataSetChanged();

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                adaptadorTareasRV.notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                Tarea tarea = snapshot.getValue(Tarea.class);

                //Todo: Esto funciona solo con el nombre, si dos tienen el mismo que? implementar el metodo equals propio?
                for (int i=0;i<listTareas.size();i++) {
                    if (listTareas.get(i).getUidTarea().equals(tarea.getUidTarea())) {
                        listTareas.remove(i);
                        break;
                    }
                }
                adaptadorTareasRV.notifyDataSetChanged();

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                adaptadorTareasRV.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void aniadirTarea(View view) {
        Intent pantallaAniadirTarea = new Intent(this,AniadirTareaActivity.class);
        pantallaAniadirTarea.putExtra("uidProyecto",uidProyecto);
        startActivity(pantallaAniadirTarea);
    }
}