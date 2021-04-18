package com.app.taskjects;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.app.taskjects.utils.Validador;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class RegistroEmpleadoActivity extends AppCompatActivity {

    //Componentes
    FirebaseFirestore db;
    FirebaseAuth mAuth;
    FirebaseUser user;
    
    Map<String, String> mapCategorias;    

    EditText etNif;
    EditText etNombre;
    EditText etApellidos;
    EditText etEmail;
    EditText etPassword;
    EditText etCif;
    AutoCompleteTextView categoriaEmpleado;

    Button btRegistrar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registro_empleado_layout);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        etNif = findViewById(R.id.etNifEmpleado);
        etNombre = findViewById(R.id.etNombreEmpleado);
        etApellidos = findViewById(R.id.etApellidosEmpleado);
        etEmail = findViewById(R.id.etEmailEmpleado);
        etPassword = findViewById(R.id.etPasswordEmpleado);
        etCif = findViewById(R.id.etCifEmpresa);
        categoriaEmpleado = findViewById(R.id.categoriaEmpleado);

        btRegistrar = findViewById(R.id.btRegistrar);

        mapCategorias = new LinkedHashMap<String, String>();
        cargarCategorias();

    }
    
    private void cargarCategorias() {

        Log.d("taskjectsdebug", "entra a lectura categorías");
        db.collection("categorias")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                mapCategorias.put(document.getString("descripcion"), document.getId());
                                Log.d("taskjectsdebug", "lectura categorías - dato recuperado:" + document.getId() + " " + document.getString("descripcion"));
                            }

                            // Carga el dropmenu de categorías con los datos recuperados
                            categoriaEmpleado.setAdapter(new ArrayAdapter<String>(RegistroEmpleadoActivity.this, R.layout.lista_categorias, new ArrayList<String>(mapCategorias.keySet())));
                        } else {
                            Toast.makeText(RegistroEmpleadoActivity.this, getString(R.string.errorAccesoBD), Toast.LENGTH_LONG).show();
                            Log.d("taskjectsdebug", "lectura categorías: no se han recuperado datos!");
                        }
                    }
                });
    }

    public void registrar(View view) {

        btRegistrar.setEnabled(false);

        if (validarDatos()) {
            darAltaAuth(); // Valida el CIF contra BD y realiza las actualizaciones
        } else {
            Toast.makeText(RegistroEmpleadoActivity.this, getString(R.string.compruebeDatos), Toast.LENGTH_LONG).show();
        }

        btRegistrar.setEnabled(true);
    }

    private boolean validarDatos() {

        boolean resultado = true;

        if (TextUtils.isEmpty(etNif.getText().toString())) {
            etNif.setError(getString(R.string.faltaNif));
            resultado = false;
        } else if (!Validador.validarCif(etNif.getText().toString())) {
            etNif.setError(getString(R.string.nifErroneo));
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

        if (TextUtils.isEmpty(etCif.getText().toString())) {
            etCif.setError(getString(R.string.faltaCif));
            resultado = false;
        } else if (!Validador.validarCif(etCif.getText().toString())) {
            etCif.setError(getString(R.string.cifErroneo));
            resultado = false;
        }

        return resultado;
    }

    private void darAltaAuth() {

    }

    @Override
    protected void onResume() {
        super.onResume();
        btRegistrar.setEnabled(true);
    }
}