package com.example.derek.barcodereader;

import android.content.ActivityNotFoundException;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import android.os.AsyncTask;
import org.json.*;
import org.w3c.dom.Text;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity implements OnClickListener {
    private Button scanButton, balanceButton, checkoutButton, micButton;
    private TextView balanceListing, totalListing;
    private TableLayout itemTable;
    private EditText balanceInput;
    public static final String EXTRA_MESSAGE = "test";
    double total;
    private int counter;
    private final int REQ_CODE_SPEECH_INPUT = 100;
    String userName;
    Map<String, TextView> stocks;
    Map<Integer, String> links;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        scanButton = (Button)findViewById(R.id.scan_button);
        balanceButton = (Button)findViewById(R.id.balance_button);
        checkoutButton = (Button)findViewById(R.id.checkout_button);
        itemTable = (TableLayout)findViewById(R.id.item_table);
        balanceListing = (TextView)findViewById(R.id.balance_listing);
        totalListing = (TextView)findViewById(R.id.total_listing);
        balanceInput = (EditText)findViewById(R.id.balance_input);
        micButton = (Button)findViewById(R.id.mic_button);
        scanButton.setOnClickListener(this);
        balanceButton.setOnClickListener(this);
        checkoutButton.setOnClickListener(this);
        micButton.setOnClickListener(this);

        counter = 0;
        total = 0;
        links = new HashMap<Integer, String>();
        stocks = new HashMap<String, TextView>();
        Intent intent = getIntent();
        userName = intent.getStringExtra("test");
//        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
//        toolbar.setTitle("Hello," + userName);
//        setSupportActionBar(toolbar);
//        getSupportActionBar().setTitle("Hello," + userName);
        new GetUserInfo().execute(userName, "", "");


    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.scan_button){
            IntentIntegrator scanIntegrator = new IntentIntegrator(this);
            scanIntegrator.initiateScan();
        }
        if (v.getId() == R.id.checkout_button){
            if (Double.parseDouble(balanceListing.getText().toString()) < total){
                Toast toast = Toast.makeText(getApplicationContext(),
                        "Insufficient funds", Toast.LENGTH_SHORT);
                toast.show();
            }
            else {
                new Checkout().execute(userName, "", "");
            }
        }
        if (v.getId() == R.id.balance_button){
            new AddBalance().execute(userName, "", "");
        }
        if (v.getId() == R.id.mic_button){

            promptSpeechInput();
        }

        if (links.get(v.getId()) != null){
            try {
                showDetails(links.get(v.getId()) + ";" + userName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == REQ_CODE_SPEECH_INPUT){
            if (resultCode == RESULT_OK && null != intent) {
                ArrayList<String> result = intent.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                String target = result.get(0);
                if (target.equals("checkout") || target.equals("check out")){
                    if (Double.parseDouble(balanceListing.getText().toString()) < total){
                        Toast toast = Toast.makeText(getApplicationContext(),
                                "Insufficient funds", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                    else {
                        new Checkout().execute(userName, "", "");
                    }
                }
                else if (target.equals("scan")){
                    IntentIntegrator scanIntegrator = new IntentIntegrator(this);
                    scanIntegrator.initiateScan();
                }
                else{
                    Toast toast = Toast.makeText(getApplicationContext(),
                            "Command: \"" + target + "\" not found.", Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
            return;
        }
        IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (scanningResult != null) {
            String scanContent = scanningResult.getContents();
            String scanFormat = scanningResult.getFormatName();
            //new RequestCode().execute(scanContent, "", "");
            while (scanContent.length() < 14){
                scanContent = '0' + scanContent;
            }
            try {
                showDetails(scanContent + ";" + userName);
            } catch (IOException e) {
                e.printStackTrace();
            }


        }
        else {
            Toast toast = Toast.makeText(getApplicationContext(),
                    "No scan data received!", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                getString(R.string.speech_prompt));
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.speech_not_supported),
                    Toast.LENGTH_SHORT).show();
        }
    }





    public void showDetails(String input) throws IOException {
        Intent intent = new Intent(this, Details.class);
        intent.putExtra(EXTRA_MESSAGE, input);

        startActivity(intent);
    }

    private TableRow makeRow(){
        TableRow newRow = new TableRow(this);
        itemTable.addView(newRow);
        return newRow;
    }
    private TextView addTextView(String s){
        TextView newText = new TextView(this);
        newText.setText(s);
        newText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        newText.setMaxWidth(600);
        newText.setOnClickListener(this);
        newText.setId(counter);
        counter++;
        return newText;
    }





    class GetUserInfo extends AsyncTask<String, String, String> {
        protected String doInBackground(String... inputs) {
            String output = "";
            String name = inputs[0];
            String url = "https://storeproject-209402.appspot.com/users/" + name;
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
                return;
            }
            String name = "";
            try {//modify
                JSONObject result = new JSONObject(output);
                name = result.getString("name");
                balanceListing.setText(String.format("%.2f", result.getDouble("balance")));
                if (!result.isNull("cart")) {
                    JSONArray cart = result.getJSONArray("cart");
                    for (int i = 0; i < cart.length(); i++) {
                        new GetNameAndPrice().execute(cart.getString(i), "", "");
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    class GetNameAndPrice extends AsyncTask<String, String, String> {
        protected String doInBackground(String... inputs) {
            String output = "";
            String code = inputs[0];
            String url = "https://storeproject-209402.appspot.com/products/" + code;
            String result = "";
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
                JSONObject jsonResult = new JSONObject(output);
                result += jsonResult.getString("name") + ";";
            } catch (JSONException e) {
                e.printStackTrace();
            }
            url = "https://storeproject-209402.appspot.com/prices/" + code;
            output = "";
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
                JSONObject jsonResult = new JSONObject(output);
                result += jsonResult.getString("price");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return result + ";" + code;

        }

        protected void onPostExecute(String output) {

            if (output.equals("404 not found")){
                Toast toast = Toast.makeText(getApplicationContext(),
                        "Invalid", Toast.LENGTH_SHORT);
                toast.show();
                return;
            }
            String[] split = output.split(";");
            String name = split[0];
            String price = split[1];
            String barcode = split[2];
            if (stocks.containsKey(name)){
                TextView targetView = stocks.get(name);
                int targetInt = 1 + Integer.parseInt(targetView.getText().toString());
                targetView.setText(Integer.toString(targetInt));
            }
            else {
                TableRow newRow = makeRow();
                TextView nameView = addTextView(name);
                nameView.setClickable(true);
                links.put(nameView.getId(), barcode);
                newRow.addView(nameView);
                newRow.addView(addTextView(""));
                TextView newStock = addTextView("1");
                newRow.addView(newStock);
                stocks.put(name, newStock);
                newRow.addView(addTextView(""));
                newRow.addView(addTextView(price));

            }
            total += Double.parseDouble(price);
            totalListing.setText(String.format("%.2f", total));
        }
    }



    class Checkout extends AsyncTask<String, String, String> {
        protected String doInBackground(String... inputs) {
            String output = "";
            double newBalance = Double.parseDouble(balanceListing.getText().toString()) - total;
            String url = "https://storeproject-209402.appspot.com/users/" + userName + "?balance=" + String.format("%.2f", newBalance);
            try {
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


            return output;

        }

        protected void onPostExecute(String output) {
            if (output.equals("404 not found")){
                Toast toast = Toast.makeText(getApplicationContext(),
                        "Invalid", Toast.LENGTH_SHORT);
                toast.show();
                return;
            }

            try {//modify
                JSONObject result = new JSONObject(output);
                double newBalance = result.getDouble("balance");
                balanceListing.setText(String.format("%.2f", newBalance));
                total = 0;
                totalListing.setText("0");
                stocks = new HashMap<String, TextView>();
                int childCount = itemTable.getChildCount();
                if (childCount > 2){
                    itemTable.removeViews(2, childCount-2);
                }
                Toast toast = Toast.makeText(getApplicationContext(),
                        "Thank you for shopping!", Toast.LENGTH_SHORT);
                toast.show();
                return;
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    class AddBalance extends AsyncTask<String, String, String> {
        protected String doInBackground(String... inputs) {
            String output = "";
            String url = "https://storeproject-209402.appspot.com/users/" + userName;

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
                double newBalance = Double.parseDouble(balance) + Double.parseDouble(balanceInput.getText().toString());
                url += "?balance=" + newBalance;
                if (!result.isNull("cart")) {
                    JSONArray cart = result.getJSONArray("cart");
                    for (int i = 0; i < cart.length(); i++) {
                        String targetCode = cart.getString(i);
                        url += "&cart=" + targetCode;

                    }
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


            return output;

        }

        protected void onPostExecute(String output) {
            if (output.equals("404 not found")){
                Toast toast = Toast.makeText(getApplicationContext(),
                        "Invalid", Toast.LENGTH_SHORT);
                toast.show();
                return;
            }

            try {
                JSONObject result = new JSONObject(output);
                balanceListing.setText(String.format("%.2f", result.getDouble("balance")));
                Toast toast = Toast.makeText(getApplicationContext(),
                        "Balance added", Toast.LENGTH_SHORT);
                toast.show();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }



/*
    class RequestCode extends AsyncTask<String, String, String> {
        protected String doInBackground(String... inputs) {
            String output = "";
            String code = inputs[0];
            String key = "3k4y8z2qyslfnlpl2ul2ro6encrrrp";
            String url = "https://api.barcodelookup.com/v2/products?barcode=" + code + "&formatted=n&key=" + key;
            //String url = "https://www.googleapis.com/customsearch/v1?key=AIzaSyAr-AK5Maj7MlJEoQkt_XiNF891qW2bS0Y&cx=005773736382830971489:6njleywqa3i&q=equate_moisturizing_lotion";

            try {
                URL obj = new URL(url);
                HttpURLConnection con = (HttpURLConnection)obj.openConnection();
                con.setRequestMethod("GET");
                //con.setDoInput(true);
                con.setDoOutput(true);
                con.setConnectTimeout(5000);
                con.setReadTimeout(5000);
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
            if (output.length() == 0){
                contentTxt.setText(output);
                return;
            }
            String details = "Not found";
            String name = "";
            try {
                JSONObject result = new JSONObject(output);
                JSONArray products = result.getJSONArray("products");
                JSONObject target = products.getJSONObject(0);
                name = target.getString("product_name");
                if (target.getString("product_name").length() == 0){
                    name = target.getString("title");
                }
                details = name + "\n\n";
                String description = target.getString("description");
                details += description + "\n\n";
                JSONArray stores = target.getJSONArray("stores");
                details += "Stores:\n";
                for (int i = 0; i < stores.length(); i++){
                    JSONObject targetStore = stores.getJSONObject(i);
                    details += "   " + targetStore.getString("store_name") + " " +
                            targetStore.getString("currency_symbol") + targetStore.getString("store_price")
                            + ", " + targetStore.getString("product_url") + "\n";
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            contentTxt.setText(details);
            try {
                showDetails(details);
                //showDetails("Equate daily moisturizing lotion, 18 oz\n other stuff");
            } catch (Exception e){
                contentTxt.setText(e.toString());
            }





        }
    }
*/



}
