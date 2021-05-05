package com.app.taskjects;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.app.taskjects.adaptadores.AdaptadorProyectosRV;
import com.app.taskjects.pojos.Proyecto;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class MainEmpleadoActivity extends AppCompatActivity {

    //Componentes
    RecyclerView rvProyectosEmpleados;
    TextView tvInfoNoProyectos;

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

            mAuth = FirebaseAuth.getInstance();
            user = mAuth.getCurrentUser();
            db = FirebaseFirestore.getInstance();

            cargarUsuarioEmpleado();
    }


    private void cargarUsuarioEmpleado() {
        db.collection("empleados")
                .whereEqualTo("uidAuth",user.getUid())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                                uidEmpleado = documentSnapshot.getId();
                                Log.d("MainEmpleadoActivity", "Empleado actual -> " + uidEmpleado);
                                cargarSharedPreferences(documentSnapshot);
                                cargarProyectos();
                            }
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
                        if (uidProyectos == null ) {
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

    private void cargarSharedPreferences(QueryDocumentSnapshot documentSnapshot) {
        SharedPreferences pref = getSharedPreferences(uidEmpleado, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("tipoLogin","C");
        editor.putString("uidEmpleado", uidEmpleado);
        editor.putString("nif", documentSnapshot.getString("nif"));
        editor.putString("nombre", documentSnapshot.getString("nombre"));
        editor.putString("email", documentSnapshot.getString("email"));
        editor.putString("password", documentSnapshot.getString("password"));
        editor.putString("uidAuth", documentSnapshot.getString("uidAuth"));
        editor.putString("uidEmpresa",documentSnapshot.getString("uidEmpresa"));
        editor.putString("categoria",documentSnapshot.getString("categoria"));

    }
}