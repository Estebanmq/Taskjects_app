package com.app.taskjects;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.app.taskjects.pojos.Empleado;
import com.app.taskjects.pojos.Empresa;
import com.app.taskjects.utils.Conversor;
import com.app.taskjects.utils.Validador;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class PerfilEmpleadoActivity extends AppCompatActivity {

    private final String EMPLEADOS = "empleados";
    private final String CATEGORIAS = "categorias";

    //Variables para manejar la BBDD
    FirebaseFirestore db;
    FirebaseAuth mAuth;
    FirebaseUser user;

    //Componentes
    EditText etNif;
    EditText etNombre;
    EditText etApellidos;
    EditText etEmail;
    AutoCompleteTextView categoriaEmpleado;

    TextView tvFechaHoraCreacion;
    TextView tvFechaHoraUltimoLogin;

    //Variables de control de la clase
    boolean modoEdit;
    Map<String, String> mapCategorias;

    //Variables para cargar los datos previos a la modificación
    String nif;
    String nombre;
    String apellidos;
    String categoria;

    String uidCategoria;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.perfil_empleado_layout);

        //Inicializo componentes
        etNif = findViewById(R.id.etNifEmpleado);
        etNombre = findViewById(R.id.etNombreEmpleado);
        etApellidos = findViewById(R.id.etApellidosEmpleado);
        etEmail = findViewById(R.id.etEmailEmpleado);
        categoriaEmpleado = findViewById(R.id.etCategoriaEmpleado);
        categoriaEmpleado.setKeyListener(null);

        //Inicializo la toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Captura el click de volver atrás
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("taskjectsdebug", "Captura el click de volver atrás en la toolbar");
                //Si está en modo edición...
                if (modoEdit) {
                    //Si hace click en el icono de la flecha para salir de la creacion de proyecto le muestro un pop up de confirmacion
                    AlertDialog.Builder alertaSalidaCreacion = new AlertDialog.Builder(PerfilEmpleadoActivity.this);
                    alertaSalidaCreacion.setMessage(getString(R.string.confirmSalidaModifPerfil))
                            //Si pulsa en cancelar no salgo de la activity
                            .setNeutralButton(getString(R.string.cancelar), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Log.d("taskjectsdebug","Ha pulsado cancelar, no se hace nada");
                                }})
                            .setPositiveButton(getString(R.string.aceptar), new DialogInterface.OnClickListener() {
                                //Si pulsa en de acuerdo cierro la activity
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    finish();
                                }}).show();
                } else {
                    finish();
                }

            }
        });

        mapCategorias = new LinkedHashMap<>();
        cargarCategorias();

        cargarDatosPantalla();

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
                            categoriaEmpleado.setAdapter(new ArrayAdapter<>(PerfilEmpleadoActivity.this, R.layout.lista_categorias, new ArrayList<String>(mapCategorias.keySet())));
                        } else {
                            Toast.makeText(PerfilEmpleadoActivity.this, getString(R.string.errorAccesoBD), Toast.LENGTH_LONG).show();
                            Log.d("taskjectsdebug", "lectura categorías: no se han recuperado datos!");
                        }
                    }
                });
    }

    private void cargarDatosPantalla() {

        Log.d("taskjectsdebug","Empresa actual: " + mAuth.getUid());
        SharedPreferences sharedPreferences = getSharedPreferences(mAuth.getUid(), Context.MODE_PRIVATE);
        etNif.setText(sharedPreferences.getString("nif", getString(R.string.error)));
        etNombre.setText(sharedPreferences.getString("nombre", getString(R.string.error)));
        etApellidos.setText(sharedPreferences.getString("apellidos", getString(R.string.error)));
        etEmail.setText(sharedPreferences.getString("email", getString(R.string.error)));

        tvFechaHoraCreacion.setText(getString(R.string.creadoEl).concat(" ").concat(Conversor.timestampToString(Locale.getDefault(), user.getMetadata().getCreationTimestamp())));
        tvFechaHoraUltimoLogin.setText(getString(R.string.ultimoLoginEl).concat(" ").concat(Conversor.timestampToString(Locale.getDefault(), user.getMetadata().getLastSignInTimestamp())));

        //Cargo los datos iniciales para validar en modo edit si se han producido cambios
        nif = sharedPreferences.getString("nif", getString(R.string.error));
        nombre = sharedPreferences.getString("nombre", getString(R.string.error));
        apellidos = sharedPreferences.getString("apellidos", getString(R.string.error));
        categoria = sharedPreferences.getString("categoria", getString(R.string.error));
    }


    public void modificarPerfil(View view) {

        if (validarDatos()) {

            //Recupera la empresa desde la BD
            db.collection(EMPLEADOS)
                    .whereEqualTo("uidAuth", mAuth.getUid())
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful() && !task.getResult().isEmpty()) {
                                Empleado empleado = null;
                                for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                                    empleado = documentSnapshot.toObject(Empleado.class);
                                }
                                actualizarEmpleado(empleado);
                            }

                        }
                    });
        }

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

        categoriaEmpleado.setError(null);
        if (TextUtils.isEmpty(categoriaEmpleado.getText().toString())) {
            categoriaEmpleado.setError(getString(R.string.faltaCategoria));
            resultado = false;
        } else {
            uidCategoria = mapCategorias.get(categoriaEmpleado.getText().toString());
        }

        //Comprueba si se han producido cambios...
        if (etNif.getText().toString().equals(nif) &&
                etNombre.getText().toString().equals(nombre) &&
                etApellidos.getText().toString().equals(apellidos) &&
                categoriaEmpleado.getText().toString().equals(categoria)) {
            //Si no se han producido cambios se muestra un Toast y no permite continuar
            Toast.makeText(PerfilEmpleadoActivity.this, getString(R.string.noHayCambios), Toast.LENGTH_LONG).show();
            resultado = false;
        }

        return resultado;
    }


    private void actualizarEmpleado(Empleado empleado) {

        empleado.setNif(etNif.getText().toString().toUpperCase());
        empleado.setNombre(etNombre.getText().toString());
        empleado.setApellidos(etApellidos.getText().toString());
        empleado.setCategoria(uidCategoria);

        DocumentReference empresaUpdate = db.collection(EMPLEADOS).document(empleado.getUid());
        empresaUpdate.set(empleado).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    AlertDialog.Builder alertaModificacionProyectoCorrecta = new AlertDialog.Builder(PerfilEmpleadoActivity.this);
                    alertaModificacionProyectoCorrecta.setMessage(getString(R.string.modifPerfilCorrecta))
                            .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    finish();
                                }
                            }).show();
                } else {
                    AlertDialog.Builder alertaErrorAccesoBBDD = new AlertDialog.Builder(PerfilEmpleadoActivity.this);
                    alertaErrorAccesoBBDD.setMessage(getString(R.string.errorAccesoBD))
                            .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Log.d("taskjectsdebug","Error al subir la empresa a bbdd");
                                }
                            }).show();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_modificar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.itemEditar:
                editar();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void editar() {

        // Si alguno de los campos tiene error es que algo ha ido mal al recuperar las SharedPreferences y no permite editar
        if (etNif.getText().toString().equals(getString(R.string.error)) ||
                etNombre.getText().toString().equals(getString(R.string.error)) ||
                etApellidos.getText().toString().equals(getString(R.string.error))) {
            Toast.makeText(PerfilEmpleadoActivity.this, getString(R.string.errorGeneral), Toast.LENGTH_LONG).show();
        } else {
            modoEdit = true;
            etNif.setEnabled(true);
            etNombre.setEnabled(true);
            etApellidos.setEnabled(true);
            categoriaEmpleado.setEnabled(true);
            findViewById(R.id.btModificar).setVisibility(View.VISIBLE);
        }
    }
}