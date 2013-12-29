package com.antsapps.glasscricket;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import com.google.android.glass.app.Card;
import com.google.android.glass.widget.CardScrollAdapter;
import com.google.android.glass.widget.CardScrollView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends Activity implements AdapterView.OnItemClickListener,
    JsonArrayRequestTask.OnResponseListener {

  public static final String MATCH_LIST_URL = "http://cricscore-api.appspot.com/csa";

  private CardScrollView mCardScrollView;
  private JSONArray mMatches;
  private MatchesCardScrollAdapter mCardScrollAdapter;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    new JsonArrayRequestTask(this).execute(MATCH_LIST_URL);

    mCardScrollView = new CardScrollView(this);
    mCardScrollAdapter = new MatchesCardScrollAdapter();
    mCardScrollView.setAdapter(mCardScrollAdapter);

    mCardScrollView.setOnItemClickListener(this);

    TextView loadingTextView = new TextView(this);
    loadingTextView.setGravity(Gravity.CENTER);
    loadingTextView.setText("Loading...");
    setContentView(loadingTextView);
  }

  @Override
  public void onResponseReceived(JSONArray response) {
    mMatches = response;
    mCardScrollAdapter.notifyDataSetChanged();
    mCardScrollView.updateViews(true);
    mCardScrollView.activate();
    setContentView(mCardScrollView);
  }

  @Override
  public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    JSONObject match = (JSONObject) mCardScrollView.getItemAtPosition(position);
    if (match == null) {
      return;
    }
    Intent matchInfo = new Intent();
    matchInfo.setClass(MainActivity.this, MatchActivity.class);
    try {
      matchInfo.putExtra("id", match.getString("id"));
    } catch (JSONException e) {
      throw new RuntimeException(e);
    }
    startActivity(matchInfo);
  }

  private class MatchesCardScrollAdapter extends CardScrollAdapter {
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
