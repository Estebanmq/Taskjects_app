package com.app.taskjects;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.app.taskjects.pojos.Empleado;
import com.app.taskjects.utils.Validador;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

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

        btRegistrar = findViewById(R.id.btRegistrar);
        lineaProgreso = findViewById(R.id.lineaProgreso);

        //Inicializo la toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Captura el click de volver atrás
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Si hace click en el icono de la flecha para salir de la creacion de proyecto le muestro un pop up de confirmacion
                AlertDialog.Builder alertaSalidaCreacion = new AlertDialog.Builder(RegistroEmpleadoActivity.this);
                alertaSalidaCreacion.setMessage(getString(R.string.confirmSalidaRegistroEmpleado))
                        //Si pulsa en cancelar no salgo de la activity
                        .setNeutralButton(getString(R.string.cancelar), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Log.d("taskjectsdebug","Salgo de la creacion de proyecto");
                            }})
                        .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                            //Si pulsa en de acuerdo cierro la activity
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                finish();
                            }})
                        .show();
            }
        });

        mapCategorias = new LinkedHashMap<String, String>();
        cargarCategorias();

        uidCategoria = "";
        uidEmpresa = "";
    }
    
    private void cargarCategorias() {

        Log.d("taskjectsdebug", "entra a lectura categorías");
        db.collection(CATEGORIAS).get()
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
            validarNifFirestore(); // Valida el NIF contra BD, valida el CIF de la empresa contra BD y realiza las actualizaciones
        }

        btRegistrar.setEnabled(true);
    }

    private boolean validarDatos() {

        boolean resultado = true;

        if (TextUtils.isEmpty(etNif.getText().toString())) {
            etNif.setError(getString(R.string.faltaNif));
            resultado = false;
        } else if (!Validador.validarNif(etNif.getText().toString())) {
            etNif.setError(getString(R.string.nifErroneo));
            resultado = false;
        }

        if (TextUtils.isEmpty(etNombre.getText().toString())) {
            etNombre.setError(getString(R.string.faltaNombre));
            resultado = false;
        }

        if (TextUtils.isEmpty(etApellidos.getText().toString())) {
            etApellidos.setError(getString(R.string.faltaApellidos));
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

        categoriaEmpleado.setError(null);
        if (TextUtils.isEmpty(categoriaEmpleado.getText().toString())) {
            categoriaEmpleado.setError(getString(R.string.faltaCategoria));
            resultado = false;
        } else {
            uidCategoria = mapCategorias.get(categoriaEmpleado.getText().toString());
        }

        if (!resultado) {
            Toast.makeText(RegistroEmpleadoActivity.this, getString(R.string.compruebeDatos), Toast.LENGTH_LONG).show();
        }

        return resultado;
    }

    private void validarNifFirestore() {

        // Pone visible la linea de progreso
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                lineaProgreso.setVisibility(View.VISIBLE);
            }
        });

        CollectionReference empleadosRef = db.collection(EMPLEADOS);
        Query query = empleadosRef.whereEqualTo("nif", etNif.getText().toString().toUpperCase());
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult().isEmpty()) {
                        validarCifFirestore();
                    } else {
                        etNif.setError(getString(R.string.nifYaExiste));
                        lineaProgreso.setVisibility(View.INVISIBLE);
                    }
                } else {
                    Log.d("taskjectsdebug", "RegistroEmpleadoActivity: la tarea no ha ido bien");
                }
            }
        });

    }


    private void validarCifFirestore() {

        CollectionReference empresasRef = db.collection(EMPRESAS);
        Query query = empresasRef.whereEqualTo("cif", etCif.getText().toString().toUpperCase());
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult().isEmpty()) {
                        etCif.setError(getString(R.string.cifNoExiste));
                    } else {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            uidEmpresa = document.getId();
                        }
                        darAltaAuth();
                        Log.d("taskjectsdebug", "es correcto, sí está el cif");
                    }
                    lineaProgreso.setVisibility(View.INVISIBLE);
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
                                    darAltaEmpleado();
                                } else {
                                    lineaProgreso.setVisibility(View.INVISIBLE);
                                    if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                        etEmail.setError(getString(R.string.emailYaExiste));
                                    } else {
                                        Toast.makeText(RegistroEmpleadoActivity.this, getString(R.string.registroCuentaFallido), Toast.LENGTH_SHORT).show();
                                        //Todo: dejar log para arreglar el problema
                                    }
                                    Log.d("taskjectsdebug", "auth: error en task: " + task.getException());
                                }
                            }
                        }
                );

        Log.d("taskjectsdebug", "sale de darAltaAuth");    }


    private void darAltaEmpleado() {

        Log.d("taskjectsdebug", "entra en darAltaEmpresa");
        Empleado empleado = new Empleado(etNif.getText().toString().toUpperCase(), etNombre.getText().toString(), etApellidos.getText().toString(), etEmail.getText().toString(), etPassword.getText().toString(), uidEmpresa, uidCategoria, user.getUid());
        db.collection(EMPLEADOS).add(empleado)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        lineaProgreso.setVisibility(View.INVISIBLE);
                        Log.d("taskjectsdebug", "empresa: entra en onSuccess!");
                        AlertDialog.Builder dialogo = new AlertDialog.Builder(RegistroEmpleadoActivity.this);
                        dialogo.setMessage(getString(R.string.altaEmpleadoDone)).setCancelable(false).setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        }).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        lineaProgreso.setVisibility(View.INVISIBLE);
                        Log.d("taskjectsdebug", "empresa: entra en onFailure! " + e.getMessage());
                        Toast.makeText(RegistroEmpleadoActivity.this, getString(R.string.registroEmpresaFallido), Toast.LENGTH_SHORT).show();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_crear, menu);
        return super.onCreateOptionsMenu(menu);
    }

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
}