package com.app.taskjects.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.taskjects.R;
import com.app.taskjects.pojos.Proyecto;

import java.util.List;

public class AdaptadorProyectosRV extends RecyclerView.Adapter<AdaptadorProyectosRV.ViewHolder> {

    private List<Proyecto> listProyectos;
    private LayoutInflater mInflater;
    private Context context;

    public AdaptadorProyectosRV(List<Proyecto>listProyectos, Context context) {
        this.listProyectos = listProyectos;
        this.mInflater = LayoutInflater.from(context);
        this.context = context;
    }

    @Override
    public int getItemCount() { return listProyectos.size(); }

    @Override
    public AdaptadorProyectosRV.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.cardview_proyecto, null);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.cvTvNombreProyecto.setText(listProyectos.get(position).getNombre());
        holder.cvTvDescripcionProyecto.setText(listProyectos.get(position).getDescripcion());
        holder.cvTvJefeEmpleado.setText(listProyectos.get(position).getCifEmpleadoJefe());
    }


    public void setTareas(List<Proyecto>itemsProyecto) { listProyectos = itemsProyecto; }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView cvTvNombreProyecto, cvTvDescripcionProyecto, cvTvJefeEmpleado;

        ViewHolder(View itemView) {
            super(itemView);
            cvTvNombreProyecto = itemView.findViewById(R.id.cvTvNombreProyecto);
            cvTvDescripcionProyecto = itemView.findViewById(R.id.cvTvDescripcionProyecto);
            cvTvJefeEmpleado = itemView.findViewById(R.id.cvTvJefeEmpleado);
        }
    }
}
