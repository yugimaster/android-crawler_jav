package com.yugimaster.jav;

import android.app.Activity;
import android.app.ProgressDialog;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MainActivity extends AppCompatActivity {

    private ProgressDialog dialog;
    private TextView mTitle;
    private String title;
    private ArrayList categoryArray = new ArrayList();
    private ListView infoListView;
    private GridView cateGridView;
//    private List<Map<String, Object>> list = new ArrayList<>();
    private List<ListItem> list;
    private CharSequence Title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Title = getTitle();
        // 显示列表信息的ListView
        infoListView = (ListView)findViewById(R.id.info_list_view);
        list = new ArrayList<ListItem>();

        if (isNetworkAvailable(MainActivity.this)) {
            Log.e("jav", "network is available");

            // 显示“正在加载”窗口
            dialog = new ProgressDialog(this);
            dialog.setMessage("正在抓取数据...");
            dialog.setCancelable(false);
            dialog.show();

            // 开辟一个线程
            new Thread(runnable).start();
        }
        else {
            // 弹出提示框
            new AlertDialog.Builder(this)
                    .setTitle("提示")
                    .setMessage("当前没有网络连接")
                    .setPositiveButton("重试", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Log.e("jav", "click");
                        }
                    }).setNegativeButton("退出", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    System.exit(0); // 退出程序
                }
            }).show();
        }
    }

    // 将数据填充到ListView中
    private void show() {
        if (list.isEmpty()) {
            TextView message = (TextView)findViewById(R.id.message);
            message.setText(R.string.message);
        } else {
//            SimpleAdapter adapter = new SimpleAdapter(this, list, R.layout.activity_items,
//                    new String[]{"title", "img", "link"},
//                    new int[]{R.id.title, R.id.icon, R.id.link});
//            infoListView.setAdapter(adapter);
            MyAdaper adaper = new MyAdaper(list);
            infoListView.setAdapter(adaper);
        }
        dialog.dismiss();
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            String url = "http://sherwoodbp.com";
            Connection conn = Jsoup.connect(url);
            // 修改http包中的header,伪装成浏览器进行抓取
            conn.header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/67.0.3396.87 Safari/537.36");
            Document doc = null;
            try {
                doc = conn.get();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ListItem listItem = null;

            // 获取页面标题
            Elements element_title = doc.select("title");
            title = element_title.first().text();
            Log.e("jav", "the html title is: " + title);

            // 获取下一页的链接
//            Elements link = doc.select("[class=pagination pagination-lg]").select("li:eq(6)");
//            next_page_url = link.select("a").attr("href");

            // 获取分类列表
            Elements navs = doc.select("[class=nav_menu clearfix]");
            for (Element ul : navs) {
                Elements lis = ul.select("li");
                for (Element li : lis) {
                    String category = li.select("a").text();
                    if (category == "")
                        continue;
                    categoryArray.add(category);
                }
            }

            // 获取ListItem
            Element element_movielist = doc.select("[class=box movie1_list]").first();
            Elements movie_list = element_movielist.select("li");
            for (Element li : movie_list) {
                String link = li.select("a").attr("href");
                String img_url = li.select("img").attr("src");
                String movie_title = li.select("h3").text();

//                Map<String, Object> map = new HashMap<>();
//                map.put("link", link);
//                map.put("img", img_url);
//                map.put("title", movie_title);
//                list.add(map);
                listItem = new ListItem(img_url, movie_title, link);
                list.add(listItem);
            }


//            dialog.dismiss();

            // 执行完毕给handler发送一个空消息
            handler.sendEmptyMessage(0);
        }
    };

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            // 收到消息后执行handler
            mTitle = (TextView)findViewById(R.id.main_title);
            mTitle.setText(title);
            ArrayAdapter adapter = new ArrayAdapter(MainActivity.this, R.layout.activity_category_items, categoryArray);
            cateGridView = (GridView)findViewById(R.id.category_list);
            cateGridView.setAdapter(adapter);
            show();
        }
    };

    // 判断是否有可用的网络连接
    public boolean isNetworkAvailable(Activity activity)
    {
        Context context = activity.getApplicationContext();
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null)
            return false;
        else
        {   // 获取所有NetworkInfo对象
            NetworkInfo[] networkInfo = cm.getAllNetworkInfo();
            if (networkInfo != null && networkInfo.length > 0)
            {
                for (int i = 0; i < networkInfo.length; i++)
                    if (networkInfo[i].getState() == NetworkInfo.State.CONNECTED)
                        return true;    // 存在可用的网络连接
            }
        }
        return false;
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                Title = getString(R.string.title_section1);
                break;
            case 2:
                Title = getString(R.string.title_section2);
                break;
            case 3:
                Title = getString(R.string.title_section3);
                break;
        }
    }


    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle saveInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((MainActivity) activity).onSectionAttached(getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }
}
