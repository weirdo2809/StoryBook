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
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;

import cz.msebera.android.httpclient.Header;

public class listOfPages extends AppCompatActivity {
    ListView lvPages;
    SwipeRefreshLayout refreshPageList;
    ArrayList<Page> pageArr = new ArrayList<Page>();
    int chapter_id;
    int chapter_num;
    String chapter_name;
    TextView tvChapterName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_of_pages);
        lvPages = (ListView) findViewById(R.id.lvPages);
        refreshPageList = (SwipeRefreshLayout) findViewById(R.id.refreshPageList);
        tvChapterName = (TextView) findViewById(R.id.tvChapterName);
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        int chpID = pref.getInt("chapter_id",999999);
        int chpNum = pref.getInt("chapter_num",999999);
        String chpName = pref.getString("chapter_name","null");

        Intent i = getIntent();
        chapter_id = i.getIntExtra("chapter_id", chpID);
        chapter_name = i.getStringExtra("chapter_name");
        chapter_num = i.getIntExtra("chapter_num",chpNum);
        if(chapter_name==null){
            chapter_name=chpName;
        }

        tvChapterName.setText("Chapter " + chapter_num + ": " + chapter_name);

        getPages();

        ActionBar ab = getSupportActionBar();
        ab.setTitle("List of pages");
        ab.setDisplayHomeAsUpEnabled(true);
        refreshPageList.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getPages();
            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();
        getPages();
    }

    private void getPages() {
        if (checkConnected()) {
            refreshPageList.setRefreshing(true);
            AsyncHttpClient client = new AsyncHttpClient();
            RequestParams params = new RequestParams();
            client.get(getResources().getString(R.string.domain) + "getPages.php?chapter_id=" + chapter_id, params, new TextHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, String res) {
                            // called when response HTTP status is "200 OK"
                            Log.e("Results GetPages()", res);
                            JSONArray jsonArray = null;
                            pageArr.clear();
                            try {
                                jsonArray = new JSONArray(res);
                                if (jsonArray.length() == 0) {
                                    Toast.makeText(listOfPages.this, "No pages are written yet for this chapter", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(listOfPages.this, jsonArray.length() + " pages available", Toast.LENGTH_SHORT).show();
                                }
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jObj = jsonArray.getJSONObject(i);
                                    Log.e("Story ID", jObj.getString("story_id"));
                                    Log.e("Chapter ID", jObj.getString("chapter_id"));
                                    Log.e("Page ID", jObj.getString("page_id"));
                                    Log.e("Page Number", jObj.getString("page_num"));
                                    Log.e("Page Text", jObj.getString("story_text"));
                                    Log.e("File Path", jObj.getString("file_path"));
                                    Page page = new Page(Integer.parseInt(jObj.getString("story_id")), Integer.parseInt(jObj.getString("chapter_id")), Integer.parseInt(jObj.getString("page_id")), Integer.parseInt(jObj.getString("page_num")), jObj.getString("story_text"), jObj.getString("file_path"));
                                    pageArr.add(page);
                                }
                                populatePageList(pageArr);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, String res, Throwable t) {
                            // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                            Log.e("onFailure(HTTPCALL)", "Failed to retrieve results: " + res);
                            refreshPageList.setRefreshing(false);
                        }
                    }
            );
        } else {
            refreshPageList.setRefreshing(false);
        }
    }

    private void populatePageList(final ArrayList<Page> page) {
        final ArrayList<String> arrPage = new ArrayList<String>();
        for (int i = 0; i < page.size(); i++) {
            arrPage.add("Page " + page.get(i).getPage_num());
        }
        Collections.sort(arrPage.subList(0, arrPage.size()));
        ArrayAdapter<String> aaPages = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, arrPage);
        lvPages.setAdapter(aaPages);
        aaPages.notifyDataSetChanged();
        refreshPageList.setRefreshing(false);
        lvPages.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(listOfPages.this, pageAct.class);
                for (int k = 0; k < arrPage.size(); k++) {
                    if (page.get(k).getPage_num() == i + 1) {
                        intent.putExtra("selectedPage", page.get(k));
                        startActivity(intent);
                        break;
                    }
                }


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
