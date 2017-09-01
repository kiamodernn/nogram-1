package ir.nogram.users.activity;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import ir.nogram.messanger.ApplicationLoader;
import ir.nogram.messanger.LocaleController;
import ir.nogram.messanger.NotificationCenter;
import ir.nogram.messanger.R;
import ir.nogram.ui.ActionBar.ActionBar;
import ir.nogram.ui.ActionBar.ActionBarMenu;
import ir.nogram.ui.ActionBar.BaseFragment;
import ir.nogram.ui.Components.LayoutHelper;
import ir.nogram.ui.ThemeManager;
import ir.nogram.users.activity.holder.HoldConst;

/**
 * Created by MhkDeveloper on 2016-10-10.
 */
public class ActivityTabSetting extends BaseFragment implements NotificationCenter.NotificationCenterDelegate, View.OnClickListener {
    Context context;
    private View view;
    private LinearLayout
            allTab , usersTab , groupsTab , superGroupTab , allGroupTab , channelsTab , botsTab , favoriteTab ;
    private TextView
            txtTabAll,
            txtUsersTab,
            txtGroupsTab,
            txtSuperGroup,
            txtAllGroup,
            txtChannel,
            txtBot,
            txtfav;

    private boolean showToast = true ;

    String defaultFont = ThemeManager.getFont();

    public ActivityTabSetting() {
    }

    public void onFragmentDestroy() {
        super.onFragmentDestroy();
//        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.refreshTabs);

    }

    @Override
    public boolean onFragmentCreate() {
        super.onFragmentCreate();
//        NotificationCenter.getInstance().addObserver(this, NotificationCenter.refreshTabs);
        swipeBackEnabled = true;
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
    }


    @Override
    public View createView(final Context context) {
        this.context = context;
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle(context.getString(R.string.TabSetting));
//        actionBar.setBackgroundColor(ThemeManager.getColor());
        ActionBarMenu menu = actionBar.createMenu();
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });
//        menu.addItem(2 , R.drawable.ic_del) ;

        fragmentView = new FrameLayout(context);
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = layoutInflater.inflate(R.layout.activity_tab_setting, null);
        ((FrameLayout) fragmentView).addView(view);
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

        initialWidget();
        return fragmentView;
    }

    private void initialWidget() {
        txtTabAll = (TextView) view.findViewById(R.id.txtTabAll);
        txtUsersTab = (TextView) view.findViewById(R.id.txtTabUser);
        txtGroupsTab = (TextView) view.findViewById(R.id.txtTabGroup);
        txtSuperGroup = (TextView) view.findViewById(R.id.txtTabSuperGroup);
        txtAllGroup = (TextView) view.findViewById(R.id.txtTabAllGroup);
        txtChannel = (TextView) view.findViewById(R.id.txtTabChannel);
        txtBot = (TextView) view.findViewById(R.id.txtTabBot);
        txtfav = (TextView) view.findViewById(R.id.txtTabFavorite);

        allTab = (LinearLayout) view.findViewById(R.id.allTab);
        usersTab = (LinearLayout) view.findViewById(R.id.userTab);
        groupsTab = (LinearLayout) view.findViewById(R.id.groupTab);
        superGroupTab = (LinearLayout) view.findViewById(R.id.superGroupTab);
        allGroupTab = (LinearLayout) view.findViewById(R.id.allGroupTab);
        channelsTab = (LinearLayout) view.findViewById(R.id.chanelsTab);
        botsTab = (LinearLayout) view.findViewById(R.id.botTab);
        favoriteTab = (LinearLayout) view.findViewById(R.id.favoriteTab);

        allTab.setOnClickListener(this);
        usersTab.setOnClickListener(this);
        groupsTab.setOnClickListener(this);
        superGroupTab.setOnClickListener(this);
        allGroupTab.setOnClickListener(this);
        channelsTab.setOnClickListener(this);
        botsTab.setOnClickListener(this);
        favoriteTab.setOnClickListener(this);

        txtTabAll.setTypeface(getTypeFace(defaultFont));
        txtUsersTab.setTypeface(getTypeFace(defaultFont));
        txtGroupsTab.setTypeface(getTypeFace(defaultFont));
        txtSuperGroup.setTypeface(getTypeFace(defaultFont));
        txtAllGroup.setTypeface(getTypeFace(defaultFont));
        txtChannel.setTypeface(getTypeFace(defaultFont));
        txtBot.setTypeface(getTypeFace(defaultFont));
        txtfav.setTypeface(getTypeFace(defaultFont));

        setValues();
    }

    private void setValues() {
        int green;
        int normal;
        if (Build.VERSION.SDK_INT >= 23) {
            green = ContextCompat.getColor(context, R.color.green_light);
            normal = ContextCompat.getColor(context, R.color.transparent);
        } else {
            green = context.getResources().getColor(R.color.green_light);
            normal = context.getResources().getColor(R.color.transparent);
        }
        SharedPreferences plusPreferences = ApplicationLoader.applicationContext.getSharedPreferences(HoldConst.PREF_MAIN , Activity.MODE_PRIVATE);
        boolean hideAll = plusPreferences.getBoolean(HoldConst.PREF_HIDE_ALL, false);
        boolean hideUsers = plusPreferences.getBoolean(HoldConst.PREF_HIDE_USERS, false);
        boolean hideGroups = plusPreferences.getBoolean(HoldConst.PREF_HIDE_GROUPS, false);
        boolean hideSGroups = plusPreferences.getBoolean(HoldConst.PREF_HIDE_SGROUPS, false);
        boolean hideAllGroups = plusPreferences.getBoolean(HoldConst.PREF_HIDE_GROUP_ALL, true);
        boolean hideChannels = plusPreferences.getBoolean(HoldConst.PREF_HIDE_CHANNELS, false);
        boolean hideBots = plusPreferences.getBoolean(HoldConst.PREF_HIDE_BOTS, false);
        boolean hideFavs = plusPreferences.getBoolean(HoldConst.PREF_HIDE_FAVES, false);
        allTab.setBackgroundColor(hideAll ? normal : green);
        usersTab.setBackgroundColor(hideUsers ? normal : green);
        groupsTab.setBackgroundColor(hideGroups ? normal : green);
        superGroupTab.setBackgroundColor(hideSGroups ? normal : green);
        allGroupTab.setBackgroundColor(hideAllGroups ? normal : green);
        channelsTab.setBackgroundColor(hideChannels ? normal : green);
        botsTab.setBackgroundColor(hideBots ? normal : green);
        favoriteTab.setBackgroundColor(hideFavs ? normal : green);

    }

    @Override
    public void didReceivedNotification(int id, Object... args) {

    }


    @Override
    public void onClick(View view) {
        int green;
        int normal;
        if (showToast){
            Toast.makeText(context , LocaleController.getString("exit_to_cahnge" , R.string.exit_to_cahnge) , Toast.LENGTH_SHORT).show();
            showToast = false ;
        }
//        ApplicationLoader.sharedPreferencesMain.edit().putBoolean(HoldConst.PREF_DIALOG_NEED_RELOAD , true).apply();
        if (Build.VERSION.SDK_INT >= 23) {
            green = ContextCompat.getColor(context, R.color.green_light);
            normal = ContextCompat.getColor(context, R.color.transparent);
        } else {
            green = context.getResources().getColor(R.color.green_light);
            normal = context.getResources().getColor(R.color.transparent);
        }
        SharedPreferences plusPreferences = ApplicationLoader.applicationContext.getSharedPreferences(HoldConst.PREF_MAIN , Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor=   plusPreferences.edit() ;
        boolean hideAll = plusPreferences.getBoolean(HoldConst.PREF_HIDE_ALL, false);
        boolean hideUsers = plusPreferences.getBoolean(HoldConst.PREF_HIDE_USERS, false);
        boolean hideGroups = plusPreferences.getBoolean(HoldConst.PREF_HIDE_GROUPS, false);
        boolean hideSGroups = plusPreferences.getBoolean(HoldConst.PREF_HIDE_SGROUPS, false);
        boolean hideAllGroups = plusPreferences.getBoolean(HoldConst.PREF_HIDE_GROUP_ALL, true);
        boolean hideChannels = plusPreferences.getBoolean(HoldConst.PREF_HIDE_CHANNELS, false);
        boolean hideBots = plusPreferences.getBoolean(HoldConst.PREF_HIDE_BOTS, false);
        boolean hideFavs = plusPreferences.getBoolean(HoldConst.PREF_HIDE_FAVES, false);
        int id = view.getId();
        switch (id){
            case R.id.allTab :
                if(hideAll){
                    allTab.setBackgroundColor(green);
                }else{
                    allTab.setBackgroundColor(normal);
                }
                hideAll = !hideAll ;
                editor.putBoolean(HoldConst.PREF_HIDE_ALL , hideAll).apply();
                break;
            case R.id.userTab :
                if(hideUsers){
                    usersTab.setBackgroundColor(green);
                }else{
                    usersTab.setBackgroundColor(normal);
                }
                hideUsers = !hideUsers ;
                editor.putBoolean(HoldConst.PREF_HIDE_USERS , hideUsers).apply();
                break;
            case R.id.groupTab :
                if(hideGroups){
                    groupsTab.setBackgroundColor(green);
                }else{
                    groupsTab.setBackgroundColor(normal);
                }
                hideGroups = !hideGroups ;
                editor.putBoolean(HoldConst.PREF_HIDE_GROUPS , hideGroups).apply();
                break;
            case R.id.superGroupTab:
                if(hideSGroups){
                    superGroupTab.setBackgroundColor(green);
                }else{
                    superGroupTab.setBackgroundColor(normal);
                }
                hideSGroups = !hideSGroups ;
                editor.putBoolean(HoldConst.PREF_HIDE_SGROUPS , hideSGroups).apply();
                break;
            case R.id.allGroupTab:
                if(hideAllGroups){
                    allGroupTab.setBackgroundColor(green);
                }else{
                    allGroupTab.setBackgroundColor(normal);
                }
                hideAllGroups = !hideAllGroups ;
                editor.putBoolean(HoldConst.PREF_HIDE_GROUP_ALL , hideAllGroups).apply();
                break;
            case R.id.chanelsTab:
                if(hideChannels){
                    channelsTab.setBackgroundColor(green);
                }else{
                    channelsTab.setBackgroundColor(normal);
                }
                hideChannels = !hideChannels ;
                editor.putBoolean(HoldConst.PREF_HIDE_CHANNELS, hideChannels).apply();
                break;
            case R.id.botTab:
                if(hideBots){
                    botsTab.setBackgroundColor(green);
                }else{
                    botsTab.setBackgroundColor(normal);
                }
                hideBots = !hideBots ;
                editor.putBoolean(HoldConst.PREF_HIDE_BOTS, hideBots).apply();
                break;
            case R.id.favoriteTab:
                if(hideFavs){
                    favoriteTab.setBackgroundColor(green);
                }else{
                    favoriteTab.setBackgroundColor(normal);
                }
                hideFavs = !hideFavs ;
                editor.putBoolean(HoldConst.PREF_HIDE_FAVES, hideFavs).apply();
                break;
        }
        if (!hideUsers || !hideGroups || hideSGroups || !hideAllGroups || !hideChannels || !hideBots || !hideFavs){
            editor.putBoolean(HoldConst.PREF_HIDE_TAB, false).apply();
        }
     }

    private Typeface getTypeFace(String patch) {

        Typeface t = Typeface.createFromAsset(ApplicationLoader.applicationContext.getAssets(), patch);
        return t;

    }
}
