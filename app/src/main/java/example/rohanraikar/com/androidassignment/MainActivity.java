package example.rohanraikar.com.androidassignment;

import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback {

    GoogleMap map;
    ArrayList<LatLng> markerPoints;
    TextView tvDistanceDuration,tvFrom,tvTo;
    TempStorage myStore;
    MyTracker tracker;
    LatLng latLng;
    double curLat,curlon,newLat,newLon;
    String destAddress,choice;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        myStore=new TempStorage(getApplicationContext());
        tvDistanceDuration = (TextView) findViewById(R.id.TV_distanceTime);
        tvFrom=(TextView)findViewById(R.id.TV_fromLocation);
        tvFrom.setText("FROM : "+myStore.getValueFromMystore("LocationData","myAddress"));
        tvTo=(TextView)findViewById(R.id.TV_toLocation);
        tracker=new MyTracker(getApplicationContext());
        // Initializing
        markerPoints = new ArrayList<LatLng>();
        // Getting reference to SupportMapFragment of the activity_main
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        // Getting Map for the SupportMapFragment
        mapFragment.getMapAsync(this);
        // Enable MyLocation Button in the Map
        // Setting onclick event listener for the map
        curLat=Double.parseDouble(myStore.getValueFromMystore("LocationData","latitude"));
        curlon=Double.parseDouble(myStore.getValueFromMystore("LocationData","longitude"));
        latLng=new LatLng(curLat,curlon);
    }

    private String getDirectionsUrl(LatLng origin, LatLng dest) {
        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        // Sensor enabled
        String sensor = "sensor=true";
        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor;
        // Output format
        String output = "json";
        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;
        return url;
    }

    /** A method to download json data from url */
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);
            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();
            // Connecting to url
            urlConnection.connect();
            // Reading data from url
            iStream = urlConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
            StringBuffer sb = new StringBuffer();
            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            data = sb.toString();
            br.close();
        } catch (Exception e) {
            Log.d("Rohan", "Exception while downloading url" + e.getMessage());

        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(curLat, curlon), 15));
        map.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);
        setUpMap();
    }

    private void setUpMap() {
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        map.setMyLocationEnabled(true);
        map.setTrafficEnabled(true);
        map.setIndoorEnabled(true);
        map.setBuildingsEnabled(true);
        map.getUiSettings().setZoomControlsEnabled(true);
        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng point) {
                // Already two location
                if (markerPoints.size() > 0) {
                    markerPoints.clear();
                    map.clear();
                }
                // Adding new item to the ArrayList
                markerPoints.add(point);
                // Creating MarkerOptions
                MarkerOptions options = new MarkerOptions();
                // Setting the position of the marker
                options.position(point);
                /**
                 * For the start location, the color of marker is GREEN and
                 * for the end location, the color of marker is RED.
                 */
                if (markerPoints.size() == 0) {
                    options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                }
                // Add new marker to the Google Map Android API V2
                map.addMarker(options);
                // Checks, whether start and end locations are captured
                if (markerPoints.size() >= 0) {
                    LatLng origin = latLng;
                    LatLng dest = markerPoints.get(0);
                    // Getting URL to the Google Directions API
                    String url = getDirectionsUrl(origin, dest);
                    DownloadTask downloadTask = new DownloadTask();
                    // Start downloading json data from Google Directions API
                    downloadTask.execute(url,"1");
                    DownloadTask downloadTask1 = new DownloadTask();
                    // Start downloading json data from Google Directions API
                    downloadTask1.execute(url+"&mode=walking","2");
                }
            }
        });
    }

    // Fetches data from url passed
    private class DownloadTask extends AsyncTask<String, Void, String> {

        // Downloading data in non-ui thread
        @Override
        protected String doInBackground(String... url) {
        // For storing data from web service
            String data = "";
            try{
                // Fetching the data from web service
                choice=url[1];
                data = downloadUrl(url[0]);
                Log.d("Rohan","Received data:"+data);
                }catch(Exception e){
                Log.d("Background Task",e.toString());
                }
            return data;
            }
        // Executes in UI thread, after the execution of
        // doInBackground()
                @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            ParserTask parserTask = new ParserTask();
            // Invokes the thread for parsing the JSON data
            parserTask.execute(result,choice);
            }
        }


    /** A class to parse the Google Places in JSON format */
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String,String>>> >{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(getApplicationContext(),"Fetching Address, Please wait",Toast.LENGTH_LONG).show();
        }

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {
            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;
            try{
                jObject = new JSONObject(jsonData[0]);
                JSONParser parser = new JSONParser();
                // Starts parsing data
                routes = parser.parse(jObject);
                }catch(Exception e){
                e.printStackTrace();
                }
            return routes;
            }
        // Executes in UI thread, after the parsing process

         @Override
         protected void onPostExecute(List<List<HashMap<String, String>>> result) {

            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = null;
            MarkerOptions markerOptions = new MarkerOptions();
            String distance = "";
            String duration = "";
            if(result.size()<0){
                Toast.makeText(getBaseContext(), "No Points", Toast.LENGTH_SHORT).show();
                return;
              }
            // Traversing through all the routes
            for(int i=0;i<result.size();i++){
                points = new ArrayList<LatLng>();
                lineOptions = new PolylineOptions();
                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);
                // Fetching all the points in i-th route
                for(int j=0;j<path.size();j++){
                    HashMap<String,String> point = path.get(j);
                    if(j==0){
                        // Get distance from the list
                        distance = (String)point.get("distance");
                        continue;
                    }else if(j==1){
                        // Get duration from the list
                        duration = (String)point.get("duration");
                        continue;
                    }
                    newLat = Double.parseDouble(point.get("lat"));
                    newLon = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(newLat, newLon);
                    points.add(position);
                }
                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(8);
                lineOptions.color(Color.BLUE);
             }
             tvDistanceDuration.setText("Distance:"+distance + ", Duration:"+duration);
             // Drawing polyline in the Google Map for the i-th route
             map.addPolyline(lineOptions);
             //Creating a background method to fetch address of new location
             GetAddress getAddress=new GetAddress();
             getAddress.execute();
         }
    }

    protected class GetAddress extends AsyncTask<String,String,String>{

        @Override
        protected String doInBackground(String[] params) {
            destAddress=tracker.getCompleteAddressString(newLat,newLon);
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            tvTo.setText("TO : "+destAddress);
        }
    }

}
