package com.app.taskjects;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.app.taskjects.adaptadores.AdaptadorTareasDAD;
import com.app.taskjects.pojos.Categoria;
import com.app.taskjects.pojos.Tarea;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.woxthebox.draglistview.BoardView;
import com.woxthebox.draglistview.ColumnProperties;
import com.woxthebox.draglistview.DragItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class TareasProyectoActivity extends MenuToolbarActivity {

    private final String CATEGORIAS = "categorias";

    //Variables para el menu de abajo
    MenuItem plusEmpleado;
    BottomAppBar bottomAppBar;
    FloatingActionButton fabTarea;

    //Variables que almacenan informacion relevante
    Map<String, Categoria> mapCategorias;
    String uidProyecto;
    String uidJefeProyecto;
    String uidEmpresa;
    String uidEmpleado;

    //Variables para manejar la bbdd y sus datos
    FirebaseFirestore db;
    FirebaseAuth mAuth;
    DatabaseReference mDatabase;

    //Variable para acceder al sharedpreferences
    SharedPreferences sharedPreferences;

    //Variables para las columnas de tareas
    int tareasCreadas = 0;
    BoardView dadTareas;
    boolean mGridLayout;

    ArrayList<Pair<Long,Tarea>> arrayEstado0 = new ArrayList<>();
    ArrayList<Pair<Long,Tarea>> arrayEstado1 = new ArrayList<>();
    ArrayList<Pair<Long,Tarea>> arrayEstado2 = new ArrayList<>();
    ArrayList<Pair<Long,Tarea>> arrayEstado3 = new ArrayList<>();
    ArrayList<Pair<Long,Tarea>> arrayEstado4 = new ArrayList<>();
    //Array de arrays para almacenar todos los arrays de tareas
    ArrayList<ArrayList<Pair<Long,Tarea>>> arrayAllEstados = new ArrayList<ArrayList<Pair<Long, Tarea>>>(){{add(arrayEstado0);add(arrayEstado1);add(arrayEstado2);add(arrayEstado3);add(arrayEstado4);}};
    //Array de arrays para almacenar todos los adaptadores
    ArrayList<AdaptadorTareasDAD>arrayAllAdaptadores = new ArrayList<AdaptadorTareasDAD>(){};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tareas_proyecto_layout);

        //Inicializo el acceso a la BD
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        //Inicializacion de componentes
        bottomAppBar = findViewById(R.id.bottomAppBar);
        setSupportActionBar(bottomAppBar);
        fabTarea = findViewById(R.id.fABTareas);
        dadTareas = findViewById(R.id.dadTareas);

        //Inicialización de variables
        mapCategorias = new TreeMap<>();
        uidProyecto = getIntent().getStringExtra("uidProyecto");
        uidJefeProyecto = getIntent().getStringExtra("uidJefeProyecto");
        uidEmpresa = getIntent().getStringExtra("uidEmpresa");

        //Inicializacion sharedpreferences
        sharedPreferences = getSharedPreferences(mAuth.getUid(), Context.MODE_PRIVATE);
        uidEmpleado = sharedPreferences.getString("uidEmpleado","");

        //Llamas a los metodos que cargan todoo lo relacionado con las tareas (columnas,datos de las tareas en la bbdd y categorias)
        cargarEscuchadoresDadTareas();
        cargaDeColumnas();
        recuperarCategorias();
    }

    @Override
    protected void onResume() {
        super.onResume();
        fabTarea.setEnabled(true);
    }

    //Metodo que agrega un listener para gesitonar los cambios de las tareas entre las distintas columnas
    private void cargarEscuchadoresDadTareas() {
        dadTareas.setHorizontalScrollBarEnabled(false);
        dadTareas.setVerticalScrollBarEnabled(false);

        //Listener para controlar donde va cada card de cada tarea
        dadTareas.setBoardListener(new BoardView.BoardListener() {

            @Override
            public void onItemDragStarted(int column, int row) { }

            //Listener para el drop de una tarea en concreto
            @Override
            public void onItemDragEnded(int fromColumn, int fromRow, int toColumn, int toRow) {
                //Si la nueva posicion no es la misma
                if (fromColumn != toColumn || fromRow != toRow) {
                    Map<String,Object> actualizaciones = new HashMap<>();
                    String uidTarea = arrayAllEstados.get(toColumn).get(toRow).second.getUidTarea();

                    actualizaciones.put("tareas/"+uidProyecto+"/"+uidTarea+"/uidEmpleado",uidEmpleado);
                    actualizaciones.put("tareas/"+uidProyecto+"/"+uidTarea+"/estado",String.valueOf(toColumn));
                    actualizaciones.put("tareas/"+uidProyecto+"/"+uidTarea+"/estado_uidEmpleado",String.valueOf(toColumn).concat("_").concat(uidEmpleado));

                    mDatabase.updateChildren(actualizaciones)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    arrayAllEstados.get(toColumn).get(toRow).second.setUidEmpleado(uidEmpleado);
                                    arrayAllAdaptadores.get(toColumn).notifyDataSetChanged();
                                } else {
                                    Log.d("taskjectsdebug", "error en BD al actualizar la tarea");
                                    Toast.makeText(TareasProyectoActivity.this,getString(R.string.errorAccesoBD),Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }
            @Override
            public void onItemChangedPosition(int oldColumn, int oldRow, int newColumn, int newRow) { }
            @Override
            public void onItemChangedColumn(int oldColumn, int newColumn) { }
            @Override
            public void onFocusedColumnChanged(int oldColumn, int newColumn) { }
            @Override
            public void onColumnDragStarted(int position) { }
            @Override
            public void onColumnDragChangedPosition(int oldPosition, int newPosition) { }
            @Override
            public void onColumnDragEnded(int position) { }
        });
        //Listener para evitar drop en ciertas columnas
        dadTareas.setBoardCallback(new BoardView.BoardCallback() {
            @Override
            public boolean canDragItemAtPosition(int column, int dragPosition) { return true; }

            //Metodo para impedir que una tarea pueda desasignarse (volver a la columna pendientes)
            @Override
            public boolean canDropItemAtPosition(int oldColumn, int oldRow, int newColumn, int newRow) {
                if (newColumn == 0) {
                    Toast.makeText(TareasProyectoActivity.this,getString(R.string.noPuedesDesasignarTarea),Toast.LENGTH_SHORT).show();
                }
                return newColumn != 0;
            }
        });
    }

    //Metodo que carga las 5 columnas en la vista
    private void cargaDeColumnas() {
        //Siempre que entro en esta activity limpio el board de las tareas
        dadTareas.clearBoard();
        //Le asigno el listener para cada tarea
        dadTareas.setCustomDragItem(mGridLayout ? null  : new DragTarea(TareasProyectoActivity.this, R.layout.dad_column_tarea_layout));

        //Recorro el array por cada estado para crear una columna por cada uno de los 5 estados
        String[]estados = getResources().getStringArray(R.array.estados_array);

        //Bucle que se encarga de crear todoo lo necesario por columna (adaptador, header y propiedades) y luego la agrega al board
        for (int i=0; i<estados.length;i++) {
            LinearLayoutManager layoutManager = mGridLayout ? new GridLayoutManager(TareasProyectoActivity.this, 4) : new LinearLayoutManager(TareasProyectoActivity.this);
            arrayAllAdaptadores.add(new AdaptadorTareasDAD(arrayAllEstados.get(i),this,R.layout.dad_column_tarea_layout,R.id.item_layout,true));
            View header = View.inflate(this, R.layout.dad_column_header, null);
            ((TextView) header.findViewById(R.id.tvEstado)).setText(estados[i]);
            ColumnProperties columnProperties = ColumnProperties.Builder.newBuilder(arrayAllAdaptadores.get(i))
                    .setLayoutManager(layoutManager)
                    .setHasFixedItemSize(false)
                    .setHeader(header)
                    .build();

            dadTareas.addColumn(columnProperties);
        }
    }

    //Metodo que recupera de la base de datos las categorias de empleados
    private void recuperarCategorias() {

        db.collection(CATEGORIAS).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            mapCategorias.put(document.getId(), document.toObject(Categoria.class));
                        }
                        cargaDeTareas();
                    } else {
                        Log.d("taskjectsdebug", "error en BD al recuperar categorias");
                        Toast.makeText(TareasProyectoActivity.this, getString(R.string.errorAccesoBD), Toast.LENGTH_LONG).show();
                    }
                });
    }

    //Metodo que crea un listener por cada estado de tarea, estos listener detectan cambios en la base de datos sobre las tareas
    private void cargaDeTareas() {
        Categoria categoria = mapCategorias.get(sharedPreferences.getString("categoria", ""));
        if (categoria.getMarca()) {
            plusEmpleado.setVisible(true);
            findViewById(R.id.fABTareas).setVisibility(View.VISIBLE);
        } else {
            plusEmpleado.setVisible(false);
            findViewById(R.id.fABTareas).setVisibility(View.INVISIBLE);
        }

        //Bucle que se encarga de agregar un listener en la base de datos por cada estado
        for (int i = 0; i < 5; i++) {
            String estado_uidEmpleado;
            if (i == 0)
                estado_uidEmpleado = "0_noasignado";
            else
                estado_uidEmpleado = String.valueOf(i).concat("_").concat(uidEmpleado);

            int posicion = i;
            mDatabase.child("tareas")
                    .child(uidProyecto)
                    .orderByChild("estado_uidEmpleado")
                    .equalTo(estado_uidEmpleado)
                    .addChildEventListener(new ChildEventListener() {
                        //onChildAdded se activa cuando se agregan hijos al estado i
                        @Override
                        public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                            //if (previousChildName != null)
                            if (snapshot.exists()) {
                                Tarea aux = snapshot.getValue(Tarea.class);
                                if (aux != null) {
                                    aux.setUidTarea(snapshot.getKey());
                                    if (!buscarTarea(posicion,aux.getUidTarea())) {
                                        long auxTareasCreadas = tareasCreadas++;
                                        arrayAllEstados.get(posicion).add(new Pair<>(auxTareasCreadas, aux));
                                        arrayAllAdaptadores.get(posicion).notifyDataSetChanged();
                                    }
                                }
                            }
                        }
                        //onChildChanged se activa cuando se detectan cambios en los hijos que tengan estado i y llama al que gestiona estos cambios
                        @Override
                        public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                            if (snapshot.exists()) {
                                Tarea aux = snapshot.getValue(Tarea.class);
                                if (aux != null) {
                                    aux.setUidTarea(snapshot.getKey());
                                    cambiarDatosTarea(posicion,arrayAllAdaptadores.get(posicion),snapshot.getKey(),aux);
                                }
                            }
                        }

                        //onChildRemoved se activa cuando se elimina un hijo con estado i y llama al metodo que gestiona estos cambios
                        @Override
                        public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                Tarea aux = snapshot.getValue(Tarea.class);
                                if (aux != null) {
                                    aux.setUidTarea(snapshot.getKey());
                                    removeTareaAntigua(posicion,arrayAllAdaptadores.get(posicion),snapshot.getKey(),posicion);
                                }
                            }
                        }

                        //Este listener se activa cuando se detectan cambios en el orden
                        @Override
                        public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) { }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) { }
                    });
        }
    }

    //Metodo que devuelve true si encuentra la tarea pasada por parametros
    private boolean buscarTarea(int arrayEstados,String uidTarea){
        for (int i=0;i<arrayAllEstados.get(arrayEstados).size();i++) {
            if (arrayAllEstados.get(arrayEstados).get(i).second.getUidTarea().equals(uidTarea))
                return true;
        }
        return false;
    }

    //Metodo que cambia los datos nuevos de una tarea por la encontrada en el array
    private void cambiarDatosTarea(int arrayEstados,AdaptadorTareasDAD adapter,String keyTarea,Tarea nuevosDatosTarea) {
        for (int i = 0; i<arrayAllEstados.get(arrayEstados).size(); i++) {
            if (arrayAllEstados.get(arrayEstados).get(i).second.getUidTarea().equals(keyTarea)) {
                int posicionTarea = adapter.getPositionForItemId(arrayAllEstados.get(arrayEstados).get(i).first);
                arrayAllEstados.get(arrayEstados).get(posicionTarea).second.setNuevosDatos(nuevosDatosTarea);
                adapter.notifyDataSetChanged();
            }
        }
    }

    //Metodo que elimina de un array especifico una tarea que ha cambiado a otro array en la base de datos
    private void removeTareaAntigua(int arrayEstados,AdaptadorTareasDAD adapter,String keyTarea,int columna){
        for (int i = 0; i<arrayAllEstados.get(arrayEstados).size(); i++) {
            if (arrayAllEstados.get(arrayEstados).get(i).second.getUidTarea().equals(keyTarea)) {
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

    //Metodo que inicia la vista para añadir tareas
    public void aniadirTarea(View view) {
        fabTarea.setEnabled(false);
        Intent pantallaAniadirTarea = new Intent(this,AniadirTareaActivity.class);
        pantallaAniadirTarea.putExtra("uidProyecto", uidProyecto);
        pantallaAniadirTarea.putExtra("uidEmpresa", uidEmpresa);
        startActivity(pantallaAniadirTarea);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        int plusEmpleadoId = -69;
        if(menu.findItem(plusEmpleadoId) == null) {
            plusEmpleado = menu.add(Menu.NONE, plusEmpleadoId, 2, getString(R.string.plusEmpleado) );

            plusEmpleado.setVisible(false);
            plusEmpleado.setIcon(R.drawable.ic_person_add_white_24dp);
            plusEmpleado.setShowAsActionFlags(
                    MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW |
                            MenuItem.SHOW_AS_ACTION_ALWAYS
            );

            // Set a click listener for the new menu item
            plusEmpleado.setOnMenuItemClickListener(menuItem -> {
                Intent intent = new Intent(TareasProyectoActivity.this, AsignarEmpleadosActivity.class);
                intent.putExtra("uidEmpresa", uidEmpresa);
                intent.putExtra("uidProyecto", uidProyecto);
                intent.putExtra("uidJefeProyecto", uidJefeProyecto);
                startActivity(intent);
                return true;
            });
        }
        super.onPrepareOptionsMenu(menu);
        return true;
    }

}