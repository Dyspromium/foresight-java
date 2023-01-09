package com.example.foresight.service;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;


public class ApiCallIntentService extends IntentService {

    private static final String apiUrl= "https://ypmlcecnxikzxlrblkgr.supabase.co/";

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public ApiCallIntentService(){
        super("ApiCallIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        //Recuperation des données necessaire a l'envoie de la requete HTTP
        String endPoint = intent.getStringExtra("apiEndpoint");
        String requestType = intent.getStringExtra("requestType");
        String params = intent.getStringExtra("params");


        //Si erreur dans ce code -> impossible de contacter la base de donnée -> error 500
        try {
            URL url = new URL(apiUrl+endPoint);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod(requestType);
            conn.setRequestProperty("apikey", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InlwbWxjZWNueGlrenhscmJsa2dyIiwicm9sZSI6InNlcnZpY2Vfcm9sZSIsImlhdCI6MTY2OTcyNzAyOSwiZXhwIjoxOTg1MzAzMDI5fQ.wmNk4Vq54W4wBGN74C-DUM-2mJ_td6MQxrQrSzIm19g");
            conn.setRequestProperty("Authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InlwbWxjZWNueGlrenhscmJsa2dyIiwicm9sZSI6InNlcnZpY2Vfcm9sZSIsImlhdCI6MTY2OTcyNzAyOSwiZXhwIjoxOTg1MzAzMDI5fQ.wmNk4Vq54W4wBGN74C-DUM-2mJ_td6MQxrQrSzIm19g");

            if (requestType.equals("POST")) {
                conn.setRequestProperty("Content-Type", "application/json");

                conn.setDoOutput(true);
                //Ajout des parametres à la requete
                try(OutputStreamWriter os = new OutputStreamWriter(conn.getOutputStream())) {
                    os.write(params);
                }
            }

            try{
                //Lecture de la reponse
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(conn.getInputStream()));
                String inputLine;

                StringBuffer response = new StringBuffer();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                //Envoie d'un signal contenant la reponse
                Intent broadcastIntent = new Intent();
                broadcastIntent.setAction("ACTION_API_RESPONSE");
                broadcastIntent.putExtra("response", response.toString());
                broadcastIntent.putExtra("code", "200");
                sendBroadcast(broadcastIntent);

            }catch(Exception e){

                BufferedReader in = new BufferedReader(
                        new InputStreamReader(conn.getErrorStream()));
                String inputLine;

                StringBuffer error = new StringBuffer();
                while ((inputLine = in.readLine()) != null) {
                    error.append(inputLine);
                }
                in.close();

                Intent broadcastIntent = new Intent();
                broadcastIntent.setAction("ACTION_API_RESPONSE");
                broadcastIntent.putExtra("response", error.toString());


                broadcastIntent.putExtra("code", "400");
                sendBroadcast(broadcastIntent);
            }
        }

        catch (Exception e) {
            //Envoie d'un signal contenant la reponse
            Intent broadcastIntent = new Intent();
            broadcastIntent.setAction("ACTION_API_RESPONSE");
            broadcastIntent.putExtra("code", "500");
            sendBroadcast(broadcastIntent);
        }
    }
}