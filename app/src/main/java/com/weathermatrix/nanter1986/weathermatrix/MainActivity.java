package com.weathermatrix.nanter1986.weathermatrix;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity{
    Document doc;
    Document doc2;
    Elements img;
    Elements img2;
    Bitmap theBitmap;
    String selectedURL;
    ArrayList<String> resultUrls = new ArrayList<String>();
    ImageView displayImage;
    TextView displayText;
    Float myLatitude;
    Float myLongitude;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    Geocoder geocoder;
    List<Address> addresses;
    String city;
    String state;
    String country;
    String postalCode;
    String knownName;
    private FusedLocationProviderClient mFusedLocationClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sharedPreferences = getPreferences(MODE_PRIVATE);
        editor = sharedPreferences.edit();
        displayImage = (ImageView) findViewById(R.id.displayImage);
        displayText = (TextView) findViewById(R.id.displayText);
        getLocation();
        getLocationInfo();

        new GetDocument().execute();


    }

    private void getLocationInfo() {
        myLatitude=sharedPreferences.getFloat("lat",0);
        myLongitude=sharedPreferences.getFloat("lon",0);
        Double dLat=(double)myLatitude;
        Double dLLon=(double)myLongitude;
        TheLogger.myLog("1", dLat +" "+ dLLon);
        TheLogger.myLog("1","in info");
        if(myLatitude ==null || myLongitude ==null){
            TheLogger.myLog("1","in null");
            displayText.setText("something is null\n"+ myLongitude +"\n"+ myLatitude);
        }else {
            TheLogger.myLog("1","in else");

            try {
                geocoder = new Geocoder(this, Locale.getDefault());
                TheLogger.myLog("1","in try1");
                addresses = geocoder.getFromLocation(myLatitude,myLongitude, 1);
                TheLogger.myLog("1","in try2");

                String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                city = addresses.get(0).getLocality();
                state = addresses.get(0).getAdminArea();
                country = addresses.get(0).getCountryName();
                postalCode = addresses.get(0).getPostalCode();
                knownName = addresses.get(0).getFeatureName();
                displayText.setText(city);
                TheLogger.myLog("1","in try2");
                TheLogger.myLog("1","city:"+city);
                TheLogger.myLog("1","adress:"+address);
                TheLogger.myLog("1","state:"+state);
                TheLogger.myLog("1","country:"+country);
                TheLogger.myLog("1","postal:"+postalCode);
                TheLogger.myLog("1","known name:"+knownName);
            } catch (IOException e) {
                TheLogger.myLog("1","in catch");
                e.printStackTrace();
            }
        }
    }

    private void getLocation() {

        displayText.setText("Getting location...");
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        try {
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                myLatitude=(float)location.getLatitude();
                                myLongitude=(float)location.getLongitude();
                                editor.putFloat("lat", myLatitude);
                                editor.putFloat("lon", myLongitude);
                                editor.commit();
                                displayText.setText("Longitude:"+myLongitude+"\nLatitude:"+myLatitude);

                            }
                        }
                    });
        }catch (SecurityException se){
            se.printStackTrace();
        }
    }


    public class GetDocument extends AsyncTask<Void,Void,Void>{

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                doc= Jsoup.connect("https://www.google.gr/search?q="+city+"+rainy&client=ubuntu&hs=QMm&channel=fs&source=lnms&tbm=isch&sa=X&ved=0ahUKEwiCqbiZvpnVAhWsI8AKHVSpD4kQ_AUICigB&biw=1301&bih=323").get();
                TheLogger.myLog("1","Grabbed document");
                img = doc.select("img[data-src]");
                TheLogger.myLog("2","Array created of size"+" "+img.size());
                for(Element e:img){
                    TheLogger.myLog("2","src:<"+e.attr("data-src")+">");
                }
                selectedURL=img.get(0).attr("data-src");
                TheLogger.myLog("2","Selected url to go: "+selectedURL);



            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                TheLogger.myLog("3","Opening doc2 started at "+selectedURL +"...... ");
                doc2=Jsoup.connect(selectedURL).ignoreContentType(true).get();
                TheLogger.myLog("3","Opened doc2 at "+selectedURL);
                TheLogger.myLog("3","The full html:\n"+doc2.html());
                theBitmap=getBitmapFromURL(selectedURL);
                TheLogger.myLog("3.1","got bitmap");

                //img2 = doc2.select("img");
                //TheLogger.myLog("4","Array from doc2 created of size"+" "+img2.size());
                //TheLogger.myLog("5","Found url:"+img2.get(0).absUrl("src"));
            } catch (IOException e) {
                e.printStackTrace();
            }



            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            displayImage.setImageBitmap(theBitmap);

        }
    }



    public static Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            // Log exception
            return null;
        }
    }





}
