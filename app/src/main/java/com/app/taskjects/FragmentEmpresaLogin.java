package com.app.taskjects;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.app.taskjects.dialogos.RecuperarPasswordDialog;
import com.app.taskjects.utils.Validador;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class FragmentEmpresaLogin extends Fragment {

    private final String EMPRESAS = "empresas";

    //Inicializacion componentes
    TextInputLayout outlinedTextFieldEmailEmpresa;
    TextInputEditText etEmailEmpresa;
    TextInputLayout outlinedTextFieldContraseniaEmpresa;
    TextInputEditText etContraseniaEmpresa;
    MaterialButton btnLoginEmpresa;
    TextView textViewRegistro;
    TextView textViewRecuperarContrasenia;

    //Inicio de sesion Firebase
    FirebaseAuth mAuth;
    FirebaseFirestore db;

    //View para hacer referencia a la pantalla de login
    View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //Creo un objeto View que tendra el inflater del fragment
        view = inflater.inflate(R.layout.fragment_empresa_login, container, false);

        //Una vez que tengo el objeto view puedo inicializar componentes del fragment
        textViewRecuperarContrasenia = view.findViewById(R.id.textViewRecuperarContraseniaEmpresa);
        textViewRegistro = view.findViewById(R.id.textViewRegistro);
        etEmailEmpresa = view.findViewById(R.id.etEmailEmpresa);
        outlinedTextFieldEmailEmpresa = view.findViewById(R.id.outlinedTextFieldEmailEmpresa);
        etContraseniaEmpresa = view.findViewById(R.id.etContraseniaEmpresa);
        outlinedTextFieldContraseniaEmpresa = view.findViewById(R.id.outlinedTextFieldContraseniaEmpresa);
        btnLoginEmpresa = view.findViewById(R.id.btnModifProyecto);

        db = FirebaseFirestore.getInstance();

        //Le agrego un Listener al TextView de recuperar contraseña/--para llamar al metodo que cambia la contraseña--\
        textViewRecuperarContrasenia.setOnClickListener(v -> {
            textViewRecuperarContrasenia.setEnabled(false);
            RecuperarPasswordDialog dialog = new RecuperarPasswordDialog();
            dialog.show(getFragmentManager(), "Recuperar la contraseña Empresa");
            textViewRecuperarContrasenia.setEnabled(true);
        });

        //Le agrego un Listener al TextView de registro para abrir la pantalla de registro para empresas
        textViewRegistro.setOnClickListener(v -> {
            textViewRegistro.setEnabled(false);
            startActivity(new Intent(view.getContext(), RegistroEmpresaActivity.class));
        });


        //Le agrego un Listener al btn para llamar a loginEmpresa
        btnLoginEmpresa.setOnClickListener(view -> loginEmpresa());

        //Devuelvo el objeto view (inflater del fragment)
        return view;
    }

    private void loginEmpresa() {
        btnLoginEmpresa.setEnabled(false);
        if (verificarDatos()) {
            db.collection(EMPRESAS)
                    .whereEqualTo("email",etEmailEmpresa.getText().toString())
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            mAuth = FirebaseAuth.getInstance();
                            //Todo: java.lang.RuntimeException: There was an error while initializing the connection to the GoogleApi: java.lang.IllegalStateException: A required meta-data tag in your app's AndroidManifest.xml does not exist.  You must have the following declaration within the <application> element:     <meta-data android:name="com.google.android.gms.version" android:value="@integer/google_play_services_version" />
                            mAuth.signInWithEmailAndPassword(etEmailEmpresa.getText().toString(), etContraseniaEmpresa.getText().toString()).addOnCompleteListener(task1 -> {
                                if (task.isSuccessful()) {
                                    startActivity(new Intent(view.getContext(), MainEmpresaActivity.class));
                                    getActivity().finish();
                                } else {
                                    Toast.makeText(view.getContext(),getString(R.string.datosLoginEmpresaIncorrectos),Toast.LENGTH_SHORT).show();
                                    btnLoginEmpresa.setEnabled(true);
                                }

                            }).addOnFailureListener(e -> Toast.makeText(getContext(),e.getMessage(),Toast.LENGTH_SHORT).show());
                        } else {
                            etEmailEmpresa.setError(getString(R.string.emailErroneo));
                            btnLoginEmpresa.setEnabled(true);
                        }
                    });
        } else {
            btnLoginEmpresa.setEnabled(true);
        }
    }



    private boolean verificarDatos() {
        //Todo: optimizar verificar datos
        boolean login = true;
        //Verifico todos los campos
        //Email
        if (TextUtils.isEmpty(etEmailEmpresa.getText().toString())) {
            etEmailEmpresa.setError(getString(R.string.faltaEmail));
            login = false;
        } else if (!Validador.validarEmail(etEmailEmpresa.getText().toString())) {
            etEmailEmpresa.setError(getString(R.string.emailErroneo));
            login = false;
        }
        //Contraseña
        if (TextUtils.isEmpty(etContraseniaEmpresa.getText().toString())) {
            etContraseniaEmpresa.setError(getString(R.string.faltaPassword));
            login = false;
        } else if (!Validador.validarPassword(etContraseniaEmpresa.getText().toString())) {
            etContraseniaEmpresa.setError(getString(R.string.passwordErroneo));
            login = false;
        }
        return login;
    }

    @Override
    public void onResume() {
        super.onResume();
        btnLoginEmpresa.setEnabled(true);
        textViewRegistro.setEnabled(true);
        textViewRecuperarContrasenia.setEnabled(true);
    }
}