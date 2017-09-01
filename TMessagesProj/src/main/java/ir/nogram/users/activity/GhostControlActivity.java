/*
 * This is the source code of Teleg for Android v. 3.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2017.
 */

package ir.nogram.users.activity;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.ArrayList;

import ir.nogram.messanger.AndroidUtilities;
import ir.nogram.messanger.ApplicationLoader;
import ir.nogram.messanger.FileLog;
import ir.nogram.messanger.LocaleController;
import ir.nogram.messanger.NotificationCenter;
import ir.nogram.messanger.R;
import ir.nogram.messanger.support.widget.LinearLayoutManager;
import ir.nogram.messanger.support.widget.RecyclerView;
import ir.nogram.ui.ActionBar.ActionBar;
import ir.nogram.ui.ActionBar.ActionBarMenu;
import ir.nogram.ui.ActionBar.AlertDialog;
import ir.nogram.ui.ActionBar.BaseFragment;
import ir.nogram.ui.ActionBar.Theme;
import ir.nogram.ui.ActionBar.ThemeDescription;
import ir.nogram.ui.Cells.HeaderCell;
import ir.nogram.ui.Cells.RadioCell;
import ir.nogram.ui.Cells.TextInfoPrivacyCell;
import ir.nogram.ui.Cells.TextSettingsCell;
import ir.nogram.ui.Components.LayoutHelper;
import ir.nogram.ui.Components.RecyclerListView;
import ir.nogram.ui.GroupCreateActivity;
import ir.nogram.users.activity.db.DatabaseMain;
import ir.nogram.users.activity.holder.Pref;

public class GhostControlActivity extends BaseFragment implements NotificationCenter.NotificationCenterDelegate {

    private ListAdapter listAdapter;
    private View doneButton;
    private RecyclerListView listView;

    public static final int GHOST_TYPE_ALLWAYS = 0 ;
    public static final int GHOST_TYPE_NEVER = 1 ;
    private int rulesType;
    private ArrayList<Integer> currentPlus;
    private ArrayList<Integer> currentMinus;
    private int lastCheckedType = -1;

    private int currentType;

    private boolean enableAnimation;

    private int sectionRow;
    private int everybodyRow;
    private int nobodyRow;
    private int detailRow;
    private int shareSectionRow;
    private int alwaysShareRow;
    private int neverShareRow;
    private int shareDetailRow;
    private int rowCount;

    private final static int done_button = 1;

    private static class LinkMovementMethodMy extends LinkMovementMethod {
        @Override
        public boolean onTouchEvent(TextView widget, Spannable buffer, MotionEvent event) {
            try {
                return super.onTouchEvent(widget, buffer, event);
            } catch (Exception e) {
                FileLog.e(e);
            }
            return false;
        }
    }

    public GhostControlActivity(int type) {
        super();
        rulesType = type;
    }

    @Override
    public boolean onFragmentCreate() {
        super.onFragmentCreate();
        checkPrivacy();
        updateRows();
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.ghostRulesUpdated);
        return true;
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.ghostRulesUpdated);
    }

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        if (rulesType == 1) {
            actionBar.setTitle(LocaleController.getString("GhostControl", R.string.GhostControl));
        }
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                } else if (id == done_button) {
                    if (getParentActivity() == null) {
                        return;
                    }
                    applyCurrentPrivacySettings();

                }
            }
        });

        ActionBarMenu menu = actionBar.createMenu();
        doneButton = menu.addItemWithWidth(done_button, R.drawable.ic_done, AndroidUtilities.dp(56));
        doneButton.setVisibility(View.GONE);

        listAdapter = new ListAdapter(context);

        fragmentView = new FrameLayout(context);
        FrameLayout frameLayout = (FrameLayout) fragmentView;
        frameLayout.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));

        listView = new RecyclerListView(context);
        listView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        listView.setVerticalScrollBarEnabled(false);
        frameLayout.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
        listView.setAdapter(listAdapter);
        listView.setOnItemClickListener(new RecyclerListView.OnItemClickListener() {
            @Override
            public void onItemClick(View view, final int position) {
                if (position == nobodyRow || position == everybodyRow) {
                    int newType = currentType;
                    if (position == nobodyRow) {
                        newType = GHOST_TYPE_NEVER;
                    } else if (position == everybodyRow) {
                        newType = GHOST_TYPE_ALLWAYS;
                    }
                    if (newType == currentType) {
                        return;
                    }
                    enableAnimation = true;
                    doneButton.setVisibility(View.VISIBLE);
                    lastCheckedType = currentType;
                    currentType = newType;
                    updateRows();
                } else if (position == neverShareRow || position == alwaysShareRow) {
                    ArrayList<Integer> createFromArray;
                    if (position == neverShareRow) {
                        createFromArray = currentMinus;
                    } else {
                        createFromArray = currentPlus;
                    }
                    if (createFromArray.isEmpty()) {
                        Bundle args = new Bundle();
                        args.putBoolean(position == neverShareRow ? "isNeverShare" : "isAlwaysShare", true);
                        args.putBoolean("isGroup", rulesType != 0);
                        GroupCreateActivity fragment = new GroupCreateActivity(args);
                        fragment.setDelegate(new GroupCreateActivity.GroupCreateActivityDelegate() {
                            @Override
                            public void didSelectUsers(ArrayList<Integer> ids) {
                                if (position == neverShareRow) {
                                    currentMinus = ids;
//                                    for (int a = 0; a < currentMinus.size(); a++) {
//                                        currentPlus.remove(currentMinus.get(a));
//                                    }
                                } else {
                                    currentPlus = ids;
//                                    for (int a = 0; a < currentPlus.size(); a++) {
//                                        currentMinus.remove(currentPlus.get(a));
//                                    }
                                }
                                doneButton.setVisibility(View.VISIBLE);
                                lastCheckedType = -1;
                                listAdapter.notifyDataSetChanged();
                            }
                        });
                        presentFragment(fragment);
                    } else {
                        for (int ids : createFromArray){
                            Log.i("ids" , ids +"") ;
                        }
                    GhostUsersActivity fragment = new GhostUsersActivity(createFromArray, false , position == alwaysShareRow , currentType);
                    fragment.setDelegate(new GhostUsersActivity.GhostUsersActivityDelegate() {
                        @Override
                        public void didUpdatedUserList(ArrayList<Integer> ids, boolean added) {
                            if (position == neverShareRow) {
                                currentMinus = ids;
//                                if (added) {
//                                    for (int a = 0; a < currentMinus.size(); a++) {
//                                        currentPlus.remove(currentMinus.get(a));
//                                    }
//                                }
                            } else {
                                currentPlus = ids;
//                                if (added) {
//                                    for (int a = 0; a < currentPlus.size(); a++) {
//                                        currentMinus.remove(currentPlus.get(a));
//                                    }
//                                }
                            }
                            doneButton.setVisibility(View.VISIBLE);
                            listAdapter.notifyDataSetChanged();
                        }
                    });
                    presentFragment(fragment);
                    }
                }

            }
        });

        return fragmentView;
    }

    @Override
    public void didReceivedNotification(int id, Object... args) {
        if (id == NotificationCenter.ghostRulesUpdated) {
            checkPrivacy();
        }
    }

    private void applyCurrentPrivacySettings() {
        AndroidUtilities.runOnUIThread(new Runnable() {
            @Override
            public void run() {
                NotificationCenter.getInstance().postNotificationName(NotificationCenter.ghostUpdated);

            }
        });
        new AddToDbUserIdsGhost().execute() ;
    }


    private void showErrorAlert() {
        if (getParentActivity() == null) {
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
        builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
        builder.setMessage(LocaleController.getString("PrivacyFloodControlError", R.string.PrivacyFloodControlError));
        builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), null);
        showDialog(builder.create());
    }

    private void checkPrivacy() {
        currentPlus = new ArrayList<>();
        currentMinus = new ArrayList<>();
        currentType =  ApplicationLoader.sharedPreferencesMain.getInt(Pref.GHOST_TYPE , GHOST_TYPE_NEVER) ;
        DatabaseMain databaseMain = DatabaseMain.getInstance(ApplicationLoader.applicationContext) ;
        currentMinus.addAll(databaseMain.getListGhostsByType(GHOST_TYPE_ALLWAYS));
        currentPlus.addAll(databaseMain.getListGhostsByType(GHOST_TYPE_NEVER));
        if (doneButton != null) {
            doneButton.setVisibility(View.GONE);
        }
        updateRows();
    }

    private void updateRows() {
        rowCount = 0;
        sectionRow = rowCount++;
        everybodyRow = rowCount++;
        nobodyRow = rowCount++;
//        if (rulesType != 0 && rulesType != 2) {
//            nobodyRow = -1;
//        } else {
//            nobodyRow = rowCount++;
//        }
        detailRow = rowCount++;
        shareSectionRow = rowCount++;
        if (currentType == GHOST_TYPE_NEVER) {
            alwaysShareRow = rowCount++;
        } else {
            alwaysShareRow = -1;
        }
        if (currentType == GHOST_TYPE_ALLWAYS) {
            neverShareRow = rowCount++;
        } else {
            neverShareRow = -1;
        }
        shareDetailRow = rowCount++;
        if (listAdapter != null) {
            listAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        lastCheckedType = -1;
        enableAnimation = false;
    }

    private class ListAdapter extends RecyclerListView.SelectionAdapter {
        private Context mContext;

        public ListAdapter(Context context) {
            mContext = context;
        }

        @Override
        public boolean isEnabled(RecyclerView.ViewHolder holder) {
            int position = holder.getAdapterPosition();
            return position == nobodyRow || position == everybodyRow || position == neverShareRow || position == alwaysShareRow;
        }

        @Override
        public int getItemCount() {
            return rowCount;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view;
            switch (viewType) {
                case 0:
                    view = new TextSettingsCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case 1:
                    view = new TextInfoPrivacyCell(mContext);
                    break;
                case 2:
                    view = new HeaderCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case 3:
                default:
                    view = new RadioCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
            }
            return new RecyclerListView.Holder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            switch (holder.getItemViewType()) {
                case 0:
                    TextSettingsCell textCell = (TextSettingsCell) holder.itemView;
                    if (position == alwaysShareRow) {
                        String value;
                        if (currentPlus.size() != 0) {
                            value = LocaleController.formatPluralString("Users", currentPlus.size());
                        } else {
                            value = LocaleController.getString("EmpryUsersPlaceholder", R.string.EmpryUsersPlaceholder);
                        }
                        if (rulesType != 0) {
                            textCell.setTextAndValue(LocaleController.getString("EnableFor", R.string.EnableFor), value, neverShareRow != -1);
                        } else {
                            textCell.setTextAndValue(LocaleController.getString("AlwaysShareWith", R.string.AlwaysShareWith), value, neverShareRow != -1);
                        }
                    } else if (position == neverShareRow) {
                        String value;
                        if (currentMinus.size() != 0) {
                            value = LocaleController.formatPluralString("Users", currentMinus.size());
                        } else {
                            value = LocaleController.getString("EmpryUsersPlaceholder", R.string.EmpryUsersPlaceholder);
                        }
                        if (rulesType != 0) {
                            textCell.setTextAndValue(LocaleController.getString("DisableFor", R.string.DisableFor), value, false);
                        } else {
                            textCell.setTextAndValue(LocaleController.getString("NeverShareWith", R.string.NeverShareWith), value, false);
                        }
                    }
                    break;
                case 3:
                    RadioCell radioCell = (RadioCell) holder.itemView;
                    int checkedType = 0;
                    if (position == everybodyRow) {
                        radioCell.setText(LocaleController.getString("EnableGhostForAll", R.string.EnableGhostForAll), lastCheckedType == 0, true);
                        checkedType = 0;
                    } else if (position == nobodyRow) {
                        radioCell.setText(LocaleController.getString("DisableGhostForAll", R.string.DisableGhostForAll), lastCheckedType == 1, false);
                        checkedType = 1;
                    }
                    if (lastCheckedType == checkedType) {
                        radioCell.setChecked(false, enableAnimation);
                    } else if (currentType == checkedType) {
                        radioCell.setChecked(true, enableAnimation);
                    }
                    break;
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (position == alwaysShareRow || position == neverShareRow) {
                return 0;
            } else if (position == shareDetailRow || position == detailRow) {
                return 1;
            } else if (position == sectionRow || position == shareSectionRow) {
                return 2;
            } else if (position == everybodyRow || position == nobodyRow) {
                return 3;
            }
            return 0;
        }
    }

    @Override
    public ThemeDescription[] getThemeDescriptions() {
        return new ThemeDescription[]{
                new ThemeDescription(listView, ThemeDescription.FLAG_CELLBACKGROUNDCOLOR, new Class[]{TextSettingsCell.class, HeaderCell.class, RadioCell.class}, null, null, null, Theme.key_windowBackgroundWhite),
                new ThemeDescription(fragmentView, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_windowBackgroundGray),

                new ThemeDescription(actionBar, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_actionBarDefault),
                new ThemeDescription(listView, ThemeDescription.FLAG_LISTGLOWCOLOR, null, null, null, null, Theme.key_actionBarDefault),
                new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_ITEMSCOLOR, null, null, null, null, Theme.key_actionBarDefaultIcon),
                new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_TITLECOLOR, null, null, null, null, Theme.key_actionBarDefaultTitle),
                new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_SELECTORCOLOR, null, null, null, null, Theme.key_actionBarDefaultSelector),

                new ThemeDescription(listView, ThemeDescription.FLAG_SELECTOR, null, null, null, null, Theme.key_listSelector),

                new ThemeDescription(listView, 0, new Class[]{View.class}, Theme.dividerPaint, null, null, Theme.key_divider),

                new ThemeDescription(listView, ThemeDescription.FLAG_BACKGROUNDFILTER, new Class[]{TextInfoPrivacyCell.class}, null, null, null, Theme.key_windowBackgroundGrayShadow),

                new ThemeDescription(listView, 0, new Class[]{TextSettingsCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlackText),
                new ThemeDescription(listView, 0, new Class[]{TextSettingsCell.class}, new String[]{"valueTextView"}, null, null, null, Theme.key_windowBackgroundWhiteValueText),

                new ThemeDescription(listView, 0, new Class[]{TextInfoPrivacyCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayText4),

                new ThemeDescription(listView, 0, new Class[]{HeaderCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlueHeader),

                new ThemeDescription(listView, 0, new Class[]{RadioCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlackText),
                new ThemeDescription(listView, ThemeDescription.FLAG_CHECKBOX, new Class[]{RadioCell.class}, new String[]{"radioButton"}, null, null, null, Theme.key_radioBackground),
                new ThemeDescription(listView, ThemeDescription.FLAG_CHECKBOXCHECK, new Class[]{RadioCell.class}, new String[]{"radioButton"}, null, null, null, Theme.key_radioBackgroundChecked),
        };
    }
    private class AddToDbUserIdsGhost extends AsyncTask<Void , Void , Void>{
        AlertDialog progressDialog = null;
        @Override
        protected void onPreExecute() {
            if (getParentActivity() != null) {
                progressDialog = new AlertDialog(getParentActivity(), 1);
                progressDialog.setMessage(LocaleController.getString("Loading", R.string.Loading));
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.setCancelable(false);
                progressDialog.show();
            }
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            ApplicationLoader.sharedPreferencesMain.edit().putInt(Pref.GHOST_TYPE , currentType).apply(); ;
            DatabaseMain.getInstance(ApplicationLoader.applicationContext).deletGhostsByType(currentType) ;
            if(currentType == GHOST_TYPE_ALLWAYS){
                for (int i = 0 ; i < currentMinus.size() ; i++){
                    DatabaseMain.getInstance(ApplicationLoader.applicationContext).insertGhost(currentMinus.get(i) , currentType) ;
                }
            }else{
                for (int i = 0 ; i < currentPlus.size() ; i++){
                    DatabaseMain.getInstance(ApplicationLoader.applicationContext).insertGhost(currentPlus.get(i) , currentType) ;
                }
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void aVoid) {
            if (getParentActivity() != null && progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            finishFragment();
            super.onPostExecute(aVoid);
        }
    }
}
