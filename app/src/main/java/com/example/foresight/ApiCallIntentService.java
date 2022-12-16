package com.example.foresight;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;


public class ApiCallIntentService extends IntentService {

    private static String apiUrl= "https://ypmlcecnxikzxlrblkgr.supabase.co/";

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public ApiCallIntentService(){
        super("ApiCallIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        //Get the URL from the intent
        String endPoint = intent.getStringExtra("apiEndpoint");
        String requestType = intent.getStringExtra("requestType");
        String params = intent.getStringExtra("params");

        try {
            URL url = new URL(apiUrl+endPoint);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod(requestType);
            conn.setRequestProperty("apikey", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InlwbWxjZWNueGlrenhscmJsa2dyIiwicm9sZSI6InNlcnZpY2Vfcm9sZSIsImlhdCI6MTY2OTcyNzAyOSwiZXhwIjoxOTg1MzAzMDI5fQ.wmNk4Vq54W4wBGN74C-DUM-2mJ_td6MQxrQrSzIm19g");
            conn.setRequestProperty("Authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InlwbWxjZWNueGlrenhscmJsa2dyIiwicm9sZSI6InNlcnZpY2Vfcm9sZSIsImlhdCI6MTY2OTcyNzAyOSwiZXhwIjoxOTg1MzAzMDI5fQ.wmNk4Vq54W4wBGN74C-DUM-2mJ_td6MQxrQrSzIm19g");

            if (requestType.equals("POST")) {
                conn.setDoOutput(true);

                // Set Request Body
                try(OutputStream os = conn.getOutputStream()) {
                    byte[] input = params.getBytes("utf-8");
                    os.write(input, 0, input.length);
                }
            }

            // Read the response
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            Intent broadcastIntent = new Intent();
            broadcastIntent.setAction("com.example.ACTION_API_RESPONSE");
            broadcastIntent.putExtra("response", response.toString());
            sendBroadcast(broadcastIntent);
        }
        catch (Exception e) {
            Log.e("IntentService", e.getMessage());
        }
    }
}