package com.app.taskjects.dialogos;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.app.taskjects.R;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.chip.ChipGroup;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class CambiarDatosTareaDialog extends DialogFragment {

    //Datos de la tarea
    private String nombreTarea;
    private String uidProyecto;
    private String uidTarea;
    private String prioridad;
    private int idPrioridadAnterior;

    //Este map almacena datos para la actualizacion en la base de datos, concretamente la ruta y el nuevo dato
    private Map<String,Object> actualizaciones = new HashMap<>();

    //Componentes
    private TextView etNombreTarea;
    private ChipGroup cgPrioridad;
    TextInputLayout outlinedTextFieldNombreTareaCDTarea;

    public CambiarDatosTareaDialog() {}

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        return crearDialogoCambiarDatosTarea();
    }

    //Metodo que crea el dialogo para modificar una tarea
    private AlertDialog crearDialogoCambiarDatosTarea() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.cambiar_datos_tarea);
        builder.setCancelable(true);
        builder.setPositiveButton(getString(R.string.aceptar), null);

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.fragment_cambiar_datos_tarea, null);
        builder.setView(v);

        outlinedTextFieldNombreTareaCDTarea = v.findViewById(R.id.outlinedTextFieldNombreTareaCDTarea);
        etNombreTarea = v.findViewById(R.id.etNombreTareaCDTarea);
        cgPrioridad = v.findViewById(R.id.chipGroupPrioridadesCDTarea);

        //Le pongo al textView del nombre de la tarea el nombre de la tarea seleccionada
        etNombreTarea.setText(this.nombreTarea);
        //Selecciono el chip que corresponde con la prioridad de la tarea seleccionada y almaceno en la variable idPrioridadAnterior la prioridad de la tarea seleccionada
        switch (this.prioridad) {
            case "0":
                idPrioridadAnterior = R.id.chipPrioBajaCDTarea;
                cgPrioridad.check(idPrioridadAnterior);
                break;
            case "1":
                idPrioridadAnterior = R.id.chipPrioMediaCDTarea;
                cgPrioridad.check(idPrioridadAnterior);
                break;
            case "2":
                idPrioridadAnterior = R.id.chipPrioAltaCDTarea;
                cgPrioridad.check(idPrioridadAnterior);
                break;
        }

        final AlertDialog dialogo =  builder.create();

        dialogo.setOnShowListener(new DialogInterface.OnShowListener(){
            @Override
            public void onShow(DialogInterface dialogInterface){
                //Instancio el boton de Aceptar en el dialogo
                Button button = dialogo.getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //Si la validacion de datos es correcta actualizo los datos modificados en la base de datos
                        if (validarDatos()) {
                            DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
                            //Aqui le paso el map con todas las modificaciones que tiene que hacer
                            mDatabase.updateChildren(actualizaciones)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            //Si la tarea ha sido completada con exito cierro el dialogo y notifico del exito de la operacion, si no, notifico del fallo
                                            if (task.isSuccessful()) {
                                                dialogo.dismiss();
                                                Toast.makeText(getContext(), getString(R.string.modifTareaCorrecta), Toast.LENGTH_SHORT).show();
                                                Log.d("Tarea modificada correctamente","Modificacion correcta");
                                            } else {
                                                Log.d("Error al actualizar los datos","");
                                                Toast.makeText(getContext(),getString(R.string.modifTareaError),Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });

                        }
                    }

                });


            }
        });
        return dialogo;
    }

    @Override
    public void onAttach(@NonNull Context context) { super.onAttach(context); }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) { return inflater.inflate(R.layout.fragment_cambiar_datos_tarea, container, false); }

    //Metodo que asigna los datos de la tarea seleccionada a sus respectivas variables
    public void setDatosTarea(String nombreTarea,String rutaAcceso) {
        this.nombreTarea = nombreTarea;

        String[] splitRutaAccesoPrioridad = rutaAcceso.split("\\*");
        this.uidProyecto = splitRutaAccesoPrioridad[0];
        this.uidTarea = splitRutaAccesoPrioridad[1];
        this.prioridad = splitRutaAccesoPrioridad[2];

    }

    //Validacion de datos de la modificacion de la tarea
    private boolean validarDatos() {
        boolean modificoTarea = true;
        outlinedTextFieldNombreTareaCDTarea.setErrorEnabled(false);

        //Si no hay cambios lo notifico, si no, valido campo a campo
        if (this.etNombreTarea.getText().toString().equals(this.nombreTarea) &&
                cgPrioridad.getCheckedChipId() == idPrioridadAnterior) {
            Toast.makeText(getContext(), getString(R.string.noHayCambios), Toast.LENGTH_LONG).show();
            modificoTarea = false;
        } else {
            //Si la tarea esta vacia no modifico, si no agrego al map la modificacion
            if (this.etNombreTarea.getText().toString().equals("")) {
                outlinedTextFieldNombreTareaCDTarea.setErrorEnabled(true);
                outlinedTextFieldNombreTareaCDTarea.setError(getString(R.string.faltaTarea));
                modificoTarea = false;
            } else {
                this.actualizaciones.put("tareas/"+uidProyecto+"/"+uidTarea+"/tarea",this.etNombreTarea.getText().toString());
            }

            //Si el chip seleccionado es distinto al que tenia la tarea antes de modificarla agrego al map el cambio respectivo
            if (cgPrioridad.getCheckedChipId() != idPrioridadAnterior) {
                switch (cgPrioridad.getCheckedChipId()) {
                    case R.id.chipPrioBajaCDTarea:
                        this.actualizaciones.put("tareas/"+uidProyecto+"/"+uidTarea+"/prioridad","0");
                        break;
                    case R.id.chipPrioMediaCDTarea:
                        this.actualizaciones.put("tareas/"+uidProyecto+"/"+uidTarea+"/prioridad","1");
                        break;
                    case R.id.chipPrioAltaCDTarea:
                        this.actualizaciones.put("tareas/"+uidProyecto+"/"+uidTarea+"/prioridad","2");
                        break;
                }
            }
        }
        return modificoTarea;
    }


}
