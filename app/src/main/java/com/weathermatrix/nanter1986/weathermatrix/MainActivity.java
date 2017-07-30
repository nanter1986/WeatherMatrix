package com.weathermatrix.nanter1986.weathermatrix;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {
    Document doc;
    Document doc2;
    Elements img;
    Elements img2;
    Bitmap[] theBitmap=new Bitmap[7];
    String selectedURL;
    ArrayList<String> resultUrls = new ArrayList<String>();

    ImageView zeroDisplayImage;
    ImageView oneDisplayImage;
    ImageView twoDisplayImage;
    ImageView threeDisplayImage;
    ImageView fourDisplayImage;
    ImageView fiveDisplayImage;
    ImageView sixDisplayImage;
    TextView zeroDisplayText;
    TextView oneDisplayText;
    TextView twoDisplayText;
    TextView threeDisplayText;
    TextView fourDisplayText;
    TextView fiveDisplayText;
    TextView sixDisplayText;


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

    String weatherCondition;

    Context context;
    Handler handler;

    private ViewFlipper mViewFlipper;
    private float initialX;

    DisplayContainer[] containers=new DisplayContainer[7];

    private FusedLocationProviderClient mFusedLocationClient;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sharedPreferences = getPreferences(MODE_PRIVATE);
        editor = sharedPreferences.edit();
        mViewFlipper = this.findViewById(R.id.view_flipper);
        referenceImageviewsAndTextviews();
        //http://www.journaldev.com/10429/android-viewflipper-example-tutorial

        handler = new Handler();
        context = getApplicationContext();
        getLocation();
        getLocationInfo();
        //new GetWeatherAsync().execute();
        //getWeather();

        new GetDocument().execute();


    }

    private void referenceImageviewsAndTextviews() {
        zeroDisplayImage = findViewById(R.id.zeroDisplayImage);
        zeroDisplayText = findViewById(R.id.zeroDisplayText);
        containers[0]=new DisplayContainer(zeroDisplayImage,zeroDisplayText,0);

        oneDisplayImage = findViewById(R.id.oneDisplayImage);
        oneDisplayText = findViewById(R.id.oneDisplayText);
        containers[1]=new DisplayContainer(oneDisplayImage,oneDisplayText,1);

        twoDisplayImage = findViewById(R.id.twoDisplayImage);
        twoDisplayText = findViewById(R.id.twoDisplayText);
        containers[2]=new DisplayContainer(twoDisplayImage,twoDisplayText,2);

        threeDisplayImage = findViewById(R.id.threeDisplayImage);
        threeDisplayText = findViewById(R.id.threeDisplayText);
        containers[3]=new DisplayContainer(threeDisplayImage,threeDisplayText,3);

        fourDisplayImage = findViewById(R.id.fourDisplayImage);
        fourDisplayText = findViewById(R.id.fourDisplayText);
        containers[4]=new DisplayContainer(fourDisplayImage,fourDisplayText,4);

        fiveDisplayImage = findViewById(R.id.fiveDisplayImage);
        fiveDisplayText = findViewById(R.id.fiveDisplayText);
        containers[5]=new DisplayContainer(fiveDisplayImage,fiveDisplayText,5);

        sixDisplayImage = findViewById(R.id.sixDisplayImage);
        sixDisplayText = findViewById(R.id.sixDisplayText);
        containers[6]=new DisplayContainer(sixDisplayImage,sixDisplayText,6);

    }

    @Override
    public boolean onTouchEvent(MotionEvent touchevent) {
        switch (touchevent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                initialX = touchevent.getX();
                break;
            case MotionEvent.ACTION_UP:
                float finalX = touchevent.getX();
                if (initialX > finalX) {
                    if (mViewFlipper.getDisplayedChild() == 6){
                        TheLogger.myLog("limit scroll","you reached the end");
                        break;
                    }


                    mViewFlipper.setInAnimation(AnimationUtils.loadAnimation(context, R.anim.in_from_left));
                    mViewFlipper.setOutAnimation(AnimationUtils.loadAnimation(context, R.anim.out_from_left));
                    TheLogger.myLog("next","showing next card");
                    mViewFlipper.showNext();
                } else {
                    if (mViewFlipper.getDisplayedChild() == 0){
                        TheLogger.myLog("limit scroll","you reached the end");
                        break;
                    }


                    mViewFlipper.setInAnimation(AnimationUtils.loadAnimation(context, R.anim.in_from_right));
                    mViewFlipper.setOutAnimation(AnimationUtils.loadAnimation(context, R.anim.out_from_right));
                    TheLogger.myLog("previous","showing previous card");
                    mViewFlipper.showPrevious();
                }
                break;
        }


        return false;


    }



    public void displayWeather(JSONObject json,int index) {
        try {
            JSONObject full = json.getJSONArray("list").getJSONObject(containers[index].day).getJSONObject("main");
            TheLogger.myLog("weather", "cool1");
            String dateLocal = json.getJSONArray("list").getJSONObject(containers[index].day).getString("dt_txt");
            String temperature = full.getString("temp");
            containers[index].tv.setText(dateLocal+" :\n"+String.format("%.2f", Double.parseDouble(temperature)) + " ℃");
            TheLogger.myLog("weather", "cool2");
            //weatherCondition = json.getJSONArray("weather").getJSONObject(0).getString("main");
            TheLogger.myLog("weather", "cool3");
        } catch (JSONException e) {
            TheLogger.myLog("weather", "something wrong");
            e.printStackTrace();
        }
    }

    private void getLocationInfo() {
        myLatitude = sharedPreferences.getFloat("lat", 0);
        myLongitude = sharedPreferences.getFloat("lon", 0);
        Double dLat = (double) myLatitude;
        Double dLLon = (double) myLongitude;
        TheLogger.myLog("1", dLat + " " + dLLon);
        TheLogger.myLog("1", "in info");
        if (myLatitude == null || myLongitude == null) {
            TheLogger.myLog("1", "in null");
            zeroDisplayText.setText("something is null\n" + myLongitude + "\n" + myLatitude);
        } else {
            TheLogger.myLog("1", "in else");

            try {
                geocoder = new Geocoder(this, Locale.getDefault());
                TheLogger.myLog("1", "in try1");
                addresses = geocoder.getFromLocation(myLatitude, myLongitude, 1);
                TheLogger.myLog("1", "in try2");

                String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                city = addresses.get(0).getLocality();
                state = addresses.get(0).getSubAdminArea();
                country = addresses.get(0).getCountryName();
                postalCode = addresses.get(0).getPostalCode();
                knownName = addresses.get(0).getFeatureName();
                zeroDisplayText.setText(city);
                TheLogger.myLog("1", "in try2");
                TheLogger.myLog("1", "city:" + city);
                TheLogger.myLog("1", "adress:" + address);
                TheLogger.myLog("1", "state:" + state);
                TheLogger.myLog("1", "country:" + country);
                TheLogger.myLog("1", "postal:" + postalCode);
                TheLogger.myLog("1", "known name:" + knownName);
                TheLogger.myLog("1", "thourough:" + addresses.get(0).getThoroughfare());
                TheLogger.myLog("1", "Sthourough:" + addresses.get(0).getSubThoroughfare());
            } catch (IOException e) {
                TheLogger.myLog("1", "in catch");
                e.printStackTrace();
            }
        }
    }

    private void getLocation() {

        zeroDisplayText.setText("Getting location...");
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        try {
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                myLatitude = (float) location.getLatitude();
                                myLongitude = (float) location.getLongitude();
                                editor.putFloat("lat", myLatitude);
                                editor.putFloat("lon", myLongitude);
                                editor.commit();
                                zeroDisplayText.setText("Longitude:" + myLongitude + "\nLatitude:" + myLatitude);

                            }
                        }
                    });
        } catch (SecurityException se) {
            se.printStackTrace();
        }
    }


    public class GetDocument extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            final JSONObject json = WeatherGrabber.grabWeather(context, myLongitude, myLatitude);
            if (json == null) {
                TheLogger.myLog("10", "JSON is null sadly");
            } else {
                for (int i = 0; i < 7; i++) {
                    try {
                        String weatherConditionLocal = json.getJSONArray("list").getJSONObject(containers[i].day).getJSONArray("weather").getJSONObject(0).getString("main");
                        String dateLocal = json.getJSONArray("list").getJSONObject(containers[i].day).getString("dt_txt");
                        TheLogger.myLog("date and weather: ", dateLocal+" : "+weatherConditionLocal.toString());
                        editor.putString("json1", json.toString());
                        TheLogger.myLog("10", "JSON is cool: " + json.toString());
                        TheLogger.myLog("10", "small JSON is cool-dayIndex: "+containers[i].day+" " + weatherConditionLocal);
                        editor.commit();
                        String urlForJsoup = "https://www.google.gr/search?q=" + city + "+" + weatherConditionLocal + "&client=ubuntu&hs=QMm&channel=fs&source=lnms&tbm=isch&sa=X&ved=0ahUKEwiCqbiZvpnVAhWsI8AKHVSpD4kQ_AUICigB&biw=1301&bih=323";
                        doc = Jsoup.connect(urlForJsoup).get();
                        TheLogger.myLog("1", "Grabbed document at: " + urlForJsoup);
                        img = doc.select("img[data-src]");
                        TheLogger.myLog("2", "Array created of size" + " " + img.size());
                        /*for (Element e : img) {
                            TheLogger.myLog("2", "src:<" + e.attr("data-src") + ">");
                        }*/
                        selectedURL = img.get(i).attr("data-src");
                        TheLogger.myLog("2", "Selected url to go: " + selectedURL);
                    } catch (JSONException e1) {
                        e1.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        TheLogger.myLog("3", "Opening doc2 started at " + selectedURL + "...... ");
                        doc2 = Jsoup.connect(selectedURL).ignoreContentType(true).get();
                        TheLogger.myLog("3", "Opened doc2 at " + selectedURL);
                        TheLogger.myLog("3", "The full html:\n" + doc2.html());
                        theBitmap[i] = getBitmapFromURL(selectedURL);
                        TheLogger.myLog("3.1", "got bitmap");

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }


            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            for(int i=0;i<7;i++){
                containers[i].iv.setImageBitmap(theBitmap[i]);
                try {
                    JSONObject json = new JSONObject(sharedPreferences.getString("json1", ""));
                    displayWeather(json,i);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }


        }

    }

}
