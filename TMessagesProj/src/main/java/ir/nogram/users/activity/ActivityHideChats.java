package ir.nogram.users.activity;

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
import ir.nogram.SQLite.FaveHide;
import ir.nogram.messanger.AndroidUtilities;
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
import ir.nogram.users.activity.adapter.DialogsAdapterHide;
import ir.nogram.users.activity.holder.HoldArgs;


/**
 * Created by MhkDeveloper on 2016-10-10.
 */
public class ActivityHideChats extends BaseFragment implements NotificationCenter.NotificationCenterDelegate {
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
    public ActivityHideChats() {

    }
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        MessagesController.getInstance().sortDialogs(null);
        NotificationCenter.getInstance().postNotificationName(NotificationCenter.dialogsNeedReload);
    }

    @Override
    public boolean onFragmentCreate() {
        super.onFragmentCreate();
        swipeBackEnabled = false;
//        NotificationCenter.getInstance().addObserver(this, NotificationCenter.dialogsNeedReload);
        if (!dialogsLoaded) {
            MessagesController.getInstance().loadDialogs(0, 100, true);
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
        actionBar.setTitle(context.getString(R.string.HideChats)
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
    int dialogsType = 0;
    DialogsAdapterHide dialogsAdapter;
    private LinearLayout tabsLayout;
    private LinearLayout layoutParent;
      TextSettingsCell textLockCount ;
    private void initialWidget() {
//        listView = (ListView) view.findViewById(R.id.lst_block_channesls);
//         channelList = MessagesController.getInstance().dialogsChannelsBackUp;
//         AdapterChannellocked adapterChannellocked = new AdapterChannellocked(context ,channelList , user_id) ;
        //Added Me
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
        textLockDialog.setText(context.getString(R.string.set_pattern_hide), true);
        textLockDialog.setLayoutParams(new ViewGroup.LayoutParams(LayoutHelper.MATCH_PARENT ,  AndroidUtilities.dp(40)));
        textLockDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle() ;
                bundle.putInt(HoldArgs.typePattern, DatabaseHandler.PATTERN_FOR_HIDE );
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

        //if(dialogsType == 0 || dialogsType > 2){
        layoutParent.addView(tabsView ,LayoutHelper.MATCH_PARENT , AndroidUtilities.dp(2));
        //}
        //channels list


        layoutParent.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        //Added Me
        //Added Me
        // onntaouch listner = new DialogsOnToautch

//        dialogsType = 3 ;
//        dialogsAdapter = new DialogsAdapterLock(context, dialogsType);
//
//        listView.setAdapter(dialogsAdapter);
//        frameLayout.addView(textLockDialog, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 60, Gravity.TOP, 0, 0, 0, 0));
        frameLayout.addView(layoutParent);

        listView.setOnItemClickListener(new RecyclerListView.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (listView == null || listView.getAdapter() == null) {
                    return;
                }
                int dLock = ApplicationLoader.databaseHandler.getLock(DatabaseHandler.PATTERN_FOR_HIDE) ;
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
                if(FaveHide.isHidenDialog((int)dialog_id )) {
                   FaveHide.deleteHide((int)dialog_id );
                }else {
                    FaveHide.addHide((int)dialog_id);
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

    }

    private void updatetextLockCount() {
        String count = ApplicationLoader.databaseHandler.countHide() ;
        textLockCount.setText(
                LocaleController.getString("hidden_count" , R.string.hidden_count).replace("count" , count)
                , true);
    }

    private ArrayList<TLRPC.TL_dialog> getDialogsArray() {
            return MessagesController.getInstance().dialogsBackUp ;
    }

    private void createTabs(final Context context) {
        refreshTabAndListViews(false);
            dialogsType = 0 ;
            dialogsAdapter = new DialogsAdapterHide(context, dialogsType);
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
//
        listView.scrollToPosition(0);
    }


    private void addTabView(Context context, ImageView iv, boolean show) {
//
        iv.setScaleType(ImageView.ScaleType.CENTER);


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
        refreshAdapterAndTabs(new DialogsAdapterHide(context, dialogsType));
    }

    private void refreshAdapterAndTabs(DialogsAdapterHide adapter) {
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


        allTab.setColorFilter(iColor, PorterDuff.Mode.SRC_IN);


        Drawable selected = getParentActivity().getResources().getDrawable(R.drawable.tab_selected);
        selected.setColorFilter(iconColor, PorterDuff.Mode.SRC_IN);

                allTab.setColorFilter(iconColor, PorterDuff.Mode.SRC_IN);
                allTab.setBackgroundDrawable(selected);
    }

    @Override
    public void didReceivedNotification(int id, Object... args) {
//        if (id == NotificationCenter.dialogsNeedReload) {
//            if (dialogsAdapter != null) {
//                if (dialogsAdapter.isDataSetChanged()) {
//                    dialogsAdapter.notifyDataSetChanged();
//                } else {
//                    updateVisibleRows(MessagesController.UPDATE_MASK_NEW_MESSAGE);
//                }
//            }
////            if (dialogsSearchAdapter != null) {
////                dialogsSearchAdapter.notifyDataSetChanged();
////            }
//            if (listView != null) {
//                try {
//                    if (MessagesController.getInstance().loadingDialogs && MessagesController.getInstance().dialogs.isEmpty()) {
//                        searchEmptyView.setVisibility(View.GONE);
//                        emptyView.setVisibility(View.GONE);
//                        listView.setEmptyView(progressView);
//                    } else {
//                        progressView.setVisibility(View.GONE);
//                        if (searching && searchWas) {
//                            emptyView.setVisibility(View.GONE);
//                            listView.setEmptyView(searchEmptyView);
//                        } else {
//                            searchEmptyView.setVisibility(View.GONE);
//                            listView.setEmptyView(emptyView);
//                        }
//                    }
//                } catch (Exception e) {
//                    FileLog.e("tmessages", e); //TODO fix it in other way?
//                }
//            }
//        }
    }

}
