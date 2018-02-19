package today.rodrigo.mobilereddit;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

/*
Author: Rod M. Mesquita (www.rodrigo.today)
Development time: ~ 4 hours
Resources utilized:

Prettifying JSON code to find tree nodes
https://jsonformatter.org/json-pretty-print

Simplyfing JSON download
https://github.com/amitshekhariitbhu/Fast-Android-Networking

Simplifying logging
https://github.com/JakeWharton/timber
*/



public class MainActivity extends AppCompatActivity {

    private String DATA_KEY = "data";
    private String CHILDREN_KEY = "children";
    private String SUBREDDIT_KEY = "subreddit";
    private String PERMALINK_KEY = "permalink";
    private String TITLE_KEY =  "title";
    private String POST_NAME = "post_name";
    private String POST_CATEGORY = "post_category";

    public static String BASE_URL = "https://www.reddit.com";
    public static String URL_KEY = "url_key";

    private JSONObject jsonObject;
    private String filterArgument = "";

    private ListView listView;
    private HashMap<String, List<String>> postsHashMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Timber.plant(new Timber.DebugTree());

        postsHashMap = new HashMap<>();

        listView = findViewById(R.id.listView);

        AndroidNetworking.initialize(getApplicationContext());
        AndroidNetworking.get(BASE_URL + "/.json")
                .setTag(this)
                .setPriority(Priority.LOW)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Timber.i("Reddit onResponse");
                        jsonObject = response;
                        organizePosts();
                    }

                    @Override
                    public void onError(ANError error) {
                        Timber.i("Reddit ANError getErrorCode:%s", error.getErrorCode());
                        Timber.i("Reddit ANError getErrorDetail:%s", error.getErrorDetail());
                        Timber.i("Reddit ANError getErrorBodyis:%s", error.getErrorBody());
                        Timber.i("Reddit ANError getResponse:%s", error.getResponse());
                        Timber.i("Reddit ANError getResponse:%s", error.getMessage());
                    }
                });

        Button filterBtn = findViewById(R.id.button);
        filterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText filterEditText = findViewById(R.id.editText);
                filterArgument = filterEditText.getText().toString();
                organizePosts();
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Timber.i("Reddit post item: %s", i);

                String postUrl = "url_placeholder";

                HashMap<String, List<String>> posts = postsHashMap;

                for (Object postObject : posts.entrySet()) {

                    Map.Entry<String, ArrayList> postCategoryAndUrl = (Map.Entry<String, ArrayList>)postObject;
                    Map.Entry postName = (Map.Entry) postObject;

                    Timber.i("Reddit getItem:%s", listView.getItemAtPosition(i));
                    Timber.i("Reddit getKey:%s", postName.getKey());

                    if(listView.getItemAtPosition(i).toString().contains(postName.getKey().toString())){
                        postUrl = postCategoryAndUrl.getValue().get(1).toString();

                        Intent webViewIntent = new Intent(MainActivity.this, WebViewActivity.class);
                        webViewIntent.putExtra(URL_KEY, postUrl);
                        startActivity(webViewIntent);

                    }

                }
                Timber.i("Reddit postsHasMap:%s", postUrl);


            }
        });

    }

    private void organizePosts(){

        /* JSON tree
        {data
            { children
                { id (1 through 25 for the first page)
                    { data (again)
                        {
                            title
                            subreddit
                            permalink
                        }
                    }
                 }
             }
         }
        */

        postsHashMap.clear();

        for (int postIndex = 0 ; postIndex < 25 ; postIndex++) {

            try {
                JSONObject dataObject = (JSONObject) jsonObject.get(DATA_KEY);
                JSONArray childrenObject = (JSONArray) dataObject.get(CHILDREN_KEY);
                JSONObject idObject = (JSONObject) childrenObject.get(postIndex);
                JSONObject subDataObject = (JSONObject) idObject.get(DATA_KEY);

                String title = subDataObject.get(TITLE_KEY).toString();
                String subreddit = subDataObject.get(SUBREDDIT_KEY).toString();
                String permalink = subDataObject.get(PERMALINK_KEY).toString();

                if(filterArgument.length() > 0 && subreddit.contains(filterArgument)){

                    List<String>linkData = new ArrayList<>();
                    linkData.add(subreddit);
                    linkData.add(permalink);

                    postsHashMap.put(title, linkData);

                } else if (filterArgument.length() == 0){

                    List<String>linkData = new ArrayList<>();
                    linkData.add(subreddit);
                    linkData.add(permalink);

                    postsHashMap.put(title, linkData);

                }

//                Timber.i("Reddit title:%s subreddit:%s permalink:%s", title.substring(0, 10), subreddit, permalink.substring(0, 10));

            } catch (JSONException e) {
                e.printStackTrace();
                Timber.i("Reddit error:%s", e.getMessage());
            }

        }
        updateUI(postsHashMap);

    }

    private void updateUI(HashMap<String, List<String>> posts){ // String and ArrayList HashMap

        List<HashMap<String, String>> postItems = new ArrayList<>();

        SimpleAdapter adapter = new SimpleAdapter(this, postItems, R.layout.post_cell,
                new String[]{POST_NAME, POST_CATEGORY},
                new int[]{R.id.postTitle, R.id.postCategory}
                );

        for (Object postObject : posts.entrySet()) {

            HashMap<String, String> finalMap = new HashMap<>();

            Map.Entry<String, ArrayList> postCategoryAndUrl = (Map.Entry<String, ArrayList>)postObject;
            Map.Entry postName = (Map.Entry) postObject;

            finalMap.put(POST_NAME, postName.getKey().toString());
            finalMap.put(POST_CATEGORY, postCategoryAndUrl.getValue().get(0).toString());

            postItems.add(finalMap);
        }

        listView.setAdapter(adapter);

    }
}
