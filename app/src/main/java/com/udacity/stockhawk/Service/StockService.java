package com.udacity.stockhawk.Service;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;

import static com.udacity.stockhawk.R.id.symbol;

/**
 * Created by hongtao on 08/12/2016.
 */

public class StockService extends IntentService{

    private final String BASE_URL = "https://query.yahooapis.com/v1/public/yql?q=";
    private final String LOG_TAG = StockService.class.getSimpleName();
    public static final String URL_EXTRA = "url";

    public StockService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        String url = intent.getStringExtra(URL_EXTRA);

        try {
            JSONObject jsonObject = new JSONObject(makeHttpRequest(createUrl(url)));
            JSONObject queryObject = jsonObject.getJSONObject("query");
            JSONObject resultsObject = queryObject.getJSONObject("results");
            JSONArray quote = resultsObject.getJSONArray("quote");
            for(int i = 0 ; i< quote.length(); i++){
                JSONObject result = quote.getJSONObject(i);
                String symbol = result.getString("symbol");
                String bidPrice = result.getString("Bid");
                String change = result.getString("Change");
                String percentageChange = result.getString("Change_PercentChange");

            }


        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private static URL createUrl(String stringUrl) {
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException e) {
            Log.e("service log", "Error with creating URL ", e);
        }
        return url;
    }

    private static String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = "";

        // If the URL is null, then return early.
        if (url == null) {
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000 /* milliseconds */);
            urlConnection.setConnectTimeout(15000 /* milliseconds */);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // If the request was successful (response code 200),
            // then read the input stream and parse the response.
            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e("service log", "Error response code: " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e("log", "Problem retrieving the movie JSON results.", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }
}
