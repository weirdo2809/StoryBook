package sg.edu.rp.webservices.storybook;

/**
 * Created by 15035648 on 19/12/2017.
 */

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class CustomPageAdapter extends PagerAdapter {
    private Context context;
    private ArrayList<Page> pageList;
    private LayoutInflater layoutInflater;

    public CustomPageAdapter(Context context, ArrayList<Page> dataObjectList){
        this.context = context;
        this.layoutInflater = (LayoutInflater)this.context.getSystemService(this.context.LAYOUT_INFLATER_SERVICE);
        this.pageList = dataObjectList;
    }
    @Override
    public int getCount() {
        return pageList.size();
    }
    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == ((View)object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Log.i("pager adap pos","pager adapter pos: " + position);
        View view = this.layoutInflater.inflate(R.layout.page_layout, container, false);
        TextView etText = view. findViewById(R.id.tvText);
        etText.setText(pageList.get(position).getStory_text());
        container.addView(view);
        return view;
    }
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

}