package com.app.taskjects;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;

public class TareasProyectoActivity extends MenuToolbarActivity {


    FloatingActionButton fABTareas;
    BottomAppBar bottomAppBar;

    String uidProyecto;

    FirebaseAuth mAuth;

    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tareas_proyecto_layout);

        //Inicializacion de variables
        bottomAppBar = findViewById(R.id.bottomAppBar);
        setSupportActionBar(bottomAppBar);
        fABTareas = findViewById(R.id.fABTareas);

        uidProyecto = getIntent().getStringExtra("uidProyecto");

        mAuth = FirebaseAuth.getInstance();
        sharedPreferences = getSharedPreferences(mAuth.getUid(), Context.MODE_PRIVATE);

        if (sharedPreferences.getString("categoria", "").equals("1")) {
            findViewById(R.id.fABTareas).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.fABTareas).setVisibility(View.INVISIBLE);
        }

    }

    public void aniadirTarea(View view) {
        Intent pantallaAniadirTarea = new Intent(this,AniadirTareaActivity.class);
        pantallaAniadirTarea.putExtra("uidProyecto",uidProyecto);
        startActivity(pantallaAniadirTarea);
    }
}