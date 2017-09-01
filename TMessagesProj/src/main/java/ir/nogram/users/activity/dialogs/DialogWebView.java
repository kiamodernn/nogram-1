package ir.nogram.users.activity.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.amnix.materiallockview.MaterialLockView;

import java.util.List;

import ir.nogram.messanger.ApplicationLoader;
import ir.nogram.messanger.R;

/**
 * Created by MhkDeveloper on 2016-12-21.
 */
public class DialogWebView extends Dialog {
    Context context ;
    String url ;
    public DialogWebView(Context context, boolean cancelable, OnCancelListener cancelListener , String url) {
        super(context, cancelable, cancelListener);
        this.context = context ;
        this.url = url ;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE) ;
        setContentView(R.layout.webview);
        WindowManager.LayoutParams layoutParams =new WindowManager.LayoutParams() ;

        Window window = getWindow() ;
        layoutParams.copyFrom(window.getAttributes());
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
        window.setAttributes(layoutParams);

        WebView  mWebview = (WebView) findViewById(R.id.webview);
        mWebview.getSettings().setJavaScriptEnabled(true); // enable javascript


        mWebview.setWebViewClient(new WebViewClient() {
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
//                Toast.makeText(activity, description, Toast.LENGTH_SHORT).show();
            }
        });

        mWebview .loadUrl(url);
//        setContentView(mWebview);
    }
}
