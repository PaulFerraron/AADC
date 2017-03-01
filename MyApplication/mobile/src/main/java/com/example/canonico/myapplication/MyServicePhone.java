package com.example.canonico.myapplication;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Environment;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.Buffer;
import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Queue;

/**
 * Created by Hans on 19/01/2016.
 */
public class MyServicePhone extends Service implements SensorEventListener {

    private static final String TAG = "sensor";

    private final String PATH_ALARM = "/alarm";
    private final String PATH_STOP = "/stop";

    private LocationManager locationMgr = null;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mSensor;

    private Sensor gravite_Sensor;
    private Sensor accelerometer_lineaire_Sensor;


    private float[] sensorValue;
    private String sensorXValueString = "0";
    private String sensorYValueString = "0";
    private String sensorZValueString = "0";
    private String sensorRezValueString = "0";
    private float sensorXValue=0, sensorYValue=0, sensorZValue=0;


    private GoogleApiClient apiClient;
    private NodeApi.NodeListener nodeListener;
    private MessageApi.MessageListener messageListener;
    private String remoteNodeId;
    private Handler handler;

    //private ArrayDeque<Long> bufferTimeStamp = new ArrayDeque<Long>();
    //private ArrayDeque<Float> bufferX = new ArrayDeque<Float>();
    //private ArrayDeque<Float> bufferY= new ArrayDeque<Float>();
    //private ArrayDeque<Float> bufferZ = new ArrayDeque<Float>();


    private ArrayList<Long> TimeStamp = new ArrayList<Long>();
    private ArrayList<Double> donnees_X = new ArrayList<Double>();
    private ArrayList<Double> donnees_Y= new ArrayList<Double>();
    private ArrayList<Double> donnees_Z = new ArrayList<Double>();

    private ArrayList<Long> TimeStamp_bf = new ArrayList<Long>();
    private ArrayList<Double> donnees_X_bf = new ArrayList<Double>();
    private ArrayList<Double> donnees_Y_bf= new ArrayList<Double>();
    private ArrayList<Double> donnees_Z_bf = new ArrayList<Double>();

    //-------------------------------------------------------------------------------------------
    //gravite
    private ArrayList<Long> TimeStamp_G = new ArrayList<Long>();
    private ArrayList<Double> donnees_G_X = new ArrayList<Double>();
    private ArrayList<Double> donnees_G_Y= new ArrayList<Double>();
    private ArrayList<Double> donnees_G_Z = new ArrayList<Double>();

    private ArrayList<Long> TimeStamp_G_bf = new ArrayList<Long>();
    private ArrayList<Double> donnees_G_X_bf = new ArrayList<Double>();
    private ArrayList<Double> donnees_G_Y_bf= new ArrayList<Double>();
    private ArrayList<Double> donnees_G_Z_bf = new ArrayList<Double>();

    //accelerometre lineaire
    private ArrayList<Long> TimeStamp_L = new ArrayList<Long>();
    private ArrayList<Double> donnees_L_X = new ArrayList<Double>();
    private ArrayList<Double> donnees_L_Y= new ArrayList<Double>();
    private ArrayList<Double> donnees_L_Z = new ArrayList<Double>();

    private ArrayList<Long> TimeStamp_L_bf = new ArrayList<Long>();
    private ArrayList<Double> donnees_L_X_bf = new ArrayList<Double>();
    private ArrayList<Double> donnees_L_Y_bf= new ArrayList<Double>();
    private ArrayList<Double> donnees_L_Z_bf = new ArrayList<Double>();

    //-------------------------------------------------------------------------------------------

    private float somX=0;
    private float somY=0;
    private float somZ=0;

    private float som2X=0;
    private float som2Y=0;
    private float som2Z=0;

    private int plateauX=0;
    private int plateauY=0;
    private int plateauZ=0;

    //--- sauvegarde automatique
    private long timeStamp_sauvegard=0;
    private String nom_fichier="chute_";
    private long pos_debut =0; //memoriser la position début d'écriture pour la prochaine sauvegarde

    //---- detection de chute
    private long timeStamp_detection=0;
    t_instance une_instance = new t_instance();
    private ArrayDeque <t_point> buffer_detection = new ArrayDeque<t_point>();

     //---
    float[] accelerometerValues = new float[3];
    float[] magneticFieldValues = new float[3];

    float[] graviteValues = new float[3];
    float[] accelerometerLineaireValues = new float[3];


    boolean chute_detectee =false;
    ArrayList<t_point> bf_traitement = new ArrayList<t_point>();;


    private long tmp_data_sauvegarde_nb = 0;
    private long tmp_data_detection_nb = 0;


///
    float[] values = new float[3];
    float deg_inclination =0;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onCreate() {
        super.onCreate();

        //
        handler  = new Handler();
//        final Vibrator vibrator = (Vibrator) getSystemService(getApplicationContext().VIBRATOR_SERVICE);
//        handler.post(new Runnable() {
//            @Override
//            public void run() {
//
//                if (vibrator.hasVibrator()) {
//                    long[] pattern = {0, 500, 600};
//                    vibrator.vibrate(pattern, 1);
//                }
//            }
//        });
        final Intent dialogIntent = new Intent(this, AlarmActivity.class);
        // Create MessageListener that receives messages sent from a mobile
        messageListener = new MessageApi.MessageListener() {
            @Override
            public void onMessageReceived(MessageEvent messageEvent) {
                if (messageEvent.getPath().equals(PATH_ALARM)) {

                    dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                    startActivity(dialogIntent);
                } else if (messageEvent.getPath().equals(PATH_STOP)) {

                }
            }
        };
        nodeListener = new NodeApi.NodeListener() {
            @Override
            public void onPeerConnected(Node node) {
                remoteNodeId = node.getId();
                handler.post(new Runnable() {
                    @Override
                    public void run() {

                        Toast.makeText(getApplication(), getString(R.string.peer_connected), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onPeerDisconnected(Node node) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {

                        Toast.makeText(getApplication(), getString(R.string.peer_disconnected), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        };
        // Create GoogleApiClient
        apiClient = new GoogleApiClient.Builder(getApplicationContext()).addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
            @Override
            public void onConnected(Bundle bundle) {
                // Register Node and Message listeners
                Wearable.NodeApi.addListener(apiClient, nodeListener);
                Wearable.MessageApi.addListener(apiClient, messageListener);
                // If there is a connected node, get it's id that is used when sending messages
                Wearable.NodeApi.getConnectedNodes(apiClient).setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
                    @Override
                    public void onResult(NodeApi.GetConnectedNodesResult getConnectedNodesResult) {
                        if (getConnectedNodesResult.getStatus().isSuccess() && getConnectedNodesResult.getNodes().size() > 0) {
                            remoteNodeId = getConnectedNodesResult.getNodes().get(0).getId();
                            //buttonAlarm.setEnabled(true);


                        }
                    }
                });
            }

            @Override
            public void onConnectionSuspended(int i) {
                //buttonAlarm.setEnabled(false);

            }
        }).addApi(Wearable.API).build();


        apiClient.connect();

    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        gravite_Sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        accelerometer_lineaire_Sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);


        //mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_UI);

        //mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mAccelerometer, 50000); //200 millisecondes

       // mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
       // mSensorManager.registerListener(this, mSensor, 2000000);
       // mSensorManager.registerListener(this, mSensor, 200000);



        mSensorManager.registerListener(this, gravite_Sensor, 50000);
        mSensorManager.registerListener(this, accelerometer_lineaire_Sensor, 50000);


        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType() == Sensor.TYPE_GRAVITY) {
            graviteValues = event.values;
            //Log.i(TAG, "GRAVITE : "+graviteValues[0] + "; "+graviteValues[1] + "; "+ graviteValues[2]);
            long temps = System.currentTimeMillis();
            double x= Math.round(graviteValues[0]*10000.0)/10000.0;
            double y= Math.round(graviteValues[1]*10000.0)/10000.0;
            double z= Math.round(graviteValues[2]*10000.0)/10000.0;

            // pour la sauvegarde
            TimeStamp_G_bf.add(temps);
            donnees_G_X_bf.add(x);
            donnees_G_Y_bf.add(y);
            donnees_G_Z_bf.add(z);
        }

        if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            accelerometerLineaireValues = event.values;
            //Log.i(TAG, "AccelerometerLineaire : "+accelerometerLineaireValues[0] + "; "+accelerometerLineaireValues[1] + "; "+ accelerometerLineaireValues[2]);
            long temps = System.currentTimeMillis();
            double x= Math.round(accelerometerLineaireValues[0]*10000.0)/10000.0;
            double y= Math.round(accelerometerLineaireValues[1]*10000.0)/10000.0;
            double z= Math.round(accelerometerLineaireValues[2]*10000.0)/10000.0;

            // pour la sauvegarde
            TimeStamp_L_bf.add(temps);
            donnees_L_X_bf.add(x);
            donnees_L_Y_bf.add(y);
            donnees_L_Z_bf.add(z);
        }


        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            magneticFieldValues = event.values;
            //calculateOrientation();
        }

        //if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
        if (event.sensor.equals(mAccelerometer)) {
            sensorValue = event.values;
            accelerometerValues = event.values;

            long temps = System.currentTimeMillis();
            double x= Math.round(sensorValue[0]*10000.0)/10000.0;
            double y= Math.round(sensorValue[1]*10000.0)/10000.0;
            double z= Math.round(sensorValue[2]*10000.0)/10000.0;

            buffer_detection.add(new t_point(temps,x,y,z));

            if(buffer_detection.size() > 3000) //on ne garde que 3000 data
            {
                buffer_detection.pollFirst();
            }

            // pour la sauvegarde
            TimeStamp_bf.add(temps);
            donnees_X_bf.add(x);
            donnees_Y_bf.add(y);
            donnees_Z_bf.add(z);

           //1. sauvegarder les données dans le fichier texte
           if( System.currentTimeMillis() -  timeStamp_sauvegard > 20000)  //20 secondes
           {
               Log.i(TAG, "Sauvegarde");

               if(timeStamp_sauvegard==0) {
                   SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                   String date = s.format(new Date());
                   nom_fichier = "data_chute_" + date + ".txt";
               }
               else if(donnees_Z.size()==0) {  //donnees_Z.size()==0 : eviter la concurrence d'accèes
                   //1. sauvegarder les donnees : Accelerometer
                   TimeStamp = new ArrayList<Long>(TimeStamp_bf);
                   donnees_X = new ArrayList<Double>(donnees_X_bf);
                   donnees_Y = new ArrayList<Double>(donnees_Y_bf);
                   donnees_Z = new ArrayList<Double>(donnees_Z_bf);

                   TimeStamp_bf.clear();
                   TimeStamp_bf = new ArrayList<Long>();
                   donnees_X_bf.clear();
                   donnees_X_bf = new ArrayList<Double>();
                   donnees_Y_bf.clear();
                   donnees_Y_bf = new ArrayList<Double>();
                   donnees_Z_bf.clear();
                   donnees_Z_bf = new ArrayList<Double>();

                   //2. sauvegarder les donnees : Graviter
                   TimeStamp_G = new ArrayList<Long>(TimeStamp_G_bf);
                   donnees_G_X = new ArrayList<Double>(donnees_G_X_bf);
                   donnees_G_Y = new ArrayList<Double>(donnees_G_Y_bf);
                   donnees_G_Z = new ArrayList<Double>(donnees_G_Z_bf);

                   TimeStamp_G_bf.clear();
                   TimeStamp_G_bf = new ArrayList<Long>();
                   donnees_G_X_bf.clear();
                   donnees_G_X_bf = new ArrayList<Double>();
                   donnees_G_Y_bf.clear();
                   donnees_G_Y_bf = new ArrayList<Double>();
                   donnees_G_Z_bf.clear();
                   donnees_G_Z_bf = new ArrayList<Double>();

                   //3. sauvegarder les donnees : Accelerometer_lineaire
                   TimeStamp_L = new ArrayList<Long>(TimeStamp_L_bf);
                   donnees_L_X = new ArrayList<Double>(donnees_L_X_bf);
                   donnees_L_Y = new ArrayList<Double>(donnees_L_Y_bf);
                   donnees_L_Z = new ArrayList<Double>(donnees_L_Z_bf);

                   TimeStamp_L_bf.clear();
                   TimeStamp_L_bf = new ArrayList<Long>();
                   donnees_L_X_bf.clear();
                   donnees_L_X_bf = new ArrayList<Double>();
                   donnees_L_Y_bf.clear();
                   donnees_L_Y_bf = new ArrayList<Double>();
                   donnees_L_Z_bf.clear();
                   donnees_L_Z_bf = new ArrayList<Double>();
                   //sauvegarde donnees

                   //---------------------------------------------------------------
                   // créer un thread : sauvegarder les donnees
                   new Thread(new Runnable() {
                       public void run() {
                           //------------------------------------------------------------------
                           Log.i(TAG, "Sauvegarder : DEBUT ..." + TimeStamp.size());


                           //gravite
                           sauvegarder_donnees(3,".gravite.csv"); //accelerometer_data
                           TimeStamp_G.clear();
                           donnees_G_X.clear();
                           donnees_G_Y.clear();
                           donnees_G_Z.clear();

                           TimeStamp_G = new ArrayList<Long>();
                           donnees_G_X = new ArrayList<Double>();
                           donnees_G_Y= new ArrayList<Double>();
                           donnees_G_Z = new ArrayList<Double>();

                           //accelerometre lineaire
                           sauvegarder_donnees(2,".lineaire.csv"); //accelerometer_data
                           TimeStamp_L.clear();
                           donnees_L_X.clear();
                           donnees_L_Y.clear();
                           donnees_L_Z.clear();

                           TimeStamp_L = new ArrayList<Long>();
                           donnees_L_X = new ArrayList<Double>();
                           donnees_L_Y= new ArrayList<Double>();
                           donnees_L_Z = new ArrayList<Double>();

                           //accelerometre
                           sauvegarder_donnees(1,""); //accelerometer_data
                           TimeStamp.clear();
                           donnees_X.clear();
                           donnees_Y.clear();
                           donnees_Z.clear();

                           TimeStamp = new ArrayList<Long>();
                           donnees_X = new ArrayList<Double>();
                           donnees_Y= new ArrayList<Double>();
                           donnees_Z = new ArrayList<Double>();

                           Log.i(TAG, "Sauvegarder : FIN ...." + TimeStamp.size());
                       }
                   }).start();
                   //---------------------------------------------------------------

               }
               else
               {
                   Log.i(TAG, "Sauvegarde : Reporter ...." );
               }

               timeStamp_sauvegard = System.currentTimeMillis();
               //
           }


            //----------------------------------------------------------------
            //2. detection de chute
            if( System.currentTimeMillis() -  timeStamp_detection > 5000) //5 seconde
            {
                if(timeStamp_detection>0 && bf_traitement.size()==0) {  //bf_traitement.size()==0 : pour éviter la concurrence d'acces
                    // preparer les donnees


                    int nb_post_traimtement =0; //=30
                    int nb_detection = 90;  //on regarde les donnees des 15 dernieres secondes
                    int pos_debut =0;
                    int pos_fin = buffer_detection.size();
                    pos_debut= Math.max(0,pos_fin-(nb_detection+nb_post_traimtement));

                    int nb_donnees = pos_fin - pos_debut;

                    if (nb_donnees > 60 && pos_fin > nb_post_traimtement) //10 secondes
                    {

                        //Toast.makeText(getApplicationContext(), "Detection : nb = " +nb_donnees,Toast.LENGTH_SHORT).show();

                        bf_traitement = new ArrayList<t_point>();
                        //ici on lance le processus de detection
                        // on n'utilise pas les donnees des 5 dernires secondes
                        for (int i = pos_debut; i < pos_fin-nb_post_traimtement; i++) {
                            bf_traitement.add((t_point) (buffer_detection.toArray())[i]);
                        }

                        //---------------------------------------------------------------
                        // créer un thread ： detection de chute
                        new Thread(new Runnable() {
                            public void run() {

                                tmp_data_detection_nb=bf_traitement.size();

                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getApplication(), "Detection : nb = " + tmp_data_detection_nb, Toast.LENGTH_SHORT).show();
                                    }
                                });
                                //------------------------------------------------------------------
                                Log.i(TAG, "Detection : DEBUT ..." + bf_traitement.size());

                                une_instance = new t_instance();
                                une_instance.charger_les_donnees(bf_traitement);
                                boolean ret = une_instance.analyser_donnees();
                                chute_detectee = ret;

                                bf_traitement.clear();
                                bf_traitement = new ArrayList<t_point>();

                                Log.i(TAG, "Detection : FIN ...." + bf_traitement.size() + " - Chute ? = "+ret);
                            }
                        }).start();
                        //---------------------------------------------------------------
                        /*
                        une_instance = new t_instance();
                        une_instance.charger_les_donnees(bf_traitement);
                        boolean ret = une_instance.analyser_donnees();

                        bf_traitement.clear();
                        bf_traitement = new ArrayList<t_point>();

                        if (ret) //chute...
                        {
                            //chute : alors poste traitement :

                            //on vider les données avant de déclencher l'alarme
                            buffer_detection.clear();

                            //ici : on active l'alarme
                            Intent dialogIntent = new Intent(this, AlarmActivity.class);
                            dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                            startActivity(dialogIntent);
                        }*/
                    }

                }
                else if(timeStamp_detection > 0)
                {
                    Log.i(TAG, "Detection : reporter ....");
                }

                //
                timeStamp_detection=System.currentTimeMillis();
            }


            //----------------------------

            if (chute_detectee) //chute...
            {   //on vider les données avant de déclencher l'alarme
                chute_detectee=false;
                buffer_detection.clear();

                //ici : on active l'alarme
                Intent dialogIntent = new Intent(this, AlarmActivity.class);
                dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                startActivity(dialogIntent);
            }

            sensorXValueString = String.valueOf(plateauX);
            sensorYValueString = String.valueOf(plateauY);
            sensorZValueString = String.valueOf(plateauZ);
            if (plateauX !=0 || plateauY!=0 || plateauZ!=0)
            System.out.println(sensorXValueString+"  " +sensorYValueString + "  "+sensorZValueString);
        }


    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }





    //-------------------------------------------------------------------------
    //on crée ici un fichier texte et stcke les données dans ce fichier
    // type = 1 : accelerometer
    //      = 2 : accelerometer_lineaire
    //      = 3 : gravite
    //-------------------------------------------------------------------------
   void sauvegarder_donnees(Integer type, String post_nom) {
        BufferedWriter writer = null;


        File Fichier1 = new File(Environment.getExternalStorageDirectory() +  File.separator + "App_chute",nom_fichier + post_nom); //on déclare notre futur fichier

        //1.lancement de l'applciation : on supprime le fichier de données
        if (timeStamp_sauvegard==0)
        {
            if(Fichier1.exists()) {
                Fichier1.delete();
                //Toast.makeText(getApplicationContext(), "Supprimer le fichier de données..", Toast.LENGTH_SHORT).show();

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplication(),"Supprimer le fichier de données..", Toast.LENGTH_SHORT).show();
                    }
                });
           }
            return;
        }

        //2. creer le fichier et continue...
       String saute_ligne = System.getProperty("line.separator");

       File monRepertoire = new File(Environment.getExternalStorageDirectory() + File.separator + "App_chute"); //pour créer le repertoire dans lequel on va mettre notre fichier

        Boolean success=true;

        if (!monRepertoire.exists())
        {
            success = monRepertoire.mkdir(); //On crée le répertoire (s'il n'existe pas!!)
        }

        if (success){
            //Toast.makeText(getApplicationContext(), "Sauvegarder des données..", Toast.LENGTH_SHORT).show();
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplication(),"Sauvegarder des données..", Toast.LENGTH_SHORT).show();
                }
            });

            try
            {
                FileOutputStream output = new FileOutputStream(Fichier1, true); //le true est pour écrire en fin de fichier, et non l'écraser

               //3. ecriture :
                SimpleDateFormat s = new SimpleDateFormat("ddMMyyhhmmss");

                long nombre_elements = TimeStamp.size();
                if(type==2) //accelerometer lineaire
                    nombre_elements = TimeStamp_L.size();
                else if(type==3) //gravite
                    nombre_elements = TimeStamp_G.size();

                String data="";

                for(int i=0;i<nombre_elements;i++)
                {

                    //data = data +s.format(TimeStamp.get(i)) + " ; " + donnees_X.get(i) +" ; "+  donnees_Y.get(i) +" ; "+ donnees_Z.get(i) +"\n" ;
                    if(type==1)//accelerometer
                        data = data +TimeStamp.get(i) + " ; " + donnees_X.get(i) +" ; "+  donnees_Y.get(i) +" ; "+ donnees_Z.get(i) +"\n" ;
                    else if (type==2)//accelerometer lineaire
                        data = data +TimeStamp_L.get(i) + " ; " + donnees_L_X.get(i) +" ; "+  donnees_L_Y.get(i) +" ; "+ donnees_L_Z.get(i) +"\n" ;
                    else if (type==3)//gravite
                        data = data +TimeStamp_G.get(i) + " ; " + donnees_G_X.get(i) +" ; "+  donnees_G_Y.get(i) +" ; "+ donnees_G_Z.get(i) +"\n" ;

                    if ((i+1)%100 == 0)
                    {
                        output.write(data.getBytes());
                        data= "";
                    }
                }

                output.write(data.getBytes());

                String msg_fin_ecriture = "Sauvegarde : OK ["+nombre_elements+"]";
                output.write(msg_fin_ecriture.getBytes());


                output.close();
                tmp_data_sauvegarde_nb = nombre_elements;

                Log.i(TAG, "Sauvegarde : OK ["+nombre_elements+"]");

                //Toast.makeText(getApplicationContext(), "Sauvegarde : OK ["+nombre_elements+"]",Toast.LENGTH_SHORT).show();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplication(),"Sauvegarde : OK ["+tmp_data_sauvegarde_nb+"]", Toast.LENGTH_SHORT).show();
                    }
                });

            }
            catch (Exception e) {

                    //Toast.makeText(getApplicationContext(), "Sauvegarde : Erreur..",Toast.LENGTH_SHORT).show();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplication(),"Sauvegarde : Erreur..", Toast.LENGTH_SHORT).show();
                    }
                });
                }
        }
        else {

            //Toast.makeText(getApplicationContext(), "Sauvegarde : Erreur..",Toast.LENGTH_SHORT).show();
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplication(),"Sauvegarde : Erreur..", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    //---------------------------------------------------------------------------------
    private  void calculateOrientation() {
        //float[] values = new float[3];
        values = new float[3];
        float[] R = new float[9];
        float[] inclineMatrix = new float[9];

        //recuperer : rotation matrix (R) et inclination matrix (inclineMatrix)
        SensorManager.getRotationMatrix(R, inclineMatrix, accelerometerValues, magneticFieldValues);
        SensorManager.getOrientation(R, values);

        double mInclination = SensorManager.getInclination(inclineMatrix);

        deg_inclination = (float) Math.toDegrees(mInclination);

        //values[0]: Azimuth, angle of rotation about the -z axis.
        // This value represents the angle between the device's y axis and the magnetic north pole.
        // When facing north, this angle is 0, when facing south, this angle is π. Likewise,
        // when facing east, this angle is π/2, and when facing west, this angle is -π/2.
        // The range of values is -π to π.

        //values[1]: Pitch, angle of rotation about the x axis.
        // This value represents the angle between a plane parallel to the device's screen and a plane parallel to the ground.
        // Assuming that the bottom edge of the device faces the user and that the screen is face-up,
        // tilting the top edge of the device toward the ground creates a positive pitch angle.
        // The range of values is -π to π.

        //values[2]: Roll, angle of rotation about the y axis.
        // This value represents the angle between a plane perpendicular to the device's screen and a plane perpendicular
        // to the ground. Assuming that the bottom edge of the device faces the user and that the screen is face-up,
        // tilting the left edge of the device toward the ground creates a positive roll angle.
        // The range of values is -π/2 to π/2.



        values[1] = (float) Math.toDegrees(values[1]);
        values[2] = (float) Math.toDegrees(values[2]);


       //---------------------------------------------------
       // boussole
        values[0] = (float) Math.toDegrees(values[0]);
       // Log.i(TAG, "boussole z: "+ values[0]+"  x=" + values[1] + "  y=" +values[2]);
        //System.out.println("boussole z: "+ values[0]+"  x=" + values[1] + "  y=" +values[2]);



        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplication(), "Inclination : "+ deg_inclination , Toast.LENGTH_SHORT).show();
            }
        });

        //values[1] = (float) Math.toDegrees(values[1]);
        //values[2] = (float) Math.toDegrees(values[2]);

        if(values[0] >= -5 && values[0] < 5){
            Log.i(TAG, "Nord");
        }
        else if(values[0] >= 5 && values[0] < 85){
            Log.i(TAG, "Nord-East");
        }
        else if(values[0] >= 85 && values[0] <=95){
            Log.i(TAG, "East");
        }
        else if(values[0] >= 95 && values[0] <175){
            Log.i(TAG, "Sud-East");
        }
        else if((values[0] >= 175 && values[0] <= 180) || (values[0]) >= -180 && values[0] < -175){
            Log.i(TAG, "Sud");
        }
        else if(values[0] >= -175 && values[0] <-95){
            Log.i(TAG, "Sud-Ouest");
        }
        else if(values[0] >= -95 && values[0] < -85){
            Log.i(TAG, "Ouest");
        }
        else if(values[0] >= -85 && values[0] <-5){
            Log.i(TAG, "Nord-Ouest");
        }


    }

}
