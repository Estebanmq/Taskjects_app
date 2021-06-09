package com.app.taskjects;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.app.taskjects.pojos.Empresa;
import com.app.taskjects.utils.Validador;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;


public class RegistroEmpresaActivity extends AppCompatActivity {

    private final String EMPRESAS = "empresas";

    //Variables para manejar la BBDD
    FirebaseFirestore db;
    FirebaseAuth mAuth;
    FirebaseUser user;

    //Componentes
    EditText etCif;
    EditText etNombre;
    EditText etDireccion;
    EditText etEmail;
    EditText etPassword;

    TextInputLayout outlinedTextFieldCif;
    TextInputLayout outlinedTextFieldNombre;
    TextInputLayout outlinedTextFieldEmail;
    TextInputLayout outlinedTextFieldPassword;

    Button btRegistrar;
    LinearProgressIndicator lineaProgreso;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registro_empresa_layout);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        etCif = findViewById(R.id.etCifEmpresa);
        etNombre = findViewById(R.id.etNombreEmpresa);
        etDireccion = findViewById(R.id.etDireccion);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPasswordEmpleado);

        outlinedTextFieldCif = findViewById(R.id.outlinedTextFieldCif);
        outlinedTextFieldNombre = findViewById(R.id.outlinedTextFieldNombre);
        outlinedTextFieldEmail = findViewById(R.id.outlinedTextFieldEmail);
        outlinedTextFieldPassword = findViewById(R.id.outlinedTextFieldPassword);

        btRegistrar = findViewById(R.id.btRegistrar);
        lineaProgreso = findViewById(R.id.lineaProgreso);

        //Inicializo la toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.registroEmpresa));

        //Captura el click de volver atrás
        toolbar.setNavigationOnClickListener(view -> mostrarDialogoSalida());
    }

    public void registrar(View view) {

        // Pone disabled el botón de registrarse
        btRegistrar.setEnabled(false);

        // Pone visible la linea de progreso
        lineaProgreso.setVisibility(View.VISIBLE);

        if (validarDatos()) {
            validarCifFirestore(); // Valida el CIF contra BD y realiza las actualizaciones
        }
    }

    private boolean validarDatos() {
        outlinedTextFieldCif.setErrorEnabled(false);
        outlinedTextFieldEmail.setErrorEnabled(false);
        outlinedTextFieldNombre.setErrorEnabled(false);
        outlinedTextFieldPassword.setErrorEnabled(false);
        boolean resultado = true;

        if (TextUtils.isEmpty(etCif.getText().toString().trim())) {
            outlinedTextFieldCif.setErrorEnabled(true);
            outlinedTextFieldCif.setError(getString(R.string.faltaCif));
            resultado = false;
        } else if (!Validador.validarCif(etCif.getText().toString().trim())) {
            outlinedTextFieldCif.setErrorEnabled(true);
            outlinedTextFieldCif.setError(getString(R.string.cifErroneo));
            resultado = false;
        }

        if (TextUtils.isEmpty(etNombre.getText().toString().trim())) {
            outlinedTextFieldNombre.setErrorEnabled(true);
            outlinedTextFieldNombre.setError(getString(R.string.faltaNombre));
            resultado = false;
        }

        if (TextUtils.isEmpty(etEmail.getText().toString().trim())) {
            outlinedTextFieldEmail.setErrorEnabled(true);
            outlinedTextFieldEmail.setError(getString(R.string.faltaEmail));
            resultado = false;
        } else if (!Validador.validarEmail(etEmail.getText().toString().trim())) {
            outlinedTextFieldEmail.setErrorEnabled(true);
            outlinedTextFieldEmail.setError(getString(R.string.emailErroneo));
            resultado = false;
        }

        if (TextUtils.isEmpty(etPassword.getText().toString().trim())) {
            outlinedTextFieldPassword.setErrorEnabled(true);
            outlinedTextFieldPassword.setError(getString(R.string.faltaPassword));
            resultado = false;
        } else if (!Validador.validarPassword(etPassword.getText().toString().trim())) {
            outlinedTextFieldPassword.setErrorEnabled(true);
            outlinedTextFieldPassword.setError(getString(R.string.passwordErroneo));
            resultado = false;
        }

        if (!resultado) {
            Toast.makeText(RegistroEmpresaActivity.this, getString(R.string.compruebeDatos), Toast.LENGTH_LONG).show();
            btRegistrar.setEnabled(true);
            lineaProgreso.setVisibility(View.INVISIBLE);
        }

        return resultado;
    }

    private void validarCifFirestore() {
        outlinedTextFieldCif.setErrorEnabled(false);
        CollectionReference empresasRef = db.collection(EMPRESAS);
        Query query = empresasRef.whereEqualTo("cif", etCif.getText().toString().toUpperCase().trim());
        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult().isEmpty()) {
                    darAltaAuth();
                } else {
                    outlinedTextFieldCif.setErrorEnabled(true);
                    outlinedTextFieldCif.setError(getString(R.string.cifYaExiste));
                    btRegistrar.setEnabled(true);
                    lineaProgreso.setVisibility(View.INVISIBLE);
                }
            } else {
                Log.d("taskjectsdebug", "error en BD al validar el CIF ");
                //Si hay algun problema al recuperar datos de la base de datos le muestro al usuario que hay un problema
                Toast.makeText(RegistroEmpresaActivity.this, getString(R.string.errorAccesoBD), Toast.LENGTH_LONG).show();
                lineaProgreso.setVisibility(View.INVISIBLE);
                btRegistrar.setEnabled(true);
            }
        });

    }

    private void darAltaAuth() {
        outlinedTextFieldEmail.setErrorEnabled(false);
        mAuth.createUserWithEmailAndPassword(etEmail.getText().toString().trim(), etPassword.getText().toString().trim())
                .addOnCompleteListener(this, task -> {
                    if(task.isSuccessful()) {
                        user = mAuth.getCurrentUser();
                        darAltaEmpresa();
                    } else {
                        btRegistrar.setEnabled(true);
                        lineaProgreso.setVisibility(View.INVISIBLE);
                        if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                            outlinedTextFieldEmail.setErrorEnabled(true);
                            outlinedTextFieldEmail.setError(getString(R.string.emailYaExiste));
                        } else {
                            Log.d("taskjectsdebug", "error en BD al dar de alta en Auth: " + task.getException().getMessage());
                            Toast.makeText(RegistroEmpresaActivity.this, getString(R.string.registroCuentaFallido), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                );
    }

    private void darAltaEmpresa() {

        Empresa empresa = new Empresa(etCif.getText().toString().toUpperCase().trim(), etNombre.getText().toString().trim(), etDireccion.getText().toString().trim(), etEmail.getText().toString().trim(), etPassword.getText().toString().trim(), user.getUid());
        db.collection(EMPRESAS).add(empresa)
                .addOnSuccessListener(documentReference -> {
                    lineaProgreso.setVisibility(View.INVISIBLE);
                    AlertDialog.Builder dialogo = new AlertDialog.Builder(RegistroEmpresaActivity.this);
                    dialogo.setMessage(getString(R.string.altaEmpresaDone))
                            .setCancelable(false)
                            .setPositiveButton(getString(R.string.ok), (dialog, which) -> finish()).show();
                })
                .addOnFailureListener(e -> {
                    Log.d("taskjectsdebug", "error en BD al dar de alta en Empresa: " + e.getMessage());
                    btRegistrar.setEnabled(true);
                    lineaProgreso.setVisibility(View.INVISIBLE);
                    Toast.makeText(RegistroEmpresaActivity.this, getString(R.string.registroEmpresaFallido), Toast.LENGTH_SHORT).show();
                });

    }

    @Override
    protected void onResume() {
        super.onResume();
        btRegistrar.setEnabled(true);
        lineaProgreso.setVisibility(View.INVISIBLE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_crear, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /*
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
//            case R.id. : // Aquí la opción pulsada
//                .        // Aquí lo que haya que hacer
//                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    */

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            mostrarDialogoSalida();
        }
        return true;
    }

    private void mostrarDialogoSalida() {

        AlertDialog.Builder alertaSalidaCreacion = new AlertDialog.Builder(RegistroEmpresaActivity.this);
        //Si pulsa en cancelar no salgo de la activity
        alertaSalidaCreacion.setMessage(getString(R.string.confirmSalidaRegistroEmpresa))
                .setNeutralButton(getString(R.string.cancelar), (dialogInterface, i) -> { })
                .setPositiveButton(getString(R.string.aceptar), (dialogInterface, i) -> finish())
                .show();
    }
}