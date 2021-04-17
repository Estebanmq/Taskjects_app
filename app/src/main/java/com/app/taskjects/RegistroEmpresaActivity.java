package com.app.taskjects;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.app.taskjects.pojos.Empresa;
import com.app.taskjects.utils.Validador;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;


public class RegistroEmpresaActivity extends AppCompatActivity {

    FirebaseFirestore db;
    FirebaseAuth mAuth;
    FirebaseUser user;

    EditText etCif;
    EditText etNombre;
    EditText etDireccion;
    EditText etEmail;
    EditText etPassword;

    Button btRegistrar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registro_empresa_layout);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        etCif = findViewById(R.id.etCif);
        etNombre = findViewById(R.id.etNombre);
        etDireccion = findViewById(R.id.etDireccion);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);

        btRegistrar = findViewById(R.id.btRegistrar);

    }

    public void registrar(View view) {

        btRegistrar.setEnabled(false);

        if (this.validarDatos()) {
            this.darAltaAuth();
        } else {
            Toast.makeText(RegistroEmpresaActivity.this, getString(R.string.compruebeDatos), Toast.LENGTH_LONG).show();
        }

        btRegistrar.setEnabled(true);
    }

    private boolean validarDatos() {

        boolean resultado = true;

        Log.d("debugeando", "entra en validar datos");
        if (TextUtils.isEmpty(etCif.getText().toString())) {
            etCif.setError(getString(R.string.faltaCif));
            resultado = false;
        } else if (!Validador.validarCif(etCif.getText().toString())) {
            etCif.setError(getString(R.string.cifErroneo));
            resultado = false;
        }

        if (TextUtils.isEmpty(etNombre.getText().toString())) {
            etNombre.setError(getString(R.string.faltaNombre));
            resultado = false;
        }

        if (TextUtils.isEmpty(etEmail.getText().toString())) {
            etEmail.setError(getString(R.string.faltaEmail));
            resultado = false;
        } else if (!Validador.validarEmail(etEmail.getText().toString())) {
            etEmail.setError(getString(R.string.emailErroneo));
            resultado = false;
        }

        if (TextUtils.isEmpty(etPassword.getText().toString())) {
            etPassword.setError(getString(R.string.faltaPassword));
            resultado = false;
        } else if (!Validador.validarPassword(etPassword.getText().toString())) {
            etPassword.setError(getString(R.string.passwordErroneo));
            resultado = false;
        }

        //Todo: Validar contra la BD los datos introducidos: - el CIF y el EMAIL no pueden existir

        Log.d("debugeando", "sale de validar datos: " + resultado);
        return resultado;
    }

    private void darAltaAuth() {

        Log.d("debugeando", "entra en darAltaAuth");
        mAuth = FirebaseAuth.getInstance();
        mAuth.createUserWithEmailAndPassword(etEmail.getText().toString(), etPassword.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        Log.d("debugeando", "auth: entra en onComplete: " + task.isSuccessful());

                        if(task.isSuccessful()) {
                            user = mAuth.getCurrentUser();
                            darAltaEmpresa();
                        } else {
                            Toast.makeText(RegistroEmpresaActivity.this, getString(R.string.registroCuentaFallido), Toast.LENGTH_SHORT).show();
                            //Todo: dejar log para arreglar el problema
                        }
                    }
                }
                );

        Log.d("debugeando", "sale de darAltaAuth");
    }

    private void darAltaEmpresa() {

        Log.d("debugeando", "entra en darAltaEmpresa");
        Empresa empresa = new Empresa(etCif.getText().toString(), etNombre.getText().toString(), etDireccion.getText().toString(), etEmail.getText().toString(), etPassword.getText().toString());
        db.collection("empresas").add(empresa)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d("debugeando", "empresa: entra en onSuccess!");
                        Toast.makeText(RegistroEmpresaActivity.this, getString(R.string.altaEmpresaDone), Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("debugeando", "empresa: entra en onFailure!");
                        Toast.makeText(RegistroEmpresaActivity.this, getString(R.string.registroEmpresaFallido), Toast.LENGTH_SHORT).show();
                        //Todo: dejar log para arreglar el problema
                    }
                });

        Log.d("debugeando", "sale de darAltaEmpresa");
    }

    @Override
    protected void onResume() {
        super.onResume();
        btRegistrar.setEnabled(true);
    }
}