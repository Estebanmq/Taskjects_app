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
        holder.cvTvJefeEmpleado.setText(listProyectos.get(position).getUidEmpleadoJefe());
        holder.cvTvUid.setText(listProyectos.get(position).getUid());

        holder.parentLayout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Log.d("taskjectsdebug","entra en onClick: " + listProyectos.get(position).getUid());

                Intent pantallaModificarProyecto = new Intent(context, ModificarProyectoActivity.class);
                pantallaModificarProyecto.putExtra("uidProyecto",listProyectos.get(position).getUid());
                context.startActivity(pantallaModificarProyecto);

            }
        });
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView cvTvNombreProyecto, cvTvDescripcionProyecto, cvTvJefeEmpleado, cvTvUid;
        RelativeLayout parentLayout;

        ViewHolder(View itemView) {
            super(itemView);
            cvTvNombreProyecto = itemView.findViewById(R.id.cvTvNombreProyecto);
            cvTvDescripcionProyecto = itemView.findViewById(R.id.cvTvDescripcionProyecto);
            cvTvJefeEmpleado = itemView.findViewById(R.id.cvTvJefeEmpleado);
            cvTvUid = itemView.findViewById(R.id.cvTvUid);

            parentLayout = itemView.findViewById(R.id.parent_layout);

        }
    }
}
