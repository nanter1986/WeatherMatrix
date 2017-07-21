package com.weathermatrix.nanter1986.weathermatrix;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends Activity {

    Document doc;
    Document doc2;
    Elements img;
    Elements img2;
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
                    //https://stackoverflow.com/questions/40162503/java-jsoup-google-image-search-result-parsing
                }
                TheLogger.myLog("2","data-src:<"+img.get(0).attr("data-src")+">");
                doc2=Jsoup.connect(img.get(0).attr("data-src")).get();
                TheLogger.myLog("3","Opened doc2 at "+img.get(0).attr("data-src"));
                img2 = doc2.select("img.irc_mi");
                TheLogger.myLog("4","Array from doc2 created of size"+" "+img2.size());
                TheLogger.myLog("5","Found url:"+img2.get(0).absUrl("src"));

            } catch (IOException e) {
                e.printStackTrace();
            }

            String title = doc.title();


            for(Element e:img){
                //TheLogger.myLog("5",e.absUrl("alt")+" href:<"+e.absUrl("href")+">");
                //https://stackoverflow.com/questions/40162503/java-jsoup-google-image-search-result-parsing
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }
}
