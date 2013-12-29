package com.antsapps.glasscricket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import com.google.android.glass.app.Card;
import com.google.android.glass.widget.CardScrollAdapter;
import com.google.android.glass.widget.CardScrollView;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class MainActivity extends Activity implements AdapterView.OnItemClickListener {

  private CardScrollView mCardScrollView;
  private JSONArray mMatches;
  private ExampleCardScrollAdapter cardScrollAdapter;

  private class RequestTask extends AsyncTask<String, String, JSONArray>{

    @Override
    protected JSONArray doInBackground(String... uri) {
      HttpClient httpclient = new DefaultHttpClient();
      Log.i("MainActivity", "doInBackground0");
      try {
        BufferedReader reader = new BufferedReader(new InputStreamReader(httpclient.execute(new HttpGet(uri[0]))
            .getEntity().getContent(), "UTF-8"));
        Log.i("MainActivity", "doInBackground1");
        StringBuilder builder = new StringBuilder();
        for (String line = null; (line = reader.readLine()) != null;) {
          builder.append(line).append("\n");
        }
        Log.i("MainActivity", "doInBackground2");
        JSONTokener tokener = new JSONTokener(builder.toString());
        JSONArray finalResult = new JSONArray(tokener);
        Log.i("MainActivity", "doInBackground3");
        return finalResult;
      } catch (IOException e) {
        throw new RuntimeException(e);
      } catch (JSONException e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    protected void onPostExecute(JSONArray jsonArray) {
      mMatches = jsonArray;
      cardScrollAdapter.notifyDataSetChanged();
      mCardScrollView.updateViews(true);
      mCardScrollView.activate();
      setContentView(mCardScrollView);
      Log.i("MainActivity", "onPostExecute");
    }
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Log.i("MainActivity", "onCreate");

    new RequestTask().execute("http://cricscore-api.appspot.com/csa");

    mCardScrollView = new CardScrollView(this);
    cardScrollAdapter = new ExampleCardScrollAdapter();
    mCardScrollView.setAdapter(cardScrollAdapter);

    mCardScrollView.setOnItemClickListener(this);

    TextView loadingTextView = new TextView(this);
    loadingTextView.setText("Loading...");
    setContentView(loadingTextView);
  }

  @Override
  public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    JSONObject match = (JSONObject) mCardScrollView.getItemAtPosition(position);
    if (match == null) {
      return;
    }
    Log.i("Main", "onItemClick");
    Intent matchInfo = new Intent();
    matchInfo.setClass(MainActivity.this, MatchActivity.class);
    try {
      matchInfo.putExtra("id", match.getString("id"));
    } catch (JSONException e) {
      throw new RuntimeException(e);
    }
    startActivity(matchInfo);
  }

  private class ExampleCardScrollAdapter extends CardScrollAdapter {
    @Override
    public int findIdPosition(Object id) {
      return -1;
    }

    @Override
    public int findItemPosition(Object item) {
      return 0;
    }

    @Override
    public int getCount() {
      return mMatches == null ? 0 : mMatches.length();
    }

    @Override
    public JSONObject getItem(int position) {
      try {
        return mMatches.getJSONObject(position);
      } catch (JSONException e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      return toView(getItem(position));
    }

    private View toView(final JSONObject match) {
      Card card = new Card(MainActivity.this);
      try {
        card.setText(String.format("%s vs. %s", match.getString("t1"), match.getString("t2")));
        return card.toView();
      } catch (JSONException e) {
        throw new RuntimeException(e);
      }
    }
  }
}
