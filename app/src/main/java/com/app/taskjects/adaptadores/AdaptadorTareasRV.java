package com.app.taskjects.adaptadores;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.taskjects.ModificarProyectoActivity;
import com.app.taskjects.R;
import com.app.taskjects.TareasProyectoActivity;
import com.app.taskjects.pojos.Proyecto;
import com.app.taskjects.pojos.Tarea;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class AdaptadorTareasRV extends RecyclerView.Adapter<AdaptadorTareasRV.ViewHolder> {

    FirebaseFirestore db;

    private List<Tarea>listTareas;
    private LayoutInflater mInflater;
    private Context context;



    public AdaptadorTareasRV(List<Tarea>listTareas, Context context) {
        this.listTareas = listTareas;
        this.mInflater = LayoutInflater.from(context);
        this.context = context;
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public int getItemCount() { return listTareas.size(); }

    @Override
    public AdaptadorTareasRV.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.cardview_tarea, parent, false);
        return new AdaptadorTareasRV.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdaptadorTareasRV.ViewHolder holder, int position) {
        String uidEmpleado = listTareas.get(position).getUidEmpleado();
        holder.cvTvNombreTarea.setText(listTareas.get(position).getTarea());
        if (uidEmpleado.equals("noasignado"))
            holder.cvTvEmpleado.setText(R.string.sinAsignar);
        else {
            db.collection("empleados")
                    .document(uidEmpleado)
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot snapshot) {
                            holder.cvTvEmpleado.setText(snapshot.getString("nombre").concat(" " + snapshot.getString("apellidos")));
                        }
                    });
        }

        switch (listTareas.get(position).getPrioridad()) {
            case "0":
                holder.tvPrioridad.setBackgroundTintList(context.getResources().getColorStateList(R.color.color_prio_baja,context.getTheme()));
                break;
            case "1":
                holder.tvPrioridad.setBackgroundTintList(context.getResources().getColorStateList(R.color.color_prio_media,context.getTheme()));
                break;
            case "2":
                holder.tvPrioridad.setBackgroundTintList(context.getResources().getColorStateList(R.color.color_prio_alta,context.getTheme()));
                break;

        }

    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView cvTvNombreTarea, cvTvEmpleado, tvPrioridad;
        RelativeLayout parentLayout;

        ViewHolder(View itemView) {
            super(itemView);
            cvTvNombreTarea = itemView.findViewById(R.id.cvTvNombreTarea);
            cvTvEmpleado = itemView.findViewById(R.id.cvTvEmpleado);
            tvPrioridad = itemView.findViewById(R.id.tvPrioridad);
            parentLayout = itemView.findViewById(R.id.parent_layout);

        }
    }
}
