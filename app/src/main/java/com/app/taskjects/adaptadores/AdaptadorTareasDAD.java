package com.app.taskjects.adaptadores;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Pair;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.app.taskjects.R;
import com.app.taskjects.TareasProyectoActivity;
import com.app.taskjects.dialogos.CambiarDatosTareaDialog;
import com.app.taskjects.dialogos.RecuperarPasswordDialog;
import com.app.taskjects.pojos.Tarea;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.woxthebox.draglistview.DragItemAdapter;

import java.util.ArrayList;


public class AdaptadorTareasDAD extends DragItemAdapter<Pair<Long, Tarea>,AdaptadorTareasDAD.ViewHolder> {

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

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);

        String uidEmpleado = listTareasAux.get(position).second.getUidEmpleado();
        holder.tvTarea.setText(listTareasAux.get(position).second.getTarea());
        Log.d("AdaptadorDebug","Nombre de la tarea ->"+listTareasAux.get(position).second.getTarea());
        if (uidEmpleado.equals("noasignado"))
            holder.tvEmpleadoTarea.setText(R.string.sinAsignar);
        else {
            db.collection("empleados")
                    .document(uidEmpleado)
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot snapshot) {
                            holder.tvEmpleadoTarea.setText(snapshot.getString("nombre").concat(" " + snapshot.getString("apellidos")));
                        }
                    });
        }
        switch (listTareasAux.get(position).second.getPrioridad()) {
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

    @Override
    public long getUniqueItemId(int position) { return mItemList.get(position).first; }

    class ViewHolder extends DragItemAdapter.ViewHolder {
        TextView tvPrioridad;
        TextView tvTarea;
        TextView tvEmpleadoTarea;


        ViewHolder(final View itemView) {
            super(itemView, mGrabHandleId, mDragOnLongPress);
            tvPrioridad = itemView.findViewById(R.id.tvPrioridad);
            tvTarea = itemView.findViewById(R.id.tvTarea);
            tvEmpleadoTarea = itemView.findViewById(R.id.tvEmpleadoTarea);
        }

        @Override
        public void onItemClicked(View view) {
            /*
            Toast.makeText(view.getContext(), "Item clicked", Toast.LENGTH_SHORT).show();
            TareasProyectoActivity.showDialog(this.tvTarea.getText().toString(),context);

             */
        }

        @Override
        public boolean onItemLongClicked(View view) {
            Toast.makeText(view.getContext(), "Item long clicked", Toast.LENGTH_SHORT).show();
            return true;
        }
    }
}
