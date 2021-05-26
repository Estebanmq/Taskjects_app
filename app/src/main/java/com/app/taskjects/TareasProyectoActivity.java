package com.app.taskjects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.app.taskjects.adaptadores.AdaptadorTareasDAD;
import com.app.taskjects.pojos.Tarea;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.woxthebox.draglistview.BoardView;
import com.woxthebox.draglistview.ColumnProperties;
import com.woxthebox.draglistview.DragItem;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class TareasProyectoActivity extends MenuToolbarActivity {

    //Variables para las columnas de tareas
    int tareasCreadas = 0;
    BoardView dadTareas;
    boolean mGridLayout;
    AdaptadorTareasDAD adaptadorTareasDADAE0;
    ArrayList<Pair<Long,Tarea>> arrayEstado0 = new ArrayList<>();
    AdaptadorTareasDAD adaptadorTareasDADAE1;
    ArrayList<Pair<Long,Tarea>> arrayEstado1 = new ArrayList<>();
    AdaptadorTareasDAD adaptadorTareasDADAE2;
    ArrayList<Pair<Long,Tarea>> arrayEstado2 = new ArrayList<>();
    AdaptadorTareasDAD adaptadorTareasDADAE3;
    ArrayList<Pair<Long,Tarea>> arrayEstado3 = new ArrayList<>();
    AdaptadorTareasDAD adaptadorTareasDADAE4;
    ArrayList<Pair<Long,Tarea>> arrayEstado4 = new ArrayList<>();

    ArrayList<ArrayList<Pair<Long,Tarea>>> arrayAllEstados = new ArrayList<ArrayList<Pair<Long, Tarea>>>(){{add(arrayEstado0);add(arrayEstado1);add(arrayEstado2);add(arrayEstado3);add(arrayEstado4);}};

    FloatingActionButton fABTareas;
    BottomAppBar bottomAppBar;

    String uidProyecto;
    String uidEmpresa;
    String uidEmpleado;

    //Variables para manejar la bbdd y sus datos
    FirebaseAuth mAuth;
    DatabaseReference mDatabase;

    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tareas_proyecto_layout);

        //Inicializacion de variables
        bottomAppBar = findViewById(R.id.bottomAppBar);
        setSupportActionBar(bottomAppBar);
        fABTareas = findViewById(R.id.fABTareas);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        sharedPreferences = getSharedPreferences(mAuth.getUid(), Context.MODE_PRIVATE);

        if (sharedPreferences.getString("categoria", "").equals("1")) {
            findViewById(R.id.fABTareas).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.fABTareas).setVisibility(View.INVISIBLE);
        }


        uidProyecto = getIntent().getStringExtra("uidProyecto");
        uidEmpresa = getIntent().getStringExtra("uidEmpresa");
        uidEmpleado = sharedPreferences.getString("uidEmpleado","");

        dadTareas = findViewById(R.id.dadTareas);

        //Listener para controlar donde va cada card de cada tarea
        dadTareas.setBoardListener(new BoardView.BoardListener() {
            @Override
            public void onItemDragStarted(int column, int row) {
                // Toast.makeText(getContext(), "Start - column: " + column + " row: " + row, Toast.LENGTH_SHORT).show();
            }

            //Listener que al terminar el drag and drop te dice antigua columna y fila y la nueva columna y fila
            @Override
            public void onItemDragEnded(int fromColumn, int fromRow, int toColumn, int toRow) {
                if (fromColumn != toColumn || fromRow != toRow) {
                    if (fromColumn == 0 && toColumn == 1) {
                        mDatabase.child("tareas")
                                .child(uidProyecto)
                                .child(arrayAllEstados.get(toColumn).get(toRow).second.getUidTarea())
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
                    }
                    mDatabase.child("tareas")
                            .child(uidProyecto)
                            .child(arrayAllEstados.get(toColumn).get(toRow).second.getUidTarea())
                            .child("estado")
                            .setValue(String.valueOf(toColumn))
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d("Tarea cambiada de estado correcto"," Correcto");
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d("Fallo al actualizar el estado de tarea",e.getMessage());
                                }
                            });
                    Log.d("oldColumn -> " + fromColumn, " newColumn -> " + toColumn);
                    Log.d("oldrow -> " + fromRow, " new row -> " + toRow);
                }
            }

            @Override
            public void onItemChangedPosition(int oldColumn, int oldRow, int newColumn, int newRow) {
                //Toast.makeText(TareasProyectoActivity.this, "Position changed - column: " + newColumn + " row: " + newRow, Toast.LENGTH_SHORT).show();
                //Log.d("oldColumn -> " + oldColumn, " newColumn -> " + newColumn);
            }

            @Override
            public void onItemChangedColumn(int oldColumn, int newColumn) {
                /*
                Toast.makeText(getContext(), "Nueva posicion de columna "+ newColumn+ " antigua " + oldColumn, Toast.LENGTH_SHORT).show();
                //Esto es para cambiar el contador de cards de cada columna, puede estar guay implementarlo
                TextView itemCount1 = mBoardView.getHeaderView(oldColumn).findViewById(R.id.item_count);
                itemCount1.setText(String.valueOf(mBoardView.getAdapter(oldColumn).getItemCount()));
                TextView itemCount2 = mBoardView.getHeaderView(newColumn).findViewById(R.id.item_count);
                itemCount2.setText(String.valueOf(mBoardView.getAdapter(newColumn).getItemCount()));

                 */

            }


            @Override
            public void onFocusedColumnChanged(int oldColumn, int newColumn) {
                //Toast.makeText(getContext(), "Focused column changed from " + oldColumn + " to " + newColumn, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onColumnDragStarted(int position) {
                //Toast.makeText(getContext(), "Column drag started from " + position, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onColumnDragChangedPosition(int oldPosition, int newPosition) {
                //Toast.makeText(getContext(), "Column changed from " + oldPosition + " to " + newPosition, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onColumnDragEnded(int position) {
                //Toast.makeText(getContext(), "Column drag ended at " + position, Toast.LENGTH_SHORT).show();
            }
        });
        //Listener para evitar drop en ciertas columnas
        dadTareas.setBoardCallback(new BoardView.BoardCallback() {
            @Override
            public boolean canDragItemAtPosition(int column, int dragPosition) {
                // Add logic here to prevent an item to be dragged
                return true;
            }

            @Override
            public boolean canDropItemAtPosition(int oldColumn, int oldRow, int newColumn, int newRow) {
                if (newColumn == 0) {
                    Toast.makeText(TareasProyectoActivity.this,"No te puedes desasignar la tarea",Toast.LENGTH_SHORT).show();
                    return false;
                }
                return true;
            }
        });

        dadTareas.clearBoard();
        dadTareas.setCustomDragItem(mGridLayout ? null  : new DragTarea(TareasProyectoActivity.this, R.layout.dad_column_tarea_layout));

        String[]estados = {"Inicial","Pendiente","En proceso","Pausada","Terminada"};
        for (int i=0; i<estados.length;i++) {
            Log.d("debugFor","valor de i "+i);
            cargarColumnas(estados[i]);
        }

        cargaDeTareas();
        //imprimirTablaDAD();
        //cargarTareasProyecto();
    }

    private void cargarColumnas(String estado) {

        LinearLayoutManager layoutManager = mGridLayout ? new GridLayoutManager(TareasProyectoActivity.this, 4) : new LinearLayoutManager(TareasProyectoActivity.this);
        switch (estado) {
            case "Inicial":
                adaptadorTareasDADAE0 = new AdaptadorTareasDAD(arrayAllEstados.get(0),this, R.layout.dad_column_tarea_layout, R.id.item_layout, true);
                View headerAE0 = View.inflate(this, R.layout.dad_column_header, null);
                ((TextView) headerAE0.findViewById(R.id.tvEstado)).setText(estado);

                ColumnProperties columnPropertiesAE0 = ColumnProperties.Builder.newBuilder(adaptadorTareasDADAE0)
                        .setLayoutManager(layoutManager)
                        .setHasFixedItemSize(false)
                        .setHeader(headerAE0)
                        .build();

                dadTareas.addColumn(columnPropertiesAE0);
                break;
            case "Pendiente":
                adaptadorTareasDADAE1 = new AdaptadorTareasDAD(arrayAllEstados.get(1),this, R.layout.dad_column_tarea_layout, R.id.item_layout, true);
                View headerAE1 = View.inflate(this, R.layout.dad_column_header, null);
                ((TextView) headerAE1.findViewById(R.id.tvEstado)).setText(estado);

                ColumnProperties columnPropertiesAE1 = ColumnProperties.Builder.newBuilder(adaptadorTareasDADAE1)
                        .setLayoutManager(layoutManager)
                        .setHasFixedItemSize(false)
                        .setHeader(headerAE1)
                        .build();

                dadTareas.addColumn(columnPropertiesAE1);
                break;
            case "En proceso":
                adaptadorTareasDADAE2 = new AdaptadorTareasDAD(arrayAllEstados.get(2), this,R.layout.dad_column_tarea_layout, R.id.item_layout, true);
                View headerAE2 = View.inflate(this, R.layout.dad_column_header, null);
                ((TextView) headerAE2.findViewById(R.id.tvEstado)).setText(estado);

                ColumnProperties columnPropertiesAE2 = ColumnProperties.Builder.newBuilder(adaptadorTareasDADAE2)
                        .setLayoutManager(layoutManager)
                        .setHasFixedItemSize(false)
                        .setHeader(headerAE2)
                        .build();

                dadTareas.addColumn(columnPropertiesAE2);
                break;
            case "Pausada":
                adaptadorTareasDADAE3 = new AdaptadorTareasDAD(arrayAllEstados.get(3),this, R.layout.dad_column_tarea_layout, R.id.item_layout, true);
                View headerAE3 = View.inflate(this, R.layout.dad_column_header, null);
                ((TextView) headerAE3.findViewById(R.id.tvEstado)).setText(estado);

                ColumnProperties columnPropertiesAE3 = ColumnProperties.Builder.newBuilder(adaptadorTareasDADAE3)
                        .setLayoutManager(layoutManager)
                        .setHasFixedItemSize(false)
                        .setHeader(headerAE3)
                        .build();

                dadTareas.addColumn(columnPropertiesAE3);
                break;

            case "Terminada":
                adaptadorTareasDADAE4 = new AdaptadorTareasDAD(arrayAllEstados.get(4),this,R.layout.dad_column_tarea_layout, R.id.item_layout, true);
                View headerAE4 = View.inflate(this, R.layout.dad_column_header, null);
                ((TextView) headerAE4.findViewById(R.id.tvEstado)).setText(estado);

                ColumnProperties columnPropertiesAE4 = ColumnProperties.Builder.newBuilder(adaptadorTareasDADAE4)
                        .setLayoutManager(layoutManager)
                        .setHasFixedItemSize(false)
                        .setHeader(headerAE4)
                        .build();

                dadTareas.addColumn(columnPropertiesAE4);
                break;
        }
    }

    private void cargaDeTareas() {
        Query queryAE0 = mDatabase.child("tareas").child(uidProyecto).orderByChild("estado").equalTo("0");
        queryAE0.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (previousChildName != null)
                    Log.d("Debug onChildAdded estado 0",previousChildName);
                if (snapshot.exists()) {
                    Tarea aux = snapshot.getValue(Tarea.class);
                    if (aux != null) {
                        if (!buscarTarea(0,aux.getUidTarea())) {
                            long auxTareasCreadas = tareasCreadas++;
                            arrayAllEstados.get(0).add(new Pair<>(auxTareasCreadas, aux));
                            adaptadorTareasDADAE0.notifyDataSetChanged();
                        }
                    }

                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Log.d("TareasProyectoActivityDebug estado 0","entro onChildChanged");
                //Log.d("Debug onChildChanged noasigando",previousChildName);
                if (snapshot.exists()) {
                    Tarea aux = snapshot.getValue(Tarea.class);
                    if (aux != null) {
                        Log.d("Debug onChildChanged estado 0",aux.toString());
                        cambiarDatosTarea(0,adaptadorTareasDADAE0,snapshot.getKey(),aux);
                    }
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                Log.d("TareasProyectoActivityDebug estado 0","entro onChildRemoved");
                //Log.d("Debug onChildRemoved noasignado","");
                if (snapshot.exists()) {
                    Tarea aux = snapshot.getValue(Tarea.class);
                    if (aux != null) {
                        Log.d("Debug onChildChanged estado 0",aux.toString());
                        removeTareaAntigua(0,adaptadorTareasDADAE0,snapshot.getKey(),0);
                    }
                }
            }

            //Este listener se activa cuando se detectan cambios en el orden
            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Log.d("TareasProyectoActivityDebug estado 0","entro onChildMoved");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("TareasProyectoActivityDebug estado 0","entro onChildCancelled");
            }
        });

        Query queryAE1 = mDatabase.child("tareas").child(uidProyecto).orderByChild("estado").equalTo("1");
        queryAE1.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (previousChildName != null)
                    Log.d("Debug onChildAdded estado 1",previousChildName);
                if (snapshot.exists()) {
                    Tarea aux = snapshot.getValue(Tarea.class);
                    if (aux != null &&aux.getUidEmpleado().equals(uidEmpleado)) {
                        if (!buscarTarea(1,aux.getUidTarea())) {
                            long auxTareasCreadas = tareasCreadas++;
                            arrayAllEstados.get(1).add(new Pair<>(auxTareasCreadas, aux));
                            adaptadorTareasDADAE1.notifyDataSetChanged();
                        }
                    }

                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Log.d("TareasProyectoActivityDebug estado 1","entro onChildChanged");
                //Log.d("Debug onChildChanged noasigando",previousChildName);
                if (snapshot.exists()) {
                    Tarea aux = snapshot.getValue(Tarea.class);
                    if (aux != null) {
                        Log.d("Debug onChildChanged estado 1",aux.toString());
                        cambiarDatosTarea(1,adaptadorTareasDADAE1,snapshot.getKey(),aux);
                    }
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                Log.d("TareasProyectoActivityDebug estado 1","entro onChildRemoved");
                //Log.d("Debug onChildRemoved noasignado","");
                if (snapshot.exists()) {
                    Tarea aux = snapshot.getValue(Tarea.class);
                    if (aux != null) {
                        Log.d("Key snapshot estado 1",snapshot.getKey());
                        Log.d("Debug onChildChanged estado 1",aux.toString());
                        removeTareaAntigua(1,adaptadorTareasDADAE1,snapshot.getKey(),1);
                    }
                }
            }

            //Este listener se activa cuando se detectan cambios en el orden
            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Log.d("TareasProyectoActivityDebug estado 1","entro onChildMoved");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("TareasProyectoActivityDebug estado 1","entro onChildCancelled");
            }
        });

        Query queryAE2 = mDatabase.child("tareas").child(uidProyecto).orderByChild("estado").equalTo("2");
        queryAE2.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (previousChildName != null)
                    Log.d("Debug onChildAdded estado 2",previousChildName);
                if (snapshot.exists()) {
                    Tarea aux = snapshot.getValue(Tarea.class);
                    if (aux != null && aux.getUidEmpleado().equals(uidEmpleado)) {
                        if (!buscarTarea(2,aux.getUidTarea())) {
                            long auxTareasCreadas = tareasCreadas++;
                            arrayAllEstados.get(2).add(new Pair<>(auxTareasCreadas, aux));
                            adaptadorTareasDADAE2.notifyDataSetChanged();
                        }
                    }

                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Log.d("TareasProyectoActivityDebug estado 2","entro onChildChanged");
                //Log.d("Debug onChildChanged noasigando",previousChildName);
                if (snapshot.exists()) {
                    Tarea aux = snapshot.getValue(Tarea.class);
                    if (aux != null) {
                        Log.d("Debug onChildChanged estado 2",aux.toString());
                        cambiarDatosTarea(2,adaptadorTareasDADAE2,snapshot.getKey(),aux);
                    }
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                Log.d("TareasProyectoActivityDebug estado 2","entro onChildRemoved");
                //Log.d("Debug onChildRemoved noasignado","");
                if (snapshot.exists()) {
                    Tarea aux = snapshot.getValue(Tarea.class);
                    if (aux != null) {
                        Log.d("Debug onChildChanged estado 2",aux.toString());
                        removeTareaAntigua(2,adaptadorTareasDADAE2,snapshot.getKey(),2);
                    }
                }
            }

            //Este listener se activa cuando se detectan cambios en el orden
            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Log.d("TareasProyectoActivityDebug estado 2","entro onChildMoved");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("TareasProyectoActivityDebug estado 2","entro onChildCancelled");
            }
        });

        Query queryAE3 = mDatabase.child("tareas").child(uidProyecto).orderByChild("estado").equalTo("3");
        queryAE3.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (previousChildName != null)
                    Log.d("Debug onChildAdded estado 3",previousChildName);
                if (snapshot.exists()) {
                    Tarea aux = snapshot.getValue(Tarea.class);
                    if (aux != null && aux.getUidEmpleado().equals(uidEmpleado)) {
                        if (!buscarTarea(3,aux.getUidTarea())) {
                            long auxTareasCreadas = tareasCreadas++;
                            arrayAllEstados.get(3).add(new Pair<>(auxTareasCreadas, aux));
                            adaptadorTareasDADAE3.notifyDataSetChanged();
                        }
                    }

                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Log.d("TareasProyectoActivityDebug estado 3","entro onChildChanged");
                //Log.d("Debug onChildChanged noasigando",previousChildName);
                if (snapshot.exists()) {
                    Tarea aux = snapshot.getValue(Tarea.class);
                    if (aux != null) {
                        Log.d("Debug onChildChanged estado 3",aux.toString());
                        cambiarDatosTarea(3,adaptadorTareasDADAE3,snapshot.getKey(),aux);
                    }
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                Log.d("TareasProyectoActivityDebug estado 3","entro onChildRemoved");
                //Log.d("Debug onChildRemoved noasignado","");
                if (snapshot.exists()) {
                    Tarea aux = snapshot.getValue(Tarea.class);
                    if (aux != null) {
                        Log.d("Debug onChildChanged estado 3",aux.toString());
                        removeTareaAntigua(3,adaptadorTareasDADAE3,snapshot.getKey(),3);
                    }
                }
            }

            //Este listener se activa cuando se detectan cambios en el orden
            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Log.d("TareasProyectoActivityDebug estado 3","entro onChildMoved");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("TareasProyectoActivityDebug estado 3","entro onChildCancelled");
            }
        });

        Query queryAE4 = mDatabase.child("tareas").child(uidProyecto).orderByChild("estado").equalTo("4");
        queryAE4.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (previousChildName != null)
                    Log.d("Debug onChildAdded estado 4",previousChildName);
                if (snapshot.exists()) {
                    Tarea aux = snapshot.getValue(Tarea.class);
                    if (aux != null && aux.getUidEmpleado().equals(uidEmpleado)) {
                        if (!buscarTarea(4,aux.getUidTarea())) {
                            long auxTareasCreadas = tareasCreadas++;
                            arrayAllEstados.get(4).add(new Pair<>(auxTareasCreadas, aux));
                            adaptadorTareasDADAE4.notifyDataSetChanged();
                        }
                    }

                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Log.d("TareasProyectoActivityDebug estado 4","entro onChildChanged");
                //Log.d("Debug onChildChanged noasigando",previousChildName);
                if (snapshot.exists()) {
                    Tarea aux = snapshot.getValue(Tarea.class);
                    if (aux != null) {
                        Log.d("Debug onChildChanged estado 4",aux.toString());
                        cambiarDatosTarea(4,adaptadorTareasDADAE4,snapshot.getKey(),aux);
                    }
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                Log.d("TareasProyectoActivityDebug estado 4","entro onChildRemoved");
                //Log.d("Debug onChildRemoved noasignado","");
                if (snapshot.exists()) {
                    Tarea aux = snapshot.getValue(Tarea.class);
                    if (aux != null) {
                        Log.d("Debug onChildChanged estado 4",aux.toString());
                        removeTareaAntigua(4,adaptadorTareasDADAE4,snapshot.getKey(),4);
                    }
                }
            }

            //Este listener se activa cuando se detectan cambios en el orden
            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Log.d("TareasProyectoActivityDebug estado 4","entro onChildMoved");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("TareasProyectoActivityDebug estado 4","entro onChildCancelled");
            }
        });
    }

    private boolean buscarTarea(int arrayEstados,String uidTarea){
        for (int i=0;i<arrayAllEstados.get(arrayEstados).size();i++) {
            if (arrayAllEstados.get(arrayEstados).get(i).second.getUidTarea().equals(uidTarea))
                return true;
        }
        return false;
    }

    //Metodo que cambia los datos nuevos de una tarea por la encontrada en el array
    private void cambiarDatosTarea(int arrayEstados,AdaptadorTareasDAD adapter,String keyTarea,Tarea nuevosDatosTarea) {
        Log.d("debug cambio datos, keyTarea -> " + keyTarea, " Tarea a encontrar -> "+nuevosDatosTarea.toString());
        for (int i = 0; i<arrayAllEstados.get(arrayEstados).size(); i++) {
            if (arrayAllEstados.get(arrayEstados).get(i).second.getUidTarea().equals(keyTarea)) {
                Log.d("Posicion del item -> ",""+adapter.getPositionForItemId(arrayAllEstados.get(arrayEstados).get(i).first));
                int posicionTarea = adapter.getPositionForItemId(arrayAllEstados.get(arrayEstados).get(i).first);
                arrayAllEstados.get(arrayEstados).get(posicionTarea).second.setNuevosDatos(nuevosDatosTarea);
                adapter.notifyDataSetChanged();
            }
        }
    }

    //Metodo que elimina de un array especifico una tarea que ha cambiado a otro array en la base de datos
    private void removeTareaAntigua(int arrayEstados,AdaptadorTareasDAD adapter,String keyTarea,int columna){
        for (int i = 0; i<arrayAllEstados.get(arrayEstados).size(); i++) {
            Log.d("Debug for tareas",arrayAllEstados.get(arrayEstados).get(i).second.toString());
            Log.d("Debug for tareas key tarea snapshot ->",keyTarea);
            Log.d("Debug for tareas key tarea array -> ",arrayAllEstados.get(arrayEstados).get(i).second.getUidTarea());
            if (arrayAllEstados.get(arrayEstados).get(i).second.getUidTarea().equals(keyTarea)) {
                Log.d("Posicion del item -> ",""+adapter.getPositionForItemId(arrayAllEstados.get(arrayEstados).get(i).first));
                dadTareas.removeItem(columna,adapter.getPositionForItemId(arrayAllEstados.get(arrayEstados).get(i).first));
                adapter.notifyDataSetChanged();

            }
        }

    }

    //Logica para hacer el drag de items
    private static class DragTarea extends DragItem {

        DragTarea(Context context, int layoutId) { super(context, layoutId); }

        @Override
        public void onBindDragView(View clickedView, View dragView) {
            Log.d("Debug tareas","Entro en el bindDragView");
            CharSequence text = ((TextView) clickedView.findViewById(R.id.tvTarea)).getText();
            ((TextView) dragView.findViewById(R.id.tvTarea)).setText(text);
            CharSequence text2 = ((TextView) clickedView.findViewById(R.id.tvEmpleadoTarea)).getText();
            ((TextView) dragView.findViewById(R.id.tvEmpleadoTarea)).setText(text2);
            TextView tvAux = clickedView.findViewById(R.id.tvPrioridad);
            dragView.findViewById(R.id.tvPrioridad).setBackground(tvAux.getBackground());

            CardView dragCard = dragView.findViewById(R.id.card);
            CardView clickedCard = clickedView.findViewById(R.id.card);

            dragCard.setMaxCardElevation(40);
            dragCard.setCardElevation(clickedCard.getCardElevation());
            // I know the dragView is a FrameLayout and that is why I can use setForeground below api level 23
            dragCard.setForeground(clickedView.getResources().getDrawable(R.drawable.card_view_drag_foreground));
        }

        @Override
        public void onMeasureDragView(View clickedView, View dragView) {
            CardView dragCard = dragView.findViewById(R.id.card);
            CardView clickedCard = clickedView.findViewById(R.id.card);
            int widthDiff = dragCard.getPaddingLeft() - clickedCard.getPaddingLeft() + dragCard.getPaddingRight() -
                    clickedCard.getPaddingRight();
            int heightDiff = dragCard.getPaddingTop() - clickedCard.getPaddingTop() + dragCard.getPaddingBottom() -
                    clickedCard.getPaddingBottom();
            int width = clickedView.getMeasuredWidth() + widthDiff;
            int height = clickedView.getMeasuredHeight() + heightDiff;
            dragView.setLayoutParams(new FrameLayout.LayoutParams(width, height));

            int widthSpec = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY);
            int heightSpec = View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY);
            dragView.measure(widthSpec, heightSpec);
        }

        @Override
        public void onStartDragAnimation(View dragView) {
            CardView dragCard = dragView.findViewById(R.id.card);
            ObjectAnimator anim = ObjectAnimator.ofFloat(dragCard, "CardElevation", dragCard.getCardElevation(), 40);
            anim.setInterpolator(new DecelerateInterpolator());
            anim.setDuration(ANIMATION_DURATION);
            anim.start();
        }

        @Override
        public void onEndDragAnimation(View dragView) {
            CardView dragCard = dragView.findViewById(R.id.card);
            ObjectAnimator anim = ObjectAnimator.ofFloat(dragCard, "CardElevation", dragCard.getCardElevation(), 40);
            anim.setInterpolator(new DecelerateInterpolator());
            anim.setDuration(ANIMATION_DURATION);
            anim.start();
        }
    }

    public void aniadirTarea(View view) {
        Intent pantallaAniadirTarea = new Intent(this,AniadirTareaActivity.class);
        pantallaAniadirTarea.putExtra("uidProyecto",uidProyecto);
        startActivity(pantallaAniadirTarea);
    }
}