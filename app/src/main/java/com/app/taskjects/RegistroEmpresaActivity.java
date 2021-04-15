package com.app.taskjects;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.app.taskjects.pojos.Empresa;
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

    boolean resultado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registro_empresa_layout);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        etCif = (EditText)findViewById(R.id.etCif);
        etNombre = (EditText)findViewById(R.id.etNombre);
        etDireccion = (EditText)findViewById(R.id.etDireccion);
        etEmail = (EditText)findViewById(R.id.etEmail);
        etPassword = (EditText)findViewById(R.id.etPassword);

    }

    public void registrar(View view) {

        String validar = validarDatos();
        if (validar == null) {
            if (this.darAltaAuth()) {
                if (this.darAltaEmpresa()) {
                    Toast.makeText(RegistroEmpresaActivity.this, "Empresa dada de alta correctamente", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            Toast.makeText(RegistroEmpresaActivity.this, validar, Toast.LENGTH_SHORT).show();
        }

    }

    private String validarDatos() {

        if (etCif.getText().toString().isEmpty()) {
            return "Informe el CIF de la empresa";
        } else if (!etCif.getText().toString().matches("^[a-zA-Z]{1}[0-9]{7}[a-zA-Z0-9]{1}$")) {
            return "CIF erróneo";
        }

        if (etNombre.getText().toString().isEmpty()) {
            return "Informe el nombre de la empresa";
        }

        if (etEmail.getText().toString().isEmpty()) {
            return "Informe el email de la empresa";
        } else if (!etEmail.getText().toString().matches("^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9-]+(?:.[a-zA-Z0-9-]+)*$")) {
            return "Email erróneo";
        }

        if (etPassword.getText().toString().isEmpty()) {
            return "Informe la contraseña de acceso";
        } else if (!etPassword.getText().toString().matches("^[A-Za-z0-9$|@#!&*]{6,24}$")) {
            return "Contraseña incorrecta. Teclee entre 6 y 24 caracteres";
        }

        return null;
    }

    private boolean darAltaAuth() {

        mAuth = FirebaseAuth.getInstance();
        mAuth.createUserWithEmailAndPassword(etEmail.getText().toString(), etPassword.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
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
        return resultado;
    }

    private boolean darAltaEmpresa() {

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
        return resultado;
    }

}