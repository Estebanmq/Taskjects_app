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
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;


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

        etCif = findViewById(R.id.etCifEmpresa);
        etNombre = findViewById(R.id.etNombreEmpleado);
        etDireccion = findViewById(R.id.etDireccion);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPasswordEmpleado);

        btRegistrar = findViewById(R.id.btRegistrar);

    }

    public void registrar(View view) {

        btRegistrar.setEnabled(false);

        if (validarDatos()) {
            validarCifFirestore(); // Valida el CIF contra BD y realiza las actualizaciones
        } else {
            Toast.makeText(RegistroEmpresaActivity.this, getString(R.string.compruebeDatos), Toast.LENGTH_LONG).show();
        }

        btRegistrar.setEnabled(true);
    }

    private boolean validarDatos() {

        boolean resultado = true;

        Log.d("taskjects", "entra en validar datos");
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

        return resultado;
    }

    private void validarCifFirestore() {

        CollectionReference empresasRef = db.collection("empresas");
        Query query = empresasRef.whereEqualTo("cif", etCif.getText().toString());
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult().isEmpty()) {
                        darAltaAuth();
                        Log.d("taskjectsdebug", "es correcto, no está el cif");
                    } else {
                        etCif.setError(getString(R.string.cifYaExtiste));
                    }
                } else {
                    Log.d("taskjectsdebug", "la tarea no ha ido bien");
                }
            }
        });

    }

    private void darAltaAuth() {

        Log.d("taskjectsdebug", "entra en darAltaAuth");
        mAuth.createUserWithEmailAndPassword(etEmail.getText().toString(), etPassword.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()) {
                            user = mAuth.getCurrentUser();
                            darAltaEmpresa();
                        } else {
                            if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                etEmail.setError(getString(R.string.emailYaExiste));
                            } else {
                                Toast.makeText(RegistroEmpresaActivity.this, getString(R.string.registroCuentaFallido), Toast.LENGTH_SHORT).show();
                                //Todo: dejar log para arreglar el problema
                            }

                            Log.d("taskjectsdebug", "auth: error en task: " + task.getException());
                        }
                    }
                }
                );

        Log.d("taskjectsdebug", "sale de darAltaAuth");
    }

    private void darAltaEmpresa() {

        Log.d("taskjectsdebug", "entra en darAltaEmpresa");
        Empresa empresa = new Empresa(etCif.getText().toString(), etNombre.getText().toString(), etDireccion.getText().toString(), etEmail.getText().toString(), etPassword.getText().toString(), user.getUid());
        db.collection("empresas").add(empresa)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d("taskjectsdebug", "empresa: entra en onSuccess!");
                        Toast.makeText(RegistroEmpresaActivity.this, getString(R.string.altaEmpresaDone), Toast.LENGTH_SHORT).show();

                        //Todo: volver a la activity anterior o a la activity de empresas
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("taskjectsdebug", "empresa: entra en onFailure! " + e.getMessage());
                        Toast.makeText(RegistroEmpresaActivity.this, getString(R.string.registroEmpresaFallido), Toast.LENGTH_SHORT).show();
                        //Todo: dejar log para arreglar el problema
                    }
                });

        Log.d("taskjectsdebug", "sale de darAltaEmpresa");
    }

    @Override
    protected void onResume() {
        super.onResume();
        btRegistrar.setEnabled(true);
    }
}