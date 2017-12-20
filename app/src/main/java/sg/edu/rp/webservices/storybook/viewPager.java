package sg.edu.rp.webservices.storybook;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class viewPager extends AppCompatActivity {
    ViewPager viewpager;
    ArrayList<Page> pageArr;
    private Menu menu;
    String url;
    ActionBar ab;
    Page selectedPage;
    int selectedPosition;
    int pageNumb;
    MediaPlayer mediaplayer = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_pager);

        Intent i = getIntent();
        pageArr = new ArrayList<>();
        viewpager = (ViewPager) findViewById(R.id.viewpager);
        getPages();

        viewpager.setPageTransformer(false, new FlipPageViewTransformer());
        selectedPage = (Page) i.getSerializableExtra("selectedPage");
        selectedPosition = i.getIntExtra("position", 9999);
        pageNumb = selectedPage.getPage_num();

        ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setTitle("Page Number: " + (selectedPage.getPage_num()));

        url = getResources().getString(R.string.domain) + selectedPage.getFile_path();

        Log.i("pager pos", "pager pos: " + selectedPosition);
        //viewpager.setCurrentItem(selectedPosition);

        //viewpager.setCurrentItem(selectedPosition, false);
        Toast.makeText(this, "Current selected: " + viewpager.getCurrentItem(), Toast.LENGTH_SHORT).show();

        if (mediaplayer != null) {
            mediaplayer.stop();
            prepareSong(url);
        } else {
            mediaplayer = new MediaPlayer();
            prepareSong(url);
        }

        viewpager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                Toast.makeText(viewPager.this, "pageSelected", Toast.LENGTH_SHORT).show();
                ab.setTitle("Page Number: " + (pageArr.get(position).getPage_num()));
                url = getResources().getString(R.string.domain) + pageArr.get(position).getFile_path();
                prepareSong(url);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }

    public void prepareSong(final String url) {
        if (!url.equals("") ) {
            AsyncHttpClient client = new AsyncHttpClient();
            RequestParams params = new RequestParams();
            client.get(url, params, new TextHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, String res) {
                            try {
                                mediaplayer.reset();
                                mediaplayer.setLooping(true);
                                mediaplayer.setDataSource(url);
                                mediaplayer.prepare();
                                mediaplayer.start();
                                Toast.makeText(viewPager.this, "playing song from: " + url, Toast.LENGTH_SHORT).show();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            MenuItem musicButton = menu.findItem(R.id.musicButton);
                            musicButton.setIcon(R.drawable.pause);
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, String res, Throwable t) {
                            Toast.makeText(viewPager.this, "This page does not have a music file attached", Toast.LENGTH_SHORT).show();
                            mediaplayer.reset();
                            MenuItem musicButton = menu.findItem(R.id.musicButton);
                            musicButton.setIcon(R.drawable.play);
                        }
                    }
            );
        }


    }

    @Override
    protected void onResume() {
        super.onResume();

        viewpager.postDelayed(new Runnable() {
            @Override
            public void run() {
                viewpager.setCurrentItem(selectedPosition);
            }
        }, 50);
    }

    public void playSong() {

            if (mediaplayer.isPlaying()) {
                mediaplayer.pause();
            } else {
                mediaplayer.start();
            }

    }

    private void getPages() {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        client.get(getResources().getString(R.string.domain) + "getPages.php?chapter_id=32", params, new TextHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, String res) {
                        // called when response HTTP status is "200 OK"
                        Log.e("Results GetPages()", res);
                        if (pageArr != null) {
                            pageArr.clear();
                        }

                        try {
                            JSONArray jsonArray = new JSONArray(res);
                            if (jsonArray.length() !=0) {
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
                                populateViewPager(pageArr);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
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

    public void populateViewPager(ArrayList<Page> pageArr) {
        CustomPageAdapter mCustomPagerAdapter = new CustomPageAdapter(viewPager.this, pageArr);
        Log.i("pager size: ", "pager size: " + mCustomPagerAdapter.getCount());
        mCustomPagerAdapter.notifyDataSetChanged();
        viewpager.setAdapter(mCustomPagerAdapter);
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
            if (url.equals(getResources().getString(R.string.domain) + "music/-")) {
                item.setIcon(R.drawable.play);
            } else {
                if (mediaplayer.isPlaying()) {
                    item.setIcon(R.drawable.play);
                    playSong();
                } else {
                    item.setIcon(R.drawable.pause);
                    playSong();
                }
            }

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mediaplayer != null) {
            if (mediaplayer.isPlaying()) {
                mediaplayer.pause();
                mediaplayer.reset();
                MenuItem musicButton = menu.findItem(R.id.musicButton);
                musicButton.setIcon(R.drawable.play);
            }
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
