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

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;

public class MainActivity extends Activity {

    Document doc;
    Elements img;
    ImageView displayImage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        displayImage=(ImageView)findViewById(R.id.displayImage);
        new GetDocument().execute();

    }

    public class GetDocument extends AsyncTask<Void,Void,Void>{

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                doc= Jsoup.connect("https://www.google.gr/search?q=chania+rainy+images&client=ubuntu&hs=Zs2&channel=fs&source=lnms&tbm=isch&sa=X&ved=0ahUKEwjs-4T3spTVAhVHwxQKHTbvDzYQ_AUICigB&biw=1301&bih=641").get();
                img = doc.getElementsByTag("img");
            } catch (IOException e) {
                e.printStackTrace();
            }

            String title = doc.title();
            //displayImage.setImageDrawable((Drawable) img.get(0));
            Log.i("title",title);
            for(int i=0;i<100;i++){
                Log.i("title",img.get(i).absUrl("alt")+" src:<"+img.get(i).absUrl("src")+">");
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
