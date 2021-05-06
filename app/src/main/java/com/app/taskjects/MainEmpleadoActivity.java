package com.app.taskjects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.TextView;
import android.widget.Toast;

import com.app.taskjects.adaptadores.AdaptadorProyectosRV;
import com.app.taskjects.pojos.Empleado;
import com.app.taskjects.pojos.Empresa;
import com.app.taskjects.pojos.Proyecto;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class MainEmpleadoActivity extends MenuToolbarActivity {

    //Componentes
    RecyclerView rvProyectosEmpleados;
    TextView tvInfoNoProyectos;
    BottomAppBar bottomAppBar;

    //Variables para manejar la bbdd y sus datos
    FirebaseAuth mAuth;
    FirebaseUser user;
    FirebaseFirestore db;
    String uidEmpleado;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.main_empleado_layout);

            //Inicializacion componente y variables
            tvInfoNoProyectos = findViewById(R.id.tvInfoNoProyectos);

            rvProyectosEmpleados = findViewById(R.id.rvProyectosEmpleados);
            rvProyectosEmpleados.setHasFixedSize(true);

            LinearLayoutManager llm = new LinearLayoutManager(MainEmpleadoActivity.this);
            rvProyectosEmpleados.setLayoutManager(llm);

            bottomAppBar = findViewById(R.id.bottomAppBar);
            setSupportActionBar(bottomAppBar);

            mAuth = FirebaseAuth.getInstance();
            user = mAuth.getCurrentUser();

            db = FirebaseFirestore.getInstance();

            cargarUsuarioEmpleado();
    }


    private void cargarUsuarioEmpleado() {
        db.collection("empleados")
                .whereEqualTo("uidAuth",user.getUid())
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value,
                                        @Nullable FirebaseFirestoreException e) {

                        if (e != null) {
                            Toast.makeText(MainEmpleadoActivity.this, getString(R.string.errorAccesoBD), Toast.LENGTH_LONG).show();
                            return;
                        }

                        for (QueryDocumentSnapshot doc : value) {
                            Empleado empleado = doc.toObject(Empleado.class);
                            uidEmpleado = empleado.getUid();
                            Log.d("MainEmpleadoActivity", "Empleado actual -> " + uidEmpleado);
                            cargarSharedPreferences(empleado);
                            cargarProyectos();
                        }
                    }
                });
    }

    List<String> uidProyectos;
    private void cargarProyectos() {
        db.collection("empleados")
                .document(uidEmpleado)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot snapshot) {
                        //PERO WTF!!
                        uidProyectos = (List<String>)snapshot.get("uidProyectos");
                        if (uidProyectos == null || uidProyectos.isEmpty()) {
                            tvInfoNoProyectos.setVisibility(TextView.VISIBLE);
                        } else {
                            tvInfoNoProyectos.setVisibility(TextView.INVISIBLE);
                            mostrarProyectos(uidProyectos);
                        }

                    }
                });
    }


    private void mostrarProyectos(List<String>uidProyectos) {
        ArrayList<Proyecto>listProyectos = new ArrayList<>();
        for (String uidProyecto : uidProyectos){
            Log.d("MainEmpleadoActivity","Proyecto cargando -> "+uidProyecto);
            db.collection("proyectos")
                    .document(uidProyecto)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot proyecto = task.getResult();
                                if (!proyecto.exists()){

                                } else {
                                    listProyectos.add(new Proyecto(uidProyecto,proyecto.getString("uidEmpresa"),
                                            proyecto.getString("nombre"),proyecto.getString("descripcion"),
                                            proyecto.getString("uidEmpleadoJefe")));

                                    //Todo: esto cambiarlo (Promesas)
                                    AdaptadorProyectosRV adaptadorProyectosRV = new AdaptadorProyectosRV(listProyectos,MainEmpleadoActivity.this);
                                    rvProyectosEmpleados.setAdapter(adaptadorProyectosRV);
                                }
                            }
                        }
                    });

        }


    }

    private void cargarSharedPreferences(Empleado empleado) {
        SharedPreferences pref = getSharedPreferences(mAuth.getUid(), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("tipoLogin","C");
        editor.putString("uidEmpleado", empleado.getUid());
        editor.putString("nif", empleado.getNif());
        editor.putString("nombre", empleado.getNombre());
        editor.putString("apellidos", empleado.getApellidos());
        editor.putString("email", empleado.getEmail());
        editor.putString("password", empleado.getPassword());
        editor.putString("uidAuth", empleado.getUidAuth());
        editor.putString("uidEmpresa", empleado.getUidEmpresa());
        editor.putString("categoria", empleado.getCategoria());
        editor.apply();
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Log.d("taskjectsdebug","Pulsado botón atrás en la activity");
            AlertDialog.Builder alertaSalidaAplicacion = new AlertDialog.Builder(this);
            alertaSalidaAplicacion.setMessage(getString(R.string.confirmSalidaAplicacion))
                    //Si pulsa en cancelar no hace nada
                    .setNeutralButton(getString(R.string.cancelar), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Log.d("taskjectsdebug","Ha pulsado cancelar, no se hace nada");
                        }})
                    .setPositiveButton(getString(R.string.aceptar), new DialogInterface.OnClickListener() {
                        //Si pulsa en de acuerdo cierra la aplicación
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            borrarSharedPreferences();
                            finishAffinity();
                        }}).show();
        }

        return true;
    }
}