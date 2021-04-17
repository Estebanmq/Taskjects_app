package com.app.taskjects;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.res.Resources;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
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
import com.google.rpc.context.AttributeContext;

public class RegistroEmpresaActivity extends AppCompatActivity {

    FirebaseFirestore db;
    FirebaseAuth mAuth;
    FirebaseUser user;

    EditText etCif;
    EditText etNombre;
    EditText etDireccion;
    EditText etEmail;
    EditText etPassword;

    boolean resultado;

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

    }

    public void registrar(View view) {

        if (this.validarDatos()) {
            if (this.darAltaAuth()) {
                if (this.darAltaEmpresa()) {
                    Toast.makeText(RegistroEmpresaActivity.this, "Empresa dada de alta correctamente", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private boolean validarDatos() {

        Log.d("debugeando", "entra en validar datos");
        if (etCif.getText().toString().isEmpty()) {
            etCif.setError(getString(R.string.faltaCif));
            return false;
        } else if (!Validador.validarCif(etCif.getText().toString())) {
            etCif.setError(getString(R.string.cifErroneo));
            return false;
        }

        if (etNombre.getText().toString().isEmpty()) {
            etNombre.setError(getString(R.string.faltaNombre));
            return false;
        }

        if (etEmail.getText().toString().isEmpty()) {
            etEmail.setError(getString(R.string.faltaEmail));
            return false;
        } else if (!Validador.validarEmail(etEmail.getText().toString())) {
            etEmail.setError(getString(R.string.emailErroneo));
            return false;
        }

        if (etPassword.getText().toString().isEmpty()) {
            etPassword.setError(getString(R.string.faltaPassword));
            return false;
        } else if (!Validador.validarPassword(etPassword.getText().toString())) {
            etPassword.setError(getString(R.string.passwordErroneo));
            return false;
        }

        Log.d("debugeando", "sale de validar datos");
        return true;
    }

    private boolean darAltaAuth() {

        Log.d("debugeando", "entra en darAltaAuth");
        mAuth = FirebaseAuth.getInstance();
        mAuth.createUserWithEmailAndPassword(etEmail.getText().toString(), etPassword.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        Log.d("debugeando", "entra en onComplete:" + task.isSuccessful());

                        if(task.isSuccessful()) {
                            user = mAuth.getCurrentUser();
                            resultado = true;
                        } else {
                            Toast.makeText(RegistroEmpresaActivity.this, "Registro de cuenta fallido", Toast.LENGTH_SHORT).show();
                            resultado = false;
                        }
                    }
                }
                );

        SystemClock.sleep(1000);
        Log.d("debugeando", "sale de darAltaAuth con resultado:" + resultado);
        return resultado;
    }

    private boolean darAltaEmpresa() {

        Log.d("debugeando", "entra en darAltaEmpresa");
        Empresa empresa = new Empresa(etCif.getText().toString(), etNombre.getText().toString(), etDireccion.getText().toString(), etEmail.getText().toString(), etPassword.getText().toString());
        db.collection("empresas").add(empresa)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        resultado = true;
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(RegistroEmpresaActivity.this, "Registro de empresa fallido", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                        resultado = false;
                    }
                });

        Log.d("debugeando", "sale de darAltaEmpresa con resultado:" + resultado);
        return resultado;
    }

}