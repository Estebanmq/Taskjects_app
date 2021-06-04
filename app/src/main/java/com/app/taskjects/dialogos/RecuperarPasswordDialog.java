package com.app.taskjects.dialogos;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.app.taskjects.R;
import com.app.taskjects.RegistroEmpleadoActivity;
import com.app.taskjects.utils.Validador;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.firestore.FirebaseFirestore;

public class RecuperarPasswordDialog extends DialogFragment {

    FirebaseFirestore db;
    FirebaseAuth mAuth;

    EditText etEmail;
    TextInputLayout outlinedTextFieldEmail;

    public RecuperarPasswordDialog() {
        // Required empty public constructor
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        return crearDialogoRecuperaPassword();
    }

    private AlertDialog crearDialogoRecuperaPassword() {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.recuperar_contrasenia);
        builder.setCancelable(true);
        builder.setPositiveButton(getString(R.string.aceptar), null);

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.fragment_recuperar_password, null);
        builder.setView(v);

        etEmail = v.findViewById(R.id.etEmail);
        outlinedTextFieldEmail = v.findViewById(R.id.outlinedTextFieldEmail);

        final AlertDialog dialogo =  builder.create();
        dialogo.setOnShowListener(new DialogInterface.OnShowListener(){
            @Override
            public void onShow(DialogInterface dialogInterface){
                Button button = dialogo.getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        recuperarPassword();
                    }
                });
            }
        });

        return dialogo;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_recuperar_password, container, false);
    }

    private boolean recuperarPassword() {
        outlinedTextFieldEmail.setErrorEnabled(false);
        if (TextUtils.isEmpty(etEmail.getText().toString())) {
            Log.d("taskjectsdebug", "asigna el error");
            outlinedTextFieldEmail.setErrorEnabled(true);
            outlinedTextFieldEmail.setError(getString(R.string.faltaEmail));
        } else if (!Validador.validarEmail(etEmail.getText().toString())) {
            outlinedTextFieldEmail.setErrorEnabled(true);
            outlinedTextFieldEmail.setError(getString(R.string.emailErroneo));
        } else {
            outlinedTextFieldEmail.setError(null);
            Log.d("taskjectsdebug", "entra en recuperarContraseña");
            mAuth.sendPasswordResetEmail(etEmail.getText().toString())
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d("taskjectsdebug", "entra en onSuccess y pone resultado a true");
                            Toast.makeText(getContext(), getString(R.string.envioEmail), Toast.LENGTH_LONG).show();
                            dismiss();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            outlinedTextFieldEmail.setErrorEnabled(true);
                            outlinedTextFieldEmail.setError(getString(R.string.emailErroneo));
                        }
                    });
        }

        return false;

    }
}