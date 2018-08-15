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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class Details extends AppCompatActivity implements View.OnClickListener {
    private TextView productDisplay, detailsDisplay, priceDisplay;
    private TableLayout comparisonTable;
    private ImageView productImage;
    private Button addButton, removeButton;
    private String code, name;
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
        String[] messageSplit = message.split(";");
        code = messageSplit[0];
        name = messageSplit[1];

        // Capture the layout's TextView and set the string as its text
        productDisplay = (TextView) findViewById(R.id.product_display);
        detailsDisplay = (TextView) findViewById(R.id.description_display);
        priceDisplay = (TextView) findViewById(R.id.price_display);
        productImage = (ImageView) findViewById(R.id.product_image);
        addButton = (Button)findViewById(R.id.add_button);
        removeButton = (Button)findViewById(R.id.remove_button);
        addButton.setOnClickListener(this);
        removeButton.setOnClickListener(this);


        new GetDetails().execute(code, "", "");
        new GetPrice().execute(code, "", "");

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
    }

    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.add_button){
            new UpdateUser().execute(code, "add", "");
        }
        if (view.getId() == R.id.remove_button){
            new UpdateUser().execute(code, "remove", "");
        }
    }

    public void toMainScreen(String input) throws IOException {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("test", input);

        startActivity(intent);
    }


    class GetDetails extends AsyncTask<String, String, String> {
        protected String doInBackground(String... inputs) {
            String output = "";
            String barcode = inputs[0];
            String url = "https://storeproject-209402.appspot.com/products/" + barcode;
            //String url = "https://www.googleapis.com/customsearch/v1?key=AIzaSyAr-AK5Maj7MlJEoQkt_XiNF891qW2bS0Y&cx=005773736382830971489:6njleywqa3i&q=equate_moisturizing_lotion";

            try {
                URL obj = new URL(url);
                HttpURLConnection con = (HttpURLConnection)obj.openConnection();
                con.setRequestMethod("GET");
                //con.setDoInput(true);
//                con.setDoOutput(true);
//                con.setConnectTimeout(5000);
//                con.setReadTimeout(5000);
                con.connect();
                InputStream in = con.getInputStream();
                InputStreamReader isw = new InputStreamReader(in);
                int data = isw.read();
                while (data != -1) {
                    char current = (char) data;
                    data = isw.read();
                    output += current;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }


            return output;

        }

        protected void onPostExecute(String output) {
            if (output.equals("404 not found")){
                Toast toast = Toast.makeText(getApplicationContext(),
                        "Invalid", Toast.LENGTH_SHORT);
                toast.show();
                try {//modify
                    toMainScreen(name);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return;
            }

            try {//modify
                JSONObject result = new JSONObject(output);
                productDisplay.setText(result.getString("name"));
                detailsDisplay.setText(result.getString("description"));
                new DownloadImageTask(productImage).execute(result.getString("image"));
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    class GetPrice extends AsyncTask<String, String, String> {
        protected String doInBackground(String... inputs) {
            String output = "";
            String barcode = inputs[0];
            String url = "https://storeproject-209402.appspot.com/prices/" + barcode;
            //String url = "https://www.googleapis.com/customsearch/v1?key=AIzaSyAr-AK5Maj7MlJEoQkt_XiNF891qW2bS0Y&cx=005773736382830971489:6njleywqa3i&q=equate_moisturizing_lotion";

            try {
                URL obj = new URL(url);
                HttpURLConnection con = (HttpURLConnection)obj.openConnection();
                con.setRequestMethod("GET");
                //con.setDoInput(true);
//                con.setDoOutput(true);
//                con.setConnectTimeout(5000);
//                con.setReadTimeout(5000);
                con.connect();
                InputStream in = con.getInputStream();
                InputStreamReader isw = new InputStreamReader(in);
                int data = isw.read();
                while (data != -1) {
                    char current = (char) data;
                    data = isw.read();
                    output += current;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }


            return output;

        }

        protected void onPostExecute(String output) {
            if (output.equals("404 not found")){
                Toast toast = Toast.makeText(getApplicationContext(),
                        "Invalid", Toast.LENGTH_SHORT);
                toast.show();
                try {//modify
                    toMainScreen(name);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return;
            }

            try {//modify
                JSONObject result = new JSONObject(output);
                priceDisplay.setText("$" + result.getString("price"));
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }


    class UpdateUser extends AsyncTask<String, String, String> {
        protected String doInBackground(String... inputs) {
            String output = "";
            String barcode = inputs[0];
            String action = inputs[1];

            String url = "https://storeproject-209402.appspot.com/stocks/" + barcode;
            try {
                URL obj = new URL(url);
                HttpURLConnection con = (HttpURLConnection)obj.openConnection();
                con.setRequestMethod("GET");
                //con.setDoInput(true);
//                con.setDoOutput(true);
//                con.setConnectTimeout(5000);
//                con.setReadTimeout(5000);
                con.connect();
                InputStream in = con.getInputStream();
                InputStreamReader isw = new InputStreamReader(in);
                int data = isw.read();
                while (data != -1) {
                    char current = (char) data;
                    data = isw.read();
                    output += current;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                JSONObject result = new JSONObject(output);
                int stock = result.getInt("stock");
                if (stock == 0 && action.equals("add")){
                    return "Out of stock";
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            output = "";
            url = "https://storeproject-209402.appspot.com/users/" + name;
            try {
                URL obj = new URL(url);
                HttpURLConnection con = (HttpURLConnection)obj.openConnection();
                con.setRequestMethod("GET");
                //con.setDoInput(true);
//                con.setDoOutput(true);
//                con.setConnectTimeout(5000);
//                con.setReadTimeout(5000);
                con.connect();
                InputStream in = con.getInputStream();
                InputStreamReader isw = new InputStreamReader(in);
                int data = isw.read();
                while (data != -1) {
                    char current = (char) data;
                    data = isw.read();
                    output += current;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                JSONObject result = new JSONObject(output);
                String balance = result.getString("balance");
                url += "?balance=" + balance;
                if (!result.isNull("cart")) {
                    JSONArray cart = result.getJSONArray("cart");
                    for (int i = 0; i < cart.length(); i++) {
                        String targetCode = cart.getString(i);
                        if (action.equals("remove") && targetCode.equals(barcode)) {
                            action = "removed";
                        } else {
                            url += "&cart=" + targetCode;
                        }
                    }
                }
                if (action.equals("add")){
                    url += "&cart=" + barcode;
                }
                output = "";
                URL obj = new URL(url);
                HttpURLConnection con = (HttpURLConnection)obj.openConnection();
                con.setRequestMethod("PUT");
                con.setDoInput(true);
                con.setDoOutput(true);
//                con.setConnectTimeout(5000);
//                con.setReadTimeout(5000);
                con.connect();
                InputStream in = con.getInputStream();
                InputStreamReader isw = new InputStreamReader(in);
                int data = isw.read();
                while (data != -1) {
                    char current = (char) data;
                    data = isw.read();
                    output += current;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            output = "";
            url = "https://storeproject-209402.appspot.com/stocks/" + barcode;
            try {
                URL obj = new URL(url);
                HttpURLConnection con = (HttpURLConnection)obj.openConnection();
                con.setRequestMethod("GET");
                //con.setDoInput(true);
//                con.setDoOutput(true);
//                con.setConnectTimeout(5000);
//                con.setReadTimeout(5000);
                con.connect();
                InputStream in = con.getInputStream();
                InputStreamReader isw = new InputStreamReader(in);
                int data = isw.read();
                while (data != -1) {
                    char current = (char) data;
                    data = isw.read();
                    output += current;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                JSONObject result = new JSONObject(output);
                int stock = result.getInt("stock");
                if (action.equals("add")){
                    if (stock == 0) {
                        return "Out of stock";
                    }
                    else{
                        stock--;
                    }
                }
                else if (action.equals("removed")){
                    stock++;
                }
                url += "?stock=" + Integer.toString(stock);
                URL obj = new URL(url);
                HttpURLConnection con = (HttpURLConnection)obj.openConnection();
                con.setRequestMethod("PUT");
                con.setDoInput(true);
                con.setDoOutput(true);
//                con.setConnectTimeout(5000);
//                con.setReadTimeout(5000);
                con.connect();
                InputStream in = con.getInputStream();
                InputStreamReader isw = new InputStreamReader(in);
                int data = isw.read();
                while (data != -1) {
                    char current = (char) data;
                    data = isw.read();
                    output += current;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (action.equals("add")){
                return "Item added";
            }
            else if (action.equals("removed")){
                return "Item removed";
            }
            else if (action.equals("remove")){
                return "Item not in cart";
            }
            return output;

        }

        protected void onPostExecute(String output) {
            if (output.equals("404 not found")){
                Toast toast = Toast.makeText(getApplicationContext(),
                        "Invalid", Toast.LENGTH_SHORT);
                toast.show();
                return;
            }
            Toast toast = Toast.makeText(getApplicationContext(),
                    output, Toast.LENGTH_SHORT);
            toast.show();


            try {//modify
                toMainScreen(name);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

/*
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
    }*/

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
