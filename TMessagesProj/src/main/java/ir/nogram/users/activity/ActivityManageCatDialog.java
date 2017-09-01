package ir.nogram.users.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import java.util.ArrayList;

import ir.nogram.messanger.ApplicationLoader;
import ir.nogram.messanger.LocaleController;
import ir.nogram.messanger.NotificationCenter;
import ir.nogram.messanger.R;
import ir.nogram.ui.ActionBar.ActionBar;
import ir.nogram.ui.ActionBar.ActionBarMenu;
import ir.nogram.ui.ActionBar.BaseFragment;
import ir.nogram.ui.Components.LayoutHelper;
import ir.nogram.users.activity.adapter.AdapterManageCatDialog;
import ir.nogram.users.activity.db.DatabaseMain;
import ir.nogram.users.activity.holder.HoldCatDialog;

/**
 * Created by MhkDeveloper on 2016-10-10.
 */
public class ActivityManageCatDialog extends BaseFragment implements NotificationCenter.NotificationCenterDelegate {
     RecyclerView listView ;
    AdapterManageCatDialog dialogsAdapter;
   ArrayList<HoldCatDialog> holdCatDialogs = new ArrayList<>() ;
    private int newCat = 5 ;


    public ActivityManageCatDialog(Bundle bundle) {

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
        holdCatDialogs.clear();
        holdCatDialogs.addAll(DatabaseMain.getInstance(getParentActivity()).loadCatDialogs());
        if (dialogsAdapter != null) {
            dialogsAdapter.notifyDataSetChanged();
        }
    }
    View view;
    @Override
    public View createView(final Context context) {
         actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle(context.getString(R.string.ManageCategory));
        ActionBarMenu menu = actionBar.createMenu();
        menu.addItem(newCat , R.drawable.at_ic_action_new) ;

        fragmentView = new FrameLayout(context);
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view =layoutInflater.inflate(R.layout.activity_manage_cat_dialog , null) ;
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

        listView = (RecyclerView) view.findViewById(R.id.rcv_cat);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(context);
        listView.setLayoutManager(mLayoutManager);
        listView.setHasFixedSize(true);
        dialogsAdapter = new AdapterManageCatDialog(holdCatDialogs, context, new AdapterManageCatDialog.AdapterManageCatDialogDelegate() {
            @Override
            public void editClicked(HoldCatDialog cat) {
                presentFragment(new AddCatDialogNameActivity(cat)) ;
            }

            @Override
            public void deldetClicked(HoldCatDialog cat) {
                createDialogDelete(cat) ;
            }

            @Override
            public void addClicked(HoldCatDialog cat) {
                Bundle bundle = new Bundle() ;
                bundle.putInt("catId" , cat.id);
                presentFragment(new DialogsActivityForSelect(bundle) ) ;

            }
        }) ;
        try {
            listView.setAdapter(dialogsAdapter);
        }catch (Exception e){
            Log.e("logErr" , e.getMessage() +"") ;
        }
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
                if(id == newCat){
                    presentFragment(new AddCatDialogNameActivity(null)) ;
                }
            }
        });
        return fragmentView;
    }
    @Override
    public void didReceivedNotification(int id, Object... args) {

    }

    private void createDialogDelete(final HoldCatDialog cat){
        AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity()) ;
        builder.setMessage(LocaleController.getString("SureDeleteCat" , R.string.SureDeleteCat).replace("%catName" , cat.catName)) ;

        builder.setPositiveButton(LocaleController.getString("Yes", R.string.Yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                DatabaseMain.getInstance(ApplicationLoader.applicationContext).deleteCatDialog(cat.id) ;
                holdCatDialogs.remove(cat) ;
                dialogsAdapter.notifyDataSetChanged();
            }
        }) ;
        builder.setNegativeButton(LocaleController.getString("No", R.string.No), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        }) ;
        builder.show() ;
    }

}
