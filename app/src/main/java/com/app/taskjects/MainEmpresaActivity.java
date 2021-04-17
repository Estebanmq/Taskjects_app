package com.app.taskjects;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainEmpresaActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_empresa_layout);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        Log.d("Usuario actual",user.getEmail());

    }

    public void aniadirTarea(View view) {

    }
}