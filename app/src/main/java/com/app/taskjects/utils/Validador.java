package com.app.taskjects.utils;

import android.util.Patterns;

import java.util.regex.Pattern;

public class Validador {

    public static boolean validarCif(String cif) { return cif.matches("^[a-zA-Z]{1}[0-9]{8}$"); }

    public static boolean validarNif(String nif) {
        return nif.matches("^[0-9]{8}[A-Za-z]{1}$");
    }

    public static boolean validarEmail(String email) {
        Pattern pattern = Patterns.EMAIL_ADDRESS;
        return pattern.matcher(email).matches();
    }

    public static boolean validarPassword(String password) { return password.matches("^[A-Za-z0-9$|@#!&*]{6,24}$"); }

}
