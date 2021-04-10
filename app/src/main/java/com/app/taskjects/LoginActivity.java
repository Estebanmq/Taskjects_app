package com.app.taskjects;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;

import com.app.taskjects.controladores.ViewPagerController;
import com.google.android.material.tabs.TabItem;
import com.google.android.material.tabs.TabLayout;

public class LoginActivity extends AppCompatActivity {

    //Declaracion de componentes
    TabLayout tabLayoutEmpresaEmpleado;
    TabItem tabItemEmpresa,tabItemEmpleado;
    ViewPager viewPager;

    //Controlador / Adaptador para asignar a cada pestaña del TabLayout su propio fragment
    ViewPagerController adaptadorViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_layout);

        //Inicializacion de componentes
        tabLayoutEmpresaEmpleado = findViewById(R.id.tabLayoutEmpresaEmpleado);
        tabItemEmpleado = findViewById(R.id.tabItemEmpleado);
        tabItemEmpresa = findViewById(R.id.tabItemEmpresa);
        viewPager = findViewById(R.id.viewPager);


        //Llamada al metodo que se encarga de rellenar cada pestaña del TabLayout con su fragment asignado
        rellenarPestañasTabLayout();


    }


    //Este metodo se encarga de asignar a cada pestaña del TabLayout su fragment especifico
    protected void rellenarPestañasTabLayout() {

        //Me creo el objeto ViewPagerController con el supportFragmentManager y el numero de pestañas que tiene el TabLayout
        adaptadorViewPager = new ViewPagerController(getSupportFragmentManager(), tabLayoutEmpresaEmpleado.getTabCount());

        //Le asigno al ViewPager el adaptador anteriormente creado
        viewPager.setAdapter(adaptadorViewPager);

        //Agrego un Listener para las pestañas pasando como parametro otro Listener de pestaña
        tabLayoutEmpresaEmpleado.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {

            //Con este metodo compruebo cada posicion de cada pestaña del TabLayout y por cada pestaña llamo al metodo notifyDataSetChanged
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
                if (tab.getPosition() == 0) {
                    adaptadorViewPager.notifyDataSetChanged();
                }

                if (tab.getPosition() == 1) {
                    adaptadorViewPager.notifyDataSetChanged();
                }

            }

            //Metodos sin utilizar
            @Override
            public void onTabUnselected(TabLayout.Tab tab) { }
            @Override
            public void onTabReselected(TabLayout.Tab tab) { }

        });
        //Le asigno otro Listener para el cambio entre pestañas al TabLayout de Empresa Empleados
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayoutEmpresaEmpleado));
    }

}