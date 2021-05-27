package com.app.taskjects.dialogos;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.app.taskjects.R;

public class CambiarDatosTareaDialog extends DialogFragment {

    private String nombreTarea;

    private TextView etNombreTarea;

    public CambiarDatosTareaDialog() {}

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        return crearDialogoCambiarDatosTarea();
    }


    private AlertDialog crearDialogoCambiarDatosTarea() {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.cambiar_datos_tarea);
        builder.setCancelable(true);
        builder.setPositiveButton(getString(R.string.aceptar), null);

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.fragment_cambiar_datos_tarea, null);
        builder.setView(v);

        //etEmail = v.findViewById(R.id.etEmail);
        etNombreTarea = v.findViewById(R.id.etNombreTareaCDTarea);
        etNombreTarea.setText(this.nombreTarea);

        final AlertDialog dialogo =  builder.create();
        dialogo.setOnShowListener(new DialogInterface.OnShowListener(){
            @Override
            public void onShow(DialogInterface dialogInterface){
                Button button = dialogo.getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
/*
                        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
                        mDatabase.child("tareas")
                                .child(sharedPreferences.getString("uidEmpresa",""))
                                .child()
                                .child("uidEmpleado")
                                .setValue(uidEmpleado)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d("Tarea asignada correcto"," Correcto");
                                        arrayAllEstados.get(1).get(toRow).second.setUidEmpleado(uidEmpleado);
                                        adaptadorTareasDADAE1.notifyDataSetChanged();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d("Fallo al actualizar el empelado de tarea",e.getMessage());
                                    }
                                });



 */
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
        return inflater.inflate(R.layout.fragment_cambiar_datos_tarea, container, false);
    }

    public void setDatosTarea(String nombreTarea) {
        this.nombreTarea = nombreTarea;
    }


}
