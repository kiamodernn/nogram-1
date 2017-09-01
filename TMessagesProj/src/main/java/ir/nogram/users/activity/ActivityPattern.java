package ir.nogram.users.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.amnix.materiallockview.MaterialLockView;

import ir.nogram.SQLite.DatabaseHandler;
import ir.nogram.messanger.ApplicationLoader;
import ir.nogram.messanger.R;
import ir.nogram.ui.ActionBar.ActionBar;
import ir.nogram.ui.ActionBar.ActionBarMenu;
import ir.nogram.ui.ActionBar.BaseFragment;
import ir.nogram.users.activity.dialogs.DialogGetPassLock;
import ir.nogram.users.activity.holder.HoldArgs;

import java.util.List;


/**
 * Created by MhkDeveloper on 2016-10-10.
 */
public class ActivityPattern extends BaseFragment implements View.OnClickListener{

    static Context context ;

    String lockTemp = "" ;
    int typePattern = 1 ;
    public ActivityPattern(Bundle  bundle){
        typePattern =  bundle.getInt(HoldArgs.typePattern , DatabaseHandler.PATTERN_FOR_LOCKED) ;
     }
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
    }

    @Override
    public boolean onFragmentCreate() {
        super.onFragmentCreate();
        swipeBackEnabled = false;

        return true;
    }
    @Override
    public void onResume() {
        super.onResume();
    }
    View view ;
    @Override
    public View createView(Context context) {
        this.context = context ;
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle(context.getString(R.string.set_pattern));
        ActionBarMenu menu = actionBar.createMenu();
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });
        if(ApplicationLoader.databaseHandler.getLock(typePattern) != 0 ){
            DialogGetPassLock dialogGetPassLock = new DialogGetPassLock(context, true, new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialogInterface) {
                    finishFragment() ;
                }
            }, typePattern , null) ;
            dialogGetPassLock.show() ;
        }
        fragmentView = new FrameLayout(context);
        LayoutInflater layoutInflater =(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view  = layoutInflater.inflate(R.layout.activity_lock_pattern, null) ;
        ((FrameLayout) fragmentView).addView(view);
//        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) view.getLayoutParams();
//        layoutParams.width = LayoutHelper.MATCH_PARENT;
//        layoutParams.height = LayoutHelper.MATCH_PARENT;
//        view.setLayoutParams(layoutParams);
//        view.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                return true;
//            }
//        });
        initialWidget() ;
//        try {
//            new GetList().execute() ;
//
//        }catch (Exception e){
//            Log.e("log" , e.getMessage()) ;
//        }
        return  fragmentView ;
    }

    Button txtCancell ;
    Button txtContinue ;
    Button txtRetry ;
    Button txtConfirm ;
    private MaterialLockView lockView ;

    boolean patternDetectedToContinue = false ;
    private void initialWidget() {
        txtCancell = (Button) view.findViewById(R.id.txt_cancell) ;
        txtContinue = (Button) view.findViewById(R.id.txt_continue) ;
        txtRetry = (Button) view.findViewById(R.id.txt_retry) ;
        txtConfirm = (Button) view.findViewById(R.id.txt_confirm) ;
        txtConfirm.setVisibility(View.GONE);
        txtRetry.setVisibility(View.GONE);
        txtContinue.setClickable(false);
        txtContinue.setAlpha(0.5f);
        txtConfirm.setAlpha(0.5f);
        txtCancell.setOnClickListener(this);
        txtContinue.setOnClickListener(this);
        txtConfirm.setOnClickListener(this);
        txtRetry.setOnClickListener(this);
        lockView = (MaterialLockView) view.findViewById(R.id.lp_pass_set) ;
        patternDetected(patternDetectedToContinue) ;
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
                 if(patternDetectedToContinue){
                          if(lockTemp.equals(SimplePattern)){
                              txtConfirm.setClickable(true);
                              txtConfirm.setAlpha(1);
                              Log.e("log" , "equals") ;
                          }else{
                              lockView.setDisplayMode(MaterialLockView.DisplayMode.Wrong);
                              new Handler().postDelayed(new Runnable() {
                                  @Override
                                  public void run() {
                                      lockView.clearPattern();

                                  }
                              }  ,  500 ) ;
                              Toast.makeText(context , "no match pass" , Toast.LENGTH_SHORT).show();
                          }
                 } else {
                   if(SimplePattern.length() <4){
                       Toast.makeText(context , "length of pass is short" , Toast.LENGTH_SHORT).show();
                       lockView.setDisplayMode(MaterialLockView.DisplayMode.Wrong);
                       patternDetected(false) ;
                       new Handler().postDelayed(new Runnable() {
                           @Override
                           public void run() {
                               lockView.clearPattern();

                           }
                       }  ,  500 ) ;
                   }else{
                       lockView.disableInput();
                       txtContinue.setClickable(true);
                       txtContinue.setAlpha(1);
                       txtRetry.setVisibility(View.VISIBLE);
                       txtCancell.setVisibility(View.GONE);
                       lockTemp = SimplePattern ;

                   }
                }

                 super.onPatternDetected(pattern, SimplePattern);
            }
        });
    }

    private void patternDetected(boolean patternDetected) {
        patternDetectedToContinue = patternDetected ;
        if(patternDetectedToContinue){
            txtCancell.setVisibility(View.GONE);
            txtRetry.setVisibility(View.VISIBLE);
            txtContinue.setVisibility(View.VISIBLE);
            txtContinue.setClickable(true);
        }else{
            txtCancell.setVisibility(View.VISIBLE);
            txtRetry.setVisibility(View.GONE);
            txtContinue.setClickable(false);
            txtConfirm.setVisibility(View.GONE);
        }
    }

    public BaseFragment parentFragment = null;


    @Override
    public void onClick(View view) {

        int id = view.getId() ;
        switch (id){
            case R.id.txt_cancell :
                finishFragment();
                break;
            case R.id.txt_continue :
                patternDetectedToContinue = true ;
                txtContinue.setVisibility(View.GONE);
                txtConfirm.setVisibility(View.VISIBLE);
                txtConfirm.setAlpha(0.5f);

                txtConfirm.setClickable(false);
                txtRetry.setVisibility(View.VISIBLE);
                lockView.clearPattern();
                lockView.enableInput();
                break;
            case R.id.txt_retry:
                patternDetectedToContinue= false ;
                txtConfirm.setVisibility(View.GONE);
                txtContinue.setVisibility(View.VISIBLE);
                txtContinue.setAlpha(0.5f);
                txtContinue.setAlpha(0.5f);

                txtContinue.setClickable(false);
                txtCancell.setVisibility(View.VISIBLE);
                txtRetry.setVisibility(View.GONE);
                lockView.clearPattern();
                lockView.enableInput();
                lockTemp = "" ;
                break;
            case R.id.txt_confirm:
                Log.i("log" , lockTemp) ;
                if(lockTemp.length() >= 4 ){
                    ApplicationLoader.databaseHandler.addLock(Integer.valueOf(lockTemp) , typePattern) ;
                    Toast.makeText(context , "success" , Toast.LENGTH_LONG).show();
                    finishFragment();
                }
                break;
        }
    }
}
