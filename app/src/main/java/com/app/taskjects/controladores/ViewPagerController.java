package com.app.taskjects.controladores;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.app.taskjects.FragmentEmpleadoLogin;
import com.app.taskjects.FragmentEmpresaLogin;


//Esta clase es la encargada de controlar el componente de ViewPager asignando a cada pestaña del TabLayout su correspondiente Fragment
public class ViewPagerController extends FragmentStateAdapter {

    //Atributo que almacena el numero de pestañas del TabLayout
    private final int numTabs;

    //Constructor. El parametro behavior contiene el numero de pestañas del TabLayout
    public ViewPagerController(@NonNull FragmentActivity fm, int numTabs) {
        super(fm);
        this.numTabs = numTabs;
    }

    //Metodo que se encarga de asignar a cada pestaña del TabLayout su respectivo fragment
    @NonNull
    @Override
    public Fragment createFragment(int position) {

        switch (position) {
            //Si la posicion es 0 (pestaña de empleado) retorno el fragment con el login de empleado
            case 0:
                return new FragmentEmpleadoLogin();

            //Si la posicion es 0 (pestaña de empleado) retorno el fragment con el login de empresa
            case 1:
                return new FragmentEmpresaLogin();

            //Por defecto no retorno nada
            default:
                return null;
        }

    }

    //Getter del numero de tabs del TabLayout
    @Override
    public int getItemCount() {
        return this.numTabs;
    }
}
