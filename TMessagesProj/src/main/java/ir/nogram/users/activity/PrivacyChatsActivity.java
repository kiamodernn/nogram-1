/*
 * This is the source code of Teleg for Android v. 3.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2017.
 */

package ir.nogram.users.activity;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import ir.nogram.messanger.ApplicationLoader;
import ir.nogram.messanger.LocaleController;
import ir.nogram.messanger.R;
import ir.nogram.ui.ActionBar.ActionBar;
import ir.nogram.ui.ActionBar.ActionBarMenu;
import ir.nogram.ui.ActionBar.BaseFragment;
import ir.nogram.ui.ActionBar.Theme;
import ir.nogram.ui.ActionBar.ThemeDescription;
import ir.nogram.ui.Cells.TextCheckCell;
import ir.nogram.ui.Cells.TextSettingsCell;
import ir.nogram.ui.Components.LayoutHelper;
import ir.nogram.users.activity.holder.HoldConst;

public class PrivacyChatsActivity extends BaseFragment {

    TextCheckCell typingStatusTextCheckCell ;
    boolean typingBool = false ;

    TextSettingsCell txtGhost ;
    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle(LocaleController.getString("PrivacyChats", R.string.PrivacyChats));
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
//                .else if (id == done_button) {
//                    MessagesController.openByUserName(firstNameField.getText().toString(), PrivacyChatsActivity.this, 0);
////                    saveName();
//                }
            }
        });

        ActionBarMenu menu = actionBar.createMenu();
//        doneButton = menu.addItemWithWidth(done_button, R.drawable.ic_done, AndroidUtilities.dp(56));

//        TLRPC.User user = MessagesController.getInstance().getUser(UserConfig.getClientUserId());
//        if (user == null) {
//            user = UserConfig.getCurrentUser();
//        }

        fragmentView = new LinearLayout(context);
        LinearLayout linearLayout = (LinearLayout) fragmentView;
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        fragmentView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        typingBool = ApplicationLoader.sharedPreferencesMain.getBoolean(HoldConst.PREF_TYPING_STATUS , false) ;
        typingStatusTextCheckCell = new TextCheckCell(context) ;
        typingStatusTextCheckCell.setTextAndCheck(LocaleController.getString("HideTypingState" ,R.string.HideTypingState) , typingBool, true);
        linearLayout.addView(typingStatusTextCheckCell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT,LayoutHelper.WRAP_CONTENT));

        txtGhost = new TextSettingsCell(context) ;
        txtGhost.setText(LocaleController.getString("GhostControl" , R.string.GhostControl) , true);
        txtGhost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presentFragment(new GhostControlActivity(1) ) ;
            }
        });
        linearLayout.addView(txtGhost, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT,LayoutHelper.WRAP_CONTENT));

        typingStatusTextCheckCell.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeTypingStatus() ;
            }
        });
        return fragmentView;
    }

    private void changeTypingStatus() {
        typingBool = !typingBool ;
        ApplicationLoader.sharedPreferencesMain.edit().putBoolean(HoldConst.PREF_TYPING_STATUS , typingBool).apply();
        typingStatusTextCheckCell.setChecked(typingBool);
    }

    @Override
    public void onResume() {
        super.onResume();

    }



    @Override
    public void onTransitionAnimationEnd(boolean isOpen, boolean backward) {

    }

    @Override
    public ThemeDescription[] getThemeDescriptions() {
        return new ThemeDescription[]{
                new ThemeDescription(fragmentView, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_windowBackgroundWhite),

                new ThemeDescription(actionBar, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_actionBarDefault),
                new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_ITEMSCOLOR, null, null, null, null, Theme.key_actionBarDefaultIcon),
                new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_TITLECOLOR, null, null, null, null, Theme.key_actionBarDefaultTitle),
                new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_SELECTORCOLOR, null, null, null, null, Theme.key_actionBarDefaultSelector),

         };
    }
}
