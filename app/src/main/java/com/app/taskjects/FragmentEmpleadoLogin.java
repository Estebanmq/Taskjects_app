package com.app.taskjects;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class FragmentEmpleadoLogin extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //Creo un objeto View que tendra el inflater del fragment
        View view = inflater.inflate(R.layout.fragment_empleado_login, container, false);

        //Una vez que tengo el objeto view puedo inicializar componentes del fragment
        TextView textViewRecuperarContrasenia = view.findViewById(R.id.textViewRecuperarContrasenia);
        TextView textViewRegistro = view.findViewById(R.id.textViewRegistro);

        //Le agrego un Listener al TextView de recuperar contraseña/--para llamar al metodo que cambia la contraseña--\
        textViewRecuperarContrasenia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(view.getContext(), "Llamo a recuperar contraseña", Toast.LENGTH_LONG)
                        .show();
            }
        });


        //Le agrego un Listener al TextView de registro /--para abrir la pantalla de registro --\
        textViewRegistro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(view.getContext(), "Abro la pantalla de registro", Toast.LENGTH_LONG)
                        .show();
            }
        });

        return view;

    }
}