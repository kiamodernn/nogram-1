package ir.nogram.users.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import ir.nogram.messanger.ApplicationLoader;
import ir.nogram.messanger.LocaleController;
import ir.nogram.messanger.NotificationCenter;
import ir.nogram.messanger.R;
import ir.nogram.ui.ActionBar.ActionBar;
import ir.nogram.ui.ActionBar.ActionBarMenu;
import ir.nogram.ui.ActionBar.BaseFragment;
import ir.nogram.ui.Components.LayoutHelper;
import ir.nogram.users.activity.adapter.AdapterChangeContacts;
import ir.nogram.users.activity.holder.HoldChangeContact;
import ir.nogram.users.activity.holder.HoldConst;
import java.util.ArrayList;
/**
 * Created by MhkDeveloper on 2016-10-10.
 */
public class ActivityChangeContacts extends BaseFragment implements NotificationCenter.NotificationCenterDelegate {
    static Context context;
    RecyclerView listView ;
    AdapterChangeContacts dialogsAdapter;
    public ActivityChangeContacts(Bundle bundle) {
    }

    public void onFragmentDestroy() {
        super.onFragmentDestroy();
    }

    @Override
    public boolean onFragmentCreate() {
        super.onFragmentCreate();
        swipeBackEnabled = true;
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (dialogsAdapter != null) {
            dialogsAdapter.notifyDataSetChanged();
        }
    }
    View view;
    @Override
    public View createView(final Context context) {
        this.context = context;
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle(context.getString(R.string.ContactsChange));
        ActionBarMenu menu = actionBar.createMenu();
        menu.addItem(2 , R.drawable.ic_del) ;

        fragmentView = new FrameLayout(context);
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view =layoutInflater.inflate(R.layout.activity_change_contacts , null) ;
        ((FrameLayout)fragmentView).addView(view);
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) view.getLayoutParams();
        layoutParams.width = LayoutHelper.MATCH_PARENT;
        layoutParams.height = LayoutHelper.MATCH_PARENT;
        view.setLayoutParams(layoutParams);
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        final ArrayList<HoldChangeContact> arr =ApplicationLoader.databaseHandler.listChanges() ;
        listView = (RecyclerView) view.findViewById(R.id.rc_change);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(context);
        listView.setLayoutManager(mLayoutManager);
        listView.setHasFixedSize(true);
        dialogsAdapter = new AdapterChangeContacts(arr , context) ;
        try {
            listView.setAdapter(dialogsAdapter);
        }catch (Exception e){
            Log.e("logErr" , e.getMessage() +"") ;
        }
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences(HoldConst.PREF_MAIN, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
//        Log.i("logLastId" ,holdCatDialogs.get(0).id +"-");
        editor.putInt(HoldConst.PREF_LAST_ID , arr.size() > 0 ? arr.get(0).id : 0 ).apply();
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
                if(id == 2){
                    AlertDialog.Builder builder = new AlertDialog.Builder(context) ;
                    builder.setMessage(LocaleController.getString("SureDelChanges" , R.string.SureDelChanges)) ;
                    builder.setPositiveButton(LocaleController.getString("yes", R.string.yes), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    ApplicationLoader.databaseHandler.deleteAllChange() ;
                                    arr.clear();
                                    dialogsAdapter.notifyDataSetChanged();
                                }
                            }

                    );
                    builder.setNegativeButton(LocaleController.getString("no", R.string.no), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                }
                            }

                    );

                    builder.show() ;
                }
            }
        });
        return fragmentView;
    }
    @Override
    public void didReceivedNotification(int id, Object... args) {

    }


}
