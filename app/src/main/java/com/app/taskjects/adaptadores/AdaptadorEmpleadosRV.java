package com.app.taskjects.adaptadores;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.taskjects.R;
import com.app.taskjects.pojos.Categoria;
import com.app.taskjects.pojos.Empleado;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Map;

public class AdaptadorEmpleadosRV extends RecyclerView.Adapter<AdaptadorEmpleadosRV.ViewHolder> {

    private final String EMPLEADOS = "empleados";

    private Context context;

    private List<Empleado> listEmpleados;

    private Map<String, Categoria> mapCategorias;

    private String uidProyecto;

    //Variables para manejar la bbdd y sus datos
    FirebaseFirestore db;

    public AdaptadorEmpleadosRV(List<Empleado>listEmpleados, Map<String, Categoria> mapCategorias, String uidProyecto) {

        //Inicializo las variables de la clase
        this.listEmpleados = listEmpleados;
        this.mapCategorias = mapCategorias;
        this.uidProyecto =  uidProyecto;

        db = FirebaseFirestore.getInstance();
    }

    @Override
    public int getItemCount() { return listEmpleados.size(); }

    @Override
    public AdaptadorEmpleadosRV.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View empleadoView = inflater.inflate(R.layout.cardview_empleado, parent, false);
        return new ViewHolder(empleadoView);
    }

    @Override
    public void onBindViewHolder(@NonNull AdaptadorEmpleadosRV.ViewHolder holder, int position) {

        Empleado empleado = listEmpleados.get(position);
        holder.cvTvNombreEmpleado.setText(empleado.getNombreApellidos());
        holder.cvTvCategoria.setText(mapCategorias.get(empleado.getCategoria()).getDescripcion());
        if (empleado.getUidProyectos().contains(uidProyecto)) {
            holder.cbIncluido.setChecked(true);
        } else {
            holder.cbIncluido.setChecked(false);
        }

        holder.cbIncluido.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.d("taskjectsdebug", "clic en la posición: " + position);
                if (isChecked) {
                    Log.d("taskjectsdebug", "pasa a checkeado");
                    listEmpleados.get(position).getUidProyectos().add(uidProyecto);
                } else {
                    Log.d("taskjectsdebug", "pasa a descheckeado");
                    listEmpleados.get(position).getUidProyectos().remove(uidProyecto);
                }
                actualizarEmpleado(position);
            }
        });
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView cvTvNombreEmpleado, cvTvCategoria;
        CheckBox cbIncluido;
        RelativeLayout parentLayout;

        ViewHolder(View itemView) {
            super(itemView);
            cvTvNombreEmpleado = itemView.findViewById(R.id.cvTvNombreEmpleado);
            cvTvCategoria = itemView.findViewById(R.id.cvTvCategoria);
            cbIncluido = itemView.findViewById(R.id.cbIncluido);
            parentLayout = itemView.findViewById(R.id.parent_layout);
        }
    }

    private void actualizarEmpleado(int position) {

        db.collection(EMPLEADOS).document(listEmpleados.get(position).getUid())
                .set(listEmpleados.get(position))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("taskjectsdebug", "DocumentSnapshot successfully written!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("taskjectsdebug", "Error writing document", e);
                        Toast.makeText(context, context.getString(R.string.errorAccesoBD), Toast.LENGTH_LONG).show();
                    }
                });
    }
}
