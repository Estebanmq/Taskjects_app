package com.app.taskjects.utils;

public class Validador {

    public static boolean validarCif(String cif) {
        return cif.matches("^[a-zA-Z]{1}[0-9]{7}[a-zA-Z0-9]{1}$");
    }

    public static boolean validarEmail(String email) {
        return email.matches("^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9-]+(?:.[a-zA-Z0-9-]+)*$");
    }

    public static boolean validarPassword(String password) {
        return password.matches("^[A-Za-z0-9$|@#!&*]{6,24}$");
    }

}
