package com.example.canonico.myapplication;

import java.util.ArrayList;

/**
 * Created by REN on 30/12/2016.
 */
public class t_instance {
    public ArrayList<t_point> liste_points ;

    public t_instance(){
        liste_points= new ArrayList<t_point>();
    }

    public void charger_les_donnees(ArrayList<t_point> _list_points)
    {
        liste_points= new ArrayList<t_point>(_list_points);
    }

    //=================================================================================
    //analyser le contenu du tableau
    // => on utilise les variances des sommes carrés des points
    //=================================================================================
    public boolean analyser_donnees()
    {
        //System.out.println("=>" + variance(1,13));
        ArrayList<Double> liste_variances_divser_par_mille = new ArrayList<Double>();
        ArrayList<Double> liste_pentes = new ArrayList<Double>();

        t_point _t_point;
        int _pas = parametre.taille_pas;
        double covXY =0;
        double _tmp=0;
        double _moy_tmp =0;


        double a_plus_1 =0;
        double a=0;

        for(int i=0;i<liste_points.size();i+=_pas)
        {
            //on divise ici la variance par 1000, sinon les valeurs sont trops grandes
            liste_variances_divser_par_mille.add(variance(i,i+parametre.ordre)/1000.0);
            //System.out.println("       =>  " + liste_variances_divser_par_mille.get(i));

            //
            if(liste_variances_divser_par_mille.size()>=2)//parametre.nb_variances_pour_regression)
            {  //a = Cov(X,Y) / var(x)

                a=liste_variances_divser_par_mille.get(liste_variances_divser_par_mille.size()-1) - liste_variances_divser_par_mille.get(liste_variances_divser_par_mille.size()-2);

                liste_pentes.add(a);

                //System.out.println("  => "+liste_variances_divser_par_mille.size() +"  a : " + a);
            }
        }

        //---------
        // detection de chute
        for(int i=0;i<liste_pentes.size()-10;i++)
        {
            //a=liste_variances_divser_par_mille.get(i);
            //a_plus_1=liste_variances_divser_par_mille.get(i+1);
            a=liste_pentes.get(i);
            a_plus_1=liste_pentes.get(i+1);


            if(Math.abs(a)> 30 && Math.abs(a_plus_1)< 5)
            //if(Math.abs(a)> 12 && Math.abs(a_plus_1)< 5)
            {
                //System.out.println("      COUCOU "  + a + "  " + a_plus_1);
                //if(Math.abs(a)>20 && (i+9)<=liste_pentes.size())
                if( a > 30 && (i+9)<=liste_pentes.size())
                //if(Math.abs(a)>12 && (i+9)<=liste_pentes.size())
                {
                    //System.out.println("      COUCOU "  + a);
                    double variance =0;
                    //prendre les 8 points suivants
                    //a = Cov(X,Y) / var(x)

                    _moy_tmp=0;
                    for(int j=1;j<=8;j++)
                    {
                        //_moy_tmp+=C.get(liste_variances_divser_par_mille.size()-j);
                        _moy_tmp+=liste_variances_divser_par_mille.get(liste_variances_divser_par_mille.size()-j);

                    }
                    _moy_tmp/=8;

                    covXY = 0;

                    for(int j=1;j<=8;j++)
                    {
                        //_tmp=liste_variances_divser_par_mille.get(liste_variances_divser_par_mille.size()-j);
                        _tmp=liste_variances_divser_par_mille.get(liste_variances_divser_par_mille.size()-j);

                        //System.out.println("      =>  " + _tmp);
                        covXY+=((9-j)-4.5)*(_tmp - _moy_tmp);

                        variance+=(_tmp - _moy_tmp)*(_tmp - _moy_tmp);
                    }

                    a=covXY/8/5.25;
                    variance/=8;

                    if(  Math.abs(a) <2 &&  variance<10)
                    {

                        System.out.println("  => CHUTE ?  todo : vérifier la direction d'accélération");
                        System.out.println("       => a = " + a);
                        return true;
                    }
                }
            }
        }

        return false;
    }


    //--------------------------------------------------------------------------
    // si chute detectee : alors on va analyser les donnees suivantes :
    //    renvoyer true : si aucun mouvement detecte
    //    renvoyer false : si mouvement detecte
    //--------------------------------------------------------------------------
    public boolean post_traitement()
    {
        //todo: detection de mouvement

        return true ;
    }
    //--------------------------------------------------------------------------
    // calculer la variance
    //--------------------------------------------------------------------------
    public double variance(int pos_debut, int pos_fin)
    {
        double moyenne = moyenne(pos_debut,pos_fin);

        //System.out.println("  => moyenne : " + moyenne);

        double somme_carre_x_moins_moy =0;
        double delta=0;
        int nb=0;
        t_point _t_point;
        for(int i=pos_debut;i<liste_points.size() && i<= pos_fin;i++)
        {
            _t_point=liste_points.get(i);
            delta= _t_point.somme_carre-moyenne;
            somme_carre_x_moins_moy+=delta*delta;
            nb++;
        }
        if(nb==0)
        {
            System.out.println("Erreur : variance() - nb = 0");
            System.exit(-2);
        }
        return somme_carre_x_moins_moy/nb;
    }

    //--------------------------------------------------------------------------
    // calculer la moyenne
    //--------------------------------------------------------------------------
    public double moyenne(int pos_debut,int pos_fin)
    {
        double somme =0;
        int nb=0;

        t_point _t_point;

        for(int i=pos_debut;i<liste_points.size() && i<= pos_fin;i++)
        {
            _t_point=liste_points.get(i);
            somme+=_t_point.somme_carre;
            nb++;
        }
        if(nb==0)
        {
            System.out.println("Erreur : moyenne() - nb = 0");
            System.exit(-3);
        }

        return somme/nb;
    }

}

// ----------------------------------------------------------------------------- //
// --	 classe noeud                                                           -- //
// ----------------------------------------------------------------------------- //
class t_point {
    public long temps;
    public double x;
    public double y;
    public double z;
    public double somme_carre;


    public t_point(t_point _p)
    {
        temps=_p.temps;
        x = _p.x;
        y = _p.y;
        z = _p.z;
        somme_carre=_p.somme_carre;
    }
    public t_point(long _temps,double _x, double _y, double _z) {
        temps=_temps;
        x = _x;
        y = _y;
        z = _z;

        somme_carre = x * x + y * y + z * z - parametre.acceleration_gravite_carre;
    }
}
