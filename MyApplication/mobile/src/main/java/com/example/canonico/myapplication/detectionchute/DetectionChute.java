package com.example.canonico.myapplication.detectionchute;

/**
 *
 * @author RL
 */
public class DetectionChute
{
    public DetectionChute(String file) {

        /*
        if(args.length!=1)
        {
            System.out.println("Usage : nom_de_fichier_entree ");
            System.exit(-1);
        }
        */
        
        t_instance une_instance = new t_instance();
        une_instance.charger_un_fichier(file);
        une_instance.analyser_donnees();
    }
}
