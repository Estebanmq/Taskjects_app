package com.app.taskjects;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.app.taskjects.dialogos.RecuperarPasswordDialog;
import com.app.taskjects.utils.Validador;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.firestore.FirebaseFirestore;

public class FragmentEmpleadoLogin extends Fragment {

    private final String EMPLEADOS = "empleados";

    //Inicializacion componentes
    TextInputEditText etEmailEmpleado;
    TextInputEditText etContraseniaEmpleado;
    TextInputLayout outlinedTextFieldEmailEmpleado;
    TextInputLayout outlinedTextFieldContraseniaEmpleado;
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
        outlinedTextFieldEmailEmpleado = view.findViewById(R.id.outlinedTextFieldEmailEmpleado);
        outlinedTextFieldContraseniaEmpleado = view.findViewById(R.id.outlinedTextFieldContraseniaEmpleado);

        btnLoginEmpleado = view.findViewById(R.id.btnLoginEmpleado);
        db = FirebaseFirestore.getInstance();

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
        outlinedTextFieldEmailEmpleado.setErrorEnabled(false);
        btnLoginEmpleado.setEnabled(false);
        if (verificarDatos()) {
            db.collection(EMPLEADOS)
                    .whereEqualTo("email",etEmailEmpleado.getText().toString().trim())
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            if (!task.getResult().isEmpty()) {
                                uidEmpresa = task.getResult().getDocuments().get(0).getString("uidEmpresa");
                                mAuth = FirebaseAuth.getInstance();
                                mAuth.signInWithEmailAndPassword(etEmailEmpleado.getText().toString().trim(), etContraseniaEmpleado.getText().toString().trim())
                                        .addOnCompleteListener(task1 -> {
                                            if (task1.isSuccessful()) {
                                                Intent intent = new Intent(view.getContext(), MainEmpleadoActivity.class);
                                                intent.putExtra("uidEmpresa", uidEmpresa);
                                                startActivity(intent);
                                                getActivity().finish();
                                            } else {
                                                Toast.makeText(view.getContext(), getString(R.string.datosLoginEmpresaIncorrectos), Toast.LENGTH_SHORT).show();
                                                btnLoginEmpleado.setEnabled(true);
                                            }})
                                        .addOnFailureListener(e -> {
                                            if (e instanceof FirebaseAuthInvalidUserException) {
                                                Toast.makeText(getContext(), getString(R.string.cuentaNoExisteDeshabilitada), Toast.LENGTH_SHORT).show();
                                            } else if (e instanceof FirebaseAuthInvalidCredentialsException) {
                                                Toast.makeText(getContext(), getString(R.string.credencialesErroneas), Toast.LENGTH_SHORT).show();
                                            } else {
                                                Log.d("FragmentEmpleadoLogin","error en BD al hacer el login: " + e.getMessage());
                                                Toast.makeText(getContext(), getString(R.string.errorAccesoBD), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            } else {
                                outlinedTextFieldEmailEmpleado.setErrorEnabled(true);
                                outlinedTextFieldEmailEmpleado.setError(getString(R.string.emailErroneo));
                                btnLoginEmpleado.setEnabled(true);
                            }
                        } else {
                            Toast.makeText(view.getContext(),getString(R.string.errorGeneral),Toast.LENGTH_SHORT).show();
                        }})
                    .addOnFailureListener(e -> {
                        Log.d("FragmentEmpleadoLogin","error en BD al buscar el email tecleado: " + e.getMessage());
                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            btnLoginEmpleado.setEnabled(true);
        }
    }

    private boolean verificarDatos() {
        outlinedTextFieldEmailEmpleado.setErrorEnabled(true);
        outlinedTextFieldContraseniaEmpleado.setErrorEnabled(true);
        boolean login = true;
        if (TextUtils.isEmpty(etEmailEmpleado.getText().toString().trim())) {
            outlinedTextFieldEmailEmpleado.setError(getString(R.string.faltaEmail));
            login = false;
        } else if (!Validador.validarEmail(etEmailEmpleado.getText().toString().trim())) {
            outlinedTextFieldEmailEmpleado.setError(getString(R.string.emailErroneo));
            login = false;
        } else {
            outlinedTextFieldEmailEmpleado.setError(null);
            outlinedTextFieldEmailEmpleado.setErrorEnabled(false);
        }

        //Contraseña
        if (TextUtils.isEmpty(etContraseniaEmpleado.getText().toString().trim())) {
            outlinedTextFieldContraseniaEmpleado.setError(getString(R.string.faltaPassword));
            login = false;
        } else if (!Validador.validarPassword(etContraseniaEmpleado.getText().toString().trim())) {
            outlinedTextFieldContraseniaEmpleado.setError(getString(R.string.passwordErroneo));
            login = false;
        } else {
            outlinedTextFieldContraseniaEmpleado.setError(null);
            outlinedTextFieldContraseniaEmpleado.setErrorEnabled(false);
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