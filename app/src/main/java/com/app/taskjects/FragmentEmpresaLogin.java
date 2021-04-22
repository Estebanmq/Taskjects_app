package com.app.taskjects;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.app.taskjects.pojos.Empresa;
import com.app.taskjects.utils.Validador;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class FragmentEmpresaLogin extends Fragment {

    //Inicializacion compoenentes
    TextInputLayout outlinedTextFieldEmailEmpresa;
    TextInputEditText etEmailEmpresa;
    TextInputLayout outlinedTextFieldContraseniaEmpresa;
    TextInputEditText etContraseniaEmpresa;
    MaterialButton btnLoginEmpresa;
    TextView textViewRegistro;
    TextView textViewRecuperarContrasenia;

    //Inicio de sesion Firebase
    FirebaseAuth mAuth;

    //View para hacer referencia a la pantalla de login
    View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //Creo un objeto View que tendra el inflater del fragment
        view = inflater.inflate(R.layout.fragment_empresa_login, container, false);

        //Una vez que tengo el objeto view puedo inicializar componentes del fragment
        textViewRecuperarContrasenia = view.findViewById(R.id.textViewRecuperarContrasenia);
        textViewRegistro = view.findViewById(R.id.textViewRegistro);
        etEmailEmpresa = view.findViewById(R.id.etEmailEmpresa);
        outlinedTextFieldEmailEmpresa = view.findViewById(R.id.outlinedTextFieldEmailEmpresa);
        etContraseniaEmpresa = view.findViewById(R.id.etContraseniaEmpresa);
        outlinedTextFieldContraseniaEmpresa = view.findViewById(R.id.outlinedTextFieldContraseniaEmpresa);
        btnLoginEmpresa = view.findViewById(R.id.btnLoginEmpresa);

        //Le agrego un Listener al TextView de recuperar contraseña/--para llamar al metodo que cambia la contraseña--\
        textViewRecuperarContrasenia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textViewRecuperarContrasenia.setEnabled(false);
                Toast.makeText(view.getContext(), "Llamo a recuperar contraseña", Toast.LENGTH_LONG)
                        .show();
            }
        });


        //Le agrego un Listener al TextView de registro para abrir la pantalla de registro para empresas
        textViewRegistro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textViewRegistro.setEnabled(false);
                Intent pantallaRegistroEmpresas = new Intent(view.getContext(), RegistroEmpresaActivity.class);
                startActivity(pantallaRegistroEmpresas);
            }
        });


        //Le agrego un Listener al btn para llamar a loginEmpresa
        btnLoginEmpresa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginEmpresa();
            }
        });

        //Devuelvo el objeto view (inflater del fragment)
        return view;
    }


    private void loginEmpresa() {
        btnLoginEmpresa.setEnabled(false);

        //Todo: Quitar esta llamada
       // startActivity(mainEmpresa);


        if (verificarDatos()) {
            mAuth = FirebaseAuth.getInstance();
            mAuth.signInWithEmailAndPassword(etEmailEmpresa.getText().toString(), etContraseniaEmpresa.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        startActivity(new Intent(view.getContext(), MainEmpresaActivity.class));
                    } else {
                        Toast.makeText(view.getContext(),getString(R.string.datosLoginEmpresaIncorrectos),Toast.LENGTH_SHORT).show();
                        btnLoginEmpresa.setEnabled(true);
                    }
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