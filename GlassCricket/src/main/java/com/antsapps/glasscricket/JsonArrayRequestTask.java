package com.antsapps.glasscricket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.os.AsyncTask;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONTokener;

/**
 * Created by anthony on 29/12/13.
 */
public class JsonArrayRequestTask extends AsyncTask<String, String, JSONArray> {

  public interface OnResponseListener {
    public void onResponseReceived(JSONArray response);
  }

  private final OnResponseListener mListener;

  JsonArrayRequestTask(OnResponseListener listener) {
    mListener = listener;
  }

  @Override
  protected JSONArray doInBackground(String... uri) {
    HttpClient httpclient = new DefaultHttpClient();
    try {
      InputStream in = httpclient.execute(new HttpGet(uri[0])).getEntity().getContent();
      BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
      StringBuilder builder = new StringBuilder();
      for (String line = null; (line = reader.readLine()) != null; ) {
        builder.append(line).append("\n");
      }
      JSONTokener tokener = new JSONTokener(builder.toString());
      JSONArray finalResult = new JSONArray(tokener);
      return finalResult;
    } catch (IOException e) {
      throw new RuntimeException(e);
    } catch (JSONException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  protected void onPostExecute(JSONArray jsonArray) {
    mListener.onResponseReceived(jsonArray);
  }
}
