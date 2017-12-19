package sg.edu.rp.webservices.storybook;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.*;

import cz.msebera.android.httpclient.Header;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    ArrayList<Story> storyArr = new ArrayList<Story>();
    SwipeRefreshLayout refreshStoryGrid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        refreshStoryGrid = (SwipeRefreshLayout) findViewById(R.id.refreshStoryGrid);
        retrievestories();
        ActionBar ab = getSupportActionBar();
        ab.setTitle("List of stories");


        refreshStoryGrid.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                retrievestories();

            }
        });
    }

    private void populateGridView(ArrayList<Story> story) {
       TextView tvEmpty = (TextView)findViewById(R.id.tvEmpty);
        if(story.isEmpty()){
            tvEmpty.setVisibility(View.VISIBLE);
        }else{
            tvEmpty.setVisibility(View.GONE);
        }
        GridView gridview = (GridView) findViewById(R.id.gvStories);
        StoryAdapter adapter = new StoryAdapter(MainActivity.this, story);
        gridview.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        refreshStoryGrid.setRefreshing(false);
        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                Intent i = new Intent(MainActivity.this, StoryDescription.class);
                i.putExtra("story_id", storyArr.get(position).getStoryId());
                SavePreferences("story_id",storyArr.get(position).getStoryId(),"Integer");
                startActivity(i);
            }
        });
    }

    private void retrievestories() {
        if(checkConnected()){
            refreshStoryGrid.setRefreshing(true);
            AsyncHttpClient client = new AsyncHttpClient();
            RequestParams params = new RequestParams();
            client.get(getResources().getString(R.string.domain) + "getStories.php", params, new TextHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, String res) {
                            // called when response HTTP status is "200 OK"
                            Log.e("Results GetStories()", res);
                            JSONArray jsonArray = null;
                            storyArr.clear();
                            try {
                                jsonArray = new JSONArray(res);
                                if (jsonArray.length() == 0) {
                                    Toast.makeText(MainActivity.this, "There is no stories available currently", Toast.LENGTH_SHORT).show();
                                }
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jObj = jsonArray.getJSONObject(i);
                                    Log.e("story ID", jObj.getString("story_id"));
                                    Log.e("story name", jObj.getString("name"));
                                    Story story = new Story(jObj.getString("name"), Integer.parseInt(jObj.getString("story_id")), jObj.getString("description"), jObj.getString("image_path"));
                                    storyArr.add(story);
                                }
                                populateGridView(storyArr);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, String res, Throwable t) {
                            // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                            Log.e("onFailure(HTTPCALL)", "Failed to retrieve results: " + res);
                            refreshStoryGrid.setRefreshing(false);
                        }
                    }
            );
        }else{
            refreshStoryGrid.setRefreshing(false);
        }

    }

    public Boolean checkConnected() {
        ConnectivityManager conMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (conMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED
                || conMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
            return true;
        } else {
            Toast.makeText(this, "Check your connection", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    private void SavePreferences(String key, Object value, String type) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (type.equalsIgnoreCase("String")) {
            editor.putString(key, String.valueOf(value));
        } else if (type.equalsIgnoreCase("Integer")) {
            editor.putInt(key, Integer.parseInt(String.valueOf(value)));
        } else if (type.equals("Boolean")) {
            editor.putBoolean(key, Boolean.parseBoolean(String.valueOf(value)));
        } else if (type.equals("Double")) {
            editor.putFloat(key, Float.parseFloat(String.valueOf(value)));
        }
        editor.commit();
    }

}
