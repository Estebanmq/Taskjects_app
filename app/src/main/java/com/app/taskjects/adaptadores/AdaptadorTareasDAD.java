package com.app.taskjects.adaptadores;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Pair;

import com.app.taskjects.R;
import com.app.taskjects.dialogos.CambiarDatosTareaDialog;
import com.app.taskjects.pojos.Tarea;
import com.google.firebase.firestore.FirebaseFirestore;
import com.woxthebox.draglistview.DragItemAdapter;

import java.util.ArrayList;


public class AdaptadorTareasDAD extends DragItemAdapter<Pair<Long, Tarea>,AdaptadorTareasDAD.ViewHolder> {

    //Accceso a la base de datos
    FirebaseFirestore db;

    private ArrayList<Pair<Long, Tarea>> listTareasAux;
    private Context context;
    private int mLayoutId;
    private int mGrabHandleId;
    private boolean mDragOnLongPress;

    public AdaptadorTareasDAD(ArrayList<Pair<Long, Tarea>> listTareas, Context context, int layoutId, int grabHandleId, boolean dragOnLongPress) {
        this.listTareasAux = listTareas;
        this.context = context;
        mLayoutId = layoutId;
        mGrabHandleId = grabHandleId;
        mDragOnLongPress = dragOnLongPress;
        db = FirebaseFirestore.getInstance();
        setItemList(listTareas);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(mLayoutId, parent, false);
        return new ViewHolder(view);
    }

    //Metodo que se encarga de asignar los datos de la tarea a los componentes de la tarjeta
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        //Objeto que almacena los datos de la tarea
        Tarea aux = listTareasAux.get(position).second;
        String uidEmpleado = aux.getUidEmpleado();

        holder.tvTarea.setText(aux.getTarea());
        if (uidEmpleado.equals("noasignado"))
            holder.tvEmpleadoTarea.setText(R.string.sinAsignar);
        else {
            db.collection("empleados")
                    .document(uidEmpleado)
                    .get()
                    .addOnSuccessListener(snapshot -> holder.tvEmpleadoTarea.setText(snapshot.getString("nombre").concat(" " + snapshot.getString("apellidos"))));
        }

        switch (aux.getPrioridad()) {
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
        holder.tvUidProyectoUidTareaPrioridad.setText(String.format("%s*%s*%s", aux.getUidProyecto(), aux.getUidTarea(),aux.getPrioridad()));
    }

    @Override
    public long getUniqueItemId(int position) { return mItemList.get(position).first; }

    //Clase que instancia los componentes de la tarjeta y los listener de las mismas
    class ViewHolder extends DragItemAdapter.ViewHolder {
        TextView tvPrioridad;
        TextView tvTarea;
        TextView tvEmpleadoTarea;
        //Este textView es uno que esta invisible y se encarga de almacenar datos importantes para luego poder acceder facilmente a la tarea en la base de datos
        TextView tvUidProyectoUidTareaPrioridad;

        ViewHolder(final View itemView) {
            super(itemView, mGrabHandleId, mDragOnLongPress);
            tvPrioridad = itemView.findViewById(R.id.tvPrioridad);
            tvTarea = itemView.findViewById(R.id.tvTarea);
            tvEmpleadoTarea = itemView.findViewById(R.id.tvEmpleadoTarea);
            tvUidProyectoUidTareaPrioridad = itemView.findViewById(R.id.tvUidProyectoUidTareaPrioridad);
        }

        //Metodo que detecta cuando se hace clic en una tarjeta especifica
        @Override
        public void onItemClicked(View view) {
            //Creo el dialogo que muestra el layout de la modificacion
            CambiarDatosTareaDialog cambiarDatosTareaDialog = new CambiarDatosTareaDialog();
            //Una vez que tengo el objeto del dialogo creado llamo al metodo setDatosTarea que se encarga de almacenar los datos de la tarea seleccionada
            cambiarDatosTareaDialog.setDatosTarea(this.tvTarea.getText().toString(),this.tvUidProyectoUidTareaPrioridad.getText().toString());
            //Muestro el dialogo en el contexto TareasProyecto con el tag Cambiar datos de la tarea
            cambiarDatosTareaDialog.show(((AppCompatActivity)context).getSupportFragmentManager(), "Cambiar datos de la tarea");
        }

        @Override
        public boolean onItemLongClicked(View view) { return true; }
    }
}
