package com.app.taskjects;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.app.taskjects.dialogos.RecuperarPasswordDialog;
import com.app.taskjects.utils.Validador;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class FragmentEmpleadoLogin extends Fragment {

    //Inicializacion componentes
    TextInputEditText etEmailEmpleado;
    TextInputEditText etContraseniaEmpleado;
    MaterialButton btnLoginEmpleado;
    TextView textViewRecuperarContrasenia;
    TextView textViewRegistro;

    //Inicio sesion firebase
    FirebaseAuth mAuth;
    FirebaseFirestore db;

    //View para hacer referancia a la pantalla de login
    View view;

    //Variables de la clase
    String uidEmpresa;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //Creo un objeto View que tendra el inflater del fragment
        view = inflater.inflate(R.layout.fragment_empleado_login, container, false);

        //Una vez que tengo el objeto view puedo inicializar componentes del fragment
        textViewRecuperarContrasenia = view.findViewById(R.id.textViewRecuperarContraseniaEmpleado);
        textViewRegistro = view.findViewById(R.id.textViewRegistro);
        etEmailEmpleado = view.findViewById(R.id.etEmailEmpleado);
        etContraseniaEmpleado = view.findViewById(R.id.etContraseniaEmpleado);
        etEmailEmpleado.setText("estebanmq7890@gmail.com");
        etContraseniaEmpleado.setText("admin1234");

        btnLoginEmpleado = view.findViewById(R.id.btnLoginEmpleado);
        db = FirebaseFirestore.getInstance();

etEmailEmpleado.setText("jm.dios.martin@gmail.com");
etContraseniaEmpleado.setText("admin1234");

        uidEmpresa = "";

        //Le agrego un Listener al TextView de recuperar contraseña/--para llamar al metodo que cambia la contraseña--\
        textViewRecuperarContrasenia.setOnClickListener(v -> {
            textViewRecuperarContrasenia.setEnabled(false);
            RecuperarPasswordDialog dialog = new RecuperarPasswordDialog();
            dialog.show(getFragmentManager(), "Recuperar la contraseña Empleado");
            textViewRecuperarContrasenia.setEnabled(true);
        });


        //Le agrego un Listener al TextView de registro para abrir la pantalla de registro para empleados
        textViewRegistro.setOnClickListener(v -> {
            textViewRegistro.setEnabled(false);
            startActivity(new Intent(view.getContext(), RegistroEmpleadoActivity.class));
        });

        //Le agrego un Listener al btn para llamar a loginEmpleado
        btnLoginEmpleado.setOnClickListener(view -> loginEmpleado());

        //Devuelvo el objeto view (inflater del fragment)
        return view;

    }

    private void loginEmpleado() {
        btnLoginEmpleado.setEnabled(false);
        if (verificarDatos()) {
            esEmpleado();
        } else {
            btnLoginEmpleado.setEnabled(true);
        }
    }

    private void esEmpleado() {

        db.collection("empleados")
                .whereEqualTo("email",etEmailEmpleado.getText().toString())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            uidEmpresa = task.getResult().getDocuments().get(0).getString("uidEmpresa");
                            mAuth = FirebaseAuth.getInstance();
                            //Todo: java.lang.RuntimeException: There was an error while initializing the connection to the GoogleApi: java.lang.IllegalStateException: A required meta-data tag in your app's AndroidManifest.xml does not exist.  You must have the following declaration within the <application> element:     <meta-data android:name="com.google.android.gms.version" android:value="@integer/google_play_services_version" />
                            mAuth.signInWithEmailAndPassword(etEmailEmpleado.getText().toString(), etContraseniaEmpleado.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        Intent intent = new Intent(view.getContext(), MainEmpleadoActivity.class);
                                        intent.putExtra("uidEmpresa", uidEmpresa);
                                        startActivity(intent);
                                    } else {
                                        Toast.makeText(view.getContext(), getString(R.string.datosLoginEmpresaIncorrectos), Toast.LENGTH_SHORT).show();
                                        btnLoginEmpleado.setEnabled(true);
                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(getContext(),e.getMessage(),Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            etEmailEmpleado.setError(getString(R.string.emailErroneo));
                            btnLoginEmpleado.setEnabled(true);
                        }
                    }
                });
    }

    private boolean verificarDatos() {
        boolean login = true;
        if (TextUtils.isEmpty(etEmailEmpleado.getText().toString())) {
            etEmailEmpleado.setError(getString(R.string.faltaEmail));
            login = false;
        } else if (!Validador.validarEmail(etEmailEmpleado.getText().toString())) {
            etEmailEmpleado.setError(getString(R.string.emailErroneo));
            login = false;
        }

        //Contraseña
        if (TextUtils.isEmpty(etContraseniaEmpleado.getText().toString())) {
            etContraseniaEmpleado.setError(getString(R.string.faltaPassword));
            login = false;
        } else if (!Validador.validarPassword(etContraseniaEmpleado.getText().toString())) {
            etContraseniaEmpleado.setError(getString(R.string.passwordErroneo));
            login = false;
        }
        return login;
    }
    @Override
    public void onResume() {
        super.onResume();
        btnLoginEmpleado.setEnabled(true);
        textViewRegistro.setEnabled(true);
        textViewRecuperarContrasenia.setEnabled(true);
    }
}