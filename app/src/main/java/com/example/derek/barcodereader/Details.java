package com.example.derek.barcodereader;

/**
 * Created by Derek on 6/27/2018.
 */

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class Details extends AppCompatActivity{
    private TextView productDisplay, codeDisplay, detailsDisplay, offerDisplay;
    private TableLayout comparisonTable;
    private ImageView productImage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Details");
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Details");


        Intent intent = getIntent();
        String message = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);

        // Capture the layout's TextView and set the string as its text
        productDisplay = (TextView) findViewById(R.id.product_display);
        codeDisplay = (TextView) findViewById(R.id.code_display);
        detailsDisplay = (TextView) findViewById(R.id.description_display);
        //details.setText(message);
        //String[] lines = message.split("\n", 2);
        comparisonTable = (TableLayout)findViewById(R.id.compareTable);
        offerDisplay = (TextView) findViewById(R.id.offer_display);
        productImage = (ImageView) findViewById(R.id.product_image);


        new RequestStores().execute(message, "", "");

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private TableRow makeRow(){
        TableRow newRow = new TableRow(this);
        comparisonTable.addView(newRow);
        return newRow;
    }
    private TextView addTextView(String s){
        TextView newText = new TextView(this);
        newText.setText(s);
        return newText;
    }

    class RequestStores extends AsyncTask<String, String, String> {
        boolean err = false;
        protected String doInBackground(String... inputs) {
            String output = "";
            String item = inputs[0].replace(' ', '+');
            //String key = "AIzaSyAr-AK5Maj7MlJEoQkt_XiNF891qW2bS0Y";
            //String engine = "005773736382830971489:6njleywqa3i";
            //String url = "https://www.googleapis.com/customsearch/v1?key=" + key + "&cx=" + engine + "&q=" + item;
            //String url = "https://www.googleapis.com/customsearch/v1?key=AIzaSyAr-AK5Maj7MlJEoQkt_XiNF891qW2bS0Y&cx=005773736382830971489:6njleywqa3i&q=equate_moisturizing_lotion";
            String key = "YDGBRILWFMOSTNIVWFERZIHADYNRMXOLPYXMXYCIRDNPZREGPENHLUSQFWCEGVWY";
            String params = "token=" + key + "&country=us&source=google-shopping&currentness=daily_updated&completeness=one_page&key=gtin&values=" + item;
            String url = "https://api.priceapi.com/jobs";
            try {
                URL obj = new URL(url);
                HttpURLConnection con = (HttpURLConnection) obj.openConnection();
                con.setRequestMethod("POST");
                con.setDoInput(true);
                con.setDoOutput(true);
                con.setConnectTimeout(5000);
                con.setReadTimeout(5000);
                con.connect();
//                OutputStreamWriter osw = new OutputStreamWriter(con.getOutputStream());
//                osw.write(params);
                byte[] postDataBytes = params.getBytes("UTF-8");
                con.getOutputStream().write(postDataBytes);
                InputStream in = con.getInputStream();
                InputStreamReader isw = new InputStreamReader(in);
                int data = isw.read();
                while (data != -1) {
                    char current = (char) data;
                    data = isw.read();
                    output += current;
                }
                boolean finished = false;
                String jobID = new JSONObject(output).getString("job_id");
                while (!finished){
                    url = "https://api.priceapi.com/jobs/" + jobID + "?token=" + key;
                    obj = new URL(url);
                    con = (HttpURLConnection) obj.openConnection();
                    con.setRequestMethod("GET");
                    con.setConnectTimeout(5000);
                    con.setReadTimeout(5000);
                    con.connect();
                    in = con.getInputStream();
                    isw = new InputStreamReader(in);
                    data = isw.read();
                    output = "";
                    while (data != -1) {
                        char current = (char) data;
                        data = isw.read();
                        output += current;
                    }
                    JSONObject result = new JSONObject(output);
                    if (result.getString("status").equals("finished")){
                        finished = true;
                    }
                }
                url = "https://api.priceapi.com/products/bulk/" + jobID + "?token=" + key;
                obj = new URL(url);
                con = (HttpURLConnection) obj.openConnection();
                con.setRequestMethod("GET");
                con.setConnectTimeout(5000);
                con.setReadTimeout(5000);
                con.connect();
                in = con.getInputStream();
                isw = new InputStreamReader(in);
                data = isw.read();
                output = "";
                while (data != -1) {
                    char current = (char) data;
                    data = isw.read();
                    output += current;
                }

            } catch (Exception e) {
                e.printStackTrace();
                err = true;
                return e.toString();
            }

            return output;

        }

        protected void onPostExecute(String output) {
            if (output.length() == 0) {
                productDisplay.setText("Online results not found");
                return;
            }
            if (err){
                productDisplay.setText(output);
                return;
            }
            String details = "Not found";
            try {
                JSONObject result = new JSONObject(output);
                JSONArray products = result.getJSONArray("products");
                details = "Online results:\n\n";

                //for (int i = 0; i < products.length(); i++) {
                    JSONObject targetResult = products.getJSONObject(0);
                    productDisplay.setText(targetResult.getString("name"));
                    detailsDisplay.setText("Details: \n" + targetResult.getString("description"));
                    codeDisplay.setText("GTIN Code: " + targetResult.getString("value"));

                    new DownloadImageTask(productImage).execute(targetResult.getString("image_url"));
                    offerDisplay.setText("Offers:");
                    JSONArray offers = targetResult.getJSONArray("offers");
                    for (int j = 0; j < offers.length(); j++){
                        JSONObject targetOffer = offers.getJSONObject(j);
//                        String link = "<a href='" + targetOffer.getString("url") + "'> Link </a>";
                        TableRow newRow = makeRow();
                        TextView shopName = addTextView(targetOffer.getString("shop_name"));
                        newRow.addView(shopName);
                        TextView price = addTextView("$" + targetOffer.getString("price"));
                        newRow.addView(price);
                        String linkText = "<a href='" + targetOffer.getString("url") + "'> Link </a>";
                        TextView link = addTextView("<a href='" + targetOffer.getString("url") + "'> Link </a>");
                        link.setClickable(true);
                        link.setMovementMethod(LinkMovementMethod.getInstance());
                        link.setText(Html.fromHtml(linkText));
                        newRow.addView(link);
                        //details += targetOffer.getString("shop_name") + " $" + targetOffer.getString("price") + ", " + linkText + "\n";
                    }

                    details += "\n\n";
                //}
            } catch (JSONException e) {
                productDisplay.setText(e.toString());
                e.printStackTrace();
            }
            //contentTxt.setText(details);
            try {
                //productDisplay.setText(details);
            } catch (Exception e) {
                productDisplay.setText(e.toString());
            }


        }
    }
    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;
        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }




}
