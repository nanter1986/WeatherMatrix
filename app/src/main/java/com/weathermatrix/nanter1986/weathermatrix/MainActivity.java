package com.weathermatrix.nanter1986.weathermatrix;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
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

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
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
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {
    private InterstitialAd interstitial;
    private Handler mHandler;
    private Runnable displayAd;

    Document doc;
    Document doc2;
    Elements img;
    Elements img2;
    Bitmap[] theBitmap=new Bitmap[7];
    String selectedURL;
    ArrayList<String> resultUrls = new ArrayList<String>();


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
        prepareIntAd();
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

    public void prepareIntAd(){
        interstitial = new InterstitialAd(MainActivity.this);
        interstitial.setAdUnitId("ca-app-pub-1155245883636527/9134776076");
        interstitial.loadAd(new AdRequest.Builder().build());
        interstitial.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                // Load the next interstitial.
                interstitial.loadAd(new AdRequest.Builder().build());
            }

            @Override
            public void onAdLoaded() {
                // Code to be executed when an ad finishes loading.
                TheLogger.myLog("Ads", "onAdLoaded");
                displayInterstitial();
            }


        });



    }

    public void displayInterstitial() {
        if (interstitial.isLoaded()) {
            interstitial.show();
        } else {
            interstitial.loadAd(new AdRequest.Builder().build());
            TheLogger.myLog("ads.............", "The interstitial wasn't loaded yet.");
        }

    }

    private void referenceImageviewsAndTextviews() {

        containers[0]=new DisplayContainer((ImageView)findViewById(R.id.zeroDisplayImage),
                (TextView) findViewById(R.id.zeroDisplayText),
                0,
                (TextView) findViewById(R.id.zeroHeadText),
                (ImageView) findViewById(R.id.zeroIcon),
                (TextView) findViewById(R.id.zeroWind));


        containers[1]=new DisplayContainer((ImageView)findViewById(R.id.oneDisplayImage),
                (TextView) findViewById(R.id.oneDisplayText),
                1,
                (TextView) findViewById(R.id.oneHeadText),
                (ImageView) findViewById(R.id.oneIcon),
                (TextView) findViewById(R.id.oneWind));


        containers[2]=new DisplayContainer((ImageView)findViewById(R.id.twoDisplayImage),
                (TextView) findViewById(R.id.twoDisplayText),
                2,
                (TextView) findViewById(R.id.twoHeadText),
                (ImageView) findViewById(R.id.twoIcon),
                (TextView) findViewById(R.id.twoWind));


        containers[3]=new DisplayContainer((ImageView)findViewById(R.id.threeDisplayImage),
                (TextView) findViewById(R.id.threeDisplayText),
                3,
                (TextView) findViewById(R.id.threeHeadText),
                (ImageView) findViewById(R.id.threeIcon),
                (TextView) findViewById(R.id.threeWind));


        containers[4]=new DisplayContainer((ImageView)findViewById(R.id.fourDisplayImage),
                (TextView) findViewById(R.id.fourDisplayText),
                4,
                (TextView) findViewById(R.id.fourHeadText),
                (ImageView) findViewById(R.id.fourIcon),
                (TextView) findViewById(R.id.fourWind));


        containers[5]=new DisplayContainer((ImageView)findViewById(R.id.fiveDisplayImage),
                (TextView) findViewById(R.id.fiveDisplayText),
                5,
                (TextView) findViewById(R.id.fiveHeadText),
                (ImageView) findViewById(R.id.fiveIcon),
                (TextView) findViewById(R.id.fiveWind));


        containers[6]=new DisplayContainer((ImageView)findViewById(R.id.sixDisplayImage),
                (TextView) findViewById(R.id.sixDisplayText),
                6,
                (TextView) findViewById(R.id.sixHeadText),
                (ImageView) findViewById(R.id.sixIcon),
                (TextView) findViewById(R.id.sixWind));

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
        displayInterstitial();

        return false;


    }

    public String setTempTextColor(double temp){
        String color="#7cfc00";
        if(temp<0){
            color="#0de6ec";
        }else if(temp<10){
            color="#0de6ec";
        }else if(temp<20){
            color="#41ec0d";
        }else if(temp<30){
            color="#ecd70d";
        }else if(temp<40){
            color="#ec9f0d";
        }else{
            color="#ec450d";
        }

        return color;
    }

    protected int getIconForWeatherDescription(String description){
        int icon=0;
        switch (description){
            case "clear sky":
                icon=R.drawable.clear_sky;
                break;
            case "few clouds":
                icon=R.drawable.few_clouds;
                break;
            case "scattered clouds":
                icon=R.drawable.scattered_clouds;
                break;
            case "broken clouds":
                icon=R.drawable.broken_clouds;
                break;
            case "rain":
                icon=R.drawable.rain;
                break;
            case "shower rain":
                icon=R.drawable.shower_rain;
                break;
            case "thunderstorm":
                icon=R.drawable.thunderstorm;
                break;
            case "snow":
                icon=R.drawable.snow;
                break;
            case "mist":
                icon=R.drawable.mist;
                break;
        }

        return icon;
    }

    public void displayWeather(JSONObject json,int index) {
        try {
            JSONObject full = json.getJSONArray("list").getJSONObject(containers[index].day).getJSONObject("main");
            TheLogger.myLog("weather", "cool1");
            String dateLocal = json.getJSONArray("list").getJSONObject(containers[index].day).getString("dt_txt");
            String weatherDescriptionLocal = json.getJSONArray("list").getJSONObject(containers[index].day).getJSONArray("weather").getJSONObject(0).getString("description");
            containers[index].date.setText(dateLocal);
            Double windSpeed = json.getJSONArray("list").getJSONObject(containers[index].day).getJSONObject("wind").getDouble("speed") * 3600/1000;
            containers[index].wind.setText(windSpeed.intValue()+" "+"km/h");
            String temperature = full.getString("temp");
            Double doubleTemp=Double.parseDouble(temperature);
            containers[index].temp.setTextColor(Color.parseColor(setTempTextColor(doubleTemp)));
            containers[index].temp.setText(String.format("%.2f", doubleTemp) + " ℃\n"+weatherDescriptionLocal);
            containers[index].icon.setBackgroundResource(getIconForWeatherDescription(weatherDescriptionLocal));
            TheLogger.myLog("weather", "cool2");
            displayInterstitial();
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
            containers[0].temp.setText("something is null\n" + myLongitude + "\n" + myLatitude);
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
                containers[0].temp.setText(city);
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

        containers[0].temp.setText("Getting location...");
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
                                containers[0].temp.setText("Longitude:" + myLongitude + "\nLatitude:" + myLatitude);

                            }
                        }
                    });
        } catch (SecurityException se) {
            se.printStackTrace();
        }
    }


    public class GetDocument extends AsyncTask<Void, Void, Void> {

        boolean grabbedNewJSONFromServer=false;



        protected boolean checkIfOneHourHasPassedSinceLastRequest(String date){
            if(date.equals("empty")){
                grabbedNewJSONFromServer=true;
                TheLogger.myLog("saved date:","is empty");
                return true;
            }
            Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int day = c.get(Calendar.DAY_OF_MONTH);
            int month = c.get(Calendar.MONTH)+1;
            TheLogger.myLog("hour check:",hour+" "+getHourFromDateString(date));
            TheLogger.myLog("day check:",day+" "+getDayFromDateString(date));
            TheLogger.myLog("month check:",month+" "+getMonthFromDateString(date));
            if(hour<=getHourFromDateString(date)+3 && day==getDayFromDateString(date) && month==getMonthFromDateString(date)){
                return false;
            }else{
                grabbedNewJSONFromServer=true;
                return true;
            }
        }

        protected int getMonthFromDateString(String date){
            int theResult=03;
            int stringLength=date.length();
            char[] charArray=date.toCharArray();
            for(int i=0;i<stringLength-2;i++){
                if(charArray[i]=='-'){
                    theResult=Integer.parseInt(""+charArray[i+1]+charArray[i+2]);
                    TheLogger.myLog("character into integer for Month: ",""+theResult);
                    break;
                }
            }
            return theResult;
        }

        protected int getDayFromDateString(String date){
            int countForDashOnDate=0;
            int theResult=03;
            int stringLength=date.length();
            char[] charArray=date.toCharArray();
            for(int i=0;i<stringLength-2;i++){
                if(charArray[i]=='-' && countForDashOnDate==1){
                    theResult=Integer.parseInt(""+charArray[i+1]+charArray[i+2]);
                    TheLogger.myLog("character into integer for day: ",""+theResult);
                    break;
                }else if(charArray[i]=='-'){
                    countForDashOnDate++;
                }
            }

            return theResult;
        }

        protected int getHourFromDateString(String date){
            int theResult=03;
            int stringLength=date.length();
            char[] charArray=date.toCharArray();
            for(int i=0;i<stringLength-2;i++){
                if(charArray[i]==' '){
                    theResult=Integer.parseInt(""+charArray[i+1]+charArray[i+2]);
                    TheLogger.myLog("character into integer for hour: ",""+theResult);
                    break;
                }
            }

            return theResult;
        }

        protected String checkIfDay(String date){
            String dayOrNight="day";
            int theResult=03;
            int stringLength=date.length();
            char[] charArray=date.toCharArray();
            for(int i=0;i<stringLength-2;i++){
                if(charArray[i]==' '){
                    theResult=Integer.parseInt(""+charArray[i+1]+charArray[i+2]);
                    TheLogger.myLog("character into integer for day or night: ",""+theResult);
                    break;
                }
            }
            if(theResult<6 || theResult>20){
                dayOrNight="night";
            }

            return dayOrNight;
        }

        protected JSONObject getNewOrOldJSON(){
            JSONObject theResult=null;
            if(checkIfOneHourHasPassedSinceLastRequest(sharedPreferences.getString("dateForCheck","empty"))){
                theResult = WeatherGrabber.grabWeather(context, myLongitude, myLatitude);
                TheLogger.myLog("hour Check:","One hour has passed,going for new json");
            }else{
                try {
                    theResult = new JSONObject(sharedPreferences.getString("json1", ""));
                    TheLogger.myLog("hour Check:","One hour hasn't passed,keeping old json");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
            return theResult;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            final JSONObject json=getNewOrOldJSON();

            if (json == null) {
                TheLogger.myLog("10", "JSON is null sadly");
            } else {
                for (int i = 0; i < 7; i++) {
                    try {
                        String weatherConditionLocal = json.getJSONArray("list").getJSONObject(containers[i].day).getJSONArray("weather").getJSONObject(0).getString("main");
                        String weatherDescriptionLocal = json.getJSONArray("list").getJSONObject(containers[i].day).getJSONArray("weather").getJSONObject(0).getString("description");
                        String dateLocal = json.getJSONArray("list").getJSONObject(containers[i].day).getString("dt_txt");
                        if(i==0 && grabbedNewJSONFromServer){
                            editor.putString("dateForCheck", dateLocal);
                            TheLogger.myLog("saved date: ",dateLocal);
                        }
                        TheLogger.myLog("date and weather: ", dateLocal+" : "+weatherConditionLocal.toString());
                        editor.putString("json1", json.toString());
                        TheLogger.myLog("10", "JSON is cool: " + json.toString());
                        TheLogger.myLog("10", "small JSON is cool-dayIndex: "+containers[i].day+" " + weatherConditionLocal);
                        editor.commit();
                        String timeOfDay=checkIfDay(dateLocal);
                        TheLogger.myLog("day or night? answer:",timeOfDay);
                        String urlForJsoup = "https://www.google.gr/search?q=" + city + "+" + weatherConditionLocal + "+" + timeOfDay +"&client=ubuntu&hs=QMm&channel=fs&source=lnms&tbm=isch&sa=X&ved=0ahUKEwiCqbiZvpnVAhWsI8AKHVSpD4kQ_AUICigB&biw=1301&bih=323";
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
                containers[i].picture.setImageBitmap(theBitmap[i]);
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
