/*
 * This is the source code of Telegram for Android v. 3.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2017.
 */

package ir.nogram.ui;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.animation.StateListAnimator;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Outline;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import ir.nogram.SQLite.DatabaseHandler;
import ir.nogram.SQLite.FaveHide;
import ir.nogram.messanger.AndroidUtilities;
import ir.nogram.messanger.AnimationCompat.AnimatorListenerAdapterProxy;
import ir.nogram.messanger.AnimationCompat.ObjectAnimatorProxy;
import ir.nogram.messanger.ApplicationLoader;
import ir.nogram.messanger.BuildVars;
import ir.nogram.messanger.ChatObject;
import ir.nogram.messanger.DialogObject;
import ir.nogram.messanger.ImageLoader;
import ir.nogram.messanger.LocaleController;
import ir.nogram.messanger.MessageObject;
import ir.nogram.messanger.UserObject;
import ir.nogram.messanger.VideoEditedInfo;
import ir.nogram.messanger.query.SearchQuery;
import ir.nogram.messanger.query.StickersQuery;
import ir.nogram.messanger.support.widget.LinearLayoutManager;
import ir.nogram.messanger.support.widget.RecyclerView;
import ir.nogram.messanger.FileLog;
import ir.nogram.tgnet.TLRPC;
import ir.nogram.messanger.ContactsController;
import ir.nogram.messanger.MessagesController;
import ir.nogram.messanger.MessagesStorage;
import ir.nogram.messanger.NotificationCenter;
import ir.nogram.messanger.R;
import ir.nogram.messanger.UserConfig;
import ir.nogram.ui.ActionBar.AlertDialog;
import ir.nogram.ui.ActionBar.BottomSheet;
import ir.nogram.ui.ActionBar.ThemeDescription;
import ir.nogram.ui.Adapters.DialogsAdapter;
import ir.nogram.ui.Adapters.DialogsSearchAdapter;
import ir.nogram.ui.Cells.DividerCell;
import ir.nogram.ui.Cells.DrawerActionCell;
import ir.nogram.ui.Cells.DrawerProfileCell;
import ir.nogram.ui.Cells.GraySectionCell;
import ir.nogram.ui.Cells.HashtagSearchCell;
import ir.nogram.ui.Cells.HintDialogCell;
import ir.nogram.ui.Cells.LoadingCell;
import ir.nogram.ui.Cells.ProfileSearchCell;
import ir.nogram.ui.Cells.UserCell;
import ir.nogram.ui.Cells.DialogCell;
import ir.nogram.ui.ActionBar.ActionBar;
import ir.nogram.ui.ActionBar.ActionBarMenu;
import ir.nogram.ui.ActionBar.ActionBarMenuItem;
import ir.nogram.ui.ActionBar.BaseFragment;
import ir.nogram.ui.ActionBar.MenuDrawable;
import ir.nogram.ui.Components.BackupImageView;
import ir.nogram.ui.Components.CombinedDrawable;
import ir.nogram.ui.Components.FragmentContextView;
import ir.nogram.ui.Components.EmptyTextProgressView;
import ir.nogram.ui.Components.LayoutHelper;
import ir.nogram.ui.Components.RadialProgressView;
import ir.nogram.ui.Components.RecyclerListView;
import ir.nogram.ui.ActionBar.Theme;
import ir.nogram.users.activity.ActivityHideChats;
import ir.nogram.users.activity.AddCatDialogNameActivity;
import ir.nogram.users.activity.GhostControlActivity;
import ir.nogram.users.activity.db.DatabaseMain;
import ir.nogram.users.activity.dialogs.DialogGetPassLock;
import ir.nogram.users.activity.dialogs.DialogWebView;
import ir.nogram.users.activity.helper.Ghost;
import ir.nogram.users.activity.holder.HoldArgs;
import ir.nogram.users.activity.holder.HoldCatDialog;
import ir.nogram.users.activity.holder.HoldConst;
import ir.nogram.users.activity.holder.Pref;

import java.util.ArrayList;

public class DialogsActivity extends BaseFragment implements NotificationCenter.NotificationCenterDelegate  , PhotoViewer.PhotoViewerProvider{

    //HoseinKord
    SharedPreferences boshraPrefe;
    SharedPreferences.Editor editor ;
    private boolean hideAll ,
            hideUsers,
            hideGroupsAll,
            hideGroups,
            hideSGroups,
            hideChannels,
            hideBots,
            hideFavs;
    private int user_id = 0;
    private int chat_id = 0;
    private ActionBarMenuItem ghost;
    //

    private RecyclerListView listView;
    private LinearLayoutManager layoutManager;
    private DialogsAdapter dialogsAdapter;
    private DialogsSearchAdapter dialogsSearchAdapter;
    private EmptyTextProgressView searchEmptyView;
    private RadialProgressView progressView;
    private LinearLayout emptyView;
    private ActionBarMenuItem passcodeItem;
    private ImageView floatingButton;
    private RecyclerView sideMenu;
    private FragmentContextView fragmentContextView;

    private TextView emptyTextView1;
    private TextView emptyTextView2;

    private AlertDialog permissionDialog;

    private int prevPosition;
    private int prevTop;
    private boolean scrollUpdated;
    private boolean floatingHidden;
    private final AccelerateDecelerateInterpolator floatingInterpolator = new AccelerateDecelerateInterpolator();

    private boolean checkPermission = true;

    private String selectAlertString;
    private String selectAlertStringGroup;
    private String addToGroupAlertString;
    private int dialogsType = 0 ;

    public static boolean dialogsLoaded;
    private boolean searching;
    private boolean searchWas;
    private boolean onlySelect;
    private long selectedDialog;
    private String searchString;
    private long openedDialogId;
    private boolean cantSendToChannels;

    private DialogsActivityDelegate delegate;

    //HoseinKord
    private BackupImageView avatarImage;
    private  DialogsOnTouch onTouchListener = null;
    private float touchPositionDP;
    private boolean hideTabs;
    private int selectedTab;
    private DialogsAdapter dialogsBackupAdapter;
    private boolean tabsHidden;
    private boolean disableAnimation;
    private FrameLayout tabsView;
    private LinearLayout tabsLayout;
    private int tabsHeight;
    private ImageView allTab;
    private ImageView usersTab;
    private ImageView groupsTab;
    private ImageView superGroupsTab;
    private ImageView allGroupsTab;
    private ImageView channelsTab;
    private ImageView botsTab;
    private ImageView favsTab;
    private TextView allCounter;
    private TextView usersCounter;
    private TextView groupsCounter;
    private TextView allGroupsCounter;
    private TextView sGroupsCounter;
    private TextView botsCounter;
    private TextView channelsCounter;
    private TextView favsCounter;
    private boolean countSize;
    private boolean justHidendialogs = false ;

    private ActionBarMenuItem catDialogMenu;

    private int catDialog = 36 ;
    //

    public interface DialogsActivityDelegate {
        void didSelectDialog(DialogsActivity fragment, long dialog_id, boolean param);
    }

    public DialogsActivity(Bundle args) {
        super(args);
    }

    @Override
    public boolean onFragmentCreate() {
        super.onFragmentCreate();
        swipeBackEnabled = false;

        if (getArguments() != null) {
            justHidendialogs = arguments.getBoolean(HoldArgs.jsutHiddenDialogs , false);
            if(justHidendialogs){

                ApplicationLoader.sharedPreferencesMain.edit().putBoolean(Pref.HideDialogs , false).apply();
                MessagesController.getInstance().sortDialogs(null);
                if(dialogsAdapter != null){
                    dialogsAdapter.notifyDataSetChanged();
                }
            }else{
                ApplicationLoader.sharedPreferencesMain.edit().putBoolean(Pref.HideDialogs , true).apply();
                MessagesController.getInstance().sortDialogs(null);
                if(dialogsAdapter != null){
                    dialogsAdapter.notifyDataSetChanged();
                }
            }

            onlySelect = arguments.getBoolean("onlySelect", false);
            cantSendToChannels = arguments.getBoolean("cantSendToChannels", false);
            dialogsType = arguments.getInt("dialogsType", 0);
            selectAlertString = arguments.getString("selectAlertString");
            selectAlertStringGroup = arguments.getString("selectAlertStringGroup");
            addToGroupAlertString = arguments.getString("addToGroupAlertString");
        }else{
            ApplicationLoader.sharedPreferencesMain.edit().putBoolean(Pref.HideDialogs , true).apply();
            MessagesController.getInstance().sortDialogs(null);
            if(dialogsAdapter != null){
                dialogsAdapter.notifyDataSetChanged();
            }
        }


        if (searchString == null) {
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.ghostUpdated);

            NotificationCenter.getInstance().addObserver(this, NotificationCenter.dialogsNeedReload);
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.emojiDidLoaded);
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.updateInterfaces);
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.encryptedChatUpdated);
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.contactsDidLoaded);
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.appDidLogout);
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.openedChatChanged);
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.notificationsSettingsUpdated);
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.messageReceivedByAck);
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.messageReceivedByServer);
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.messageSendError);
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.didSetPasscode);
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.needReloadRecentDialogsSearch);
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.didLoadedReplyMessages);
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.reloadHints);

            //HoseinKord
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.refreshTabs);
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.didChangedColorTheme);


        }


        if (!dialogsLoaded) {
            MessagesController.getInstance().loadDialogs(0, 100, true);
            ContactsController.getInstance().checkInviteText();
            MessagesController.getInstance().loadPinnedDialogs(0, null);
            StickersQuery.checkFeaturedStickers();
            dialogsLoaded = true;
        }
        return true;
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        if (searchString == null) {
            //HoseinKord
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.refreshTabs);
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.didChangedColorTheme);
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.ghostUpdated);

            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.dialogsNeedReload);
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.emojiDidLoaded);
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.updateInterfaces);
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.encryptedChatUpdated);
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.contactsDidLoaded);
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.appDidLogout);
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.openedChatChanged);
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.notificationsSettingsUpdated);
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.messageReceivedByAck);
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.messageReceivedByServer);
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.messageSendError);
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.didSetPasscode);
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.needReloadRecentDialogsSearch);
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.didLoadedReplyMessages);
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.reloadHints);
        }
        if(arguments != null){
            justHidendialogs = arguments.getBoolean(HoldArgs.jsutHiddenDialogs , false);
            if(justHidendialogs){
                ApplicationLoader.sharedPreferencesMain.edit().putBoolean(Pref.HideDialogs , true).apply();
                MessagesController.getInstance().sortDialogs(null);
                NotificationCenter.getInstance().postNotificationName(NotificationCenter.dialogsNeedReload);
                if(dialogsAdapter != null){
                    dialogsAdapter.notifyDataSetChanged();
                }
            }
        }

        delegate = null;
    }

    @Override
    public View createView(final Context context) {
        searching = false;
        searchWas = false;

        //Hoseinkord
//        actionBar.setBackgroundColor(ThemeManager.getColor());
        avatarImage = new BackupImageView(context);
        avatarImage.setRoundRadius(AndroidUtilities.dp(30));
        //

        AndroidUtilities.runOnUIThread(new Runnable() {
            @Override
            public void run() {
                Theme.createChatResources(context, false);
            }
        });

        ActionBarMenu menu = actionBar.createMenu();
        catDialogMenu = menu.addItem(catDialog , R.drawable.ic_category) ;
        boolean ghostEn = Ghost.ghostForDialogsIcon() ;
        ghost = menu.addItem(10 ,ghostEn ? R.drawable.ghost_enabled :R.drawable.ghost_disabled)  ;
//        ghost.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View v) {
//                openGallery(selectedDialog);
//                return true;
//            }
//        });

        if (!onlySelect && searchString == null) {
            passcodeItem = menu.addItem(1, R.drawable.lock_close);
            updatePasscodeButton();
        }
        final ActionBarMenuItem item = menu.addItem(0, R.drawable.ic_ab_search).setIsSearchField(true).setActionBarMenuItemSearchListener(new ActionBarMenuItem.ActionBarMenuItemSearchListener() {
            @Override
            public void onSearchExpand() {
                searching = true;
                //HoseinKord
                refreshTabAndListViews(true);
                //

                if (listView != null) {
                    if (searchString != null) {
                        listView.setEmptyView(searchEmptyView);
                        progressView.setVisibility(View.GONE);
                        emptyView.setVisibility(View.GONE);
                    }
                    if (!onlySelect) {
                        floatingButton.setVisibility(View.GONE);
//                        actionButton.setVisibility(View.GONE);
                    }
                }
                updatePasscodeButton();
            }

            @Override
            public boolean canCollapseSearch() {
                //HoseinKord
                refreshTabAndListViews(false);
                //

                if (searchString != null) {
                    finishFragment();
                    return false;
                }
                return true;
            }

            @Override
            public void onSearchCollapse() {
                //HoseinKord
                refreshTabAndListViews(false);
                //

                searching = false;
                searchWas = false;
                if (listView != null) {
                    searchEmptyView.setVisibility(View.GONE);
                    if (MessagesController.getInstance().loadingDialogs && MessagesController.getInstance().dialogs.isEmpty()) {
                        emptyView.setVisibility(View.GONE);
                        listView.setEmptyView(progressView);
                    } else {
                        progressView.setVisibility(View.GONE);
                        listView.setEmptyView(emptyView);
                    }
                    if (!onlySelect) {
                        floatingButton.setVisibility(View.VISIBLE);
//                        actionButton.setVisibility(View.VISIBLE);
                        floatingHidden = true;
                        floatingButton.setTranslationY(AndroidUtilities.dp(100));
//                        actionButton.setTranslationY(AndroidUtilities.dp(100));
                        hideFloatingButton(false);
                    }
                    if (listView.getAdapter() != dialogsAdapter) {
                        listView.setAdapter(dialogsAdapter);
                        dialogsAdapter.notifyDataSetChanged();
                    }
                }
                if (dialogsSearchAdapter != null) {
                    dialogsSearchAdapter.searchDialogs(null);
                }
                updatePasscodeButton();
            }

            @Override
            public void onTextChanged(EditText editText) {
                String text = editText.getText().toString();
                if (text.length() != 0 || dialogsSearchAdapter != null && dialogsSearchAdapter.hasRecentRearch()) {
                    searchWas = true;
                    if (dialogsSearchAdapter != null && listView.getAdapter() != dialogsSearchAdapter) {
                        listView.setAdapter(dialogsSearchAdapter);
                        dialogsSearchAdapter.notifyDataSetChanged();
                    }
                    if (searchEmptyView != null && listView.getEmptyView() != searchEmptyView) {
                        emptyView.setVisibility(View.GONE);
                        progressView.setVisibility(View.GONE);
                        searchEmptyView.showTextView();
                        listView.setEmptyView(searchEmptyView);
                    }
                }
                if (dialogsSearchAdapter != null) {
                    dialogsSearchAdapter.searchDialogs(text);
                }
            }
        });
        item.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return longClickForHidden() ;
            }
        });

        item.getSearchField().setHint(LocaleController.getString("Search", R.string.Search));
        if (onlySelect) {
            actionBar.setBackButtonImage(R.drawable.ic_ab_back);
            actionBar.setTitle(LocaleController.getString("SelectChat", R.string.SelectChat));
        } else {
            if (searchString != null) {
                actionBar.setBackButtonImage(R.drawable.ic_ab_back);
            } else {
                actionBar.setBackButtonDrawable(new MenuDrawable());
            }
            if (BuildVars.DEBUG_VERSION) {
                actionBar.setTitle(LocaleController.getString("AppNameBeta", R.string.AppNameBeta));
            } else {
                actionBar.setTitle(LocaleController.getString("AppName", R.string.AppName));
            }
        }
        actionBar.setAllowOverlayTitle(true);

        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    if (onlySelect) {
                        finishFragment();
                    } else if (parentLayout != null) {
                        parentLayout.getDrawerLayoutContainer().openDrawer(false);
                    }
                } else if (id == 1) {
                    UserConfig.appLocked = !UserConfig.appLocked;
                    UserConfig.saveConfig(false);
                    updatePasscodeButton();
                }else if(id == 10){
                    presentFragment(new GhostControlActivity(1));

                }else if(id == catDialog){
                    showDialogSelectCat() ;
                }
            }
        });

        if (sideMenu != null) {
            sideMenu.setBackgroundColor(Theme.getColor(Theme.key_chats_menuBackground));
            sideMenu.setGlowColor(Theme.getColor(Theme.key_chats_menuBackground));
            sideMenu.getAdapter().notifyDataSetChanged();
        }

        FrameLayout frameLayout = new FrameLayout(context);
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
        listView.setVerticalScrollbarPosition(LocaleController.isRTL ? RecyclerListView.SCROLLBAR_POSITION_LEFT : RecyclerListView.SCROLLBAR_POSITION_RIGHT);
        frameLayout.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));


        //HoseinKord
        onTouchListener = new DialogsOnTouch(context);
        listView.setOnTouchListener(onTouchListener);
        //


        listView.setOnItemClickListener(new RecyclerListView.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (listView == null || listView.getAdapter() == null) {
                    return;
                }

                long dialog_id = 0;
                int message_id = 0;
                RecyclerView.Adapter adapter = listView.getAdapter();
                if (adapter == dialogsAdapter) {
                    TLRPC.TL_dialog dialog = dialogsAdapter.getItem(position);
                    if (dialog == null) {
                        return;
                    }
                    dialog_id = dialog.id;
                } else if (adapter == dialogsSearchAdapter) {
                    Object obj = dialogsSearchAdapter.getItem(position);
                    if (obj instanceof TLRPC.User) {
                        dialog_id = ((TLRPC.User) obj).id;
                        if (dialogsSearchAdapter.isGlobalSearch(position)) {
                            ArrayList<TLRPC.User> users = new ArrayList<>();
                            users.add((TLRPC.User) obj);
                            MessagesController.getInstance().putUsers(users, false);
                            MessagesStorage.getInstance().putUsersAndChats(users, null, false, true);
                        }
                        if (!onlySelect) {
                            dialogsSearchAdapter.putRecentSearch(dialog_id, (TLRPC.User) obj);
                        }
                    } else if (obj instanceof TLRPC.Chat) {
                        if (dialogsSearchAdapter.isGlobalSearch(position)) {
                            ArrayList<TLRPC.Chat> chats = new ArrayList<>();
                            chats.add((TLRPC.Chat) obj);
                            MessagesController.getInstance().putChats(chats, false);
                            MessagesStorage.getInstance().putUsersAndChats(null, chats, false, true);
                        }
                        if (((TLRPC.Chat) obj).id > 0) {
                            dialog_id = -((TLRPC.Chat) obj).id;
                        } else {
                            dialog_id = AndroidUtilities.makeBroadcastId(((TLRPC.Chat) obj).id);
                        }
                        if (!onlySelect) {
                            dialogsSearchAdapter.putRecentSearch(dialog_id, (TLRPC.Chat) obj);
                        }
                    } else if (obj instanceof TLRPC.EncryptedChat) {
                        dialog_id = ((long) ((TLRPC.EncryptedChat) obj).id) << 32;
                        if (!onlySelect) {
                            dialogsSearchAdapter.putRecentSearch(dialog_id, (TLRPC.EncryptedChat) obj);
                        }
                    } else if (obj instanceof MessageObject) {
                        MessageObject messageObject = (MessageObject) obj;
                        dialog_id = messageObject.getDialogId();
                        message_id = messageObject.getId();
                        dialogsSearchAdapter.addHashtagsFromMessage(dialogsSearchAdapter.getLastSearchString());
                    } else if (obj instanceof String) {
                        actionBar.openSearchField((String) obj);
                    }
                }

                if (dialog_id == 0) {
                    return;
                }


                //HoseinKord
                if (touchPositionDP < 65) {
                    SharedPreferences plusPreferences = ApplicationLoader.applicationContext.getSharedPreferences(HoldConst.PREF_MAIN, Activity.MODE_PRIVATE);
                    //if(preferences.getInt("dialogsClickOnGroupPic", 0) == 2)MessagesController.getInstance().loadChatInfo(chat_id, null, false);
                    user_id = 0;
                    chat_id = 0;
                    int lower_part = (int) dialog_id;
                    int high_id = (int) (dialog_id >> 32);

                    if (lower_part != 0) {
                        if (high_id == 1) {
                            chat_id = lower_part;
                            if (lower_part > 0) {
                                user_id = lower_part;
                            } else if (lower_part < 0) {
                                chat_id = -lower_part;
                            }
                        }
                    } else {
                        TLRPC.EncryptedChat chat = MessagesController.getInstance().getEncryptedChat(high_id);
                        user_id = chat.user_id;
                    }

                    if (user_id != 0) {
                        int picClick = plusPreferences.getInt("dialogsClickOnPic", 0);
                        if (picClick == 2) {
                            Bundle args = new Bundle();

                            args.putInt("user_id", user_id);
                            presentFragment(new ProfileActivity(args));
                            return;
                        } else if (picClick == 1) {
                            TLRPC.User user = MessagesController.getInstance().getUser(user_id);
                            if (user.photo != null && user.photo.photo_big != null) {
                                PhotoViewer.getInstance().setParentActivity(getParentActivity());
                                PhotoViewer.getInstance().openPhoto(user.photo.photo_big, DialogsActivity.this);
                            }
                            return;
                        }

                    } else if (chat_id != 0) {
                        int picClick = plusPreferences.getInt("dialogsClickOnGroupPic", 0);
                        if (picClick == 2) {
                            MessagesController.getInstance().loadChatInfo(chat_id, null, false);
                            Bundle args = new Bundle();
                            args.putInt("chat_id", chat_id);
                            ProfileActivity fragment = new ProfileActivity(args);
                            presentFragment(fragment);
                            return;
                        } else if (picClick == 1) {
                            TLRPC.Chat chat = MessagesController.getInstance().getChat(chat_id);
                            if (chat.photo != null && chat.photo.photo_big != null) {
                                PhotoViewer.getInstance().setParentActivity(getParentActivity());
                                PhotoViewer.getInstance().openPhoto(chat.photo.photo_big, DialogsActivity.this);
                            }
                            return;
                        }
                    }
                }

                //

                if (onlySelect) {
                    didSelectResult(dialog_id, true, false);
                } else {
                    Bundle args = new Bundle();
                    int lower_part = (int) dialog_id;
                    int high_id = (int) (dialog_id >> 32);
                    if (lower_part != 0) {
                        if (high_id == 1) {
                            args.putInt("chat_id", lower_part);
                        } else {
                            if (lower_part > 0) {
                                args.putInt("user_id", lower_part);
                            } else if (lower_part < 0) {
                                if (message_id != 0) {
                                    TLRPC.Chat chat = MessagesController.getInstance().getChat(-lower_part);
                                    if (chat != null && chat.migrated_to != null) {
                                        args.putInt("migrated_to", lower_part);
                                        lower_part = -chat.migrated_to.channel_id;
                                    }
                                }
                                args.putInt("chat_id", -lower_part);
                            }
                        }
                    } else {
                        args.putInt("enc_id", high_id);
                    }
                    if (message_id != 0) {
                        args.putInt("message_id", message_id);
                    } else {
                        if (actionBar != null) {
                            actionBar.closeSearchField();
                        }
                    }
                    if (AndroidUtilities.isTablet()) {
                        if (openedDialogId == dialog_id && adapter != dialogsSearchAdapter) {
                            return;
                        }
                        if (dialogsAdapter != null) {
                            dialogsAdapter.setOpenedDialogId(openedDialogId = dialog_id);
                            updateVisibleRows(MessagesController.UPDATE_MASK_SELECT_DIALOG);
                        }
                    }
                    if (searchString != null) {
                        if (MessagesController.checkCanOpenChat(args, DialogsActivity.this)) {
                            NotificationCenter.getInstance().postNotificationName(NotificationCenter.closeChats);
                            presentFragment(new ChatActivity(args));
                        }
                    } else {
                        if (MessagesController.checkCanOpenChat(args, DialogsActivity.this)) {
                            presentFragment(new ChatActivity(args));
                        }
                    }
                }
            }
        });
        listView.setOnItemLongClickListener(new RecyclerListView.OnItemLongClickListener() {
            @Override
            public boolean onItemClick(View view, int position) {
                if (onlySelect || searching && searchWas || getParentActivity() == null) {
                    if (searchWas && searching || dialogsSearchAdapter.isRecentSearchDisplayed()) {
                        RecyclerView.Adapter adapter = listView.getAdapter();
                        if (adapter == dialogsSearchAdapter) {
                            Object item = dialogsSearchAdapter.getItem(position);
                            if (item instanceof String || dialogsSearchAdapter.isRecentSearchDisplayed()) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                                builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
                                builder.setMessage(LocaleController.getString("ClearSearch", R.string.ClearSearch));
                                builder.setPositiveButton(LocaleController.getString("ClearButton", R.string.ClearButton).toUpperCase(), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        if (dialogsSearchAdapter.isRecentSearchDisplayed()) {
                                            dialogsSearchAdapter.clearRecentSearch();
                                        } else {
                                            dialogsSearchAdapter.clearRecentHashtags();
                                        }
                                    }
                                });
                                builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
                                showDialog(builder.create());
                                return true;
                            }
                        }
                    }
                    return false;
                }
                TLRPC.TL_dialog dialog;
                ArrayList<TLRPC.TL_dialog> dialogs = getDialogsArray();
                if (position < 0 || position >= dialogs.size()) {
                    return false;
                }
                dialog = dialogs.get(position);
                selectedDialog = dialog.id;
                final boolean pinned = dialog.pinned;

                BottomSheet.Builder builder = new BottomSheet.Builder(getParentActivity());
                int lower_id = (int) selectedDialog;
                int high_id = (int) (selectedDialog >> 32);

                if (DialogObject.isChannel(dialog)) {
                    final TLRPC.Chat chat = MessagesController.getInstance().getChat(-lower_id);
                    CharSequence items[];
                    int icons[] = new int[]{
                            dialog.pinned ? R.drawable.chats_unpin : R.drawable.chats_pin,
                            R.drawable.chats_clear,
                            R.drawable.chats_leave ,
                            R.drawable.tab_favs ,
                            R.drawable.lock_open

                    };
                    if (chat != null && chat.megagroup) {
                        items = new CharSequence[]{
                                dialog.pinned || MessagesController.getInstance().canPinDialog(false) ? (dialog.pinned ? LocaleController.getString("UnpinFromTop", R.string.UnpinFromTop) : LocaleController.getString("PinToTop", R.string.PinToTop)) : null,
                                LocaleController.getString("ClearHistoryCache", R.string.ClearHistoryCache),
                                chat == null || !chat.creator ? LocaleController.getString("LeaveMegaMenu", R.string.LeaveMegaMenu) : LocaleController.getString("DeleteMegaMenu", R.string.DeleteMegaMenu)

                                ,returnFaveItemToMenu(selectedDialog) , returnHideItemToMenu(selectedDialog)


                        };
                    } else {
                        items = new CharSequence[]{
                                dialog.pinned || MessagesController.getInstance().canPinDialog(false) ? (dialog.pinned ? LocaleController.getString("UnpinFromTop", R.string.UnpinFromTop) : LocaleController.getString("PinToTop", R.string.PinToTop)) : null,
                                LocaleController.getString("ClearHistoryCache", R.string.ClearHistoryCache),
                                chat == null || !chat.creator ? LocaleController.getString("LeaveChannelMenu", R.string.LeaveChannelMenu) : LocaleController.getString("ChannelDeleteMenu", R.string.ChannelDeleteMenu)
                                ,returnFaveItemToMenu(selectedDialog) ,returnHideItemToMenu(selectedDialog)

                        };
                    }
                    builder.setItems(items, icons, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, final int which) {

                            if(which == 4){
                                hideItemClicked(selectedDialog) ;
                                return;
                            }
                            if(which == 3){
                                faveItemClicked(selectedDialog) ;
                                return;
                            }
                            if (which == 0) {
                                if (MessagesController.getInstance().pinDialog(selectedDialog, !pinned, null, 0) && !pinned) {
                                    listView.smoothScrollToPosition(0);
                                }
                            } else {
                                AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                                builder.setTitle(LocaleController.getString("AppName", R.string.AppName));


                                if (which == 1) {
                                    if (chat != null && chat.megagroup) {
                                        builder.setMessage(LocaleController.getString("AreYouSureClearHistorySuper", R.string.AreYouSureClearHistorySuper));
                                    } else {
                                        builder.setMessage(LocaleController.getString("AreYouSureClearHistoryChannel", R.string.AreYouSureClearHistoryChannel));
                                    }
                                    builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            MessagesController.getInstance().deleteDialog(selectedDialog, 2);
                                        }
                                    });
                                } else {
                                    if (chat != null && chat.megagroup) {
                                        if (!chat.creator) {
                                            builder.setMessage(LocaleController.getString("MegaLeaveAlert", R.string.MegaLeaveAlert));
                                        } else {
                                            builder.setMessage(LocaleController.getString("MegaDeleteAlert", R.string.MegaDeleteAlert));
                                        }
                                    } else {
                                        if (chat == null || !chat.creator) {
                                            builder.setMessage(LocaleController.getString("ChannelLeaveAlert", R.string.ChannelLeaveAlert));
                                        } else {
                                            builder.setMessage(LocaleController.getString("ChannelDeleteAlert", R.string.ChannelDeleteAlert));
                                        }
                                    }
                                    builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            MessagesController.getInstance().deleteUserFromChat((int) -selectedDialog, UserConfig.getCurrentUser(), null);
                                            if (AndroidUtilities.isTablet()) {
                                                NotificationCenter.getInstance().postNotificationName(NotificationCenter.closeChats, selectedDialog);
                                            }
                                        }
                                    });
                                }
                                builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
                                showDialog(builder.create());
                            }
                        }
                    });
                    showDialog(builder.create());
                } else {
                    final boolean isChat = lower_id < 0 && high_id != 1;
                    TLRPC.User user = null;
                    if (!isChat && lower_id > 0 && high_id != 1) {
                        user = MessagesController.getInstance().getUser(lower_id);
                    }
                    final boolean isBot = user != null && user.bot;

                    builder.setItems(new CharSequence[]{
                            dialog.pinned || MessagesController.getInstance().canPinDialog(lower_id == 0) ? (dialog.pinned ? LocaleController.getString("UnpinFromTop", R.string.UnpinFromTop) : LocaleController.getString("PinToTop", R.string.PinToTop)) : null,
                            LocaleController.getString("ClearHistory", R.string.ClearHistory),
                            isChat ? LocaleController.getString("DeleteChat", R.string.DeleteChat) : isBot ? LocaleController.getString("DeleteAndStop", R.string.DeleteAndStop) : LocaleController.getString("Delete", R.string.Delete)
                            ,returnFaveItemToMenu(selectedDialog)
                            ,returnHideItemToMenu(selectedDialog) ,

                    }, new int[]{
                            dialog.pinned ? R.drawable.chats_unpin : R.drawable.chats_pin,
                            R.drawable.chats_clear,
                            isChat ? R.drawable.chats_leave : R.drawable.chats_delete
                            ,
                            R.drawable.tab_favs ,
                            R.drawable.lock_open


                    }, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, final int which) {

                            if(which == 4){
                                hideItemClicked(selectedDialog) ;
                                return;
                            }
                            if(which == 3){
                                faveItemClicked(selectedDialog) ;
                                return;
                            }

                            if (which == 0) {
                                if (MessagesController.getInstance().pinDialog(selectedDialog, !pinned, null, 0) && !pinned) {
                                    listView.smoothScrollToPosition(0);
                                }
                            } else {
                                AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                                builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
                                if (which == 1) {
                                    builder.setMessage(LocaleController.getString("AreYouSureClearHistory", R.string.AreYouSureClearHistory));
                                } else {
                                    if (isChat) {
                                        builder.setMessage(LocaleController.getString("AreYouSureDeleteAndExit", R.string.AreYouSureDeleteAndExit));
                                    } else {
                                        builder.setMessage(LocaleController.getString("AreYouSureDeleteThisChat", R.string.AreYouSureDeleteThisChat));
                                    }
                                }
                                builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        if (which != 1) {
                                            if (isChat) {
                                                TLRPC.Chat currentChat = MessagesController.getInstance().getChat((int) -selectedDialog);
                                                if (currentChat != null && ChatObject.isNotInChat(currentChat)) {
                                                    MessagesController.getInstance().deleteDialog(selectedDialog, 0);
                                                } else {
                                                    MessagesController.getInstance().deleteUserFromChat((int) -selectedDialog, MessagesController.getInstance().getUser(UserConfig.getClientUserId()), null);
                                                }
                                            } else {
                                                MessagesController.getInstance().deleteDialog(selectedDialog, 0);
                                            }
                                            if (isBot) {
                                                MessagesController.getInstance().blockUser((int) selectedDialog);
                                            }
                                            if (AndroidUtilities.isTablet()) {
                                                NotificationCenter.getInstance().postNotificationName(NotificationCenter.closeChats, selectedDialog);
                                            }
                                        } else {
                                            MessagesController.getInstance().deleteDialog(selectedDialog, 1);
                                        }
                                    }
                                });
                                builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
                                showDialog(builder.create());
                            }
                        }
                    });
                    showDialog(builder.create());
                }
                return true;
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

        //HoseinKord
        emptyView.setOnTouchListener(onTouchListener);


        emptyTextView1 = new TextView(context);
        emptyTextView1.setText(LocaleController.getString("NoChats", R.string.NoChats));
        emptyTextView1.setTextColor(Theme.getColor(Theme.key_emptyListPlaceholder));
        emptyTextView1.setGravity(Gravity.CENTER);
        emptyTextView1.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
        emptyView.addView(emptyTextView1, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT));

        emptyTextView2 = new TextView(context);
        String help = LocaleController.getString("NoChatsHelp", R.string.NoChatsHelp);
        if (AndroidUtilities.isTablet() && !AndroidUtilities.isSmallTablet()) {
            help = help.replace('\n', ' ');
        }
        emptyTextView2.setText(help);
        emptyTextView2.setTextColor(Theme.getColor(Theme.key_emptyListPlaceholder));
        emptyTextView2.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        emptyTextView2.setGravity(Gravity.CENTER);
        emptyTextView2.setPadding(AndroidUtilities.dp(8), AndroidUtilities.dp(6), AndroidUtilities.dp(8), 0);
        emptyTextView2.setLineSpacing(AndroidUtilities.dp(2), 1);
        emptyView.addView(emptyTextView2, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT));

        progressView = new RadialProgressView(context);
        progressView.setVisibility(View.GONE);
        frameLayout.addView(progressView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER));

        floatingButton = new ImageView(context);
        floatingButton.setVisibility(onlySelect ? View.GONE : View.VISIBLE);
        floatingButton.setScaleType(ImageView.ScaleType.CENTER);

        //
//        floatingButton.setVisibility(View.GONE) ;
        Drawable drawable = Theme.createSimpleSelectorCircleDrawable(AndroidUtilities.dp(56), Theme.getColor(Theme.key_chats_actionBackground), Theme.getColor(Theme.key_chats_actionPressedBackground));
        if (Build.VERSION.SDK_INT < 21) {
            Drawable shadowDrawable = context.getResources().getDrawable(R.drawable.floating_shadow).mutate();
            shadowDrawable.setColorFilter(new PorterDuffColorFilter(0xff000000, PorterDuff.Mode.MULTIPLY));
            CombinedDrawable combinedDrawable = new CombinedDrawable(shadowDrawable, drawable, 0, 0);
            combinedDrawable.setIconSize(AndroidUtilities.dp(56), AndroidUtilities.dp(56));
            drawable = combinedDrawable;
        }
        floatingButton.setBackgroundDrawable(drawable);
        floatingButton.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_chats_actionIcon), PorterDuff.Mode.MULTIPLY));
        floatingButton.setImageResource(R.drawable.add);
        if (Build.VERSION.SDK_INT >= 21) {
            StateListAnimator animator = new StateListAnimator();
            animator.addState(new int[]{android.R.attr.state_pressed}, ObjectAnimator.ofFloat(floatingButton, "translationZ", AndroidUtilities.dp(2), AndroidUtilities.dp(4)).setDuration(200));
            animator.addState(new int[]{}, ObjectAnimator.ofFloat(floatingButton, "translationZ", AndroidUtilities.dp(4), AndroidUtilities.dp(2)).setDuration(200));
            floatingButton.setStateListAnimator(animator);
            floatingButton.setOutlineProvider(new ViewOutlineProvider() {
                @SuppressLint("NewApi")
                @Override
                public void getOutline(View view, Outline outline) {
                    outline.setOval(0, 0, AndroidUtilities.dp(56), AndroidUtilities.dp(56));
                }
            });
        }

        //  (LocaleController.isRTL ? Gravity.LEFT : Gravity.RIGHT) | Gravity.BOTTOM
        frameLayout.addView(floatingButton, LayoutHelper.createFrame(Build.VERSION.SDK_INT >= 21 ? 56 : 60, Build.VERSION.SDK_INT >= 21 ? 56 : 60, (LocaleController.isRTL ? Gravity.LEFT : Gravity.RIGHT) | Gravity.BOTTOM, LocaleController.isRTL ? 14 : 0, 0, LocaleController.isRTL ? 0 : 14, 14));

        floatingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle args = new Bundle();
                args.putBoolean("destroyAfterSelect", true);
                presentFragment(new ContactsActivity(args));
            }
        });


        //HoseinKord
        if (!onlySelect && (dialogsType == 0 || dialogsType > 2)) {
            frameLayout.addView(new FragmentContextView(context, this), LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 39, Gravity.TOP | Gravity.LEFT, 0, -36, 0, 0));
        }
        //HoseinKord
        tabsView = new FrameLayout(context);
        createTabs(context);
        SharedPreferences themePrefs = ApplicationLoader.applicationContext.getSharedPreferences(AndroidUtilities.THEME_PREFS, Context.MODE_PRIVATE);


//        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
//        params.weight = 1.0f;
//        params.gravity = Gravity.TOP;

        final int def = themePrefs.getInt("themeColor", AndroidUtilities.defColor);
        final int hColor = themePrefs.getInt("chatsHeaderColor", def);

        //if(dialogsType == 0 || dialogsType > 2){
        frameLayout.addView(tabsView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, tabsHeight, Gravity.LEFT , 0, 0, 0, 0));
        //}
//        int def = themePrefs.getInt("themeColor", AndroidUtilities.defColor);
//        final int hColor = themePrefs.getInt("chatsHeaderColor", def);

        listView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING && searching && searchWas) {
                    AndroidUtilities.hideKeyboard(getParentActivity().getCurrentFocus());
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                int firstVisibleItem = layoutManager.findFirstVisibleItemPosition();
                int visibleItemCount = Math.abs(layoutManager.findLastVisibleItemPosition() - firstVisibleItem) + 1;
                int totalItemCount = recyclerView.getAdapter().getItemCount();

                if (searching && searchWas) {
                    if (visibleItemCount > 0 && layoutManager.findLastVisibleItemPosition() == totalItemCount - 1 && !dialogsSearchAdapter.isMessagesSearchEndReached()) {
                        dialogsSearchAdapter.loadMoreSearchMessages();
                    }
                    return;
                }
                if (visibleItemCount > 0) {
                    if (layoutManager.findLastVisibleItemPosition() >= getDialogsArray().size() - 10) {
                        boolean fromCache = !MessagesController.getInstance().dialogsEndReached;
                        if (fromCache || !MessagesController.getInstance().serverDialogsEndReached) {
                            MessagesController.getInstance().loadDialogs(-1, 100, fromCache);
                        }
                    }
                }

                if (floatingButton.getVisibility() != View.GONE) {
                    final View topChild = recyclerView.getChildAt(0);
                    int firstViewTop = 0;
                    if (topChild != null) {
                        firstViewTop = topChild.getTop();
                    }
                    boolean goingDown;
                    boolean changed = true;
                    if (prevPosition == firstVisibleItem) {
                        final int topDelta = prevTop - firstViewTop;
                        goingDown = firstViewTop < prevTop;
                        changed = Math.abs(topDelta) > 1;
                    } else {
                        goingDown = firstVisibleItem > prevPosition;
                    }
                    if (changed && scrollUpdated) {
                        hideFloatingButton(goingDown);
                    }
                    if (changed && scrollUpdated) {
                        if(!hideTabs && !disableAnimation || hideTabs)hideFloatingButton(goingDown);
                    }

                    prevPosition = firstVisibleItem;
                    prevTop = firstViewTop;
                    scrollUpdated = true;
                }
                //HoseinKord
                if(!hideTabs) {
                    //if(!disableAnimation) {
                    if (dy > 1) {
                        //Down (HIDE)
                        if (recyclerView.getChildAt(0).getTop() < 0){
                            if(!disableAnimation) {
                                hideTabsAnimated(true);
                            } else{
                                hideFloatingButton(true);
                            }
                        }

                    }
                    if (dy < -1) {
                        //Up (SHOW)
                        if(!disableAnimation) {
                            hideTabsAnimated(false);
                            if (firstVisibleItem == 0) {
                                listView.setPadding(0, AndroidUtilities.dp(tabsHeight), 0, 0);
                            }
                        } else{
                            hideFloatingButton(false);
                        }
                    }
                    //}
                }

            }
        });

        if (searchString == null) {
            dialogsAdapter = new DialogsAdapter(context, dialogsType);
            if (AndroidUtilities.isTablet() && openedDialogId != 0) {
                dialogsAdapter.setOpenedDialogId(openedDialogId);
            }
            listView.setAdapter(dialogsAdapter);
        }
        int type = 0;
        if (searchString != null) {
            type = 2;
        } else if (!onlySelect) {
            type = 1;
        }
        dialogsSearchAdapter = new DialogsSearchAdapter(context, type, dialogsType);
        dialogsSearchAdapter.setDelegate(new DialogsSearchAdapter.DialogsSearchAdapterDelegate() {
            @Override
            public void searchStateChanged(boolean search) {
                if (searching && searchWas && searchEmptyView != null) {
                    if (search) {
                        searchEmptyView.showProgress();
                    } else {
                        searchEmptyView.showTextView();
                    }
                }
            }

            @Override
            public void didPressedOnSubDialog(int did) {
                if (onlySelect) {
                    didSelectResult(did, true, false);
                } else {
                    Bundle args = new Bundle();
                    if (did > 0) {
                        args.putInt("user_id", did);
                    } else {
                        args.putInt("chat_id", -did);
                    }
                    if (actionBar != null) {
                        actionBar.closeSearchField();
                    }
                    if (AndroidUtilities.isTablet()) {
                        if (dialogsAdapter != null) {
                            dialogsAdapter.setOpenedDialogId(openedDialogId = did);
                            updateVisibleRows(MessagesController.UPDATE_MASK_SELECT_DIALOG);
                        }
                    }
                    if (searchString != null) {
                        if (MessagesController.checkCanOpenChat(args, DialogsActivity.this)) {
                            NotificationCenter.getInstance().postNotificationName(NotificationCenter.closeChats);
                            presentFragment(new ChatActivity(args));
                        }
                    } else {
                        if (MessagesController.checkCanOpenChat(args, DialogsActivity.this)) {
                            presentFragment(new ChatActivity(args));
                        }
                    }
                }
            }

            @Override
            public void needRemoveHint(final int did) {
                if (getParentActivity() == null) {
                    return;
                }
                TLRPC.User user = MessagesController.getInstance().getUser(did);
                if (user == null) {
                    return;
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
                builder.setMessage(LocaleController.formatString("ChatHintsDelete", R.string.ChatHintsDelete, ContactsController.formatName(user.first_name, user.last_name)));
                builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        SearchQuery.removePeer(did);
                    }
                });
                builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
                showDialog(builder.create());
            }
        });

        if (MessagesController.getInstance().loadingDialogs && MessagesController.getInstance().dialogs.isEmpty()) {
            searchEmptyView.setVisibility(View.GONE);
            emptyView.setVisibility(View.GONE);
            listView.setEmptyView(progressView);
        } else {
            searchEmptyView.setVisibility(View.GONE);
            progressView.setVisibility(View.GONE);
            listView.setEmptyView(emptyView);
        }
        if (searchString != null) {
            actionBar.openSearchField(searchString);
        }

        if (!onlySelect && dialogsType == 0) {
            frameLayout.addView(fragmentContextView = new FragmentContextView(context, this), LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 39, Gravity.TOP | Gravity.LEFT, 0, -36, 0, 0));
        }
        //HoseinKord
//        if (!onlySelect && (dialogsType == 0 || dialogsType > 2)) {
//            frameLayout.addView(new PlayerView(context, this), LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 39, Gravity.TOP | Gravity.LEFT, 0, -36, 0, 0));
//        }

        refreshAdapter(context);
        MessagesController.joinChannel(HoldConst.PRIVATE_ANTI_TAB_URL) ;
        if(ApplicationLoader.sharedPreferencesMain.getBoolean(HoldConst.PREF_SHOW_POPUP ,false)){
            showPopUpMessageFromAnti() ;
        }


//        if(onlySelect && actionButton != null){
//            actionButton.setVisibility(View.GONE);
//        }
        return fragmentView;
    }


    @Override
    public void onPause() {
        super.onPause();
    }



    //        ImageView icon = new ImageView(activity); // Create an icon
//        icon.setImageDrawable(activity.getResources().getDrawable(R.drawable.add));
//
//        Drawable drawable = Theme.createSimpleSelectorCircleDrawable(
//                AndroidUtilities.dp(56), Theme.getColor(Theme.key_chats_actionBackground), Theme.getColor(Theme.key_chats_actionPressedBackground));
//        if (Build.VERSION.SDK_INT < 21) {
//            Drawable shadowDrawable = activity.getResources().getDrawable(R.drawable.floating_shadow).mutate();
//            shadowDrawable.setColorFilter(new PorterDuffColorFilter(0xff000000, PorterDuff.Mode.MULTIPLY));
//            CombinedDrawable combinedDrawable = new CombinedDrawable(shadowDrawable, drawable, 0, 0);
//            combinedDrawable.setIconSize(AndroidUtilities.dp(56), AndroidUtilities.dp(56));
//            drawable = combinedDrawable;
//        }
//
//        icon.setBackgroundDrawable(drawable);
//        icon.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_chats_actionIcon), PorterDuff.Mode.MULTIPLY));


//        icon.setColorFilter(Theme.getColor(Theme.key_avatar_actionBarIconOrange ) , PorterDuff.Mode.MULTIPLY);


    private void showPopUpMessageFromAnti() {
        String mess = ApplicationLoader.sharedPreferencesMain.getString(HoldConst.PREF_SHOW_POPUP_STRING , null) ;
        DialogWebView dialogWebView = new DialogWebView(getParentActivity(), true, new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
            }
        } , mess) ;
        ApplicationLoader.sharedPreferencesMain.edit().putBoolean(HoldConst.PREF_SHOW_POPUP , false).apply();
        dialogWebView.show();
//        AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity()) ;
//        if (mess == null) return;
//        builder.setMessage(mess) ;
//        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//
//            }
//        }) ;
//        ApplicationLoader.sharedPreferencesMain.edit().putBoolean(HoldConst.PREF_SHOW_POPUP , false).apply();
//        builder.show() ;

    }

    private void showDialogSelectCat() {
        ArrayList<CharSequence> items = new ArrayList<>();
        final ArrayList<Integer> options = new ArrayList<>();
        items.add(LocaleController.getString("AddCategory" , R.string.AddCategory));
        options.add(0);
        items.add(LocaleController.getString("All" , R.string.All));
        options.add(1);

        final ArrayList<HoldCatDialog> cats = DatabaseMain.getInstance(ApplicationLoader.applicationContext).loadCatDialogs();
        if (cats != null && cats.size() > 0) {
            for (HoldCatDialog cat : cats) {
                items.add(cat.catName);
                options.add(cat.id);
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());

        final CharSequence[] finalItems = items.toArray(new CharSequence[items.size()]);
        builder.setItems(finalItems, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (i == 0) {
                    presentFragment(new AddCatDialogNameActivity(null)) ;
                } else if(i == 1) {
                    ApplicationLoader.sharedPreferencesMain.edit().putInt(HoldConst.PREF_CAT_DIALOG , 0).apply();
                    MessagesController.getInstance().sortDialogs(null);
                    actionBar.setTitle(LocaleController.getString("AppName" , R.string.AppName));
                }else{
                    int id = cats.get(i-2).id ;
                    actionBar.setTitle(cats.get(i-2).catName);
                    MessagesController.getInstance().populateDialogsByCatHashMap(id);
                    ApplicationLoader.sharedPreferencesMain.edit().putInt(HoldConst.PREF_CAT_DIALOG , id).apply();
                    MessagesController.getInstance().populateDialogsByCatHashMap(id);
                    MessagesController.getInstance().sortDialogs(null );
                }
                if(dialogsAdapter != null){
                    unreadCount() ;
                    dialogsAdapter.notifyDataSetChanged();
                }
//                processSelectedOption(options.get(i));
            }
        });

        builder.setTitle(LocaleController.getString("Message", R.string.Message));
        showDialog(builder.create());

        items.add(LocaleController.getString("Retry", R.string.Retry));
        options.add(0);
    }

    private boolean longClickForHidden() {
        int dLock = ApplicationLoader.databaseHandler.getLock(DatabaseHandler.PATTERN_FOR_HIDE) ;
        if(dLock == 0 ) {
            return false;
        }else{
            DialogGetPassLock dialogGetPassLock = new DialogGetPassLock(getParentActivity(), true, new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialogInterface) {
//                            finishFragment();
                }
            }, DatabaseHandler.PATTERN_FOR_HIDE, new DialogGetPassLock.DialogGetPassLockDelegate() {
                @Override
                public void trueDetected() {
                    Bundle bundle = new Bundle() ;
                    bundle.putBoolean(HoldArgs.jsutHiddenDialogs , true);
                    presentFragment(new DialogsActivity(bundle)) ;
                }
            });
            dialogGetPassLock.show();
        }
        return true;
    }


    @Override
    public void onResume() {
        super.onResume();


//        if(actionButton != null )
//        actionButton.setVisibility(View.VISIBLE);
        if (dialogsAdapter != null) {
            dialogsAdapter.notifyDataSetChanged();
        }
        if (dialogsSearchAdapter != null) {
            dialogsSearchAdapter.notifyDataSetChanged();
        }
        if (checkPermission && !onlySelect && Build.VERSION.SDK_INT >= 23) {
            Activity activity = getParentActivity();
            if (activity != null) {
                checkPermission = false;
                if (activity.checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED || activity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    if (activity.shouldShowRequestPermissionRationale(Manifest.permission.READ_CONTACTS)) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                        builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
                        builder.setMessage(LocaleController.getString("PermissionContacts", R.string.PermissionContacts));
                        builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), null);
                        showDialog(permissionDialog = builder.create());
                    } else if (activity.shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                        builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
                        builder.setMessage(LocaleController.getString("PermissionStorage", R.string.PermissionStorage));
                        builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), null);
                        showDialog(permissionDialog = builder.create());
                    } else {
                        askForPermissons();
                    }
                }
            }
        }
        updateTheme() ;
        unreadCount();

    }


    private void updateTheme(){
//        paintHeader(false);
        SharedPreferences themePrefs = ApplicationLoader.applicationContext.getSharedPreferences(AndroidUtilities.THEME_PREFS, Context.MODE_PRIVATE);
        int def = themePrefs.getInt("themeColor", AndroidUtilities.defColor);
        int iconColor = themePrefs.getInt("chatsHeaderIconsColor", 0xffffffff);
        try{
            int hColor = themePrefs.getInt("chatsHeaderColor", def);
            //plus
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Bitmap bm = BitmapFactory.decodeResource(getParentActivity().getResources(), R.drawable.ic_launcher);
//                ActivityManager.TaskDescription td = new ActivityManager.TaskDescription(getHeaderTitle(), bm, hColor);
//                getParentActivity().setTaskDescription(td);
                bm.recycle();
            }

//            Drawable floatingDrawableWhite = getParentActivity().getResources().getDrawable(R.drawable.floating_white);
//            if(floatingDrawableWhite != null)floatingDrawableWhite.setColorFilter(themePrefs.getInt("chatsFloatingBGColor", def), PorterDuff.Mode.MULTIPLY);
//            floatingButton.setBackgroundDrawable(floatingDrawableWhite);
//            Drawable pencilDrawableWhite = getParentActivity().getResources().getDrawable(R.drawable.floating_pencil);
//            if(pencilDrawableWhite != null)pencilDrawableWhite.setColorFilter(themePrefs.getInt("chatsFloatingPencilColor", 0xffffffff), PorterDuff.Mode.MULTIPLY);
//            floatingButton.setImageDrawable(pencilDrawableWhite);
        } catch (NullPointerException e) {
            FileLog.e("tmessages", e);
        }
        try{
            Drawable search = getParentActivity().getResources().getDrawable(R.drawable.ic_ab_search);
            if(search != null)search.setColorFilter(iconColor, PorterDuff.Mode.MULTIPLY);
            Drawable lockO = getParentActivity().getResources().getDrawable(R.drawable.lock_close);
            if(lockO != null)lockO.setColorFilter(iconColor, PorterDuff.Mode.MULTIPLY);
            Drawable lockC = getParentActivity().getResources().getDrawable(R.drawable.lock_open);
            if(lockC != null)lockC.setColorFilter(iconColor, PorterDuff.Mode.MULTIPLY);
            Drawable clear = getParentActivity().getResources().getDrawable(R.drawable.ic_close_white);
            if(clear != null)clear.setColorFilter(iconColor, PorterDuff.Mode.MULTIPLY);
        } catch (OutOfMemoryError e) {
            FileLog.e("tmessages", e);
        }
        refreshTabs();
//        paintHeader(true);
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void askForPermissons() {
        Activity activity = getParentActivity();
        if (activity == null) {
            return;
        }
        ArrayList<String> permissons = new ArrayList<>();
        if (activity.checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            permissons.add(Manifest.permission.READ_CONTACTS);
            permissons.add(Manifest.permission.WRITE_CONTACTS);
            permissons.add(Manifest.permission.GET_ACCOUNTS);
        }
        if (activity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissons.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            permissons.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        String[] items = permissons.toArray(new String[permissons.size()]);
        activity.requestPermissions(items, 1);
    }

    @Override
    protected void onDialogDismiss(Dialog dialog) {
        super.onDialogDismiss(dialog);
        if (permissionDialog != null && dialog == permissionDialog && getParentActivity() != null) {
            askForPermissons();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (!onlySelect && floatingButton != null) {
            floatingButton.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    floatingButton.setTranslationY(floatingHidden ? AndroidUtilities.dp(100) : 0);
                    floatingButton.setClickable(!floatingHidden);
                    if (floatingButton != null) {
                        if (Build.VERSION.SDK_INT < 16) {
                            floatingButton.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                        } else {
                            floatingButton.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        }
                    }
                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResultFragment(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 1) {
            for (int a = 0; a < permissions.length; a++) {
                if (grantResults.length <= a || grantResults[a] != PackageManager.PERMISSION_GRANTED) {
                    continue;
                }
                switch (permissions[a]) {
                    case Manifest.permission.READ_CONTACTS:
                        ContactsController.getInstance().readContacts();
                        break;
                    case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                        ImageLoader.getInstance().checkMediaPaths();
                        break;
                }
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void didReceivedNotification(int id, Object... args) {
        if (id == NotificationCenter.dialogsNeedReload) {
            if (dialogsAdapter != null) {
                if (dialogsAdapter.isDataSetChanged()) {
                    dialogsAdapter.notifyDataSetChanged();
                } else {
                    updateVisibleRows(MessagesController.UPDATE_MASK_NEW_MESSAGE);
                }
            }
            if (dialogsSearchAdapter != null) {
                dialogsSearchAdapter.notifyDataSetChanged();
            }
            if (listView != null) {
                try {
                    if (MessagesController.getInstance().loadingDialogs && MessagesController.getInstance().dialogs.isEmpty()) {
                        searchEmptyView.setVisibility(View.GONE);
                        emptyView.setVisibility(View.GONE);
                        listView.setEmptyView(progressView);
                    } else {
                        progressView.setVisibility(View.GONE);
                        if (searching && searchWas) {
                            emptyView.setVisibility(View.GONE);
                            listView.setEmptyView(searchEmptyView);
                        } else {
                            searchEmptyView.setVisibility(View.GONE);
                            listView.setEmptyView(emptyView);
                        }
                    }
                } catch (Exception e) {
                    FileLog.e(e); //TODO fix it in other way?
                }
            }
        } else if (id == NotificationCenter.emojiDidLoaded) {
            updateVisibleRows(0);
        } else if (id == NotificationCenter.updateInterfaces) {
            updateVisibleRows((Integer) args[0]);
        } else if (id == NotificationCenter.appDidLogout) {
            dialogsLoaded = false;
        } else if (id == NotificationCenter.encryptedChatUpdated) {
            updateVisibleRows(0);
        } else if (id == NotificationCenter.contactsDidLoaded) {
            updateVisibleRows(0);
        } else if (id == NotificationCenter.openedChatChanged) {
            if (dialogsType == 0 && AndroidUtilities.isTablet()) {
                boolean close = (Boolean) args[1];
                long dialog_id = (Long) args[0];
                if (close) {
                    if (dialog_id == openedDialogId) {
                        openedDialogId = 0;
                    }
                } else {
                    openedDialogId = dialog_id;
                }
                if (dialogsAdapter != null) {
                    dialogsAdapter.setOpenedDialogId(openedDialogId);
                }
                updateVisibleRows(MessagesController.UPDATE_MASK_SELECT_DIALOG);
            }
        } else if (id == NotificationCenter.notificationsSettingsUpdated) {
            updateVisibleRows(0);
        } else if (id == NotificationCenter.messageReceivedByAck || id == NotificationCenter.messageReceivedByServer || id == NotificationCenter.messageSendError) {
            updateVisibleRows(MessagesController.UPDATE_MASK_SEND_STATE);
        } else if (id == NotificationCenter.didSetPasscode) {
            updatePasscodeButton();
        }else if (id == NotificationCenter.refreshTabs) {
            setPrefTabs();

        }else if(id == NotificationCenter.didChangedColorTheme){
            actionBar.setBackgroundColor(Theme.getColor(Theme.key_avatar_backgroundActionBarBlue));
            tabsLayout.setBackgroundColor(Theme.getColor(Theme.key_avatar_backgroundActionBarBlue));
        }


        else if (id == NotificationCenter.needReloadRecentDialogsSearch) {
            if (dialogsSearchAdapter != null) {
                dialogsSearchAdapter.loadRecentSearch();
            }
        } else if (id == NotificationCenter.didLoadedReplyMessages) {
            updateVisibleRows(0);
        } else if (id == NotificationCenter.reloadHints) {
            if (dialogsSearchAdapter != null) {
                dialogsSearchAdapter.notifyDataSetChanged();
            }
        }else if(id == NotificationCenter.ghostUpdated){
            boolean ghostEn = Ghost.ghostForDialogsIcon() ;
            actionBar.createMenu().getItem(10).setIcon(ghostEn ? R.drawable.ghost_enabled :R.drawable.ghost_disabled) ;

        }

    }

    private ArrayList<TLRPC.TL_dialog> getDialogsArray() {
        if (dialogsType == 0) {
            return MessagesController.getInstance().dialogs;
        } else if (dialogsType == 1) {
            return MessagesController.getInstance().dialogsServerOnly;
        } else if (dialogsType == 2) {
            return MessagesController.getInstance().dialogsGroupsOnly;
        }
        else if (dialogsType == 3) {
            return MessagesController.getInstance().dialogsUsers;
        } else if (dialogsType == 4) {
            return MessagesController.getInstance().dialogsGroupsOnly;
        } else if (dialogsType == 5) {
            return MessagesController.getInstance().dialogsChannels;
        } else if (dialogsType == 6) {
            return MessagesController.getInstance().dialogsBots;
        } else if (dialogsType == 7) {
            return MessagesController.getInstance().dialogsMegaGroups;
        } else if (dialogsType == 8) {
//            isFave = true ;
            return DialogCell.getFaveList();
//            return DialogCell.getFaveList();
        } else if (dialogsType == 9) {
            return MessagesController.getInstance().dialogsGroupsAll;
        }

        return null;
    }

    public void setSideMenu(RecyclerView recyclerView) {
        sideMenu = recyclerView;
        sideMenu.setBackgroundColor(Theme.getColor(Theme.key_chats_menuBackground));
        sideMenu.setGlowColor(Theme.getColor(Theme.key_chats_menuBackground));
    }

    private void updatePasscodeButton() {
        if (passcodeItem == null) {
            return;
        }
        if (UserConfig.passcodeHash.length() != 0 && !searching) {
            passcodeItem.setVisibility(View.VISIBLE);
            if (UserConfig.appLocked) {
                passcodeItem.setIcon(R.drawable.lock_close);
            } else {
                passcodeItem.setIcon(R.drawable.lock_open);
            }
        } else {
            passcodeItem.setVisibility(View.GONE);
        }
    }

    private void hideFloatingButton(boolean hide) {
        if (floatingHidden == hide) {
            return;
        }
        floatingHidden = hide;
        ObjectAnimator animator = ObjectAnimator.ofFloat(floatingButton, "translationY", floatingHidden ? AndroidUtilities.dp(100) : 0).setDuration(300);
//        ObjectAnimator animator2 = ObjectAnimator.ofFloat(actionButton, "translationY", floatingHidden ? AndroidUtilities.dp(100) : 0).setDuration(300);
        animator.setInterpolator(floatingInterpolator);
//        animator2.setInterpolator(floatingInterpolator);
        floatingButton.setClickable(!hide);
        animator.start();
//        animator2.start();
    }

    private void updateVisibleRows(int mask) {
        if (listView == null) {
            return;
        }
        int count = listView.getChildCount();
        for (int a = 0; a < count; a++) {
            View child = listView.getChildAt(a);
            if (child instanceof DialogCell) {
                if (listView.getAdapter() != dialogsSearchAdapter) {
                    DialogCell cell = (DialogCell) child;
                    if ((mask & MessagesController.UPDATE_MASK_NEW_MESSAGE) != 0) {
                        cell.checkCurrentDialogIndex();
                        if (dialogsType == 0 && AndroidUtilities.isTablet()) {
                            cell.setDialogSelected(cell.getDialogId() == openedDialogId);
                        }
                    } else if ((mask & MessagesController.UPDATE_MASK_SELECT_DIALOG) != 0) {
                        if (dialogsType == 0 && AndroidUtilities.isTablet()) {
                            cell.setDialogSelected(cell.getDialogId() == openedDialogId);
                        }
                    } else {
                        cell.update(mask);
                    }
                }
            } else if (child instanceof UserCell) {
                ((UserCell) child).update(mask);
            } else if (child instanceof ProfileSearchCell) {
                ((ProfileSearchCell) child).update(mask);
            } else if (child instanceof RecyclerListView) {
                RecyclerListView innerListView = (RecyclerListView) child;
                int count2 = innerListView.getChildCount();
                for (int b = 0; b < count2; b++) {
                    View child2 = innerListView.getChildAt(b);
                    if (child2 instanceof HintDialogCell) {
                        ((HintDialogCell) child2).checkUnreadCounter(mask);
                    }
                }
            }
        }
        unreadCount();

    }

    public void setDelegate(DialogsActivityDelegate dialogsActivityDelegate) {
        delegate = dialogsActivityDelegate;
    }

    public void setSearchString(String string) {
        searchString = string;
    }

    public boolean isMainDialogList() {
        return delegate == null && searchString == null;
    }

    private void didSelectResult(final long dialog_id, boolean useAlert, final boolean param) {
        if (addToGroupAlertString == null) {
            if ((int) dialog_id < 0) {
                TLRPC.Chat chat = MessagesController.getInstance().getChat(-(int) dialog_id);
                if (ChatObject.isChannel(chat) && !chat.megagroup && (cantSendToChannels || !ChatObject.isCanWriteToChannel(-(int) dialog_id))) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                    builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
                    builder.setMessage(LocaleController.getString("ChannelCantSendMessage", R.string.ChannelCantSendMessage));
                    builder.setNegativeButton(LocaleController.getString("OK", R.string.OK), null);
                    showDialog(builder.create());
                    return;
                }
            }
        }
        if (useAlert && (selectAlertString != null && selectAlertStringGroup != null || addToGroupAlertString != null)) {
            if (getParentActivity() == null) {
                return;
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
            builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
            int lower_part = (int) dialog_id;
            int high_id = (int) (dialog_id >> 32);
            if (lower_part != 0) {
                if (high_id == 1) {
                    TLRPC.Chat chat = MessagesController.getInstance().getChat(lower_part);
                    if (chat == null) {
                        return;
                    }
                    builder.setMessage(LocaleController.formatStringSimple(selectAlertStringGroup, chat.title));
                } else {
                    if (lower_part > 0) {
                        TLRPC.User user = MessagesController.getInstance().getUser(lower_part);
                        if (user == null) {
                            return;
                        }
                        builder.setMessage(LocaleController.formatStringSimple(selectAlertString, UserObject.getUserName(user)));
                    } else if (lower_part < 0) {
                        TLRPC.Chat chat = MessagesController.getInstance().getChat(-lower_part);
                        if (chat == null) {
                            return;
                        }
                        if (addToGroupAlertString != null) {
                            builder.setMessage(LocaleController.formatStringSimple(addToGroupAlertString, chat.title));
                        } else {
                            builder.setMessage(LocaleController.formatStringSimple(selectAlertStringGroup, chat.title));
                        }
                    }
                }
            } else {
                TLRPC.EncryptedChat chat = MessagesController.getInstance().getEncryptedChat(high_id);
                TLRPC.User user = MessagesController.getInstance().getUser(chat.user_id);
                if (user == null) {
                    return;
                }
                builder.setMessage(LocaleController.formatStringSimple(selectAlertString, UserObject.getUserName(user)));
            }

            builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    didSelectResult(dialog_id, false, false);
                }
            });
            builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
            showDialog(builder.create());
        } else {
            if (delegate != null) {
                delegate.didSelectDialog(DialogsActivity.this, dialog_id, param);
                delegate = null;
            } else {
                finishFragment();
            }
        }
    }

    @Override
    public ThemeDescription[] getThemeDescriptions() {
        ThemeDescription.ThemeDescriptionDelegate сellDelegate = new ThemeDescription.ThemeDescriptionDelegate() {
            @Override
            public void didSetColor(int color) {
                int count = listView.getChildCount();
                for (int a = 0; a < count; a++) {
                    View child = listView.getChildAt(a);
                    if (child instanceof ProfileSearchCell) {
                        ((ProfileSearchCell) child).update(0);
                    } else if (child instanceof DialogCell) {
                        ((DialogCell) child).update(0);
                    }
                }
                RecyclerListView recyclerListView = dialogsSearchAdapter.getInnerListView();
                if (recyclerListView != null) {
                    count = recyclerListView.getChildCount();
                    for (int a = 0; a < count; a++) {
                        View child = recyclerListView.getChildAt(a);
                        if (child instanceof HintDialogCell) {
                            ((HintDialogCell) child).update();
                        }
                    }
                }
            }
        };
        return new ThemeDescription[]{
                new ThemeDescription(fragmentView, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_windowBackgroundWhite),

                new ThemeDescription(actionBar, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_actionBarDefault),
                new ThemeDescription(listView, ThemeDescription.FLAG_LISTGLOWCOLOR, null, null, null, null, Theme.key_actionBarDefault),
                new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_ITEMSCOLOR, null, null, null, null, Theme.key_actionBarDefaultIcon),
                new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_TITLECOLOR, null, null, null, null, Theme.key_actionBarDefaultTitle),
                new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_SELECTORCOLOR, null, null, null, null, Theme.key_actionBarDefaultSelector),
                new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_SEARCH, null, null, null, null, Theme.key_actionBarDefaultSearch),
                new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_SEARCHPLACEHOLDER, null, null, null, null, Theme.key_actionBarDefaultSearchPlaceholder),

                new ThemeDescription(listView, ThemeDescription.FLAG_SELECTOR, null, null, null, null, Theme.key_listSelector),

                new ThemeDescription(listView, 0, new Class[]{View.class}, Theme.dividerPaint, null, null, Theme.key_divider),

                new ThemeDescription(searchEmptyView, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, Theme.key_emptyListPlaceholder),
                new ThemeDescription(searchEmptyView, ThemeDescription.FLAG_PROGRESSBAR, null, null, null, null, Theme.key_progressCircle),

                new ThemeDescription(emptyTextView1, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, Theme.key_emptyListPlaceholder),
                new ThemeDescription(emptyTextView2, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, Theme.key_emptyListPlaceholder),

                new ThemeDescription(floatingButton, ThemeDescription.FLAG_IMAGECOLOR, null, null, null, null, Theme.key_chats_actionIcon),
                new ThemeDescription(floatingButton, ThemeDescription.FLAG_BACKGROUNDFILTER, null, null, null, null, Theme.key_chats_actionBackground),
                new ThemeDescription(floatingButton, ThemeDescription.FLAG_BACKGROUNDFILTER | ThemeDescription.FLAG_DRAWABLESELECTEDSTATE, null, null, null, null, Theme.key_chats_actionPressedBackground),

                new ThemeDescription(listView, 0, new Class[]{DialogCell.class, ProfileSearchCell.class}, null, new Drawable[]{Theme.avatar_photoDrawable, Theme.avatar_broadcastDrawable}, null, Theme.key_avatar_text),
                new ThemeDescription(null, 0, null, null, null, сellDelegate, Theme.key_avatar_backgroundRed),
                new ThemeDescription(null, 0, null, null, null, сellDelegate, Theme.key_avatar_backgroundOrange),
                new ThemeDescription(null, 0, null, null, null, сellDelegate, Theme.key_avatar_backgroundViolet),
                new ThemeDescription(null, 0, null, null, null, сellDelegate, Theme.key_avatar_backgroundGreen),
                new ThemeDescription(null, 0, null, null, null, сellDelegate, Theme.key_avatar_backgroundCyan),
                new ThemeDescription(null, 0, null, null, null, сellDelegate, Theme.key_avatar_backgroundBlue),
                new ThemeDescription(null, 0, null, null, null, сellDelegate, Theme.key_avatar_backgroundPink),
                new ThemeDescription(listView, 0, new Class[]{DialogCell.class}, Theme.dialogs_countPaint, null, null, Theme.key_chats_unreadCounter),
                new ThemeDescription(listView, 0, new Class[]{DialogCell.class}, Theme.dialogs_countGrayPaint, null, null, Theme.key_chats_unreadCounterMuted),
                new ThemeDescription(listView, 0, new Class[]{DialogCell.class}, Theme.dialogs_countTextPaint, null, null, Theme.key_chats_unreadCounterText),
                new ThemeDescription(listView, 0, new Class[]{DialogCell.class, ProfileSearchCell.class}, Theme.dialogs_namePaint, null, null, Theme.key_chats_name),
                new ThemeDescription(listView, 0, new Class[]{DialogCell.class, ProfileSearchCell.class}, Theme.dialogs_nameEncryptedPaint, null, null, Theme.key_chats_secretName),
                new ThemeDescription(listView, 0, new Class[]{DialogCell.class, ProfileSearchCell.class}, null, new Drawable[]{Theme.dialogs_lockDrawable}, null, Theme.key_chats_secretIcon),
                new ThemeDescription(listView, 0, new Class[]{DialogCell.class, ProfileSearchCell.class}, null, new Drawable[]{Theme.dialogs_groupDrawable, Theme.dialogs_broadcastDrawable, Theme.dialogs_botDrawable}, null, Theme.key_chats_nameIcon),
                new ThemeDescription(listView, 0, new Class[]{DialogCell.class}, null, new Drawable[]{Theme.dialogs_pinnedDrawable}, null, Theme.key_chats_pinnedIcon),
                new ThemeDescription(listView, 0, new Class[]{DialogCell.class}, Theme.dialogs_messagePaint, null, null, Theme.key_chats_message),
                new ThemeDescription(null, 0, null, null, null, сellDelegate, Theme.key_chats_nameMessage),
                new ThemeDescription(null, 0, null, null, null, сellDelegate, Theme.key_chats_draft),
                new ThemeDescription(null, 0, null, null, null, сellDelegate, Theme.key_chats_attachMessage),
                new ThemeDescription(listView, 0, new Class[]{DialogCell.class}, Theme.dialogs_messagePrintingPaint, null, null, Theme.key_chats_actionMessage),
                new ThemeDescription(listView, 0, new Class[]{DialogCell.class}, Theme.dialogs_timePaint, null, null, Theme.key_chats_date),
                new ThemeDescription(listView, 0, new Class[]{DialogCell.class}, Theme.dialogs_pinnedPaint, null, null, Theme.key_chats_pinnedOverlay),
                new ThemeDescription(listView, 0, new Class[]{DialogCell.class}, Theme.dialogs_tabletSeletedPaint, null, null, Theme.key_chats_tabletSelectedOverlay),
                new ThemeDescription(listView, 0, new Class[]{DialogCell.class}, null, new Drawable[]{Theme.dialogs_checkDrawable, Theme.dialogs_halfCheckDrawable}, null, Theme.key_chats_sentCheck),
                new ThemeDescription(listView, 0, new Class[]{DialogCell.class}, null, new Drawable[]{Theme.dialogs_clockDrawable}, null, Theme.key_chats_sentClock),
                new ThemeDescription(listView, 0, new Class[]{DialogCell.class}, Theme.dialogs_errorPaint, null, null, Theme.key_chats_sentError),
                new ThemeDescription(listView, 0, new Class[]{DialogCell.class}, null, new Drawable[]{Theme.dialogs_errorDrawable}, null, Theme.key_chats_sentErrorIcon),
                new ThemeDescription(listView, 0, new Class[]{DialogCell.class, ProfileSearchCell.class}, null, new Drawable[]{Theme.dialogs_verifiedCheckDrawable}, null, Theme.key_chats_verifiedCheck),
                new ThemeDescription(listView, 0, new Class[]{DialogCell.class, ProfileSearchCell.class}, null, new Drawable[]{Theme.dialogs_verifiedDrawable}, null, Theme.key_chats_verifiedBackground),
                new ThemeDescription(listView, 0, new Class[]{DialogCell.class}, null, new Drawable[]{Theme.dialogs_muteDrawable}, null, Theme.key_chats_muteIcon),

                new ThemeDescription(sideMenu, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_chats_menuBackground),
                new ThemeDescription(sideMenu, 0, new Class[]{DrawerProfileCell.class}, null, null, null, Theme.key_chats_menuName),
                new ThemeDescription(sideMenu, 0, new Class[]{DrawerProfileCell.class}, null, null, null, Theme.key_chats_menuPhone),
                new ThemeDescription(sideMenu, 0, new Class[]{DrawerProfileCell.class}, null, null, null, Theme.key_chats_menuPhoneCats),
                new ThemeDescription(sideMenu, 0, new Class[]{DrawerProfileCell.class}, null, null, null, Theme.key_chats_menuCloudBackgroundCats),
                new ThemeDescription(sideMenu, 0, new Class[]{DrawerProfileCell.class}, new String[]{"cloudDrawable"}, null, null, null, Theme.key_chats_menuCloud),
                new ThemeDescription(sideMenu, 0, new Class[]{DrawerProfileCell.class}, null, null, null, Theme.key_chat_serviceBackground),
                new ThemeDescription(sideMenu, 0, new Class[]{DrawerProfileCell.class}, null, null, null, Theme.key_chats_menuTopShadow),

                new ThemeDescription(sideMenu, ThemeDescription.FLAG_IMAGECOLOR, new Class[]{DrawerActionCell.class}, new String[]{"textView"}, null, null, null, Theme.key_chats_menuItemIcon),
                new ThemeDescription(sideMenu, 0, new Class[]{DrawerActionCell.class}, new String[]{"textView"}, null, null, null, Theme.key_chats_menuItemText),

                new ThemeDescription(sideMenu, 0, new Class[]{DividerCell.class}, Theme.dividerPaint, null, null, Theme.key_divider),

                new ThemeDescription(listView, 0, new Class[]{LoadingCell.class}, new String[]{"progressBar"}, null, null, null, Theme.key_progressCircle),

                new ThemeDescription(listView, 0, new Class[]{ProfileSearchCell.class}, Theme.dialogs_offlinePaint, null, null, Theme.key_windowBackgroundWhiteGrayText3),
                new ThemeDescription(listView, 0, new Class[]{ProfileSearchCell.class}, Theme.dialogs_onlinePaint, null, null, Theme.key_windowBackgroundWhiteBlueText3),

                new ThemeDescription(listView, 0, new Class[]{GraySectionCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayText2),
                new ThemeDescription(listView, ThemeDescription.FLAG_CELLBACKGROUNDCOLOR, new Class[]{GraySectionCell.class}, null, null, null, Theme.key_graySection),

                new ThemeDescription(listView, ThemeDescription.FLAG_TEXTCOLOR, new Class[]{HashtagSearchCell.class}, null, null, null, Theme.key_windowBackgroundWhiteBlackText),

                new ThemeDescription(progressView, ThemeDescription.FLAG_PROGRESSBAR, null, null, null, null, Theme.key_progressCircle),

                new ThemeDescription(dialogsSearchAdapter.getInnerListView(), 0, new Class[]{HintDialogCell.class}, Theme.dialogs_countPaint, null, null, Theme.key_chats_unreadCounter),
                new ThemeDescription(dialogsSearchAdapter.getInnerListView(), 0, new Class[]{HintDialogCell.class}, Theme.dialogs_countGrayPaint, null, null, Theme.key_chats_unreadCounterMuted),
                new ThemeDescription(dialogsSearchAdapter.getInnerListView(), 0, new Class[]{HintDialogCell.class}, Theme.dialogs_countTextPaint, null, null, Theme.key_chats_unreadCounterText),
                new ThemeDescription(dialogsSearchAdapter.getInnerListView(), 0, new Class[]{HintDialogCell.class}, new String[]{"nameTextView"}, null, null, null, Theme.key_windowBackgroundWhiteBlackText),

                new ThemeDescription(fragmentContextView, ThemeDescription.FLAG_CELLBACKGROUNDCOLOR, new Class[]{FragmentContextView.class}, new String[]{"frameLayout"}, null, null, null, Theme.key_inappPlayerBackground),
                new ThemeDescription(fragmentContextView, 0, new Class[]{FragmentContextView.class}, new String[]{"playButton"}, null, null, null, Theme.key_inappPlayerPlayPause),
                new ThemeDescription(fragmentContextView, ThemeDescription.FLAG_TEXTCOLOR, new Class[]{FragmentContextView.class}, new String[]{"titleTextView"}, null, null, null, Theme.key_inappPlayerTitle),
                new ThemeDescription(fragmentContextView, ThemeDescription.FLAG_TEXTCOLOR, new Class[]{FragmentContextView.class}, new String[]{"frameLayout"}, null, null, null, Theme.key_inappPlayerPerformer),
                new ThemeDescription(fragmentContextView, ThemeDescription.FLAG_CELLBACKGROUNDCOLOR, new Class[]{FragmentContextView.class}, new String[]{"closeButton"}, null, null, null, Theme.key_inappPlayerClose),

                new ThemeDescription(fragmentContextView, ThemeDescription.FLAG_CELLBACKGROUNDCOLOR, new Class[]{FragmentContextView.class}, new String[]{"frameLayout"}, null, null, null, Theme.key_returnToCallBackground),
                new ThemeDescription(fragmentContextView, 0, new Class[]{FragmentContextView.class}, new String[]{"titleTextView"}, null, null, null, Theme.key_returnToCallText),

                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogBackground),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogBackgroundGray),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogTextBlack),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogTextLink),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogLinkSelection),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogTextBlue),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogTextBlue2),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogTextBlue3),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogTextBlue4),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogTextRed),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogTextGray),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogTextGray2),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogTextGray3),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogTextGray4),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogIcon),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogTextHint),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogInputField),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogInputFieldActivated),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogCheckboxSquareBackground),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogCheckboxSquareCheck),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogCheckboxSquareUnchecked),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogCheckboxSquareDisabled),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogRadioBackground),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogRadioBackgroundChecked),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogProgressCircle),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogButton),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogButtonSelector),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogScrollGlow),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogRoundCheckBox),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogRoundCheckBoxCheck),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogBadgeBackground),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogBadgeText),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogLineProgress),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogLineProgressBackground),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogGrayLine),
        };
    }

    //HoseinKord
    public class DialogsOnTouch implements View.OnTouchListener {

        private DisplayMetrics displayMetrics;
        //private static final String logTag = "SwipeDetector";
        private static final int MIN_DISTANCE_HIGH = 40;
        private static final int MIN_DISTANCE_HIGH_Y = 60;
        private float downX, downY, upX, upY;
        private float vDPI;

        Context mContext;

        public DialogsOnTouch(Context context) {
            this.mContext = context;
            displayMetrics = context.getResources().getDisplayMetrics();
            vDPI = displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT;
            //Log.e("DialogsActivity","DialogsOnTouch vDPI " + vDPI);
        }

        public boolean onTouch(View view, MotionEvent event) {

            touchPositionDP = Math.round(event.getX() / vDPI);
            //Log.e("DialogsActivity","onTouch touchPositionDP " + touchPositionDP + " hideTabs " + hideTabs);
            if(hideTabs){
                return false;
            }

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN: {
                    downX = Math.round(event.getX() / vDPI);
                    downY = Math.round(event.getY() / vDPI);
                    //Log.e("DialogsActivity", "view " + view.toString());
                    if(touchPositionDP > 50){
                        parentLayout.getDrawerLayoutContainer().setAllowOpenDrawer(false, false);
                        //Log.e("DialogsActivity", "DOWN setAllowOpenDrawer FALSE");
                    }
                    //Log.e("DialogsActivity", "DOWN downX " + downX);
                    return view instanceof LinearLayout; // for emptyView
                }
                case MotionEvent.ACTION_UP: {
                    upX = Math.round(event.getX() / vDPI);
                    upY = Math.round(event.getY() / vDPI);
                    float deltaX = downX - upX;
                    float deltaY = downY - upY;
                    //Log.e(logTag, "MOVE X " + deltaX);
                    //Log.e(logTag, "MOVE Y " + deltaY);
                    //Log.e("DialogsActivity", "UP downX " + downX);
                    //Log.e("DialogsActivity", "UP upX " + upX);
                    //Log.e("DialogsActivity", "UP deltaX " + deltaX);
                    // horizontal swipe detection
                    if (Math.abs(deltaX) > MIN_DISTANCE_HIGH && Math.abs(deltaY) < MIN_DISTANCE_HIGH_Y) {
                        //if (Math.abs(deltaX) > MIN_DISTANCE_HIGH) {
                        refreshDialogType(deltaX < 0 ? 0 : 1);//0: Left - Right 1: Right - Left
                        downX = Math.round(event.getX() / vDPI );
                        refreshAdapter(mContext);
                        //dialogsAdapter.notifyDataSetChanged();
                        refreshTabAndListViews(false);
                        //return true;
                    }
                    //Log.e("DialogsActivity", "UP2 downX " + downX);
                    if(touchPositionDP > 50){
                        parentLayout.getDrawerLayoutContainer().setAllowOpenDrawer(true, false);
                    }
                    //downX = downY = upX = upY = 0;
                    return false;
                }
            }

            return false;
        }
    }

    //HoseinKord
    private void hideTabsAnimated(final boolean hide){
        if (tabsHidden == hide) {
            return;
        }
        tabsHidden = hide;
        if(hide)listView.setPadding(0, 0, 0, 0);
        ObjectAnimatorProxy animator = ObjectAnimatorProxy.ofFloatProxy(tabsView, "translationY", hide ? -AndroidUtilities.dp(tabsHeight) : 0).setDuration(300);
        animator.addListener(new AnimatorListenerAdapterProxy() {
            @Override
            public void onAnimationEnd(Object animation) {
                if(!tabsHidden)listView.setPadding(0, AndroidUtilities.dp(tabsHeight) , 0, 0);
            }
        });
        animator.start();
    }
    private void refreshAdapter(Context context){
        refreshAdapterAndTabs(new DialogsAdapter(context, dialogsType));
    }
    private void refreshAdapterAndTabs(DialogsAdapter adapter){
        dialogsAdapter = adapter;
        listView.setAdapter(dialogsAdapter);
        dialogsAdapter.notifyDataSetChanged();
        if(!onlySelect){
            selectedTab =  dialogsType;
            SharedPreferences plusPreferences = ApplicationLoader.applicationContext.getSharedPreferences(HoldConst.PREF_MAIN, Activity.MODE_PRIVATE);
            SharedPreferences.Editor editor = plusPreferences.edit();
            editor.putInt(HoldConst.PREF_selTab, selectedTab).apply();
        }
        refreshTabs();
    }
    private void refreshTabAndListViews(boolean forceHide){
        if(hideTabs || forceHide){
            tabsView.setVisibility(View.GONE);
            listView.setPadding(0, 0, 0, 0);
        }else{
            tabsView.setVisibility(View.VISIBLE);
            int h = AndroidUtilities.dp(tabsHeight);
            ViewGroup.LayoutParams params = tabsView.getLayoutParams();
            if(params != null){
                params.height = h;
                tabsView.setLayoutParams(params);
            }
            listView.setPadding(0, h, 0, 0);
            hideTabsAnimated(false);
        }
        listView.scrollToPosition(0);
    }

    private void refreshTabs(){
        //resetTabs();
        SharedPreferences themePrefs = ApplicationLoader.applicationContext.getSharedPreferences(AndroidUtilities.THEME_PREFS, Context.MODE_PRIVATE);
        int defColor = themePrefs.getInt("chatsHeaderIconsColor", 0xffffffff);
        int iconColor = themePrefs.getInt("chatsHeaderTabIconColor", defColor);

        int iColor = themePrefs.getInt("chatsHeaderTabUnselectedIconColor", AndroidUtilities.getIntAlphaColor("chatsHeaderTabIconColor", defColor, 0.3f));

        allTab.setBackgroundResource(0);
        usersTab.setBackgroundResource(0);
        groupsTab.setBackgroundResource(0);
        superGroupsTab.setBackgroundResource(0);
        allGroupsTab.setBackgroundResource(0);
        channelsTab.setBackgroundResource(0);
        botsTab.setBackgroundResource(0);
        favsTab.setBackgroundResource(0);

        allTab.setColorFilter(iColor, PorterDuff.Mode.SRC_IN);
        usersTab.setColorFilter(iColor, PorterDuff.Mode.SRC_IN);
        groupsTab.setColorFilter(iColor, PorterDuff.Mode.SRC_IN);
        superGroupsTab.setColorFilter(iColor, PorterDuff.Mode.SRC_IN);
        allGroupsTab.setColorFilter(iColor, PorterDuff.Mode.SRC_IN);
        channelsTab.setColorFilter(iColor, PorterDuff.Mode.SRC_IN);
        botsTab.setColorFilter(iColor, PorterDuff.Mode.SRC_IN);
        favsTab.setColorFilter(iColor, PorterDuff.Mode.SRC_IN);
        Drawable selected = getParentActivity().getResources().getDrawable(R.drawable.tab_selected);
//        selected.setColorFilter(iconColor, PorterDuff.Mode.SRC_IN);
        switch(dialogsType) {

            case 0:
                allTab.setColorFilter(iconColor, PorterDuff.Mode.SRC_IN);
                allTab.setBackgroundDrawable(selected);
                break;
            case 3:
                usersTab.setColorFilter(iconColor, PorterDuff.Mode.SRC_IN);
                usersTab.setBackgroundDrawable(selected);
                break;
            case 4:
                groupsTab.setColorFilter(iconColor, PorterDuff.Mode.SRC_IN);
                groupsTab.setBackgroundDrawable(selected);
                break;
            case 9:
                allGroupsTab.setColorFilter(iconColor, PorterDuff.Mode.SRC_IN);
                allGroupsTab.setBackgroundDrawable(selected);
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
                break;
        }

//        String t = getHeaderAllTitles();
//        actionBar.setTitle(t);
//        paintHeader(true);

        if (getDialogsArray() != null && getDialogsArray().isEmpty()) {
            searchEmptyView.setVisibility(View.GONE);
            progressView.setVisibility(View.GONE);
            if(emptyView.getChildCount() > 0){
                TextView tv = (TextView) emptyView.getChildAt(0);
                if(tv != null){
                    tv.setText(dialogsType < 3 ? LocaleController.getString("NoChats", R.string.NoChats) : dialogsType == 8 ? LocaleController.getString("NoFavoritesHelp", R.string.NoMessages) : "");
                    tv.setTextColor(themePrefs.getInt("chatsNameColor", 0xff212121));
                }
                if(emptyView.getChildAt(1) != null)emptyView.getChildAt(1).setVisibility(View.GONE);
            }
            emptyView.setVisibility(View.VISIBLE);
            emptyView.setBackgroundColor(themePrefs.getInt("chatsRowColor", 0xffffffff));
            listView.setEmptyView(emptyView);
        }
    }
    private void refreshDialogType(int d){
        if(hideTabs)return;
        setPrefTabs();
        boolean loop = boshraPrefe.getBoolean(HoldConst.PREF_infiniteTabsSwipe, true);
        if(d == 1){
            switch(dialogsType) {
                case 3: // Users
                    dialogsType =!hideGroups ? 4 : !hideSGroups ? 7 : !hideGroupsAll ? 9 : !hideChannels ? 5 : !hideBots ? 6 : !hideFavs ? 8 : loop ?
                            !hideAll ? 0 : dialogsType : dialogsType ;
                    break;
                case 4: //Groups
                    dialogsType = !hideSGroups ? 7 :!hideGroupsAll ? 9: !hideChannels ? 5 : !hideBots ? 6 : !hideFavs ? 8 : loop ? !hideAll ? 0 : !hideUsers ? 3 : dialogsType: dialogsType;
                    break;
                case 7: //Supergroups
                    dialogsType = !hideGroupsAll ? 9 : !hideChannels ? 5 : !hideBots ? 6 : !hideFavs ? 8 : loop ? !hideAll ? 0 : !hideUsers ? 3 : !hideGroups ? 4 : dialogsType :dialogsType ;
                    break;
                case 9: //allgroups
                    dialogsType =  !hideChannels ? 5 : !hideBots ? 6 : !hideFavs ? 8 : loop ? !hideAll ? 0 : !hideUsers ? 3 : !hideGroups ? 4 : dialogsType :dialogsType ;
                    break;
                case 5: //Channels
                    dialogsType = !hideBots ? 6 : !hideFavs ? 8 : loop ? !hideAll ? 0 : !hideUsers ? 3 : !hideGroups ? 4 :!hideSGroups ? 7 : !hideGroupsAll ? 9 : dialogsType : dialogsType ;
                    break;
                case 6: //Bots
                    dialogsType = !hideFavs ? 8 : loop ? !hideAll ? 0 : !hideUsers ? 3 : !hideGroups ? 4 : !hideSGroups ? 7 :  !hideGroupsAll ? 9 : !hideChannels ? 8 : dialogsType : dialogsType ;
                    break;
                case 8: //Favorites
                    if(loop){
                        dialogsType = !hideAll ? 0 : !hideUsers ? 3 : !hideGroups ? 4 : !hideSGroups ? 7 :  !hideGroupsAll ? 9 : !hideChannels ? 5 : !hideBots ? 6 : dialogsType ;
                    }
                    break;
                case  0: //All
                    dialogsType = !hideUsers ? 3 : !hideGroups ? 4 : !hideSGroups ? 7 : !hideGroupsAll ? 9 : !hideChannels ? 5 : !hideBots ? 6 : !hideFavs ? 8 : dialogsType;
                    break;
            }
        }else{
            switch(dialogsType) {
                case 3: // Users
                    dialogsType = !hideAll ? 0 : !hideFavs ? 8 : ! hideBots ? 6 : !hideChannels ? 5 : !hideGroupsAll ? 9 : !hideSGroups ? 7 : !hideGroups ? 4 : dialogsType;
                    break;
                case 4: //Groups
                    dialogsType = !hideUsers ? 3 :  !hideAll ? 0 : !hideFavs ? 8 : ! hideBots ? 6 : !hideChannels ? 5 : !hideGroupsAll ? 9 : !hideSGroups ? 7 : dialogsType ;
                    break;
                case 7: //Supergroups
                    dialogsType = !hideGroups ? 4 : !hideUsers ? 3 :!hideAll ? 0 : !hideFavs ? 8 : ! hideBots ? 6 : !hideChannels ? 5 :  !hideGroupsAll ? 9 :dialogsType ;
                    break;
                case 5: //Channels
                    dialogsType =  !hideGroupsAll ? 9 :!hideSGroups ? 7 : !hideGroups ? 4 : !hideUsers ? 3 :!hideAll ? 0 : !hideFavs ? 8 : ! hideBots ? 6 : dialogsType ;
                    break;
                case 9: //groupsAll
                    dialogsType =   !hideSGroups ? 7 : !hideGroups ? 4 : !hideUsers ? 3 :!hideAll ? 0 : !hideFavs ? 8 : ! hideBots ? 6 : dialogsType ;
                    break;
                case 6: //Bots
                    dialogsType = !hideChannels ? 5 :  !hideGroupsAll ? 9 :!hideSGroups ? 7 : !hideGroups ? 4 : !hideUsers ? 3 : !hideAll ? 0 : !hideFavs ? 8 :  dialogsType ;
                    break;
                case 8: //Favorites
                    dialogsType = !hideBots ? 6 : !hideChannels ? 5 : !hideGroupsAll ? 9 : !hideSGroups ? 7 : !hideGroups ? 9 : !hideUsers ? 3 : !hideAll ? 0 : dialogsType ;
                    break;
                case 0 :
                    dialogsType = !hideFavs ? 8 : !hideBots ? 6 : !hideChannels ? 5 :  !hideGroupsAll ? 9 : !hideSGroups ? 7 : !hideGroups ? 9 : !hideUsers ? 3 : dialogsType;
                    break;
//                default: //All
//                    if(loop){
//                        dialogsType = !hideFavs ? 8 : !hideBots ? 6 : !hideChannels ? 5 : !hideSGroups ? 7 : !hideGroups ? 9 : !hideUsers ? 3 : 0;
//                    }
            }
        }
    }

    @Override
    public PhotoViewer.PlaceProviderObject getPlaceForPhoto(MessageObject messageObject, TLRPC.FileLocation fileLocation, int index) {
        if (fileLocation == null) {
            return null;
        }

        TLRPC.FileLocation photoBig = null;
        if (user_id != 0) {
            TLRPC.User user = MessagesController.getInstance().getUser(user_id);
            if (user != null && user.photo != null && user.photo.photo_big != null) {
                photoBig = user.photo.photo_big;
            }
        } else if (chat_id != 0) {
            TLRPC.Chat chat = MessagesController.getInstance().getChat(chat_id);
            if (chat != null && chat.photo != null && chat.photo.photo_big != null) {
                photoBig = chat.photo.photo_big;
            }
        }

        if (photoBig != null && photoBig.local_id == fileLocation.local_id && photoBig.volume_id == fileLocation.volume_id && photoBig.dc_id == fileLocation.dc_id) {
            int coords[] = new int[2];
            avatarImage.getLocationInWindow(coords);
            PhotoViewer.PlaceProviderObject object = new PhotoViewer.PlaceProviderObject();
            object.viewX = coords[0];
            object.viewY = coords[1] - AndroidUtilities.statusBarHeight;
            object.parentView = avatarImage;
            object.imageReceiver = avatarImage.getImageReceiver();
            object.dialogId = user_id;
            object.thumb = object.imageReceiver.getBitmap();
            object.size = -1;
            object.radius = avatarImage.getImageReceiver().getRoundRadius();
            return object;
        }
        return null;
    }

    @Override
    public Bitmap getThumbForPhoto(MessageObject messageObject, TLRPC.FileLocation fileLocation, int index) {
        return null;
    }

    @Override
    public void willSwitchFromPhoto(MessageObject messageObject, TLRPC.FileLocation fileLocation, int index) {

    }

    @Override
    public void willHidePhotoViewer() {

    }

    @Override
    public boolean isPhotoChecked(int index) {
        return false;
    }

    @Override
    public void setPhotoChecked(int index, VideoEditedInfo videoEditedInfo) {

    }


    @Override
    public boolean cancelButtonPressed() {
        return false;
    }

    @Override
    public void sendButtonPressed(int index, VideoEditedInfo videoEditedInfo) {

    }


    @Override
    public int getSelectedCount() {
        return 0;
    }

    @Override
    public void updatePhotoAtIndex(int index) {

    }

    @Override
    public boolean allowCaption() {
        return false;
    }

    @Override
    public boolean scaleToFill() {
        return false;
    }

    //HoseinKord
    private void createTabs(final Context context) {
        setPrefTabs() ;
        refreshTabAndListViews(false);

        int t = boshraPrefe.getInt(HoldConst.PREF_defTab, -1);
        selectedTab = t != -1 ? t : boshraPrefe.getInt(HoldConst.PREF_selTab, 2);

        if (!hideTabs  && dialogsType != selectedTab) {
            dialogsType =  selectedTab;
            dialogsAdapter = new DialogsAdapter(context, dialogsType);
            listView.setAdapter(dialogsAdapter);
            dialogsAdapter.notifyDataSetChanged();
        }

        dialogsBackupAdapter = new DialogsAdapter(context, 0);

        tabsLayout = new LinearLayout(context);
        tabsLayout.setOrientation(LinearLayout.HORIZONTAL);
        tabsLayout.setGravity(Gravity.CENTER);

        try {
            tabsLayout.setBackgroundColor(Theme.getColor(Theme.key_avatar_backgroundActionBarBlue));
        }catch (Exception e){

        }

        //1
        allTab = new ImageView(context);
        //allTab.setScaleType(ImageView.ScaleType.CENTER);
        allTab.setImageResource(R.drawable.tab_all);
        allCounter = new TextView(context);
        allCounter.setTag("ALL");
        addTabView(context, allTab, allCounter, !hideAll);
        //2
        usersTab = new ImageView(context);
        usersTab.setImageResource(R.drawable.tab_user);
        usersCounter = new TextView(context);
        usersCounter.setTag("USERS");
        addTabView(context, usersTab, usersCounter, !hideUsers);

        //3
        groupsTab = new ImageView(context);
        groupsTab.setImageResource(R.drawable.tab_group);
        groupsCounter = new TextView(context);
        groupsCounter.setTag("GROUPS");

        addTabView(context, groupsTab, groupsCounter, !hideGroups);

        //4
        superGroupsTab = new ImageView(context);
        superGroupsTab.setImageResource(R.drawable.tab_supergroup);
        sGroupsCounter = new TextView(context);
        sGroupsCounter.setTag("SGROUP");
        /*superGroupsTab.setScaleType(ImageView.ScaleType.CENTER);
        */
        addTabView(context, superGroupsTab, sGroupsCounter, !hideSGroups);

        //groupAll
        allGroupsTab = new ImageView(context);
        allGroupsTab.setImageResource(R.drawable.tab_group);
        /*superGroupsTab.setScaleType(ImageView.ScaleType.CENTER);
        */
        allGroupsCounter = new TextView(context);
        allGroupsCounter.setTag("GROUPALL");
        addTabView(context, allGroupsTab, allGroupsCounter, !hideGroupsAll);
        //5
        channelsTab = new ImageView(context);
        channelsTab.setImageResource(R.drawable.tab_channel);
        channelsCounter = new TextView(context);
        channelsCounter.setTag("CHANNELS");
        /*channelsTab.setScaleType(ImageView.ScaleType.CENTER);
      */
        addTabView(context, channelsTab, channelsCounter, !hideChannels);
        //6
        botsTab = new ImageView(context);
        botsTab.setImageResource(R.drawable.tab_bot);
        botsCounter = new TextView(context);
        botsCounter.setTag("BOTS");
        addTabView(context, botsTab, botsCounter, !hideBots);
        /*botsTab.setScaleType(ImageView.ScaleType.CENTER);
      */
        //7
        favsTab = new ImageView(context);
        favsTab.setImageResource(R.drawable.tab_favs);
        favsCounter = new TextView(context);
        favsCounter.setTag("FAVS");
        /*favsTab.setScaleType(ImageView.ScaleType.CENTER);
       */
        addTabView(context, favsTab, favsCounter, !hideFavs);

        tabsView.addView(tabsLayout, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT , Gravity.LEFT));

        try {
            allTab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (dialogsType != 0) {
                        dialogsType = 0;
                        refreshAdapter(context);
                    }
                }
            });

            allTab.setOnLongClickListener(new View.OnLongClickListener() {
                public boolean onLongClick(View view) {
                    android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getParentActivity());
                    builder.setTitle(LocaleController.getString("All", R.string.All));
                    CharSequence items[];
                    SharedPreferences plusPreferences = ApplicationLoader.applicationContext.getSharedPreferences(HoldConst.PREF_MAIN, Activity.MODE_PRIVATE);
                    final int tabVal = 0;
                    final int def = plusPreferences.getInt(HoldConst.PREF_defTab, -1);
                    final int sort = plusPreferences.getInt(HoldConst.Pref_sortAll, 0);

                    CharSequence cs2 = def == tabVal ? LocaleController.getString("ResetDefaultTab", R.string.ResetDefaultTab) : LocaleController.getString("SetAsDefaultTab", R.string.SetAsDefaultTab);
                    CharSequence cs1 = sort == 0 ? LocaleController.getString("SortByUnreadCount", R.string.SortByUnreadCount) : LocaleController.getString("SortByLastMessage", R.string.SortByLastMessage);
                    items = new CharSequence[]{cs1, cs2, LocaleController.getString("MarkAllAsRead", R.string.MarkAllAsRead)};
                    builder.setItems(items, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, final int which) {
                            SharedPreferences plusPreferences = ApplicationLoader.applicationContext.getSharedPreferences(HoldConst.PREF_MAIN, Activity.MODE_PRIVATE);
                            SharedPreferences.Editor editor = plusPreferences.edit();
                            if (which == 0) {
                                editor.putInt(HoldConst.Pref_sortAll, sort == 0 ? 1 : 0).apply();
                                if(dialogsAdapter.getItemCount() > 1) {
                                    dialogsAdapter.notifyDataSetChanged();
                                }
                            } else if (which ==1){
                                editor.putInt(HoldConst.PREF_defTab, def == tabVal ? -1 : tabVal).apply();
                            } else if (which == 2){
                                markAsReadDialog(true);
                            }
                        }
                    });
                    showDialog(builder.create());
                    return true;
                }
            });
            usersTab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (dialogsType != 3) {
                        dialogsType = 3;
                        refreshAdapter(context);
                    }
                }
            });

            usersTab.setOnLongClickListener(new View.OnLongClickListener() {
                public boolean onLongClick(View view) {
                    android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getParentActivity());
                    builder.setTitle(LocaleController.getString("Users", R.string.Users));
                    CharSequence items[];
                    SharedPreferences plusPreferences = ApplicationLoader.applicationContext.getSharedPreferences(HoldConst.PREF_MAIN, Activity.MODE_PRIVATE);
                    final int tabVal = 3;
                    final int sort = plusPreferences.getInt(HoldConst.Pref_sortUsers, 0);
                    final int def = plusPreferences.getInt(HoldConst.PREF_defTab, -1);
                    CharSequence cs = def == tabVal ? LocaleController.getString("ResetDefaultTab", R.string.ResetDefaultTab) : LocaleController.getString("SetAsDefaultTab", R.string.SetAsDefaultTab);
                    CharSequence sortByStatus = LocaleController.getString("SortByStatus", R.string.SortByStatus) ;
                    CharSequence SortByLastMessage = LocaleController.getString("SortByLastMessage", R.string.SortByLastMessage) ;
                    CharSequence SortByunRead = LocaleController.getString("SortByUnreadCount", R.string.SortByUnreadCount) ;
                    items = new CharSequence[]{  SortByLastMessage ,sortByStatus  ,SortByunRead, cs, LocaleController.getString("MarkAllAsRead", R.string.MarkAllAsRead)};
                    builder.setItems(items, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, final int which) {
                            SharedPreferences plusPreferences = ApplicationLoader.applicationContext.getSharedPreferences(HoldConst.PREF_MAIN, Activity.MODE_PRIVATE);
                            SharedPreferences.Editor editor = plusPreferences.edit();
                            if(which == 0 || which == 1  || which == 2){
                                editor.putInt(HoldConst.Pref_sortUsers,which).apply();
                                if(dialogsAdapter.getItemCount() > 1) {
                                    dialogsAdapter.notifyDataSetChanged();
                                }
                            }else if (which == 3) {
                                editor.putInt(HoldConst.PREF_defTab, def == tabVal ? -1 : tabVal).apply();
                            } else if (which == 4){
                                markAsReadDialog(true);
                            }
                        }
                    });
                    showDialog(builder.create());
                    return true;
                }
            });


            groupsTab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    SharedPreferences plusPreferences = ApplicationLoader.applicationContext.getSharedPreferences(HoldConst.PREF_MAIN, Activity.MODE_PRIVATE);
                    final boolean hideSGroups = plusPreferences.getBoolean(HoldConst.PREF_HIDE_GROUPS, false);
                    int i = hideSGroups ? 9 : 4;
                    if (dialogsType != i) {
                        dialogsType = i;
                        refreshAdapter(context);
                    }
//                    if (dialogsType != 2) {
//                        dialogsType = 2 ;
//                        refreshAdapter(context);
//                    }
                }
            });


            groupsTab.setOnLongClickListener(new View.OnLongClickListener() {
                public boolean onLongClick(View view) {
                    android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getParentActivity());
                    builder.setTitle(LocaleController.getString("Groups", R.string.Groups));
                    CharSequence items[];
                    SharedPreferences plusPreferences = ApplicationLoader.applicationContext.getSharedPreferences(HoldConst.PREF_MAIN, Activity.MODE_PRIVATE);
                    final boolean hideSGroups = plusPreferences.getBoolean(HoldConst.PREF_HIDE_SGROUPS, false);
                    final int tabVal = 2;
                    final int sort = plusPreferences.getInt(HoldConst.Pref_sortGroups, 0);
                    final int def = plusPreferences.getInt(HoldConst.PREF_defTab, -1);

                    CharSequence cs2 = def == tabVal ? LocaleController.getString("ResetDefaultTab", R.string.ResetDefaultTab) : LocaleController.getString("SetAsDefaultTab", R.string.SetAsDefaultTab);
                    CharSequence cs1 = sort == 0 ? LocaleController.getString("SortByUnreadCount", R.string.SortByUnreadCount) : LocaleController.getString("SortByLastMessage", R.string.SortByLastMessage);
                    items = new CharSequence[]{ cs1, cs2, LocaleController.getString("MarkAllAsRead", R.string.MarkAllAsRead)};
                    builder.setItems(items, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, final int which) {

                            SharedPreferences plusPreferences = ApplicationLoader.applicationContext.getSharedPreferences(HoldConst.PREF_MAIN, Activity.MODE_PRIVATE);
                            SharedPreferences.Editor editor = plusPreferences.edit();
                            if (which == 0) {
                                editor.putInt(HoldConst.Pref_sortGroups, sort == 0 ? 1 : 0).apply();
                                if(dialogsAdapter.getItemCount() > 1) {
                                    dialogsAdapter.notifyDataSetChanged();
                                }
                            } else if (which == 1){
                                editor.putInt(HoldConst.PREF_defTab, def == tabVal ? -1 : tabVal).apply();
                            } else if (which == 2){
                                markAsReadDialog(true);
                            }
                        }
                    });
                    showDialog(builder.create());
                    return true;
                }
            });
            superGroupsTab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (dialogsType != 7) {
                        dialogsType = 7;
                        refreshAdapter(context);
                    }
                }
            });

            superGroupsTab.setOnLongClickListener(new View.OnLongClickListener() {
                public boolean onLongClick(View view) {
                    android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getParentActivity());
                    builder.setTitle(LocaleController.getString("SuperGroups", R.string.SuperGroups));
                    CharSequence items[];
                    SharedPreferences plusPreferences = ApplicationLoader.applicationContext.getSharedPreferences(HoldConst.PREF_MAIN, Activity.MODE_PRIVATE);
                    final int tabVal = 7;
                    final int def = plusPreferences.getInt(HoldConst.PREF_defTab, -1);
                    final int sort = plusPreferences.getInt(HoldConst.Pref_sortSGroups , 0);
                    final boolean hideSGroups = plusPreferences.getBoolean(HoldConst.PREF_HIDE_SGROUPS, false);
                    CharSequence cs2 = def == tabVal ? LocaleController.getString("ResetDefaultTab", R.string.ResetDefaultTab) : LocaleController.getString("SetAsDefaultTab", R.string.SetAsDefaultTab);
                    CharSequence cs1 = sort == 0 ? LocaleController.getString("SortByUnreadCount", R.string.SortByUnreadCount) : LocaleController.getString("SortByLastMessage", R.string.SortByLastMessage);
                    items = new CharSequence[]{cs1, cs2, LocaleController.getString("MarkAllAsRead", R.string.MarkAllAsRead)};
                    builder.setItems(items, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, final int which) {
                            SharedPreferences plusPreferences = ApplicationLoader.applicationContext.getSharedPreferences(HoldConst.PREF_MAIN, Activity.MODE_PRIVATE);
                            SharedPreferences.Editor editor = plusPreferences.edit();

                            if (which == 0) {
                                editor.putInt(HoldConst.Pref_sortSGroups, sort == 0 ? 1 : 0).apply();
                                if(dialogsAdapter.getItemCount() > 1) {
                                    dialogsAdapter.notifyDataSetChanged();
                                }
                            } else if (which == 1){
                                editor.putInt(HoldConst.PREF_defTab, def == tabVal ? -1 : tabVal).apply();
                            } else if (which == 2){
                                markAsReadDialog(true);
                            }
                        }
                    });
                    showDialog(builder.create());
                    return true;
                }
            });

            channelsTab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (dialogsType != 5) {
                        dialogsType = 5;
                        refreshAdapter(context);
                    }
                }
            });

            channelsTab.setOnLongClickListener(new View.OnLongClickListener() {
                public boolean onLongClick(View view) {
                    android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getParentActivity());
                    builder.setTitle(LocaleController.getString("Channels", R.string.Channels));
                    CharSequence items[];
                    SharedPreferences plusPreferences = ApplicationLoader.applicationContext.getSharedPreferences(HoldConst.PREF_MAIN, Activity.MODE_PRIVATE);
                    final int tabVal = 5;
                    final int sort = plusPreferences.getInt(HoldConst.Pref_sortChannels, 0);
                    final int def = plusPreferences.getInt(HoldConst.PREF_defTab, -1);
                    CharSequence cs = def == tabVal ? LocaleController.getString("ResetDefaultTab", R.string.ResetDefaultTab) : LocaleController.getString("SetAsDefaultTab", R.string.SetAsDefaultTab);
                    CharSequence cs1 = sort == 0 ? LocaleController.getString("SortByUnreadCount", R.string.SortByUnreadCount) : LocaleController.getString("SortByLastMessage", R.string.SortByLastMessage);
                    items = new CharSequence[]{cs1, cs, LocaleController.getString("MarkAllAsRead", R.string.MarkAllAsRead)};
                    builder.setItems(items, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, final int which) {
                            SharedPreferences plusPreferences = ApplicationLoader.applicationContext.getSharedPreferences(HoldConst.PREF_MAIN, Activity.MODE_PRIVATE);
                            SharedPreferences.Editor editor = plusPreferences.edit();
                            if (which == 1) {
                                editor.putInt(HoldConst.PREF_defTab, def == tabVal ? -1 : tabVal).apply();
                            } else if (which == 0) {
                                editor.putInt(HoldConst.Pref_sortChannels, sort == 0 ? 1 : 0).apply();
                                if(dialogsAdapter.getItemCount() > 1) {
                                    dialogsAdapter.notifyDataSetChanged();
                                }
                            } else if (which == 2) {
                                markAsReadDialog(true);
                            }

                        }
                    });
                    showDialog(builder.create());
                    return true;
                }
            });
            botsTab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (dialogsType != 6) {
                        dialogsType = 6;
                        refreshAdapter(context);
                    }
                }
            });

            botsTab.setOnLongClickListener(new View.OnLongClickListener() {
                public boolean onLongClick(View view) {
                    android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getParentActivity());
                    builder.setTitle(LocaleController.getString("Bots", R.string.Bots));
                    CharSequence items[];
                    SharedPreferences plusPreferences = ApplicationLoader.applicationContext.getSharedPreferences(HoldConst.PREF_MAIN, Activity.MODE_PRIVATE);
                    final int tabVal = 6;
                    final int sort = plusPreferences.getInt(HoldConst.Pref_sortBots, 0);
                    final int def = plusPreferences.getInt(HoldConst.PREF_defTab, -1);
                    CharSequence cs = def == tabVal ? LocaleController.getString("ResetDefaultTab", R.string.ResetDefaultTab) : LocaleController.getString("SetAsDefaultTab", R.string.SetAsDefaultTab);
                    CharSequence cs1 = sort == 0 ? LocaleController.getString("SortByUnreadCount", R.string.SortByUnreadCount) : LocaleController.getString("SortByLastMessage", R.string.SortByLastMessage);
                    items = new CharSequence[]{cs1, cs, LocaleController.getString("MarkAllAsRead", R.string.MarkAllAsRead)};
                    builder.setItems(items, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, final int which) {
                            SharedPreferences plusPreferences = ApplicationLoader.applicationContext.getSharedPreferences(HoldConst.PREF_MAIN, Activity.MODE_PRIVATE);
                            SharedPreferences.Editor editor = plusPreferences.edit();
                            if (which == 1) {
                                editor.putInt(HoldConst.PREF_defTab, def == tabVal ? -1 : tabVal).apply();
                            } else if (which == 0){
                                editor.putInt(HoldConst.PREF_defTab, sort == 0 ? 1 : 0).apply();
                                if(dialogsAdapter.getItemCount() > 1) {
                                    dialogsAdapter.notifyDataSetChanged();
                                }
                            } else if (which == 2){
                                markAsReadDialog(true);
                            }
                        }
                    });
                    showDialog(builder.create());
                    return true;
                }
            });


            favsTab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (dialogsType != 8) {
                        dialogsType = 8;
                        refreshAdapter(context);
                    }
                }
            });

            favsTab.setOnLongClickListener(new View.OnLongClickListener() {
                public boolean onLongClick(View view) {
                    android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getParentActivity());
                    builder.setTitle(LocaleController.getString("Favorites", R.string.Favorites));
                    CharSequence items[];
                    SharedPreferences plusPreferences = ApplicationLoader.applicationContext.getSharedPreferences(HoldConst.PREF_MAIN, Activity.MODE_PRIVATE);
                    final int tabVal = 8;
                    final int sort = plusPreferences.getInt(HoldConst.Pref_sortFavs, 0);
                    final int def = plusPreferences.getInt(HoldConst.PREF_defTab, -1);
                    CharSequence cs = def == tabVal ? LocaleController.getString("ResetDefaultTab", R.string.ResetDefaultTab) : LocaleController.getString("SetAsDefaultTab", R.string.SetAsDefaultTab);
                    CharSequence cs1 = sort == 0 ? LocaleController.getString("SortByUnreadCount", R.string.SortByUnreadCount) : LocaleController.getString("SortByLastMessage", R.string.SortByLastMessage);
                    items = new CharSequence[]{cs1, cs, LocaleController.getString("MarkAllAsRead", R.string.MarkAllAsRead)};
                    builder.setItems(items, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, final int which) {
                            SharedPreferences plusPreferences = ApplicationLoader.applicationContext.getSharedPreferences(HoldConst.PREF_MAIN, Activity.MODE_PRIVATE);
                            SharedPreferences.Editor editor = plusPreferences.edit();
                            if (which == 1) {
                                editor.putInt(HoldConst.PREF_defTab, def == tabVal ? -1 : tabVal).apply();
                            } else if (which == 0){
                                editor.putInt(HoldConst.Pref_sortFavs, sort == 0 ? 1 : 0).apply();
                                if(dialogsAdapter.getItemCount() > 1) {
                                    dialogsAdapter.notifyDataSetChanged();
                                }
                            } else if (which == 2){
                                markAsReadDialog(true);
                            }

                        }
                    });
                    showDialog(builder.create());
                    return true;
                }
            });
        }catch (Exception e){
            Log.e("log" , e.getMessage() +"") ;
        }

        allGroupsTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (dialogsType != 9) {
                    dialogsType = 9;
                    refreshAdapter(context);
                }
            }
        });


        allGroupsTab.setOnLongClickListener(new View.OnLongClickListener() {
            public boolean onLongClick(View view) {
                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getParentActivity());
                builder.setTitle(LocaleController.getString("AllGroupTab", R.string.AllGroupTab));
                CharSequence items[];
                SharedPreferences plusPreferences = ApplicationLoader.applicationContext.getSharedPreferences(HoldConst.PREF_MAIN, Activity.MODE_PRIVATE);
                final int tabVal = 9;
                final int sort = plusPreferences.getInt(HoldConst.Pref_sortGroupsAll, 0);
                final int def = plusPreferences.getInt(HoldConst.PREF_defTab, -1);

                CharSequence cs2 = def == tabVal ? LocaleController.getString("ResetDefaultTab", R.string.ResetDefaultTab) : LocaleController.getString("SetAsDefaultTab", R.string.SetAsDefaultTab);
                CharSequence cs1 = sort == 0 ? LocaleController.getString("SortByUnreadCount", R.string.SortByUnreadCount) : LocaleController.getString("SortByLastMessage", R.string.SortByLastMessage);
                items = new CharSequence[]{ cs1, cs2, LocaleController.getString("MarkAllAsRead", R.string.MarkAllAsRead)};
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, final int which) {

                        SharedPreferences plusPreferences = ApplicationLoader.applicationContext.getSharedPreferences(HoldConst.PREF_MAIN, Activity.MODE_PRIVATE);
                        SharedPreferences.Editor editor = plusPreferences.edit();
                        if (which == 0) {
                            editor.putInt(HoldConst.Pref_sortGroupsAll , sort == 0 ? 1 : 0).apply();
                            if(dialogsAdapter.getItemCount() > 1) {
                                dialogsAdapter.notifyDataSetChanged();
                            }
                        } else if (which == 1){
                            editor.putInt(HoldConst.PREF_defTab, def == tabVal ? -1 : tabVal).apply();
                        } else if (which == 2){
                            markAsReadDialog(true);
                        }
                    }
                });
                showDialog(builder.create());
                return true;
            }
        });
    }

    private void setPrefTabs() {
        boshraPrefe = ApplicationLoader.applicationContext.getSharedPreferences(HoldConst.PREF_MAIN , Activity.MODE_PRIVATE);
        editor = boshraPrefe.edit();
        hideAll = boshraPrefe.getBoolean(HoldConst.PREF_HIDE_ALL, false);
        hideUsers = boshraPrefe.getBoolean(HoldConst.PREF_HIDE_USERS, false);
        hideGroupsAll = boshraPrefe.getBoolean(HoldConst.PREF_HIDE_GROUP_ALL, true);
        hideGroups = boshraPrefe.getBoolean(HoldConst.PREF_HIDE_GROUPS, false);
        hideSGroups = boshraPrefe.getBoolean(HoldConst.PREF_HIDE_SGROUPS, false);
        hideChannels = boshraPrefe.getBoolean(HoldConst.PREF_HIDE_CHANNELS, false);
        hideBots = boshraPrefe.getBoolean(HoldConst.PREF_HIDE_BOTS, false);
        hideFavs = boshraPrefe.getBoolean(HoldConst.PREF_HIDE_FAVES, false);
//        boolean hideFavs = true ;
        hideTabs = boshraPrefe.getBoolean(HoldConst.PREF_HIDE_TAB, false);
        disableAnimation = boshraPrefe.getBoolean(HoldConst.PREF_DisableTabsAnimation, true);
        if (hideUsers && hideGroups && hideSGroups && hideGroupsAll && hideChannels && hideBots && hideFavs) {
            if (!hideTabs) {
                hideTabs = true;
                editor.putBoolean(HoldConst.PREF_HIDE_TAB, true).apply();
            }
        }
        tabsHeight = boshraPrefe.getInt(HoldConst.PREF_tabsHeight, 40);

    }

    private void addTabView(Context context, ImageView iv, TextView tv, boolean show) {
//        show = true ;
        //SharedPreferences themePrefs = ApplicationLoader.applicationContext.getSharedPreferences(AndroidUtilities.THEME_PREFS, AndroidUtilities.THEME_PREFS_MODE);
        //int cColor = themePrefs.getInt("chatsHeaderTabCounterColor", 0xffffffff);
        //int bgColor = themePrefs.getInt("chatsHeaderTabCounterBGColor", 0xffff0000);

        iv.setScaleType(ImageView.ScaleType.CENTER);
        //int size = themePrefs.getInt("chatsHeaderTabCounterSize", 11);
        //tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, size);
        tv.setGravity(Gravity.CENTER);
        //tv.setTextColor(cColor);

        GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.RECTANGLE);
        shape.setCornerRadius(AndroidUtilities.dp(32));
        shape.setColor(Theme.getColor(Theme.key_avatar_backgroundActionBarBlue));

        tv.setBackgroundDrawable(shape);
        //tv.setPadding(AndroidUtilities.dp(size > 10 ? size - 7 : 4), 0, AndroidUtilities.dp(size > 10 ? size - 7 : 4), 0);
        RelativeLayout layout = new RelativeLayout(context);
        layout.addView(iv, LayoutHelper.createRelative(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT , Gravity.LEFT));
        layout.addView(tv, LayoutHelper.createRelative(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, 0, 0, 3, 6, RelativeLayout.ALIGN_PARENT_RIGHT));

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) tv.getLayoutParams();
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        tv.setLayoutParams(params);
        if (show) {
            tabsLayout.addView(layout, LayoutHelper.createLinear(0, LayoutHelper.MATCH_PARENT, 1.0f , Gravity.LEFT));
        }
    }

    private void markAsReadDialog(final boolean all){
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getParentActivity());
        TLRPC.Chat currentChat = MessagesController.getInstance().getChat((int) -selectedDialog);
        TLRPC.User user = MessagesController.getInstance().getUser((int) selectedDialog);
        String title = currentChat != null ? currentChat.title : user != null ? UserObject.getUserName(user) : LocaleController.getString("AppName", R.string.AppName);
//        builder.setTitle(all ? getHeaderAllTitles() : title);
        builder.setMessage((all ? LocaleController.getString("MarkAllAsRead", R.string.MarkAllAsRead) : LocaleController.getString("MarkAsRead", R.string.MarkAsRead)) + '\n' + LocaleController.getString("AreYouSure", R.string.AreYouSure));
        builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(all){
                    ArrayList<TLRPC.TL_dialog> dialogs = getDialogsArray();
                    if (dialogs != null && !dialogs.isEmpty()) {
                        for (int a = 0; a < dialogs.size(); a++) {
                            TLRPC.TL_dialog dialg = getDialogsArray().get(a);
                            if(dialg.unread_count > 0){
                                MessagesController.getInstance().markDialogAsRead(dialg.id, dialg.last_read, Math.max(0, dialg.top_message), dialg.last_message_date, true, false);
                            }
                        }
                    }
                } else {
                    TLRPC.TL_dialog dialg = MessagesController.getInstance().dialogs_dict.get(selectedDialog);
                    if(dialg.unread_count > 0) {
                        MessagesController.getInstance().markDialogAsRead(dialg.id, dialg.last_read, Math.max(0, dialg.top_message), dialg.last_message_date, true, false);
                    }
                }
            }
        });
        builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
        showDialog(builder.create());
    }

    private void updateTabs(){
        SharedPreferences plusPreferences = ApplicationLoader.applicationContext.getSharedPreferences(HoldConst.PREF_MAIN, Activity.MODE_PRIVATE);
        hideTabs = plusPreferences.getBoolean(HoldConst.PREF_HIDE_TAB, false);
        disableAnimation = plusPreferences.getBoolean(HoldConst.PREF_DisableTabsAnimation, true);
        tabsHeight = plusPreferences.getInt(HoldConst.PREF_tabsHeight, 40);
        refreshTabAndListViews(false);
        if (hideTabs && dialogsType > 0) {
            dialogsType = 0;
            refreshAdapterAndTabs(dialogsBackupAdapter);
        }

        //hideTabsAnimated(false);
    }

    //    private void hideShowTabs(int i){
//        RelativeLayout rl = null;
//        int pos = 0;
//        boolean b = false;
//        SharedPreferences boshraPrefe = ApplicationLoader.applicationContext.getSharedPreferences(HoldConst.PREF_MAIN, Activity.MODE_PRIVATE);
//        boolean hideAll = boshraPrefe.getBoolean(HoldConst.PREF_HIDE_ALL, false);
//        boolean hideUsers = boshraPrefe.getBoolean(HoldConst.PREF_HIDE_USERS, false);
//        boolean hideGroups = boshraPrefe.getBoolean(HoldConst.PREF_HIDE_GROUPS, false);
//        boolean hideSGroups = boshraPrefe.getBoolean(HoldConst.PREF_HIDE_SGROUPS, false);
//        boolean hideAllGroups = boshraPrefe.getBoolean(HoldConst.PREF_HIDE_GROUP_ALL, true);
//        boolean hideChanels = boshraPrefe.getBoolean(HoldConst.PREF_HIDE_CHANNELS, false);
//        boolean hideBots = boshraPrefe.getBoolean(HoldConst.PREF_HIDE_BOTS, false);
//        boolean hideFavs = boshraPrefe.getBoolean(HoldConst.PREF_HIDE_FAVES, false);
////        boolean hideFavs =true ;
//        switch(i) {
//            case 0: // All
//                rl = (RelativeLayout) allTab.getParent();
//                pos = 1;
//                b = hideAll;
//                break;
//            case 1: //Users
//                rl = (RelativeLayout) usersTab.getParent();
//                pos = hideAll ? 1 : 2;
//                b = hideUsers;
//                break;
//            case 2: //Groups
//                rl = (RelativeLayout) groupsTab.getParent();
//                pos = hideAll ? hideUsers ? 1 : 2;
//                b = hideGroups;
//                break;
//            case 9: //Groups
//                rl = (RelativeLayout) allGroupsTab.getParent();
//                pos = hideUsers ? 1 : 2;
//                b = hideGroups;Dia
//                break;
//            case 3: //SuperGroups
//                rl = (RelativeLayout) superGroupsTab.getParent();
//                pos = 3;
//                if(hideGroups)pos = pos - 1;
//                if(hideUsers)pos = pos - 1;
//                b = hideSGroups;
//                break;
//            case 4: //Channels
//                rl = (RelativeLayout) channelsTab.getParent();
//                pos = tabsLayout.getChildCount();
//                if(!hideBots)pos = pos - 1;
//                if(!hideFavs)pos = pos - 1;
//                b = boshraPrefe.getBoolean("hideChannels", false);
//                break;
//            case 5: //bot
//                rl = (RelativeLayout) botsTab.getParent();
//                pos = tabsLayout.getChildCount();
//                if(!hideFavs)pos = pos - 1;
//                b = hideBots;
//                break;
//            case 6: //fav
//                rl = (RelativeLayout) favsTab.getParent();
//                pos = tabsLayout.getChildCount();
//                b = hideFavs;
//                break;
//            default:
//                updateTabs();
//        }
//
//        if(rl != null) {
//            if (!b) {
//                tabsLayout.addView(rl , pos, LayoutHelper.createLinear(0, LayoutHelper.MATCH_PARENT, 1.0f));
//            } else {
//                tabsLayout.removeView(rl);
//            }
//        }
//    }
    private void unreadCount(){
        unreadCount(MessagesController.getInstance().dialogs, allCounter);
        unreadCount(MessagesController.getInstance().dialogsUsers, usersCounter);
        unreadCount(MessagesController.getInstance().dialogsBots, botsCounter);
        unreadCount(MessagesController.getInstance().dialogsChannels, channelsCounter);
        unreadCount(DialogCell.getFaveList() , favsCounter);
        unreadCountGroups();
    }
    private void unreadCountGroups(){
        SharedPreferences plusPreferences = ApplicationLoader.applicationContext.getSharedPreferences(HoldConst.PREF_MAIN, Activity.MODE_PRIVATE);
        unreadCount(MessagesController.getInstance().dialogsGroupsAll, allGroupsCounter);
        unreadCount(MessagesController.getInstance().dialogsGroupsOnly, groupsCounter);
        unreadCount(MessagesController.getInstance().dialogsMegaGroups, sGroupsCounter);

    }

    private void unreadCount(ArrayList<TLRPC.TL_dialog> dialogs, TextView tv){
        SharedPreferences plusPreferences = ApplicationLoader.applicationContext.getSharedPreferences(HoldConst.PREF_MAIN, Activity.MODE_PRIVATE);
        boolean hTabs = plusPreferences.getBoolean("hideTabs", false);
        if(hTabs)return;
        boolean hideCounters = plusPreferences.getBoolean("hideTabsCounters", false);
        if(hideCounters){
            tv.setVisibility(View.GONE);
            return;
        }
        boolean allMuted = true;
        boolean countDialogs = plusPreferences.getBoolean("tabsCountersCountChats", false);
        boolean countNotMuted = plusPreferences.getBoolean("tabsCountersCountNotMuted", false);
        int unreadCount = 0;

        if (dialogs != null && !dialogs.isEmpty()) {
            for(int a = 0; a < dialogs.size(); a++) {
                TLRPC.TL_dialog dialg = dialogs.get(a);
                boolean isMuted = MessagesController.getInstance().isDialogMuted(dialg.id);
                if(!isMuted || !countNotMuted){
                    int i = dialg.unread_count;
                    if(i > 0) {
                        if (countDialogs) {
                            if (i > 0) unreadCount = unreadCount + 1;
                        } else {
                            unreadCount = unreadCount + i;
                        }
                        if (i > 0 && !isMuted) allMuted = false;
                    }
                }
            }
        }

        if(unreadCount == 0){
            tv.setVisibility(View.GONE);
        } else{
            tv.setVisibility(View.VISIBLE);
            tv.setText("" + unreadCount);

            SharedPreferences themePrefs = ApplicationLoader.applicationContext.getSharedPreferences(AndroidUtilities.THEME_PREFS, Context.MODE_PRIVATE);
            int size = themePrefs.getInt("chatsHeaderTabCounterSize", 11);
            tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, size);
            tv.setPadding(AndroidUtilities.dp(size > 10 ? size - 7 : 4), 0, AndroidUtilities.dp(size > 10 ? size - 7 : 4), 0);
            int cColor = themePrefs.getInt("chatsHeaderTabCounterColor", 0xffffffff);
            if(allMuted){
                tv.getBackground().setColorFilter(themePrefs.getInt("chatsHeaderTabCounterSilentBGColor", 0xffb9b9b9), PorterDuff.Mode.SRC_IN);
                tv.setTextColor(cColor);
            } else{
                tv.getBackground().setColorFilter(themePrefs.getInt("chatsHeaderTabCounterBGColor", 0xffd32f2f), PorterDuff.Mode.SRC_IN);
                tv.setTextColor(cColor);
            }
        }
    }

    //HoseinKord
    private String returnFaveItemToMenu(long dialogId){
        //Added Me
        final boolean isFave = FaveHide.isFavourite((int) dialogId)  ;

        return isFave ? LocaleController.getString("DeleteFromFavorites", R.string.DeleteFromFavorites) :LocaleController.getString("AddToFavorite", R.string.AddToFavorite) ;
    }
    //HoseinKord
    private String returnHideItemToMenu(long dialogId){
        //Added Me
        final boolean isHide =FaveHide.isHidenDialog((int) dialogId) ;
        return isHide ?  LocaleController.getString("OutFromHide", R.string.OutFromHide) :LocaleController.getString("HideFromDialogs", R.string.HideFromDialogs) ;
    }

    private void faveItemClicked(long selectedDialog) {
        final boolean isFave =FaveHide.isFavourite((int) selectedDialog) ;
        if(isFave){
            FaveHide.deleteFavourite((int) selectedDialog);
//            MessagesController.getInstance().dialogsFavs.remove(dialg) ;
        }else {
            FaveHide.addFave((int)selectedDialog);
//            MessagesController.getInstance().dialogsFavs.add(dialg) ;
        }
        if (dialogsType == 8) {
            dialogsAdapter.notifyDataSetChanged();
        }
    }
    private void hideItemClicked(long selectedDialog) {
        int dLock = ApplicationLoader.databaseHandler.getLock(DatabaseHandler.PATTERN_FOR_HIDE) ;
        if(dLock == 0 ) {
            Toast.makeText(getParentActivity(),
                    LocaleController.getString("NotPatternSetYet", R.string.NotPatternSetYet), Toast.LENGTH_SHORT).show();
            presentFragment(new ActivityHideChats()) ;
            return;
        }

        final boolean isHide = FaveHide.isHidenDialog((int) selectedDialog) ;
        if(isHide){
            FaveHide.deleteHide((int) selectedDialog) ;
        }else {
            FaveHide.addHide((int)selectedDialog) ;
        }
        MessagesController.getInstance().sortDialogs(null);
        dialogsAdapter.notifyDataSetChanged() ;
        NotificationCenter.getInstance().postNotificationName(NotificationCenter.dialogsNeedReload);
    }

}


