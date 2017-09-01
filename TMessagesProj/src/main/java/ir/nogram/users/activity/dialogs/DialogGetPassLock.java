package ir.nogram.users.activity.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.amnix.materiallockview.MaterialLockView;


import java.util.List;

import ir.nogram.messanger.ApplicationLoader;
import ir.nogram.messanger.R;

/**
 * Created by MhkDeveloper on 2016-12-21.
 */
public class DialogGetPassLock extends Dialog {
    String correctPattern = "14789" ;
    Context context ;
    int typePattern = 1 ;
    DialogGetPassLockDelegate dialogGetPassLockDelegate ;
    public interface DialogGetPassLockDelegate{
        public void trueDetected() ;
    }

    public DialogGetPassLock(Context context, boolean cancelable, OnCancelListener cancelListener , int type , DialogGetPassLockDelegate dialogGetPassLockDelegate) {
        super(context, cancelable, cancelListener);
        this.context = context ;
        this.typePattern = type ;
        this.dialogGetPassLockDelegate = dialogGetPassLockDelegate ;
    }

    private MaterialLockView lockView ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE) ;

        setContentView(R.layout.dialog_get_lock_pattern);
        WindowManager.LayoutParams layoutParams =new WindowManager.LayoutParams() ;

        Window window = getWindow() ;
        layoutParams.copyFrom(window.getAttributes());
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
        window.setAttributes(layoutParams);
        lockView = (MaterialLockView) findViewById(R.id.lp_get_pass) ;
        TextView txt_cancell_lock = (TextView) findViewById(R.id.txt_cancell_lock);
//        lockView.setInStealthMode(true);

        lockView.setOnPatternListener(new MaterialLockView.OnPatternListener() {
            @Override
            public void onPatternStart() {
                super.onPatternStart();
            }
            @Override
            public void onPatternCleared() {
                super.onPatternCleared();
            }

            @Override
            public void onPatternCellAdded(List<MaterialLockView.Cell> pattern, String SimplePattern) {
                super.onPatternCellAdded(pattern, SimplePattern);
            }
            @Override
            public void onPatternDetected(List<MaterialLockView.Cell> pattern, String SimplePattern) {
                if(ApplicationLoader.databaseHandler.getLock(typePattern) == Integer.valueOf(SimplePattern)){
                    lockView.setDisplayMode(MaterialLockView.DisplayMode.Correct);
                    if(dialogGetPassLockDelegate != null){
                        dialogGetPassLockDelegate.trueDetected();
                    }
                    dismiss();
                }else {
                    lockView.setDisplayMode(MaterialLockView.DisplayMode.Wrong);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            lockView.clearPattern();

                        }
                    } , 500) ;
                }
                super.onPatternDetected(pattern, SimplePattern);
            }
        });
        txt_cancell_lock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancel();
            }
        });
    }
}
