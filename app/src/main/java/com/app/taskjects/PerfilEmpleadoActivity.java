package com.app.taskjects;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.app.taskjects.pojos.Empleado;
import com.app.taskjects.utils.Conversor;
import com.app.taskjects.utils.Validador;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

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
    TextInputLayout outlinedTextFieldNifEmpleado;
    TextInputLayout outlinedTextFieldNombre;
    TextInputLayout outlinedTextFieldApellidos;
    TextInputLayout outlinedTextFieldCategoria;

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
    String descCategoria;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.perfil_empleado_layout);

        //Inicio variables
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();

        mapCategorias = new LinkedHashMap<>();

        //Inicializo componentes
        etNif = findViewById(R.id.etNifEmpleado);
        etNombre = findViewById(R.id.etNombreEmpleado);
        etApellidos = findViewById(R.id.etApellidosEmpleado);
        etEmail = findViewById(R.id.etEmailEmpleado);
        categoriaEmpleado = findViewById(R.id.etCategoriaEmpleado);
        categoriaEmpleado.setKeyListener(null);

        outlinedTextFieldNifEmpleado = findViewById(R.id.outlinedTextFieldNifEmpleado);
        outlinedTextFieldNombre = findViewById(R.id.outlinedTextFieldNombre);
        outlinedTextFieldApellidos = findViewById(R.id.outlinedTextFieldApellidos);
        outlinedTextFieldCategoria = findViewById(R.id.outlinedTextFieldCategoria);

        tvFechaHoraCreacion = findViewById(R.id.tvFechaHoraCreacion);
        tvFechaHoraUltimoLogin = findViewById(R.id.tvFechaHoraUltimoLogin);

        //Inicializo la toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.perfil));

        //Captura el click de volver atrás
        toolbar.setNavigationOnClickListener(view -> {
            //Si está en modo edición...
            if (modoEdit) {
                mostrarDialogoSalida();
            } else {
                finish();
            }
        });

        cargarDatosPantalla();

        cargarCategorias();

    }

    private void cargarDatosPantalla() {

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

    private void cargarCategorias() {

        db.collection(CATEGORIAS)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            mapCategorias.put(document.getString("descripcion"), document.getId());
                            if (document.getId().equals(categoria)) {
                                descCategoria = document.getString("descripcion");
                            }
                        }
                        // Carga el dropmenu de categorías con los datos recuperados
                        categoriaEmpleado.setText(descCategoria);
                        categoriaEmpleado.setAdapter(new ArrayAdapter<>(PerfilEmpleadoActivity.this, R.layout.lista_categorias, new ArrayList<>(mapCategorias.keySet())));
                    } else {
                        Toast.makeText(PerfilEmpleadoActivity.this, getString(R.string.errorAccesoBD), Toast.LENGTH_LONG).show();
                        Log.d("taskjectsdebug", "error en BD al recuperar categorias");
                    }
                });
    }

    public void modificarPerfil(View view) {

        //Pone el botón de modificarPerfil como disabled para evitar doble-clic
        findViewById(R.id.btModificar).setEnabled(false);

        //Valida los datos y si están correctos se recupera el empleado para luego actualizarlo
        if (validarDatos()) {

            //Recupera la empresa desde la BD
            db.collection(EMPLEADOS)
                    .whereEqualTo("uidAuth", mAuth.getUid())
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            Empleado empleado = task.getResult().getDocuments().get(0).toObject(Empleado.class);
                            actualizarEmpleado(empleado);
                        } else {
                            Log.d("taskjectsdebug","error en BD al actualizar el empleado");
                            Toast.makeText(PerfilEmpleadoActivity.this, getString(R.string.errorAccesoBD), Toast.LENGTH_LONG).show();
                            //Pone el botón de modificarPerfil como enabled
                            findViewById(R.id.btModificar).setEnabled(true);
                        }
                    });
        } else {
            //Pone el botón de modificarPerfil como enabled
            findViewById(R.id.btModificar).setEnabled(true);
        }

    }

    private boolean validarDatos() {
        outlinedTextFieldNombre.setErrorEnabled(false);
        outlinedTextFieldApellidos.setErrorEnabled(false);
        outlinedTextFieldCategoria.setErrorEnabled(false);
        outlinedTextFieldNifEmpleado.setErrorEnabled(false);
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

        outlinedTextFieldCategoria.setError(null);
        if (TextUtils.isEmpty(categoriaEmpleado.getText().toString())) {
            outlinedTextFieldCategoria.setErrorEnabled(true);
            outlinedTextFieldCategoria.setError(getString(R.string.faltaCategoria));
            resultado = false;
        } else {
            uidCategoria = mapCategorias.get(categoriaEmpleado.getText().toString());
        }

        //Comprueba si se han producido cambios...
        if (etNif.getText().toString().trim().equalsIgnoreCase(nif) &&
                etNombre.getText().toString().trim().equalsIgnoreCase(nombre) &&
                etApellidos.getText().toString().trim().equalsIgnoreCase(apellidos) &&
                uidCategoria.equalsIgnoreCase(categoria)) {
            //Si no se han producido cambios se muestra un Toast y no permite continuar
            Toast.makeText(PerfilEmpleadoActivity.this, getString(R.string.noHayCambios), Toast.LENGTH_LONG).show();
            resultado = false;
        }

        return resultado;
    }


    private void actualizarEmpleado(Empleado empleado) {

        empleado.setNif(etNif.getText().toString().toUpperCase().trim());
        empleado.setNombre(etNombre.getText().toString().trim());
        empleado.setApellidos(etApellidos.getText().toString().trim());
        empleado.setCategoria(uidCategoria);

        DocumentReference empresaUpdate = db.collection(EMPLEADOS).document(empleado.getUid());
        empresaUpdate.set(empleado).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                AlertDialog.Builder alertaModificacionProyectoCorrecta = new AlertDialog.Builder(PerfilEmpleadoActivity.this);
                alertaModificacionProyectoCorrecta.setMessage(getString(R.string.modifPerfilCorrecta))
                        .setPositiveButton(getString(R.string.ok), (dialogInterface, i) -> finish()).show();
            } else {
                Log.d("taskjectsdebug","error en BD al actualizar el empleado");
                //Si hay algun problema al recuperar datos de la base de datos le muestro al usuario que hay un problema
                Toast.makeText(PerfilEmpleadoActivity.this, getString(R.string.errorAccesoBD), Toast.LENGTH_LONG).show();
                //Pone el botón de modificarPerfil como enabled
                findViewById(R.id.btModificar).setEnabled(true);
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
            Log.d("taskjectsdebug","error al recuperar datos del empleado de sharedPreferences");
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

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            //Si está en modo edición...
            if (modoEdit) {
                mostrarDialogoSalida();
            } else {
                finish();
            }
        }
        return true;
    }

    private void mostrarDialogoSalida() {

        AlertDialog.Builder alertaSalidaCreacion = new AlertDialog.Builder(PerfilEmpleadoActivity.this);
        //Si pulsa en cancelar no salgo de la activity
        alertaSalidaCreacion.setMessage(getString(R.string.confirmSalidaModifPerfil))
                .setNeutralButton(getString(R.string.cancelar), (dialogInterface, i) -> { })
                .setPositiveButton(getString(R.string.aceptar), (dialogInterface, i) -> finish())
                .show();
    }
}