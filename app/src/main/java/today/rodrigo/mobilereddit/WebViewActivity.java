package today.rodrigo.mobilereddit;

import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import timber.log.Timber;

public class WebViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);

        WebView webView = findViewById(R.id.webview_reddit);
        webView.getSettings().setJavaScriptEnabled(true);
        Intent intent = getIntent();
        String redditUrl = intent.getStringExtra(MainActivity.URL_KEY);
        if(redditUrl != null){
            String completeUrl = MainActivity.BASE_URL + redditUrl;
            Timber.i( "Reddit URL:%s", completeUrl);
            webView.loadUrl(completeUrl);
        }

    }
}
