/*
 * This is the source code of Telegram for Android v. 3.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2017.
 */

package ir.nogram.ui.Adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.view.ViewGroup;

import ir.nogram.messanger.AndroidUtilities;
import ir.nogram.messanger.ApplicationLoader;
import ir.nogram.messanger.LocaleController;
import ir.nogram.messanger.MessagesController;
import ir.nogram.messanger.R;
import ir.nogram.messanger.UserConfig;
import ir.nogram.messanger.support.widget.RecyclerView;
import ir.nogram.ui.ActionBar.Theme;
import ir.nogram.ui.Cells.DrawerActionCell;
import ir.nogram.ui.Cells.DividerCell;
import ir.nogram.ui.Cells.EmptyCell;
import ir.nogram.ui.Cells.DrawerProfileCell;
import ir.nogram.ui.Components.RecyclerListView;
import ir.nogram.users.activity.holder.HoldConst;

import java.util.ArrayList;

public class DrawerLayoutAdapter extends RecyclerListView.SelectionAdapter {

    private Context mContext;
    private ArrayList<Item> items = new ArrayList<>(17);

    //    public static final int MENU_POS_Family_setting = 15;
    public static final int MENU_POS_NewGroup = 2;
    public static final int MENU_POS_NewSecretChat = 3;
    public static final int MENU_POS_NewChannel = 4;
    public static final int MENU_POS_Contacts = 6;
    public static final int MENU_POS_Calls= 7;
    public static final int MENU_POS_Settings = 8;
    public static final int MENU_POS_COLOR_THEME = 9;
    public static final int MENU_POS_FONTS= 10;
    public static final int MENU_POS_ContactsChange = 11;
    public static final int MENU_POS_InviteFriends = 12;
    public static final int MENU_POS_TelegramFaq = 13;
    public static final int MENU_POS_PRIVACY_CHATS = 14;
    public static final int MENU_POS_ONLINE_CONTACTS= 15;
    public static final int MENU_POS_LOCK_CHATS = 16;
    public static final int MENU_POS_HIDE_CHATS = 17;
    public static final int MENU_POS_TAB_SETTING = 18;
    public static final int MENU_POS_DIALOG_CAT= 19;
//    public static final int MENU_POS_FAMILYSETTINGS = 15;

    public DrawerLayoutAdapter(Context context) {
        mContext = context;
        Theme.createDialogsResources(context);
        resetItems();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public void notifyDataSetChanged() {
        resetItems();
        super.notifyDataSetChanged();
    }

    @Override
    public boolean isEnabled(RecyclerView.ViewHolder holder) {
        return holder.getItemViewType() == 3;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        switch (viewType) {
            case 0:
                view = new DrawerProfileCell(mContext);
                break;
            case 1:
            default:
                view = new EmptyCell(mContext, AndroidUtilities.dp(8));
                break;
            case 2:
                view = new DividerCell(mContext);
                break;
            case 3:
                view = new DrawerActionCell(mContext);
                break;
        }
        view.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return new RecyclerListView.Holder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case 0:
                ((DrawerProfileCell) holder.itemView).setUser(MessagesController.getInstance().getUser(UserConfig.getClientUserId()));
                holder.itemView.setBackgroundColor(Theme.getColor(Theme.key_avatar_backgroundActionBarBlue));
                break;
            case 3:
                items.get(position).bind((DrawerActionCell) holder.itemView);
                break;
        }
    }

    @Override
    public int getItemViewType(int i) {
        if (i == 0) {
            return 0;
        } else if (i == 1) {
            return 1;
        } else if (i == 5) {
            return 2;
        }
        return 3;
    }

    private void resetItems() {
        items.clear();
        if (!UserConfig.isClientActivated()) {
            return;
        }
        items.add(null); // profile
        items.add(null); // padding
        items.add(new Item(MENU_POS_NewGroup, LocaleController.getString("NewGroup", R.string.NewGroup), R.drawable.menu_newgroup));
        items.add(new Item(MENU_POS_NewSecretChat, LocaleController.getString("NewSecretChat", R.string.NewSecretChat), R.drawable.menu_secret));
        items.add(new Item(MENU_POS_NewChannel , LocaleController.getString("NewChannel", R.string.NewChannel), R.drawable.menu_broadcast));
        items.add(null); // divider
        items.add(new Item(MENU_POS_Contacts, LocaleController.getString("Contacts", R.string.Contacts), R.drawable.menu_contacts));
        if (MessagesController.getInstance().callsEnabled) {
            items.add(new Item(MENU_POS_Calls, LocaleController.getString("Calls", R.string.Calls), R.drawable.menu_calls));
        }
        items.add(new Item(MENU_POS_Settings, LocaleController.getString("Settings", R.string.Settings), R.drawable.menu_settings));
        items.add(new Item(MENU_POS_COLOR_THEME, LocaleController.getString("changeColor", R.string.changeColor), R.drawable.menu_color));
        items.add(new Item(MENU_POS_FONTS, LocaleController.getString("changeFont", R.string.changeFont), R.drawable.menu_font));
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences(HoldConst.PREF_MAIN, Context.MODE_PRIVATE);
        int last_id_seen = preferences.getInt(HoldConst.PREF_LAST_ID , 0) ;
        int count = ApplicationLoader.databaseHandler.countChangeUnReaded(last_id_seen) ;
        items.add(new Item(MENU_POS_ContactsChange, LocaleController.getString("ContactsChange", R.string.ContactsChange) +" " +count, R.drawable.menu_contacts));
        items.add(new Item(MENU_POS_InviteFriends, LocaleController.getString("InviteFriends", R.string.InviteFriends), R.drawable.menu_invite));
        items.add(new Item(MENU_POS_TelegramFaq, LocaleController.getString("TelegramFaq", R.string.TelegramFaq), R.drawable.menu_help));
        items.add(new Item(MENU_POS_PRIVACY_CHATS, LocaleController.getString("PrivacyChats", R.string.PrivacyChats), R.drawable.ghost_enabled));
        items.add(new Item(MENU_POS_ONLINE_CONTACTS, LocaleController.getString("OnlineContacts", R.string.OnlineContacts), R.drawable.menu_contacts));
        items.add(new Item(MENU_POS_LOCK_CHATS, LocaleController.getString("lock_count", R.string.lock_count), R.drawable.lock_close));
        items.add(new Item(MENU_POS_HIDE_CHATS, LocaleController.getString("hidden_count", R.string.hidden_count), R.drawable.menu_broadcast));
        items.add(new Item(MENU_POS_TAB_SETTING, LocaleController.getString("TabSetting", R.string.TabSetting), R.drawable.tab_all));
        items.add(new Item(MENU_POS_DIALOG_CAT, LocaleController.getString("ManageCategory", R.string.ManageCategory), R.drawable.ic_category));

    }

    public int getId(int position) {
        if (position < 0 || position >= items.size()) {
            return -1;
        }
        Item item = items.get(position);
        return item != null ? item.id : -1;
    }

    private class Item {
        public int icon;
        public String text;
        public int id;

        public Item(int id, String text, int icon) {
            this.icon = icon;
            this.id = id;
            this.text = text;
        }

        public void bind(DrawerActionCell actionCell) {
            actionCell.setTextAndIcon(text, icon);
        }
    }
}
