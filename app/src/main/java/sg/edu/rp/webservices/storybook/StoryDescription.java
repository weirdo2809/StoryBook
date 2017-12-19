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
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class StoryDescription extends AppCompatActivity {
    int storyID;
    TextView tvTitle, tvDesc;
    ImageView ivThumbnail;
    ArrayList<Chapter> chapterArr = new ArrayList<>();
    ActionBar ab;
    ListView lvChapters;
    SwipeRefreshLayout refreshChaptersList ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story_description);
        tvTitle = (TextView) findViewById(R.id.tvTitle);
        tvDesc = (TextView) findViewById(R.id.tvDesc);
        ivThumbnail = (ImageView) findViewById(R.id.ivThumbnail);
        lvChapters = (ListView) findViewById(R.id.lvChapters);
        refreshChaptersList = (SwipeRefreshLayout) findViewById(R.id.refreshChaptersList);

        ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setTitle("Story Description");


        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        int stryID = pref.getInt("story_id",999999);
        Intent i = getIntent();
        storyID = i.getIntExtra("story_id", stryID);

        retrievestory();
        retrieveChapters();
        refreshChaptersList.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                retrievestory();
                retrieveChapters();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        retrievestory();
        retrieveChapters();
    }

    private void retrievestory() {
        if(checkConnected()){
            AsyncHttpClient client = new AsyncHttpClient();
            RequestParams params = new RequestParams();
            client.get(getResources().getString(R.string.domain)+"getStories.php?story_id=" + storyID, params, new TextHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, String res) {
                            // called when response HTTP status is "200 OK"
                            Log.e("Results GetStory()", res);
                            JSONArray jsonArray = null;
                            try {
                                jsonArray = new JSONArray(res);
                                if(jsonArray.length()==0){
                                    Toast.makeText(StoryDescription.this, "No story retrieved with ID: "+storyID, Toast.LENGTH_SHORT).show();
                                }
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jObj = jsonArray.getJSONObject(i);
                                    Log.e("story ID", jObj.getString("story_id"));
                                    Log.e("story name", jObj.getString("name"));
                                    Story story = new Story(jObj.getString("name"), Integer.parseInt(jObj.getString("story_id")), jObj.getString("description"),jObj.getString("image_path"));
                                    tvTitle.setText(story.getStoryName());
                                    tvDesc.setText(story.getStoryDesc());
                                    ab.setTitle(story.getStoryName());
                                    Picasso.with(StoryDescription.this).load("http://10.0.2.2/StoriesAndMusic_AdminSite/"+story.getImage_path()).resize(400,750).into(ivThumbnail);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, String res, Throwable t) {
                            // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                            Log.e("onFailure(HTTPCALL)", "Failed to retrieve story with id : " + storyID + " with results:" + res);
                        }
                    }
            );
        }else{
            Toast.makeText(this, "Check your connection", Toast.LENGTH_SHORT).show();
        }

    }

    private void retrieveChapters() {
        if(checkConnected()){
            refreshChaptersList.setRefreshing(true);
            AsyncHttpClient client = new AsyncHttpClient();
            RequestParams params = new RequestParams();
            client.get(getResources().getString(R.string.domain)+"getChapters.php?story_id=" + storyID, params, new TextHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, String res) {
                            // called when response HTTP status is "200 OK"
                            Log.e("Results GetChapters()", res);
                            JSONArray jsonArray = null;
                            chapterArr.clear();
                            try {
                                jsonArray = new JSONArray(res);
                                if(jsonArray.length()==0){
                                    Toast.makeText(StoryDescription.this, "There are no chapters for this story currently", Toast.LENGTH_SHORT).show();
                                }
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jObj = jsonArray.getJSONObject(i);
                                    Log.e("story ID", jObj.getString("story_id"));
                                    Log.e("Chapter ID", jObj.getString("chapter_id"));
                                    Log.e("Chapter Number", jObj.getString("chapter_num"));
                                    Log.e("Chapter Name", jObj.getString("chapter_name"));
                                    Chapter chap = new Chapter(Integer.parseInt(jObj.getString("story_id")), Integer.parseInt(jObj.getString("chapter_id")), Integer.parseInt(jObj.getString("chapter_num")), jObj.getString("chapter_name"));
                                    chapterArr.add(chap);
                                }
                                populateListView(chapterArr);
                            } catch (JSONException e) {
                                e.printStackTrace();

                            }
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, String res, Throwable t) {
                            // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                            Log.e("onFailure(HTTPCALL)", "Failed to retrieve story with id : " + storyID + " with results:" + res);
                            refreshChaptersList.setRefreshing(false);
                        }
                    }
            );
        }else{
            refreshChaptersList.setRefreshing(false);

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

    private void populateListView(ArrayList<Chapter> chapter) {
        ArrayList<String> arrChapter = new ArrayList<String>();
        for (int i = 0; i < chapter.size(); i++) {
            int chapterNum = chapter.get(i).getChapter_num();
            String chapterName = chapter.get(i).getName();
            String chapterRow = chapterNum + ": " + chapterName;
            arrChapter.add(chapterRow);
        }
        ArrayAdapter<String> aaChapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, arrChapter);
        lvChapters.setAdapter(aaChapter);
        aaChapter.notifyDataSetChanged();
        refreshChaptersList.setRefreshing(false);
        lvChapters.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(StoryDescription.this, listOfPages.class);
                intent.putExtra("chapter_id",chapterArr.get(i).getChapter_id());
                intent.putExtra("chapter_name",chapterArr.get(i).getName());
                intent.putExtra("chapter_num",chapterArr.get(i).getChapter_num());

                SavePreferences("story_id", chapterArr.get(i).getStory_id(), "Integer");
                SavePreferences("chapter_id",chapterArr.get(i).getChapter_id(),"Integer");
                SavePreferences("chapter_name",chapterArr.get(i).getName(),"String");
                SavePreferences("chapter_num",chapterArr.get(i).getChapter_num(),"Integer");
                startActivity(intent);
            }
        });
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
}

