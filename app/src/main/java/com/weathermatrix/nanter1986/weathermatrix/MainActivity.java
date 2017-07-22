package com.weathermatrix.nanter1986.weathermatrix;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.media.Image;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

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
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends Activity {
    Document doc;
    Document doc2;
    Elements img;
    Elements img2;
    Bitmap theBitmap;
    String selectedURL;
    ArrayList<String> resultUrls = new ArrayList<String>();
    ImageView displayImage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        displayImage=findViewById(R.id.displayImage);

        new GetDocument().execute();


    }

    public class GetDocument extends AsyncTask<Void,Void,Void>{

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                doc= Jsoup.connect("https://www.google.gr/search?q=chania+rainy&client=ubuntu&hs=QMm&channel=fs&source=lnms&tbm=isch&sa=X&ved=0ahUKEwiCqbiZvpnVAhWsI8AKHVSpD4kQ_AUICigB&biw=1301&bih=323").get();
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
