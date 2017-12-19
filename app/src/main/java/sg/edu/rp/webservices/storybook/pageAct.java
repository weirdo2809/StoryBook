package sg.edu.rp.webservices.storybook;

import android.content.Intent;
import android.media.MediaPlayer;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class pageAct extends AppCompatActivity {
    Menu menu;
    MediaPlayer mediaplayer;
    Page selectedPage;
    String url;
    SwipeRefreshLayout refreshPage;
    TextView etText;
    Page page;
    Button btnPrev, btnNext;
    ActionBar ab;
    int pageNumb;
    String storyText;
int numRows;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_page);
        Intent i = getIntent();

        ab = getSupportActionBar();
        btnPrev = (Button) findViewById(R.id.btnPrev);
        btnNext = (Button) findViewById(R.id.btnNext);
        etText = (TextView) findViewById(R.id.tvText);
        refreshPage = (SwipeRefreshLayout) findViewById(R.id.refreshPage);

        selectedPage = (Page) i.getSerializableExtra("selectedPage");
        storyText = selectedPage.getStory_text();
        etText.setText(storyText);
        pageNumb = selectedPage.getPage_num();
        ab.setTitle("Page Number: " + pageNumb);
        Toast.makeText(this, "Page Number: " + selectedPage.getPage_num(), Toast.LENGTH_SHORT).show();

        url =getResources().getString(R.string.domain) + selectedPage.getFile_path();
        playSong(url);


        refreshPage.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getPage(pageNumb, selectedPage.getChapter_id());

                getNumOfPages(selectedPage.getChapter_id());
            }
        });
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pageNumb = pageNumb + 1;
                Toast.makeText(pageAct.this, "Page num: " + pageNumb, Toast.LENGTH_SHORT).show();
                getPage(pageNumb, selectedPage.getChapter_id());
                getNumOfPages(selectedPage.getChapter_id());
            }
        });
        btnPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pageNumb = pageNumb - 1;
                Toast.makeText(pageAct.this, "Page num: " + pageNumb, Toast.LENGTH_SHORT).show();
                getPage(pageNumb, selectedPage.getChapter_id());
                getNumOfPages(selectedPage.getChapter_id());
            }
        });

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mediaplayer != null) {
            if (mediaplayer.isPlaying()) {
                mediaplayer.pause();
                mediaplayer.release();
                mediaplayer = null;
                MenuItem musicButton = menu.findItem(R.id.musicButton);
                musicButton.setIcon(R.drawable.play);
            }
        }
    }

    public void playSong(String url) {
        if (mediaplayer != null) {
            if (mediaplayer.isPlaying()) {
                mediaplayer.pause();
            } else {
                mediaplayer.start();
            }
        } else {
            try {
                if (url != "") {
                    mediaplayer = new MediaPlayer();
                    mediaplayer.setLooping(true);
                    mediaplayer.setDataSource(url); // setup song from https://www.hrupin.com/wp-content/uploads/mp3/testsong_20_sec.mp3 URL to mediaplayer data source
                    mediaplayer.prepare(); // you must call this method after setup the datasource in setDataSource method. After calling prepare() the instance of MediaPlayer starts load data from URL to internal buffer.
                    mediaplayer.start();
                    MenuItem musicButton = menu.findItem(R.id.musicButton);
                    musicButton.setIcon(R.drawable.pause);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            Toast.makeText(this, "playing song from: " + url, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.music_menu, menu);
        this.menu = menu;
        return super.onCreateOptionsMenu(menu);
    }

    // handle button activities
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.musicButton) {

            if (mediaplayer != null) {
                if (mediaplayer.isPlaying()) {
                    item.setIcon(R.drawable.play);
                    playSong(url);
                } else {
                    item.setIcon(R.drawable.pause);
                    playSong(url);
                }
            } else {
                playSong(url);
            }

        } else if (id == R.id.pageOne) {
            pageNumb = 1;
            getPage(pageNumb, selectedPage.getChapter_id());
            getNumOfPages(selectedPage.getChapter_id());
        }
        return super.onOptionsItemSelected(item);
    }

    private void getNumOfPages(int chapterNum) {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();

        //http://localhost/StoriesAndMusic_AdminSite/getPages.php?page_num=3&chapter_id=32
        client.get(getResources().getString(R.string.domain)+"getNumberOfPages.php?chapter_id=" + chapterNum, params, new TextHttpResponseHandler() {

            @Override
                    public void onSuccess(int statusCode, Header[] headers, String res) {
                        numRows = Integer.parseInt(res);
                if (pageNumb <= 1) {
                    btnPrev.setVisibility(View.INVISIBLE);
                } else if(pageNumb<numRows){
                    btnPrev.setVisibility(View.VISIBLE);
                    btnNext.setVisibility(View.VISIBLE);
                }else{
                    btnNext.setVisibility(View.INVISIBLE);
                }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String res, Throwable t) {
                        // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                        Log.e("onFailure(HTTPCALL)", "Failed to retrieve results: " + res);
                    }
                }
        );
    }

    private void getPage(final int pageNum, int chapterNum) {

        refreshPage.setRefreshing(true);
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        client.get(getResources().getString(R.string.domain)+"getPages.php?page_num=" + pageNum + "&chapter_id=" + chapterNum, params, new TextHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, String res) {
                        // called when response HTTP status is "200 OK"
                        Log.e("Results GetPage()", res);
                        JSONArray jsonArray = null;
                        try {
                            jsonArray = new JSONArray(res);
                            if (jsonArray.length() == 0) {
                                Toast.makeText(pageAct.this, "This page does not exist", Toast.LENGTH_SHORT).show();
                                refreshPage.setRefreshing(false);
                                if (mediaplayer != null) {
                                    mediaplayer.pause();
                                    mediaplayer.release();
                                    mediaplayer = null;
                                }
                                url = "";
                                ab.setTitle("NON EXISTANT PAGE");
                                etText.setText("");
                            } else {
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jObj = jsonArray.getJSONObject(i);
                                    Log.e("Story ID", jObj.getString("story_id"));
                                    Log.e("Chapter ID", jObj.getString("chapter_id"));
                                    Log.e("Page ID", jObj.getString("page_id"));
                                    Log.e("Page Number", jObj.getString("page_num"));
                                    Log.e("Page Text", jObj.getString("story_text"));
                                    Log.e("File Path", jObj.getString("file_path"));
                                    page = new Page(Integer.parseInt(jObj.getString("story_id")), Integer.parseInt(jObj.getString("chapter_id")), Integer.parseInt(jObj.getString("page_id")), Integer.parseInt(jObj.getString("page_num")), jObj.getString("story_text"), jObj.getString("file_path"));
                                }
                                populatePage(page);
                                pageNumb = page.getPage_num();
                                ab.setTitle("Page Number: " + page.getPage_num());
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String res, Throwable t) {
                        // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                        Log.e("onFailure(HTTPCALL)", "Failed to retrieve results: " + res);
                        refreshPage.setRefreshing(false);
                    }
                }
        );

    }

    public void populatePage(Page page) {
        refreshPage.setRefreshing(false);
        if (mediaplayer != null) {
            mediaplayer.pause();
            mediaplayer.release();
            mediaplayer = null;
        }
        playSong(getResources().getString(R.string.domain) + page.getFile_path());
        etText.setText(page.getStory_text());
    }

}
