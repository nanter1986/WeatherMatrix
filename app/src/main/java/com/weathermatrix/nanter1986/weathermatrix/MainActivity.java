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
                doc= Jsoup.connect("https://www.google.gr/search?q=chania+rainy+images&client=ubuntu&hs=Zs2&channel=fs&source=lnms&tbm=isch&sa=X&ved=0ahUKEwjs-4T3spTVAhVHwxQKHTbvDzYQ_AUICigB&biw=1301&bih=641").get();
                TheLogger.myLog("1","Grabbed document");
                img = doc.select("a.rg_l");
                TheLogger.myLog("2","Array created of size"+" "+img.size());
                for(Element e:img){
                    TheLogger.myLog("2",e.absUrl("alt")+" href:<"+e.absUrl("href")+">");
                    //https://stackoverflow.com/questions/40162503/java-jsoup-google-image-search-result-parsing
                    //https://www.google.gr/imgres?imgurl=http%3A%2F%2Fstatic.panoramio.com%2Fphotos%2Flarge%2F57610207.jpg&imgrefurl=http%3A%2F%2Fwww.panoramio.com%2Fphoto%2F57610207&docid=aDqLzb4c26eHTM&tbnid=X3n6NaF933naSM%3A&vet=10ahUKEwiU-MqKgZfVAhWjIcAKHULkBOQQMwgkKAAwAA..i&w=1024&h=691&client=ubuntu&bih=641&biw=1301&q=chania%20rainy%20images&ved=0ahUKEwiU-MqKgZfVAhWjIcAKHULkBOQQMwgkKAAwAA&iact=mrc&uact=8
                }
                doc2=Jsoup.connect(img.get(0).absUrl("href")).get();
                TheLogger.myLog("3","Opened doc2 at "+img.get(0).absUrl("href"));
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
