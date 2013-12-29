package com.antsapps.glasscricket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
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

public class MatchActivity extends Activity {

  private CardScrollView mCardScrollView;
  private JSONArray mMatches;
  private ExampleCardScrollAdapter cardScrollAdapter;
  private String mId;

  private class RequestTask extends AsyncTask<String, String, JSONArray>{

    @Override
    protected JSONArray doInBackground(String... uri) {
      HttpClient httpclient = new DefaultHttpClient();
      try {
        BufferedReader reader = new BufferedReader(new InputStreamReader(httpclient.execute(new HttpGet(uri[0]))
            .getEntity().getContent(), "UTF-8"));
        StringBuilder builder = new StringBuilder();
        for (String line = null; (line = reader.readLine()) != null;) {
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
      mMatches = jsonArray;
      cardScrollAdapter.notifyDataSetChanged();
      mCardScrollView.updateViews(true);
      mCardScrollView.activate();
      setContentView(mCardScrollView);
      Log.i("RT", "onPostExecute");
    }
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    mId = getIntent().getStringExtra("id");

    new RequestTask().execute(String.format("http://cricscore-api.appspot.com/csa?id=%s", mId));

    mCardScrollView = new CardScrollView(this);
    cardScrollAdapter = new ExampleCardScrollAdapter();
    mCardScrollView.setAdapter(cardScrollAdapter);

    TextView loadingTextView = new TextView(this);
    loadingTextView.setText("Loading...");
    setContentView(loadingTextView);
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

    private View toView(JSONObject match) {
      Card card = new Card(MatchActivity.this);
      try {
        card.setText(match.getString("de"));
        card.setFootnote(match.getString("si"));
      } catch (JSONException e) {
        throw new RuntimeException(e);
      }
      return card.toView();
    }
  }
}
