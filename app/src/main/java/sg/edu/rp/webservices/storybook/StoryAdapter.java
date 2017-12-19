package sg.edu.rp.webservices.storybook;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by 15035648 on 13/12/2017.
 */

class StoryAdapter extends BaseAdapter {
    Context context;
    protected ArrayList<Story> storyArr;
    LayoutInflater inflater;

    public StoryAdapter(Context context, ArrayList<Story> storyArr) {
        this.storyArr = storyArr;
        this.inflater = LayoutInflater.from(context);
        this.context = context;
    }

    @Override
    public int getCount() {
        return storyArr.size();
    }

    @Override
    public Story getItem(int position) {
        return storyArr.get(position);
    }

    @Override
    public long getItemId(int position) {
        return storyArr.get(position).getStoryId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View grid;
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {

            grid = new View(context);
            grid = inflater.inflate(R.layout.grid_single, null);
            TextView textView = (TextView) grid.findViewById(R.id.grid_text);
            ImageView imageView = (ImageView) grid.findViewById(R.id.grid_image);
            Picasso.with(context).load("http://10.0.2.2/StoriesAndMusic_AdminSite/"+storyArr.get(position).getImage_path()).resize(400,650).into(imageView);
            String storyName = storyArr.get(position).getStoryName();
            if(storyName.length()> 20){
              String newStoryName =  storyName.substring(0,17);
                textView.setText(newStoryName+"...");

            }else{
                textView.setText(storyName);
            }

        } else {
            grid = (View) convertView;
        }

        return grid;
    }
}
