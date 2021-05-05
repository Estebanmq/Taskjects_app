package com.app.taskjects.adaptadores;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.taskjects.MenuToolbarActivity;
import com.app.taskjects.ModificarProyectoActivity;
import com.app.taskjects.PerfilEmpresaActivity;
import com.app.taskjects.R;
import com.app.taskjects.TareasProyectoActivity;
import com.app.taskjects.pojos.Proyecto;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class AdaptadorProyectosRV extends RecyclerView.Adapter<AdaptadorProyectosRV.ViewHolder> {

    //Variables para gestionar el usuario de firebase
    FirebaseAuth mAuth;
    FirebaseUser user;

    //SharedPreferences
    SharedPreferences sharedPreferences;

    private List<Proyecto> listProyectos;
    private LayoutInflater mInflater;
    private Context context;

    public AdaptadorProyectosRV(List<Proyecto>listProyectos, Context context) {
        this.listProyectos = listProyectos;
        this.mInflater = LayoutInflater.from(context);
        this.context = context;

        //Inicio variables
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        sharedPreferences = context.getSharedPreferences(mAuth.getUid(), Context.MODE_PRIVATE);

    }

    @Override
    public int getItemCount() { return listProyectos.size(); }

    @Override
    public AdaptadorProyectosRV.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.cardview_proyecto, parent, false);
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

                Intent intent;
                if (sharedPreferences.getString("tipoLogin", "").equals("E")) {
                    intent = new Intent(context, ModificarProyectoActivity.class);
                } else {
                    //Todo: hacer PerfilEmpleadoActivity y cambiar la línea de abajo con el nombre correcto
                    //intent = new Intent(MenuToolbarActivity.this, PerfilEmpleadoActivity.class);
                    intent = new Intent(context, TareasProyectoActivity.class);
                }
                intent.putExtra("uidProyecto",listProyectos.get(position).getUid());
                intent.putExtra("uidEmpresa",listProyectos.get(position).getUidEmpresa());
                context.startActivity(intent);

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
