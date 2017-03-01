package com.example.canonico.myapplication;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private NavigationView navigationView;
    private DrawerLayout drawer;
    private ActionBarDrawerToggle toggle;
    private static final String TAG = "MainActivity";
    private Intent service1;

    private PowerManager.WakeLock wakeLock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        service1 = new Intent(MainActivity.this, MyServicePhone.class);
        startService(service1);

        //methode avec wake_lock => risque d'epuiser le batterie
        //startWakefulService(MainActivity.this,service1);

        // Wakelock implémenté d'une autre manière
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyWakeLock");
        wakeLock.acquire();
        Log.i(TAG, "wakeLock acquired");

        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {

        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.Ff
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
          //  return true;
        }

        return super.onOptionsItemSelected(item);
    }
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        Fragment fragment=null;

        if (id == R.id.nav_camara) {
           fragment=new TextToFragment();

          //  startActivity(new Intent(this, TestActivity.class));
        } else if (id == R.id.nav_gallery) {
            fragment=new SMSFragment();
        } else if (id == R.id.nav_slideshow) {
            fragment=new CardiaqueFragment();
        } else if (id == R.id.nav_manage) {
             //accelerometre
            fragment=new AccelerometerFragment();
        }
        else if (id == R.id.nav_sms) {
            fragment=new SMSReceiveFragment();
        }
        else if (id == R.id.nav_add_contact) {
            fragment=new AddContactFragment();
        }
        else if (id == R.id.nav_list_contacts) {
            fragment=new ContactFragment();
        }
        else if (id == R.id.nav_quitter) {
            wakeLock.release(); // Déverrouillage
            Log.i(TAG, "wakeLock released");
            stopService(service1);
            //android.os.Process.killProcess(android.os.Process.myPid());
            Log.i(TAG, "Exit");
             //finish();
            //finish();
            System.exit(1);
            //return true;
        }

//        else if (id == R.id.nav_alarm) {
//            //fragment=new AlarmFragment();
//        }


        /*
        FragmentManager fragmentManager = getFragmentManager();

        fragmentManager.beginTransaction().replace(R.id.content_main, fragment).commit();
*/


        // Highlight the selected item, update the title, and close the drawer
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
