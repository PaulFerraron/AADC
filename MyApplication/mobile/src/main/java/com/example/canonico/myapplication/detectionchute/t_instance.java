package com.example.canonico.myapplication.detectionchute;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

/**
 *
 * @author RL
 */
public class t_instance
{
    public ArrayList<t_point> liste_points ; 
     
    public t_instance(){
        liste_points= new ArrayList<t_point>();
    }
     
    // ici les methodes
    //================================================================
    // charger un fichier.
    //================================================================
    public void charger_un_fichier(String nom_fichier)
    {
         liste_points.clear(); //vider la liste des points
         
         String ligne = "";
         String [] mot=new String[2];
         double x,y,z;
        
        
        if(nom_fichier.length()==0)
            System.out.println("Erreur : nom_de_fichier_entree ");
        
        System.out.println("DEBUT : lire le fichier - "+nom_fichier);
        try {
            File source;
            source = new File(nom_fichier);
            FileReader Input = new FileReader(source);
            BufferedReader Lecteur;
            Lecteur = new BufferedReader(Input);

            do{
                ligne = Lecteur.readLine();

                if(ligne== null)break;

                mot=ligne.split(";");
                
                if(mot.length!=3) //probleme ici
                {
                    System.out.println("Erreur : format du fichier...");
                    System.exit(-1);
                }
                
                //point de depart.
                x= Double.parseDouble(mot[0]);
                y= Double.parseDouble(mot[1]);
                z= Double.parseDouble(mot[2]);
                
                t_point p1= new t_point(x,y,z);  
                liste_points.add(p1);
                System.out.println("   => : "+ligne + "   somme carre : " + p1.somme_carre); 
            }while(ligne.length()>0);
          
            Lecteur.close();
  
        } catch (Exception E) {
            System.out.println(E.getMessage());
        }
        
        System.out.println("FIN : lire  fichier - nb. points : " + liste_points.size());
        System.out.println("===================================== ");
    }
    
    //=================================================================================
    //analyser le contenu du tableau
    // => on utilise les variances des sommes carrés des points
    //=================================================================================
    public void analyser_donnees()
    {
        ArrayList<Double> liste_variances_divser_par_mille = new ArrayList<Double>();
        ArrayList<Double> liste_pentes = new ArrayList<Double>();
        
        int _pas = Parametre.taille_pas;
        double covXY =0;
        double _tmp=0; 
        double _moy_tmp =0;
          
      
        double a_plus_1 =0;
        double a=0;
          
        for(int i=0;i<liste_points.size();i+=_pas)
        {
            //on divise ici la variance par 1000, sinon les valeurs sont trops grandes
            liste_variances_divser_par_mille.add(variance(i,i+Parametre.ordre)/1000.0);
              
            if(liste_variances_divser_par_mille.size()>=2)//Parametre.nb_variances_pour_regression) 
            {  
                a=liste_variances_divser_par_mille.get(liste_variances_divser_par_mille.size()-1) - liste_variances_divser_par_mille.get(liste_variances_divser_par_mille.size()-2);

                liste_pentes.add(a);
            }
        }
          
        //---------
        // detection de chute
        for(int i=0;i<liste_pentes.size()-10;i++)
        { 
            a=liste_pentes.get(i);
            a_plus_1=liste_pentes.get(i+1);
              
            if(Math.abs(a)> 20 && Math.abs(a_plus_1)< 5)
            {
                if( a > 20 && (i+9)<=liste_pentes.size()) 
                //if(Math.abs(a)>12 && (i+9)<=liste_pentes.size())
                {
                    double variance = 0;

                    _moy_tmp=0;
                    for(int j=1;j<=8;j++)
                    {
                        _moy_tmp+=liste_variances_divser_par_mille.get(liste_variances_divser_par_mille.size()-j);
                    }
                    
                    _moy_tmp/=8;

                    covXY = 0;

                    for(int j=1;j<=8;j++)
                    {
                        _tmp=liste_variances_divser_par_mille.get(liste_variances_divser_par_mille.size()-j);
                        
                        covXY+=((9-j)-4.5)*(_tmp - _moy_tmp); 

                        variance+=(_tmp - _moy_tmp)*(_tmp - _moy_tmp);
                    }

                    a=covXY/8/5.25;
                    variance/=8;      

                    if(Math.abs(a) <2 &&  variance<10) 
                    {
                        System.out.println("  => CHUTE ?  todo : vérifier la direction d'accélération"); 
                        System.out.println("       => a = " + a); 
                    }
                }
            } 
        }
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