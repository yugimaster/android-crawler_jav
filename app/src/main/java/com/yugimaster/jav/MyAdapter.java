package com.yugimaster.jav;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MyAdapter extends BaseAdapter implements AbsListView.OnScrollListener {

    private List<ListItem> list;
    private Context context;
    private LayoutInflater inflater;

    private int ImgStart, ImgEnd;
    private ListView listView;
    private ImageLoader_scroll_better loader_scroll;
    // First loading pretreatment
    private int first_flag = 1;

    static List<String> URLS = new ArrayList<String>();

    private ImageLoader loader;

    public MyAdapter(Context context, List<ListItem>list, ListView listView) {
        this.context = context;
        this.list = list;
        inflater = LayoutInflater.from(context);
        loader = new ImageLoader();
        //初始化URLS列表
        for (int i=0; i<list.size(); i++) {
            URLS.add(list.get(i).img);
        }
        this.listView = listView;
        listView.setOnScrollListener(this);
        loader_scroll = new ImageLoader_scroll_better(listView);
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            // Need add false else error
            convertView = inflater.inflate(R.layout.activity_items, parent, false);
            // Also use null is available but not parent like below
            //convertView = inflater.inflate(R.layout.activity_items, null);
            holder.img = (ImageView)convertView.findViewById(R.id.icon);
            holder.title = (TextView)convertView.findViewById(R.id.title);
            holder.link = (TextView)convertView.findViewById(R.id.link);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder)convertView.getTag();
        }

        holder.title.setText(list.get(position).title);
        holder.link.setText(list.get(position).link);
        holder.img.setImageResource(R.drawable.ic_launcher);
        holder.img.setTag(list.get(position).img);

        return convertView;
    }

    class ViewHolder {
        ImageView img;
        TextView title, link;
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        // TODO Auto-generated method stub
        if (scrollState == SCROLL_STATE_IDLE) {
            // Normal (not scroll), start loading task
            loader_scroll.LoadImageByAsyncTask(ImgStart, ImgEnd);
        } else {
            // Stop task
            loader_scroll.cancleAllTask();
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                         int totalItemCount) {
        // TODO Auto-generated method stub
        ImgStart = firstVisibleItem;
        ImgEnd = ImgStart + visibleItemCount;

        if (first_flag == 1 && visibleItemCount > 0) {
            // First loading pretreatment
            loader_scroll.LoadImageByAsyncTask(ImgStart, ImgEnd);
        }
    }
}
