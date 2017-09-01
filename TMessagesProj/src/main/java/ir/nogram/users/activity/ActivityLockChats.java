package ir.nogram.users.activity;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;

import ir.nogram.SQLite.DatabaseHandler;
import ir.nogram.messanger.AndroidUtilities;
import ir.nogram.messanger.AnimationCompat.AnimatorListenerAdapterProxy;
import ir.nogram.messanger.AnimationCompat.ObjectAnimatorProxy;
import ir.nogram.messanger.ApplicationLoader;
import ir.nogram.messanger.ContactsController;
import ir.nogram.messanger.LocaleController;
import ir.nogram.messanger.MessagesController;
import ir.nogram.messanger.NotificationCenter;
import ir.nogram.messanger.R;
import ir.nogram.messanger.query.StickersQuery;
import ir.nogram.messanger.support.widget.LinearLayoutManager;
import ir.nogram.messanger.support.widget.RecyclerView;
import ir.nogram.tgnet.TLRPC;
import ir.nogram.ui.ActionBar.ActionBar;
import ir.nogram.ui.ActionBar.ActionBarMenu;
import ir.nogram.ui.ActionBar.BaseFragment;
import ir.nogram.ui.Cells.TextSettingsCell;
import ir.nogram.ui.Components.EmptyTextProgressView;
import ir.nogram.ui.Components.LayoutHelper;
import ir.nogram.ui.Components.RecyclerListView;
import ir.nogram.users.activity.adapter.DialogsAdapterLock;
import ir.nogram.users.activity.holder.HoldArgs;
import ir.nogram.users.activity.holder.Pref;


/**
 * Created by MhkDeveloper on 2016-10-10.
 */
public class ActivityLockChats extends BaseFragment implements NotificationCenter.NotificationCenterDelegate {
    static Context context;
    private ImageView allTab;
    private ImageView usersTab;
    private ImageView groupsTab;
    private ImageView superGroupsTab;
    private ImageView channelsTab;
    private ImageView botsTab;
    private ImageView favsTab;
    private ProgressBar progressView;

    public static boolean dialogsLoaded;

    public ActivityLockChats() {
    }

    public void onFragmentDestroy() {
        super.onFragmentDestroy();
    }

    @Override
    public boolean onFragmentCreate() {
        super.onFragmentCreate();
        swipeBackEnabled = false;

        if(!dialogsLoaded){
            MessagesController.getInstance().loadDialogs(0, 300, true);
            ContactsController.getInstance().checkInviteText();
            StickersQuery.checkFeaturedStickers();
            dialogsLoaded = true;
        }
        return true;
    }

    private boolean searching;
    private boolean searchWas;

    @Override
    public void onResume() {
        super.onResume();
        if (dialogsAdapter != null) {
            dialogsAdapter.notifyDataSetChanged();
        }
    }
    View view;
    FrameLayout frameLayout;
    private LinearLayout emptyView;
    private EmptyTextProgressView searchEmptyView;

    @Override
    public View createView(Context context) {
        searching = false;
        searchWas = false;
        this.context = context;
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle(context.getString(R.string.LockChats)
        );
        ActionBarMenu menu = actionBar.createMenu();
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });
        fragmentView = new FrameLayout(context);

        frameLayout = new FrameLayout(context);
        initialWidget();

        if (MessagesController.getInstance().loadingDialogs && MessagesController.getInstance().dialogs.isEmpty()) {
            searchEmptyView.setVisibility(View.GONE);
            emptyView.setVisibility(View.GONE);
            listView.setEmptyView(progressView);
        } else {
            searchEmptyView.setVisibility(View.GONE);
            progressView.setVisibility(View.GONE);
            listView.setEmptyView(emptyView);
        }
        return fragmentView;
    }

    RecyclerListView listView;

    private LinearLayoutManager layoutManager;
    private FrameLayout tabsView;
    int dialogsType = 3;
    DialogsAdapterLock dialogsAdapter;
    private LinearLayout tabsLayout;
    private LinearLayout layoutParent;
      TextSettingsCell textLockCount ;
    private void initialWidget() {
        layoutParent = new LinearLayout(context) ;
        layoutParent.setOrientation(LinearLayout.VERTICAL);
        fragmentView = frameLayout;
        listView = new RecyclerListView(context);
        listView.setVerticalScrollBarEnabled(true);
        listView.setItemAnimator(null);
        listView.setInstantClick(true);
        listView.setLayoutAnimation(null);
        listView.setTag(4);
        layoutManager = new LinearLayoutManager(context) {
            @Override
            public boolean supportsPredictiveItemAnimations() {
                return false;
            }
        };
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        listView.setLayoutManager(layoutManager);
        listView.setVerticalScrollbarPosition(LocaleController.isRTL ? ListView.SCROLLBAR_POSITION_LEFT : ListView.SCROLLBAR_POSITION_RIGHT);
//        listView.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.lst_back));
        final TextSettingsCell textLockDialog = new TextSettingsCell(context);
        textLockDialog.setText(context.getString(R.string.set_pattern_lock), true);
        textLockDialog.setLayoutParams(new ViewGroup.LayoutParams(LayoutHelper.MATCH_PARENT ,  AndroidUtilities.dp(40)));
        textLockDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle() ;
                bundle.putInt(HoldArgs.typePattern, DatabaseHandler.PATTERN_FOR_LOCKED);
                presentFragment(new ActivityPattern(bundle));
            }
        });
        layoutParent.addView(textLockDialog ,LayoutHelper.MATCH_PARENT , LayoutHelper.WRAP_CONTENT);

        textLockCount = new TextSettingsCell(context);
        textLockCount.setForegroundGravity(Gravity.RIGHT);
        updatetextLockCount() ;
        textLockCount.setLayoutParams(new ViewGroup.LayoutParams(LayoutHelper.MATCH_PARENT ,  AndroidUtilities.dp(40)));
        layoutParent.addView(textLockCount ,LayoutHelper.MATCH_PARENT , LayoutHelper.WRAP_CONTENT);

        tabsView = new FrameLayout(context);
        createTabs(context);
        layoutParent.addView(tabsView ,LayoutHelper.MATCH_PARENT , AndroidUtilities.dp(2));

        layoutParent.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        frameLayout.addView(layoutParent);

        listView.setOnItemClickListener(new RecyclerListView.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (listView == null || listView.getAdapter() == null) {
                    return;
                }
                int dLock = ApplicationLoader.databaseHandler.getLock(DatabaseHandler.PATTERN_FOR_LOCKED) ;
                if(dLock == 0 ){
                    Toast.makeText(context , LocaleController.getString("NotPatternSetYet", R.string.NotPatternSetYet), Toast.LENGTH_SHORT).show();

                    Vibrator v = (Vibrator) getParentActivity().getSystemService(Context.VIBRATOR_SERVICE);
                    if (v != null) {
                        v.vibrate(200);
                    }
                    AndroidUtilities.shakeView(textLockDialog, 2, 0);
                    return;
                }
                long dialog_id = 0;
                int message_id = 0;
                RecyclerView.Adapter adapter = listView.getAdapter();
                    TLRPC.TL_dialog dialog = dialogsAdapter.getItem(position);
                    if (dialog == null) {
                        return;
                    }
                    dialog_id = dialog.id;
                    Log.i("log" , dialog_id +"") ;
//                if(dialog_id <0){
//                    dialog_id = -dialog_id ;
//                }
                if(ApplicationLoader.databaseHandler.islocked((int)dialog_id )) {
                    ApplicationLoader.databaseHandler.deleteLockDialog((int)dialog_id) ;
                }else {
                    ApplicationLoader.databaseHandler.addLockDialog((int)dialog_id) ;
                }
                updatetextLockCount();
                dialogsAdapter.notifyDataSetChanged();
            }
        });
        //
        listView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    AndroidUtilities.hideKeyboard(getParentActivity().getCurrentFocus());
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                int firstVisibleItem = layoutManager.findFirstVisibleItemPosition();
                int visibleItemCount = Math.abs(layoutManager.findLastVisibleItemPosition() - firstVisibleItem) + 1;
                int totalItemCount = recyclerView.getAdapter().getItemCount();
                if (visibleItemCount > 0) {
                    if (layoutManager.findLastVisibleItemPosition() >= getDialogsArray().size() - 10) {
                        MessagesController.getInstance().loadDialogs(-1, 100, !MessagesController.getInstance().dialogsEndReached);
                    }
                }
            }
        });

        searchEmptyView = new EmptyTextProgressView(context);
        searchEmptyView.setVisibility(View.GONE);
        searchEmptyView.setShowAtCenter(true);
        searchEmptyView.setText(LocaleController.getString("NoResult", R.string.NoResult));
        frameLayout.addView(searchEmptyView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        emptyView = new LinearLayout(context);
        emptyView.setOrientation(LinearLayout.VERTICAL);
        emptyView.setVisibility(View.GONE);
        emptyView.setGravity(Gravity.CENTER);
        frameLayout.addView(emptyView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
//        emptyView.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                return true;
//            }
//        });

        //Added Me
//        emptyView.setOnTouchListener(onTouchListener);
        //
        TextView textView = new TextView(context);
        textView.setText(LocaleController.getString("NoChats", R.string.NoChats));
        textView.setTextColor(0xff959595);
        textView.setGravity(Gravity.CENTER);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
        emptyView.addView(textView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT));

        progressView = new ProgressBar(context);

        progressView.setVisibility(View.GONE);
        frameLayout.addView(progressView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER));

        //        try {
//            new GetList().execute() ;
//
//        }catch (Exception e){
//            Log.e("log" , e.getMessage()) ;
//        }

    }

    private void updatetextLockCount() {
        String count = ApplicationLoader.databaseHandler.countLock() ;
        textLockCount.setText(
                LocaleController.getString("lock_count" , R.string.lock_count).replace("count" , count)
                , true);
    }

    private ArrayList<TLRPC.TL_dialog> getDialogsArray() {
        return MessagesController.getInstance().dialogsBackUp ;
    }

    private void createTabs(final Context context) {
        refreshTabAndListViews(false);
        dialogsType = 0 ;
        dialogsAdapter = new DialogsAdapterLock(context, dialogsType);
        listView.setAdapter(dialogsAdapter);
        dialogsAdapter.notifyDataSetChanged();
        tabsLayout = new LinearLayout(context);
        tabsLayout.setOrientation(LinearLayout.HORIZONTAL);
        tabsLayout.setGravity(Gravity.CENTER);
        try {
            tabsLayout.setBackgroundColor(context.getResources().getColor(R.color.back_tabs_channel_lock));
        } catch (Exception e) {

        }
        //1
        allTab = new ImageView(context);
        //allTab.setScaleType(ImageView.ScaleType.CENTER);
        allTab.setImageResource(R.drawable.tab_all);
        //tabsLayout.addView(allTab, LayoutHelper.createLinear(0, LayoutHelper.MATCH_PARENT, 1.0f));
        addTabView(context, allTab, false);

    }


    private boolean tabsHidden;

    private void refreshTabAndListViews(boolean forceHide) {
        listView.setPadding(0, 0, 0, 0);
//        boolean hideTabs = false;
//        if (hideTabs || forceHide) {
//            tabsView.setVisibility(View.GONE);
//            listView.setPadding(0, 0, 0, 0);
//        } else {
//            tabsView.setVisibility(View.VISIBLE);
//            int h = AndroidUtilities.dp(40);
//            ViewGroup.LayoutParams params = tabsView.getLayoutParams();
//            if (params != null) {
//                params.height = h;
//                tabsView.setLayoutParams(params);
//            }
//            listView.setPadding(0, h, 0, 0);
//            hideTabsAnimated(false);
//        }
        listView.scrollToPosition(0);
    }

    private void hideTabsAnimated(final boolean hide) {
        if (tabsHidden == hide) {
            return;
        }
        tabsHidden = hide;
        if (hide) listView.setPadding(0, 0, 0, 0);
        ObjectAnimatorProxy animator = ObjectAnimatorProxy.ofFloatProxy(tabsView, "translationY", hide ? -AndroidUtilities.dp(40) : 0).setDuration(300);
        animator.addListener(new AnimatorListenerAdapterProxy() {
            @Override
            public void onAnimationEnd(Object animation) {
                if (!tabsHidden) listView.setPadding(0, AndroidUtilities.dp(40), 0, 0);
            }
        });
        animator.start();
    }

    private void addTabView(Context context, ImageView iv, boolean show) {
//        show = true ;
        //SharedPreferences themePrefs = ApplicationLoader.applicationContext.getSharedPreferences(AndroidUtilities.THEME_PREFS, AndroidUtilities.THEME_PREFS_MODE);
        //int cColor = themePrefs.getInt("chatsHeaderTabCounterColor", 0xffffffff);
        //int bgColor = themePrefs.getInt("chatsHeaderTabCounterBGColor", 0xffff0000);

        iv.setScaleType(ImageView.ScaleType.CENTER);
        //int size = themePrefs.getInt("chatsHeaderTabCounterSize", 11);
        //tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, size);
//        tv.setGravity(Gravity.CENTER);
        //tv.setTextColor(cColor);

        GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.RECTANGLE);
        shape.setCornerRadius(AndroidUtilities.dp(32));

        RelativeLayout layout = new RelativeLayout(context);
        layout.addView(iv, LayoutHelper.createRelative(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
//
        if (show) {
            tabsLayout.addView(layout, LayoutHelper.createLinear(0, LayoutHelper.MATCH_PARENT, 1.0f));
        }
    }

    private void refreshAdapter(Context context) {
        refreshAdapterAndTabs(new DialogsAdapterLock(context, dialogsType));
    }

    private void refreshAdapterAndTabs(DialogsAdapterLock adapter) {
        dialogsAdapter = adapter;
        listView.setAdapter(dialogsAdapter);
        dialogsAdapter.notifyDataSetChanged();
        refreshTabs();
    }

    private void refreshTabs() {
//        resetTabs();
        SharedPreferences themePrefs = ApplicationLoader.applicationContext.getSharedPreferences(AndroidUtilities.THEME_PREFS, Context.MODE_PRIVATE);
        int defColor = themePrefs.getInt("chatsHeaderIconsColor", 0xffffffff);
        int iconColor = themePrefs.getInt("chatsHeaderTabIconColor", defColor);

        int iColor = themePrefs.getInt("chatsHeaderTabUnselectedIconColor", AndroidUtilities.getIntAlphaColor("chatsHeaderTabIconColor", defColor, 0.3f));

        allTab.setBackgroundResource(0);
        usersTab.setBackgroundResource(0);
        groupsTab.setBackgroundResource(0);
        superGroupsTab.setBackgroundResource(0);
        channelsTab.setBackgroundResource(0);
        botsTab.setBackgroundResource(0);
        favsTab.setBackgroundResource(0);

        allTab.setColorFilter(iColor, PorterDuff.Mode.SRC_IN);
        usersTab.setColorFilter(iColor, PorterDuff.Mode.SRC_IN);
        groupsTab.setColorFilter(iColor, PorterDuff.Mode.SRC_IN);
        superGroupsTab.setColorFilter(iColor, PorterDuff.Mode.SRC_IN);
        channelsTab.setColorFilter(iColor, PorterDuff.Mode.SRC_IN);
        botsTab.setColorFilter(iColor, PorterDuff.Mode.SRC_IN);
        favsTab.setColorFilter(iColor, PorterDuff.Mode.SRC_IN);

        Drawable selected = getParentActivity().getResources().getDrawable(R.drawable.tab_selected);
        selected.setColorFilter(iconColor, PorterDuff.Mode.SRC_IN);

        switch (dialogsType == 9 ? 4 : dialogsType) {
            case 3:
                usersTab.setColorFilter(iconColor, PorterDuff.Mode.SRC_IN);
                usersTab.setBackgroundDrawable(selected);
                break;
            case 4:
            case 2:
                groupsTab.setColorFilter(iconColor, PorterDuff.Mode.SRC_IN);
                groupsTab.setBackgroundDrawable(selected);
                break;
            case 5:
                channelsTab.setColorFilter(iconColor, PorterDuff.Mode.SRC_IN);
                channelsTab.setBackgroundDrawable(selected);
                break;
            case 6:
                botsTab.setColorFilter(iconColor, PorterDuff.Mode.SRC_IN);
                botsTab.setBackgroundDrawable(selected);
                break;
            case 7:
                superGroupsTab.setColorFilter(iconColor, PorterDuff.Mode.SRC_IN);
                superGroupsTab.setBackgroundDrawable(selected);
                break;
            case 8:
                favsTab.setColorFilter(iconColor, PorterDuff.Mode.SRC_IN);
                favsTab.setBackgroundDrawable(selected);
                break;
            default:
                allTab.setColorFilter(iconColor, PorterDuff.Mode.SRC_IN);
                allTab.setBackgroundDrawable(selected);
        }
    }

    @Override
    public void didReceivedNotification(int id, Object... args) {
    }



    boolean hideTabs = false;
    private float touchPositionDP;

    private void refreshDialogType(int d) {
        if (hideTabs) return;
        SharedPreferences plusPreferences = ApplicationLoader.applicationContext.getSharedPreferences(Pref.MAIN, Activity.MODE_PRIVATE);
        boolean hideUsers = plusPreferences.getBoolean("hideUsers", false);
        boolean hideGroups = plusPreferences.getBoolean("hideGroups", false);
        boolean hideSGroups = plusPreferences.getBoolean("hideSGroups", false);
        boolean hideChannels = plusPreferences.getBoolean("hideChannels", false);
        boolean hideBots = plusPreferences.getBoolean("hideBots", false);
//        boolean hideFavs = plusPreferences.getBoolean("hideFavs", false);
        boolean hideFavs = true;
        boolean loop = plusPreferences.getBoolean("infiniteTabsSwipe", false);
        if (d == 1) {
            switch (dialogsType) {
                case 3: // Users
                    //AddedMe
                    dialogsType = 2;
//                    if(hideGroups){
//                        dialogsType = !hideSGroups ? 7 : !hideChannels ? 5 : !hideBots ? 6 : !hideFavs ? 8 : loop ? 0 : dialogsType;
//                    }else{
//                        dialogsType = hideSGroups ? 9 : 4;
//                    }
                    break;
                case 4: //Groups
                case 2: //Groups
                    dialogsType = !hideSGroups ? 7 : !hideChannels ? 5 : !hideBots ? 6 : !hideFavs ? 8 : loop ? 0 : dialogsType;
                    break;
                case 9: //Groups
                case 7: //Supergroups
                    dialogsType = !hideChannels ? 5 : !hideBots ? 6 : !hideFavs ? 8 : loop ? 0 : dialogsType;
                    break;
                case 5: //Channels
                    dialogsType = !hideBots ? 6 : !hideFavs ? 8 : loop ? 0 : dialogsType;
                    break;
                case 6: //Bots
                    dialogsType = !hideFavs ? 8 : loop ? 0 : dialogsType;
                    break;
                case 8: //Favorites
                    if (loop) {
                        dialogsType = 0;
                    }
                    break;
                default: //All
                    dialogsType = !hideUsers ? 3 : !hideGroups && hideSGroups ? 9 : !hideGroups ? 7 : !hideChannels ? 5 : !hideBots ? 6 : !hideFavs ? 8 : loop ? 0 : dialogsType;
            }
        } else {
            switch (dialogsType) {
                case 3: // Users
                    dialogsType = 3;
                    break;
                case 4: //Groups
                case 9: //Groups
                case 2: //Groups
                    dialogsType = !hideUsers ? 3 : 0;
                    break;
                case 7: //Supergroups
                    //AddedMe
//                    dialogsType = !hideGroups ? 4 : !hideUsers ? 3 : 0;
                    dialogsType = 2;
                    break;
                case 5: //Channels
                    dialogsType = !hideSGroups ? 7 : !hideGroups ? 9 : !hideUsers ? 3 : 0;
                    break;
                case 6: //Bots

                    dialogsType = !hideChannels ? 5 : !hideSGroups ? 7 : !hideGroups ? 9 : !hideUsers ? 3 : 0;
                    break;
                case 8: //Favorites
                    dialogsType = !hideBots ? 6 : !hideChannels ? 5 : !hideSGroups ? 7 : !hideGroups ? 9 : !hideUsers ? 3 : 0;
                    break;
                default: //All
                    if (loop) {
                        dialogsType = !hideFavs ? 8 : !hideBots ? 6 : !hideChannels ? 5 : !hideSGroups ? 7 : !hideGroups ? 9 : !hideUsers ? 3 : 0;
                    }
            }
        }
    }
}
