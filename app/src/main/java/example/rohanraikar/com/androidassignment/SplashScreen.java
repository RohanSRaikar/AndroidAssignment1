package example.rohanraikar.com.androidassignment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.widget.Toast;

/**
 * Created by rohan.raikar on 23/03/2017.
 */

public class SplashScreen extends AppCompatActivity {
    MyTracker trackeMe;
    double latitude,longitude;
    TempStorage myStore;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);
        myStore=new TempStorage(getApplicationContext());
        trackeMe=new MyTracker(getApplicationContext());
        if(trackeMe.canGetLocation()){
            longitude = trackeMe.getLongitude();
            latitude = trackeMe .getLatitude();
            String myAddress=trackeMe.getCompleteAddressString(latitude,longitude);
            myStore.setMyStore("LocationData","myAddress",myAddress);
            myStore.setMyStore("LocationData","longitude",String.valueOf(longitude));
            myStore.setMyStore("LocationData","latitude",String.valueOf(latitude));
            Toast.makeText(getApplicationContext(),"Longitude:"+Double.toString(longitude)+"\nLatitude:"+Double.toString(latitude),Toast.LENGTH_SHORT).show();
        }
        else
        {
            trackeMe.showSettingsAlert();
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // This method will be executed once the timer is over
                // Start your app main activity
                Intent i = new Intent(SplashScreen.this, MainActivity.class);
                startActivity(i);

                // close this activity
                finish();
            }
        }, 4000);

    }
}
