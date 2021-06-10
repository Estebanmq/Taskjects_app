package com.app.taskjects;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MenuToolbarActivity extends AppCompatActivity {

    //Variables para gestionar el usuario de firebase
    FirebaseAuth mAuth;
    FirebaseUser user;

    //SharedPreferences
    SharedPreferences sharedPreferences;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.bottom_app_bar, menu);

        //Inicio variables
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        sharedPreferences = getSharedPreferences(mAuth.getUid(), Context.MODE_PRIVATE);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.verPerfil:
                Intent intent;
                //Dependiendo de si es una empresa o un empleado llama a ver una activity u otra
                if (sharedPreferences.getString("tipoLogin", "").equals("E")) {
                    intent = new Intent(MenuToolbarActivity.this, PerfilEmpresaActivity.class);
                } else {
                    intent = new Intent(MenuToolbarActivity.this, PerfilEmpleadoActivity.class);
                }
                startActivity(intent);
                return true;
            case R.id.cerrarSesion:
                AlertDialog.Builder alertaCerrarSesion = new AlertDialog.Builder(this);
                //Si pulsa en de acuerdo cierra la aplicación
                alertaCerrarSesion.setMessage(getString(R.string.confirmCerrarSesion))
                        //Si pulsa en cancelar no hace nada
                        .setNeutralButton(getString(R.string.cancelar), (dialogInterface, i) -> { })
                        .setPositiveButton(getString(R.string.aceptar), (dialogInterface, i) -> {
                            borrarSharedPreferences();
                            mAuth.signOut();

                            Intent intent1 = new Intent(MenuToolbarActivity.this, LoginActivity.class);
                            startActivity(intent1);
                            finish();

                        }).show();
                return true;
            case R.id.salir:
                AlertDialog.Builder alertaSalidaAplicacion = new AlertDialog.Builder(this);
                //Si pulsa en de acuerdo cierra la aplicación
                alertaSalidaAplicacion.setMessage(getString(R.string.confirmSalidaAplicacion))
                        //Si pulsa en cancelar no hace nada
                        .setNeutralButton(getString(R.string.cancelar), (dialogInterface, i) -> { })
                        .setPositiveButton(getString(R.string.aceptar), (dialogInterface, i) -> {
                            borrarSharedPreferences();
                            finishAffinity();
                        }).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    protected void borrarSharedPreferences() {

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

    }
}
