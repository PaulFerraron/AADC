package com.example.canonico.myapplication;

/**
 * Created by REN on 30/12/2016.
 */
public class parametre {
    static final double epsilon = 0.00001;

    static final double acceleration_gravite_carre = 9.8 *9.8;

    static final int ordre = 12;//14;//25;//13; //nb de points à utiliser pour chaque calcul
    static final int taille_pas = 1;//5; //pour le calcul de la variance

    static final int nb_variances_pour_regression = 5; //pour calculer la pente
}
