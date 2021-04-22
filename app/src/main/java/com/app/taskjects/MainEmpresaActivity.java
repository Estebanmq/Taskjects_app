package com.app.taskjects;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.app.taskjects.pojos.Empresa;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.List;

public class MainEmpresaActivity extends AppCompatActivity {

    //Variables para gestionar el usuario de firebase
    FirebaseAuth mAuth;
    FirebaseUser user;
    FirebaseFirestore db;
    String uidEmpresa;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_empresa_layout);

        //Inicio variables
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        cargarUsuarioEmpresa();

    }

    //Metodo que muestra el fragment para añadir un proyecto
    public void aniadirProyecto(View view) {
        Intent pantallaAniadirProyecto = new Intent(MainEmpresaActivity.this,AniadirProyectoActivity.class);
        pantallaAniadirProyecto.putExtra("uidEmpresa",uidEmpresa);
        startActivity(pantallaAniadirProyecto);

    }


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
                                Log.d("UID Empresa",uidEmpresa);
                            }

                        }

                    }
                });


    }
}