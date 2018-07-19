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
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private ProgressDialog dialog;
    private TextView mTitle;
    private String title;
    private ArrayList<HashMap<String, String>> cateList = new ArrayList<HashMap<String, String>>();
    private ListView infoListView;
    private GridView cateGridView;
    private List<ListItem> list;
    private CharSequence Title;
    private String url;
    private String currentCate;
    private int firstLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Title = getTitle();
        // Show ListView info
        infoListView = (ListView)findViewById(R.id.info_list_view);
        cateGridView = (GridView)findViewById(R.id.category_list);
        list = new ArrayList<ListItem>();
        currentCate = "home";
        firstLauncher = 1;

        if (isNetworkAvailable(MainActivity.this)) {

            // Show loading dialog
            dialog = new ProgressDialog(this);
            dialog.setMessage("正在抓取数据...");
            dialog.setCancelable(false);
            dialog.show();

            url = "http://sherwoodbp.com";
            // Start a thread
            new Thread(runnable).start();
        }
        else {
            // 弹出提示框
            // Open tips dialog
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
                    System.exit(0); // exit
                }
            }).show();
        }
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            get_html_content();

            // Send a message to handler after finish
            handler.sendEmptyMessage(0);
        }
    };

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            // Receive message and run handler
            mTitle = (TextView)findViewById(R.id.main_title);
            mTitle.setText(title);
            if (firstLauncher == 1) {
                initGridView();
                firstLauncher = 0;
            }
            initListView();
            dialog.dismiss();
        }
    };

    // Init ListView
    private void initListView() {
        if (list.isEmpty()) {
            TextView message = (TextView)findViewById(R.id.message);
            message.setText(R.string.message);
        } else {
            MyAdapter adapter = new MyAdapter(MainActivity.this, list, infoListView);
            infoListView.setAdapter(adapter);
        }
    }

    private void initGridView() {
        SimpleAdapter simpleAdapter = new SimpleAdapter(MainActivity.this, cateList,
                R.layout.activity_category_items,
                new String[]{"name"},
                new int[]{R.id.cate_list_item});
        cateGridView.setAdapter(simpleAdapter);
        cateGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Get HashMap Object in item
                HashMap<String, String> map =
                        (HashMap<String, String>)cateGridView.getItemAtPosition(position);
                String cate_name = map.get("name");
                String link = map.get("link");
                Toast.makeText(getApplicationContext(),
                        "You choose Item" + position + "," + "its value is: " + cate_name,
                        Toast.LENGTH_SHORT).show();
                currentCate = cate_name;
                switchOver(link);
            }
        });
    }

    // Check if the Internet is connected
    public boolean isNetworkAvailable(Activity activity)
    {
        Context context = activity.getApplicationContext();
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null)
            return false;
        else
        {   // Get all NetworkInfo Object
            NetworkInfo[] networkInfo = cm.getAllNetworkInfo();
            if (networkInfo != null && networkInfo.length > 0)
            {
                for (int i = 0; i < networkInfo.length; i++)
                    if (networkInfo[i].getState() == NetworkInfo.State.CONNECTED)
                        return true;    // Has connected Internet
            }
        }
        return false;
    }

    public void switchOver(final String link) {
        if (isNetworkAvailable(MainActivity.this)) {
            // Show loading dialog
            dialog = new ProgressDialog(this);
            dialog.setMessage("正在抓取数据...");
            dialog.setCancelable(false);
            dialog.show();

            url = "http://sherwoodbp.com" + link;
            clearListView();
            new Thread(runnable).start();
        } else {
            // Open tips dialog
            new AlertDialog.Builder(this)
                    .setTitle("提示")
                    .setMessage("当前没有网络连接！")
                    .setPositiveButton("重试", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switchOver(link);
                        }
                    }).setNegativeButton("退出", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    System.exit(0);
                }
            }).show();
        }
    }

    public void get_html_content() {
        Connection conn = Jsoup.connect(url);
        // Fix header in http and camouflage the web to get data
        conn.header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/67.0.3396.87 Safari/537.36");
        Document doc = null;
        ListItem listItem = null;
        try {
            doc = conn.get();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Get html title
        Elements element_title = doc.select("title");
        title = element_title.first().text();
        Log.e("jav", "the html title is: " + title);


        // Get category list
        if (firstLauncher == 1) {
            Elements navs = doc.select("[class=nav_menu clearfix]");
            for (Element ul : navs) {
                Elements lis = ul.select("li");
                for (Element li : lis) {
                    String category = li.select("a").text();
                    String link = li.select("a").attr("href");
                    if (category.equals(""))
                        continue;
                    HashMap<String, String> map = new HashMap<String, String>();
                    map.put("name", category);
                    map.put("link", link);
                    cateList.add(map);
                }
            }
        }

        // Get ListItem
        Element element_movielist = null;
        if (currentCate.equals("home")) {
            element_movielist = doc.select("[class=box movie1_list]").first();
        } else {
            element_movielist = doc.select("[class=box movie_list]").first();
        }
        Elements movie_list = element_movielist.select("li");
        for (Element li : movie_list) {
            String link = li.select("a").attr("href");
            String img_url = li.select("img").attr("src");
            String movie_title = li.select("h3").text();

            listItem = new ListItem(img_url, movie_title, link);
            list.add(listItem);
        }
    }

    public void clearListView() {
        list.clear();
        infoListView.setAdapter(null);
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
