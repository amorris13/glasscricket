package com.antsapps.glasscricket;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.glass.app.Card;
import com.google.android.glass.widget.CardScrollAdapter;
import com.google.android.glass.widget.CardScrollView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MatchActivity extends Activity implements JsonArrayRequestTask.OnResponseListener {

  public static final String MATCH_DETAILS_URL = "http://cricscore-api.appspot.com/csa?id=%s";

  private CardScrollView mCardScrollView;
  private JSONArray mMatches;
  private ExampleCardScrollAdapter mCardScrollAdapter;
  private String mId;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    mId = getIntent().getStringExtra("id");

    new JsonArrayRequestTask(this).execute(String.format(MATCH_DETAILS_URL, mId));

    mCardScrollView = new CardScrollView(this);
    mCardScrollAdapter = new ExampleCardScrollAdapter();
    mCardScrollView.setAdapter(mCardScrollAdapter);

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
