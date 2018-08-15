package com.example.derek.barcodereader;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class LoginActivity extends AppCompatActivity implements OnClickListener {

    private Button loginButton;
    private EditText usernameInput;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        loginButton = (Button)findViewById(R.id.login_button);
        usernameInput = (EditText)findViewById(R.id.user_input);
        loginButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.login_button) {
            String input = usernameInput.getText().toString();
            new LoginAttempt().execute(input, "", "");
        }
    }

    public void toMainScreen(String input) throws IOException {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("test", input);

        startActivity(intent);
    }

    class LoginAttempt extends AsyncTask<String, String, String> {
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
                //con.setDoOutput(true);
                //con.setConnectTimeout(5000);
                //con.setReadTimeout(5000);
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


            if (output.equals("404 not found")){
                url = "https://storeproject-209402.appspot.com/users?name=" + name + "&balance=0.00";
                output = "";
                try {
                    URL obj = new URL(url);
                    HttpURLConnection con = (HttpURLConnection)obj.openConnection();
                    con.setRequestMethod("POST");
                    con.setDoInput(true);
                    con.setDoOutput(true);
                    //con.setConnectTimeout(5000);
                    //con.setReadTimeout(5000);
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
            }



            return output;

        }

        protected void onPostExecute(String output) {
            if (output.equals("404 not found")){
                Toast toast = Toast.makeText(getApplicationContext(),
                        "Invalid username", Toast.LENGTH_SHORT);
                toast.show();
                return;
            }
            String name = "";
            try {//modify
                JSONObject result = new JSONObject(output);
                name = result.getString("name");
                toMainScreen(name);

            } catch (Exception e) {
                Toast toast = Toast.makeText(getApplicationContext(),
                        e.toString(), Toast.LENGTH_SHORT);
                toast.show();
                e.printStackTrace();
            }

        }
    }
}
