package com.example.canonico.myapplication.detectionchute;

/**
 *
 * @author Paul
 */
public class t_point
{
    public double x;
    public double y; 
    public double z; 
    public double somme_carre;


    public t_point(double _x, double _y, double _z)
    {
        x = _x;
        y = _y;
        z = _z; 

        somme_carre= x*x + y*y + z*z - Parametre.acceleration_gravite_carre;
    }
};
