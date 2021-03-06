package com.app.taskjects;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.app.taskjects.pojos.Empleado;
import com.app.taskjects.utils.Validador;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class RegistroEmpleadoActivity extends AppCompatActivity {

    final String EMPLEADOS = "empleados";
    final String CATEGORIAS = "categorias";
    final String EMPRESAS = "empresas";

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

    TextInputLayout outlinedTextFieldNifEmpleado;
    TextInputLayout outlinedTextFieldNombre;
    TextInputLayout outlinedTextFieldApellidos;
    TextInputLayout outlinedTextFieldEmailEmpleado;
    TextInputLayout outlinedTextFieldPassword;
    TextInputLayout outlinedTextFieldCif;
    TextInputLayout outlinedTextFieldCategoria;

    Button btRegistrar;
    LinearProgressIndicator lineaProgreso;

    String uidCategoria;
    String uidEmpresa;

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
        categoriaEmpleado = findViewById(R.id.etCategoriaEmpleado);
        categoriaEmpleado.setKeyListener(null);

        outlinedTextFieldNifEmpleado = findViewById(R.id.outlinedTextFieldNifEmpleado);
        outlinedTextFieldNombre = findViewById(R.id.outlinedTextFieldNombre);
        outlinedTextFieldApellidos = findViewById(R.id.outlinedTextFieldApellidos);
        outlinedTextFieldEmailEmpleado = findViewById(R.id.outlinedTextFieldEmailEmpleado);
        outlinedTextFieldPassword = findViewById(R.id.outlinedTextFieldPassword);
        outlinedTextFieldCif = findViewById(R.id.outlinedTextFieldCif);
        outlinedTextFieldCategoria = findViewById(R.id.outlinedTextFieldCategoria);

        btRegistrar = findViewById(R.id.btRegistrar);
        lineaProgreso = findViewById(R.id.lineaProgreso);

        //Inicializo las variables de la clase
        mapCategorias = new LinkedHashMap<>();
        uidCategoria = "";
        uidEmpresa = "";

        //Inicializo la toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.registroEmpleado));

        //Captura el click de volver atr??s
        toolbar.setNavigationOnClickListener(view -> mostrarDialogoSalida());

        cargarCategorias();
    }
    
    private void cargarCategorias() {

        db.collection(CATEGORIAS).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            mapCategorias.put(document.getString("descripcion"), document.getId());
                        }
                        // Carga el dropmenu de categor??as con los datos recuperados
                        categoriaEmpleado.setAdapter(new ArrayAdapter<>(RegistroEmpleadoActivity.this, R.layout.lista_categorias, new ArrayList<>(mapCategorias.keySet())));
                    } else {
                        Toast.makeText(RegistroEmpleadoActivity.this, getString(R.string.errorAccesoBD), Toast.LENGTH_LONG).show();
                    }
                });
    }

    public void registrar(View view) {

        // Pone disabled el bot??n de registrarse
        btRegistrar.setEnabled(false);

        // Pone visible la linea de progreso
        lineaProgreso.setVisibility(View.VISIBLE);

        if (validarDatos()) {
            validarNifFirestore(); // Valida el NIF contra BD, valida el CIF de la empresa contra BD y realiza las actualizaciones
        }
    }

    private boolean validarDatos() {
        outlinedTextFieldNifEmpleado.setErrorEnabled(false);
        outlinedTextFieldNombre.setErrorEnabled(false);
        outlinedTextFieldApellidos.setErrorEnabled(false);
        outlinedTextFieldEmailEmpleado.setErrorEnabled(false);
        outlinedTextFieldPassword.setErrorEnabled(false);
        outlinedTextFieldCategoria.setErrorEnabled(false);
        outlinedTextFieldCif.setErrorEnabled(false);
        boolean resultado = true;

        if (TextUtils.isEmpty(etNif.getText().toString().trim())) {
            outlinedTextFieldNifEmpleado.setErrorEnabled(true);
            outlinedTextFieldNifEmpleado.setError(getString(R.string.faltaNif));
            resultado = false;
        } else if (!Validador.validarNif(etNif.getText().toString().trim())) {
            outlinedTextFieldNifEmpleado.setErrorEnabled(true);
            outlinedTextFieldNifEmpleado.setError(getString(R.string.nifErroneo));
            resultado = false;
        }

        if (TextUtils.isEmpty(etNombre.getText().toString().trim())) {
            outlinedTextFieldNombre.setErrorEnabled(true);
            outlinedTextFieldNombre.setError(getString(R.string.faltaNombre));
            resultado = false;
        }

        if (TextUtils.isEmpty(etApellidos.getText().toString().trim())) {
            outlinedTextFieldApellidos.setErrorEnabled(true);
            outlinedTextFieldApellidos.setError(getString(R.string.faltaApellidos));
            resultado = false;
        }

        if (TextUtils.isEmpty(etEmail.getText().toString().trim())) {
            outlinedTextFieldEmailEmpleado.setErrorEnabled(true);
            outlinedTextFieldEmailEmpleado.setError(getString(R.string.faltaEmail));
            resultado = false;
        } else if (!Validador.validarEmail(etEmail.getText().toString().trim())) {
            outlinedTextFieldEmailEmpleado.setErrorEnabled(true);
            outlinedTextFieldEmailEmpleado.setError(getString(R.string.emailErroneo));
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

        if (TextUtils.isEmpty(etCif.getText().toString().trim())) {
            outlinedTextFieldCif.setErrorEnabled(true);
            outlinedTextFieldCif.setError(getString(R.string.faltaCif));
            resultado = false;
        } else if (!Validador.validarCif(etCif.getText().toString().trim())) {
            outlinedTextFieldCif.setErrorEnabled(true);
            outlinedTextFieldCif.setError(getString(R.string.cifErroneo));
            resultado = false;
        }

        outlinedTextFieldCategoria.setError(null);
        if (TextUtils.isEmpty(categoriaEmpleado.getText().toString())) {
            outlinedTextFieldCategoria.setErrorEnabled(true);
            outlinedTextFieldCategoria.setError(getString(R.string.faltaCategoria));
            resultado = false;
        } else {
            uidCategoria = mapCategorias.get(categoriaEmpleado.getText().toString());
        }

        if (!resultado) {
            Toast.makeText(RegistroEmpleadoActivity.this, getString(R.string.compruebeDatos), Toast.LENGTH_LONG).show();
            btRegistrar.setEnabled(true);
            lineaProgreso.setVisibility(View.INVISIBLE);
        }

        return resultado;
    }

    private void validarNifFirestore() {
        outlinedTextFieldNifEmpleado.setErrorEnabled(false);
        CollectionReference empleadosRef = db.collection(EMPLEADOS);
        Query query = empleadosRef.whereEqualTo("nif", etNif.getText().toString().toUpperCase().trim());
        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult().isEmpty()) {
                    validarCifFirestore();
                } else {
                    outlinedTextFieldNifEmpleado.setErrorEnabled(true);
                    outlinedTextFieldNifEmpleado.setError(getString(R.string.nifYaExiste));
                    lineaProgreso.setVisibility(View.INVISIBLE);
                    btRegistrar.setEnabled(true);
                }
            } else {
                Log.d("taskjectsdebug", "error en BD al validar NIF");
                //Si hay algun problema al recuperar datos de la base de datos le muestro al usuario que hay un problema
                Toast.makeText(RegistroEmpleadoActivity.this, getString(R.string.errorAccesoBD), Toast.LENGTH_LONG).show();
                lineaProgreso.setVisibility(View.INVISIBLE);
                btRegistrar.setEnabled(true);
            }
        });

    }


    private void validarCifFirestore() {
        outlinedTextFieldCif.setErrorEnabled(false);
        CollectionReference empresasRef = db.collection(EMPRESAS);
        Query query = empresasRef.whereEqualTo("cif", etCif.getText().toString().toUpperCase().trim());
        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult().isEmpty()) {
                    outlinedTextFieldCif.setErrorEnabled(true);
                    outlinedTextFieldCif.setError(getString(R.string.cifNoExiste));
                    lineaProgreso.setVisibility(View.INVISIBLE);
                    btRegistrar.setEnabled(true);
                } else {
                    uidEmpresa = task.getResult().getDocuments().get(0).getId();
                    darAltaAuth();
                }
            } else {
                Log.d("taskjectsdebug", "error en BD al validar CIF");
                //Si hay algun problema al recuperar datos de la base de datos le muestro al usuario que hay un problema
                Toast.makeText(RegistroEmpleadoActivity.this, getString(R.string.errorAccesoBD), Toast.LENGTH_LONG).show();
                btRegistrar.setEnabled(true);
                lineaProgreso.setVisibility(View.INVISIBLE);
            }
        });

    }

    private void darAltaAuth() {
        outlinedTextFieldEmailEmpleado.setErrorEnabled(false);
        mAuth.createUserWithEmailAndPassword(etEmail.getText().toString().trim(), etPassword.getText().toString().trim())
                .addOnCompleteListener(this, task -> {
                    if(task.isSuccessful()) {
                        user = mAuth.getCurrentUser();
                        darAltaEmpleado();
                    } else {
                        lineaProgreso.setVisibility(View.INVISIBLE);
                        btRegistrar.setEnabled(true);
                        if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                            outlinedTextFieldEmailEmpleado.setErrorEnabled(true);
                            outlinedTextFieldEmailEmpleado.setError(getString(R.string.emailYaExiste));
                        } else {
                            Log.d("taskjectsdebug", "auth: error al dar de alta");
                            Toast.makeText(RegistroEmpleadoActivity.this, getString(R.string.registroCuentaFallido), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                );
        }


    private void darAltaEmpleado() {

        Empleado empleado = new Empleado(etNif.getText().toString().toUpperCase().trim(), etNombre.getText().toString().trim(), etApellidos.getText().toString().trim(), etEmail.getText().toString().trim(), etPassword.getText().toString().trim(), uidEmpresa, uidCategoria, user.getUid());
        db.collection(EMPLEADOS).add(empleado)
                .addOnSuccessListener(documentReference -> {
                    lineaProgreso.setVisibility(View.INVISIBLE);
                    AlertDialog.Builder dialogo = new AlertDialog.Builder(RegistroEmpleadoActivity.this);
                    dialogo.setMessage(getString(R.string.altaEmpleadoDone))
                            .setCancelable(false)
                            .setPositiveButton(getString(R.string.ok), (dialog, which) -> finish()).show();
                })
                .addOnFailureListener(e -> {
                    Log.d("taskjectsdebug", "error en BD al dar de alta el empleado: " + e.getMessage());
                    btRegistrar.setEnabled(true);
                    lineaProgreso.setVisibility(View.INVISIBLE);
                    Toast.makeText(RegistroEmpleadoActivity.this, getString(R.string.registroEmpleadoFallido), Toast.LENGTH_SHORT).show();
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
//            case R.id. : // Aqu?? la opci??n pulsada
//                .        // Aqu?? lo que haya que hacer
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

        AlertDialog.Builder alertaSalidaCreacion = new AlertDialog.Builder(RegistroEmpleadoActivity.this);
        //Si pulsa en cancelar no salgo de la activity
        alertaSalidaCreacion.setMessage(getString(R.string.confirmSalidaRegistroEmpleado))
                .setNeutralButton(getString(R.string.cancelar), (dialogInterface, i) -> { })
                .setPositiveButton(getString(R.string.aceptar), (dialogInterface, i) -> finish())
                .show();
    }
}