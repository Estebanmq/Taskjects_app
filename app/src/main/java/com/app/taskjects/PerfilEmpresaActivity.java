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
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.app.taskjects.pojos.Empresa;
import com.app.taskjects.utils.Conversor;
import com.app.taskjects.utils.Validador;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Locale;

public class PerfilEmpresaActivity extends AppCompatActivity {

    private final String EMPRESAS = "empresas";

    //Variables para manejar la BBDD
    FirebaseFirestore db;
    FirebaseAuth mAuth;
    FirebaseUser user;

    //Variables de control de la clase
    boolean modoEdit;

    //Componentes
    EditText etCif;
    EditText etNombre;
    EditText etDireccion;
    EditText etEmail;

    TextView tvFechaHoraCreacion;
    TextView tvFechaHoraUltimoLogin;

    //Variables para cargar los datos previos a la modificación
    String cif;
    String nombre;
    String direccion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.perfil_empresa_layout);

        //Inicio variables de acceso a BD
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        //Inicio componentes
        etCif = findViewById(R.id.etCifEmpresa);
        etNombre = findViewById(R.id.etNombreEmpleado);
        etDireccion = findViewById(R.id.etDireccion);
        etEmail = findViewById(R.id.etEmail);

        tvFechaHoraCreacion = findViewById(R.id.tvFechaHoraCreacion);
        tvFechaHoraUltimoLogin = findViewById(R.id.tvFechaHoraUltimoLogin);

        //Inicializo la toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Captura el click de volver atrás
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Si está en modo edición...
                if (modoEdit) {
                    mostrarDialogoSalida();
                } else {
                    finish();
                }

            }
        });

        cargarDatosPantalla();

    }

    private void cargarDatosPantalla() {

        Log.d("taskjectsdebug","Empresa actual: " + mAuth.getUid());
        SharedPreferences sharedPreferences = getSharedPreferences(mAuth.getUid(), Context.MODE_PRIVATE);
        etCif.setText(sharedPreferences.getString("cif", getString(R.string.error)));
        etNombre.setText(sharedPreferences.getString("nombre", getString(R.string.error)));
        etDireccion.setText(sharedPreferences.getString("direccion", getString(R.string.error)));
        etEmail.setText(sharedPreferences.getString("email", getString(R.string.error)));

        tvFechaHoraCreacion.setText(getString(R.string.creadoEl).concat(" ").concat(Conversor.timestampToString(Locale.getDefault(), user.getMetadata().getCreationTimestamp())));
        tvFechaHoraUltimoLogin.setText(getString(R.string.ultimoLoginEl).concat(" ").concat(Conversor.timestampToString(Locale.getDefault(), user.getMetadata().getLastSignInTimestamp())));

        //Cargo los datos iniciales para validar en modo edit si se han producido cambios
        cif = sharedPreferences.getString("cif", getString(R.string.error));
        nombre = sharedPreferences.getString("nombre", getString(R.string.error));
        direccion = sharedPreferences.getString("direccion", getString(R.string.error));
    }

    public void modificarPerfil(View view) {

        //Pone el botón de modificarPerfil como disabled para evitar doble-clic
        findViewById(R.id.btModificar).setEnabled(false);

        if (validarDatos()) {

            //Recupera la empresa desde la BD
            db.collection(EMPRESAS)
                    .whereEqualTo("uidAuth", mAuth.getUid())
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful() && !task.getResult().isEmpty()) {
                                Empresa empresa = task.getResult().getDocuments().get(0).toObject(Empresa.class);
                                actualizarEmpresa(empresa);
                            } else {
                                Toast.makeText(PerfilEmpresaActivity.this, getString(R.string.errorAccesoBD), Toast.LENGTH_LONG).show();
                                //Pone el botón de modificarPerfil como enabled
                                findViewById(R.id.btModificar).setEnabled(true);
                            }

                        }
                    });
        } else {
            //Pone el botón de modificarPerfil como enabled
            findViewById(R.id.btModificar).setEnabled(true);
        }
    }

    private boolean validarDatos() {

        boolean resultado = true;

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

        //Comprueba si se han producido cambios...
        if (etCif.getText().toString().equalsIgnoreCase(cif) &&
                etNombre.getText().toString().equalsIgnoreCase(nombre) &&
                etDireccion.getText().toString().equalsIgnoreCase(direccion)) {
            //Si no se han producido cambios se muestra un Toast y no permite continuar
            Toast.makeText(PerfilEmpresaActivity.this, getString(R.string.noHayCambios), Toast.LENGTH_LONG).show();
            resultado = false;
        }

        return resultado;
    }

    private void actualizarEmpresa(Empresa empresa) {

        empresa.setCif(etCif.getText().toString().toUpperCase());
        empresa.setNombre(etNombre.getText().toString());
        empresa.setDireccion(etDireccion.getText().toString());
        DocumentReference empresaUpdate = db.collection(EMPRESAS).document(empresa.getUid());
        empresaUpdate.set(empresa).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    AlertDialog.Builder alertaModificacionProyectoCorrecta = new AlertDialog.Builder(PerfilEmpresaActivity.this);
                    alertaModificacionProyectoCorrecta.setMessage(getString(R.string.modifPerfilCorrecta))
                            .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    finish();
                                }
                            }).show();
                } else {
                    AlertDialog.Builder alertaErrorAccesoBBDD = new AlertDialog.Builder(PerfilEmpresaActivity.this);
                    alertaErrorAccesoBBDD.setMessage(getString(R.string.errorAccesoBD))
                            .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Log.d("taskjectsdebug","Error al subir la empresa a bbdd");
                                }
                            }).show();
                    //Pone el botón de modificarPerfil como enabled
                    findViewById(R.id.btModificar).setEnabled(true);
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
        if (etCif.getText().toString().equals(getString(R.string.error)) ||
                etNombre.getText().toString().equals(getString(R.string.error)) ||
                etDireccion.getText().toString().equals(getString(R.string.error))) {
            Toast.makeText(PerfilEmpresaActivity.this, getString(R.string.errorGeneral), Toast.LENGTH_LONG).show();
        } else {
            modoEdit = true;
            etCif.setEnabled(true);
            etNombre.setEnabled(true);
            etDireccion.setEnabled(true);
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

        AlertDialog.Builder alertaSalidaCreacion = new AlertDialog.Builder(PerfilEmpresaActivity.this);
        //Si pulsa en cancelar no salgo de la activity
        alertaSalidaCreacion.setMessage(getString(R.string.confirmSalidaModifPerfil))
                .setNeutralButton(getString(R.string.cancelar), (dialogInterface, i) -> { })
                .setPositiveButton(getString(R.string.aceptar), (dialogInterface, i) -> finish())
                .show();
    }
}