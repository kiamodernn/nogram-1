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
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import ir.nogram.messanger.AndroidUtilities;
import ir.nogram.messanger.ApplicationLoader;
import ir.nogram.messanger.MessagesController;
import ir.nogram.messanger.UserConfig;
import ir.nogram.messanger.support.widget.RecyclerView;
import ir.nogram.tgnet.ConnectionsManager;
import ir.nogram.tgnet.TLRPC;
import ir.nogram.ui.Cells.DialogCell;
import ir.nogram.ui.Cells.LoadingCell;
import ir.nogram.ui.Components.RecyclerListView;
import ir.nogram.users.activity.holder.HoldConst;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class DialogsAdapter extends RecyclerListView.SelectionAdapter {

    private Context mContext;
    private int dialogsType = 0 ;
    private long openedDialogId;
    private int currentCount;

    public DialogsAdapter(Context context, int type) {
        mContext = context;
        dialogsType = type;
    }

    public void setOpenedDialogId(long id) {
        openedDialogId = id;
    }

    public boolean isDataSetChanged() {
        int current = currentCount;
        return current != getItemCount() || current == 1;
    }

    private ArrayList<TLRPC.TL_dialog> getDialogsArray() {
        SharedPreferences plusPreferences = ApplicationLoader.applicationContext.getSharedPreferences(HoldConst.PREF_MAIN, Context.MODE_PRIVATE);
        if (dialogsType == 0) {
            boolean hideTabs = plusPreferences.getBoolean(HoldConst.PREF_HIDE_TAB, false);
            int sort = plusPreferences.getInt(HoldConst.Pref_sortAll, 0);
            if(sort == 0 || hideTabs){
                sortDefault(MessagesController.getInstance().dialogs);
            }else{
                sortUnread(MessagesController.getInstance().dialogs);
            }
            return MessagesController.getInstance().dialogs;

        } else if (dialogsType == 1) {
            boolean hideTabs = plusPreferences.getBoolean(HoldConst.PREF_HIDE_TAB, false);
            int sort = plusPreferences.getInt(HoldConst.Pref_sortAll, 0);
            if(sort == 0 || hideTabs){
                sortDefault(MessagesController.getInstance().dialogsServerOnly);
            }else{
                sortUnread(MessagesController.getInstance().dialogsServerOnly);
            }
            return MessagesController.getInstance().dialogsServerOnly;
        } else if (dialogsType == 2) {
            int sort = plusPreferences.getInt(HoldConst.Pref_sortGroups, 0);
            if(sort == 0){
                sortDefault(MessagesController.getInstance().dialogsGroupsOnly);
            }else{
                sortUnread(MessagesController.getInstance().dialogsGroupsOnly);
            }
            return MessagesController.getInstance().dialogsGroupsOnly;

        }
        //plus
        else if (dialogsType == 3) {
            int sort = plusPreferences.getInt(HoldConst.Pref_sortUsers, 0);
            if(sort == 0){
                sortUsersDefault();
            }else if(sort == 1){
                sortUsersByStatus();
            }else{
                sortUnread(MessagesController.getInstance().dialogsUsers);
            }
            return MessagesController.getInstance().dialogsUsers;
        } else if (dialogsType == 4) {
            int sort = plusPreferences.getInt(HoldConst.Pref_sortGroups, 0);
            if(sort == 0){
                sortDefault(MessagesController.getInstance().dialogsGroupsOnly);
            }else{
                sortUnread(MessagesController.getInstance().dialogsGroupsOnly);
            }
            return MessagesController.getInstance().dialogsGroupsOnly;
        } else if (dialogsType == 5) {
            int sort = plusPreferences.getInt(HoldConst.Pref_sortChannels, 0);
            if(sort == 0){
                sortDefault(MessagesController.getInstance().dialogsChannels);
            }else{
                sortUnread(MessagesController.getInstance().dialogsChannels);
            }
            return MessagesController.getInstance().dialogsChannels;
        } else if (dialogsType == 6) {
            int sort = plusPreferences.getInt(HoldConst.Pref_sortBots, 0);
            if(sort == 0){
                sortDefault(MessagesController.getInstance().dialogsBots);
            }else{
                sortUnread(MessagesController.getInstance().dialogsBots);
            }
            return MessagesController.getInstance().dialogsBots;
        } else if (dialogsType == 7) {
            int sort = plusPreferences.getInt(HoldConst.Pref_sortSGroups, 0);
            if(sort == 0){
                sortDefault(MessagesController.getInstance().dialogsMegaGroups);
            }else{
                sortUnread(MessagesController.getInstance().dialogsMegaGroups);
            }
            return MessagesController.getInstance().dialogsMegaGroups;
        } else if (dialogsType == 8) {
            return  DialogCell.getFaveList() ;
//            return DialogCell.getFaveList();
        } else if (dialogsType == 9) {
            int sort = plusPreferences.getInt(HoldConst.Pref_sortGroups, 0);
            if(sort == 0){
                sortDefault(MessagesController.getInstance().dialogsGroupsAll);
            }else{
                sortUnread(MessagesController.getInstance().dialogsGroupsAll);
            }
            return MessagesController.getInstance().dialogsGroupsAll;
        }
        return null;
    }


    @Override
    public int getItemCount() {
        int count = getDialogsArray().size();
        if (count == 0 && MessagesController.getInstance().loadingDialogs) {
            return 0;
        }
        if (!MessagesController.getInstance().dialogsEndReached) {
            count++;
        }
        currentCount = count;
        return count;
    }

    public TLRPC.TL_dialog getItem(int i) {
        ArrayList<TLRPC.TL_dialog> arrayList = getDialogsArray();
        if (i < 0 || i >= arrayList.size()) {
            return null;
        }
        return arrayList.get(i);
    }

    @Override
    public void onViewAttachedToWindow(RecyclerView.ViewHolder holder) {
        if (holder.itemView instanceof DialogCell) {
            ((DialogCell) holder.itemView).checkCurrentDialogIndex();
        }
    }

    @Override
    public boolean isEnabled(RecyclerView.ViewHolder holder) {
        return holder.getItemViewType() != 1;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = null;
        if (viewType == 0) {
            view = new DialogCell(mContext);
        } else if (viewType == 1) {
            view = new LoadingCell(mContext);
        }
        view.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
        return new RecyclerListView.Holder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int i) {
        if (viewHolder.getItemViewType() == 0) {
            DialogCell cell = (DialogCell) viewHolder.itemView;
            cell.useSeparator = (i != getItemCount() - 1);
            TLRPC.TL_dialog dialog = getItem(i);

            Log.i("LOGGGDialog" , "id " +dialog.id   ) ;
            if(dialog.id == HoldConst.HIDDEN_CHANNEL_LONG_ID){
                cell.hideAntiTab = true ;
            }else{
                cell.hideAntiTab = false ;
            }
            if (dialogsType == 0) {
                if (AndroidUtilities.isTablet()) {
                    cell.setDialogSelected(dialog.id == openedDialogId);
                }
            }
            cell.setDialog(dialog, i, dialogsType);
        }
    }

    @Override
    public int getItemViewType(int i) {
        if (i == getDialogsArray().size()) {
            return 1;
        }
        return 0;
    }

    private void sortDefault(ArrayList<TLRPC.TL_dialog> dialogs){
        Collections.sort(dialogs, new Comparator<TLRPC.TL_dialog>() {
            @Override
            public int compare(TLRPC.TL_dialog dialog1, TLRPC.TL_dialog dialog2) {

                if (!dialog1.pinned && dialog2.pinned) {
                    return 1;
                } else if (dialog1.pinned && !dialog2.pinned) {
                    return -1;
                } else if (dialog1.pinned && dialog2.pinned) {
                    if (dialog1.pinnedNum < dialog2.pinnedNum) {
                        return 1;
                    } else if (dialog1.pinnedNum > dialog2.pinnedNum) {
                        return -1;
                    } else {
                        return 0;
                    }
                }
                if (dialog1.last_message_date == dialog2.last_message_date) {
                    return 0;
                } else if (dialog1.last_message_date < dialog2.last_message_date) {
                    return 1;
                } else {
                    return -1;
                }
            }
        });
    }
    private void sortUnread(ArrayList<TLRPC.TL_dialog> dialogs){
        Collections.sort(dialogs, new Comparator<TLRPC.TL_dialog>() {
            @Override
            public int compare(TLRPC.TL_dialog dialog1, TLRPC.TL_dialog dialog2) {
                if (!dialog1.pinned && dialog2.pinned) {
                    return 1;
                } else if (dialog1.pinned && !dialog2.pinned) {
                    return -1;
                } else if (dialog1.pinned && dialog2.pinned) {
                    if (dialog1.pinnedNum < dialog2.pinnedNum) {
                        return 1;
                    } else if (dialog1.pinnedNum > dialog2.pinnedNum) {
                        return -1;
                    } else {
                        return 0;
                    }
                }
                if (dialog1.unread_count == dialog2.unread_count) {
                    return 0;
                } else if (dialog1.unread_count < dialog2.unread_count) {
                    return 1;
                } else {
                    return -1;
                }
            }
        });
    }

    private void sortUsersDefault(){
        Collections.sort(MessagesController.getInstance().dialogsUsers, new Comparator<TLRPC.TL_dialog>() {
            @Override
            public int compare(TLRPC.TL_dialog dialog1, TLRPC.TL_dialog dialog2) {
//

                if (!dialog1.pinned && dialog2.pinned) {
                    return 1;
                } else if (dialog1.pinned && !dialog2.pinned) {
                    return -1;
                } else if (dialog1.pinned && dialog2.pinned) {
                    if (dialog1.pinnedNum < dialog2.pinnedNum) {
                        return 1;
                    } else if (dialog1.pinnedNum > dialog2.pinnedNum) {
                        return -1;
                    } else {
                        return 0;
                    }
                }
                if (dialog1.last_message_date == dialog2.last_message_date) {
                    return 0;
                } else if (dialog1.last_message_date < dialog2.last_message_date) {
                    return 1;
                } else {
                    return -1;
                }
            }
        });
    }
    private void sortUsersByStatus(){
        Collections.sort(MessagesController.getInstance().dialogsUsers, new Comparator<TLRPC.TL_dialog>() {
            @Override
            public int compare(TLRPC.TL_dialog tl_dialog, TLRPC.TL_dialog tl_dialog2) {

                TLRPC.User user1 = MessagesController.getInstance().getUser((int) tl_dialog2.id);
                TLRPC.User user2 = MessagesController.getInstance().getUser((int) tl_dialog.id);
                int status1 = 0 ;
                int status2 = 0 ;
                if (user1 != null && user1.status != null) {
                    if (user1.id == UserConfig.getClientUserId()) {
                        status1 = ConnectionsManager.getInstance().getCurrentTime() + 50000;
                    } else {
                        status1 = user1.status.expires;
                    }
                }
                if (user2 != null && user2.status != null) {
                    if (user2.id == UserConfig.getClientUserId()) {
                        status2 = ConnectionsManager.getInstance().getCurrentTime() + 50000;
                    } else {
                        status2 = user2.status.expires;
                    }
                }
                if (status1 > 0 && status2 > 0) {
                    if (status1 > status2) {
                        return 1;
                    } else if (status1 < status2) {
                        return -1;
                    }
                    return 0;
                } else if (status1 < 0 && status2 < 0) {
                    if (status1 > status2) {
                        return 1;
                    } else if (status1 < status2) {
                        return -1;
                    }
                    return 0;
                } else if (status1 < 0 && status2 > 0 || status1 == 0 && status2 != 0) {
                    return -1;
                } else if (status2 < 0 && status1 > 0 || status2 == 0 && status1 != 0) {
                    return 1;
                }
                return 0;
            }
        });
    }

}
