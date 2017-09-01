/*
 * This is the source code of Teleg for Android v. 3.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2017.
 */

package ir.nogram.users.activity.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.HashMap;

import ir.nogram.messanger.AndroidUtilities;
import ir.nogram.messanger.MessagesController;
import ir.nogram.messanger.support.widget.RecyclerView;
import ir.nogram.tgnet.TLRPC;
import ir.nogram.ui.Cells.LoadingCell;
import ir.nogram.ui.Components.RecyclerListView;
import ir.nogram.users.activity.cell.DialogCellAds;

public class DialogsAdapterAds extends RecyclerListView.SelectionAdapter {
    private Context mContext;
    private int dialogsType;
    private long openedDialogId;
    private int currentCount;
    HashMap<Integer , Integer> hashMapSelectedDialogs = new HashMap<>() ;
    public DialogsAdapterAds(Context context, int type , HashMap<Integer , Integer> hasjMapSelectedDialogs) {
        mContext = context;
        dialogsType = type;
      this.hashMapSelectedDialogs = hasjMapSelectedDialogs ;
    }

    public void setOpenedDialogId(long id) {
        openedDialogId = id;
    }


    private ArrayList<TLRPC.TL_dialog> getDialogsArray() {

            return MessagesController.getInstance().dialogsChannels;

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
        if (holder.itemView instanceof DialogCellAds) {
            ((DialogCellAds) holder.itemView).checkCurrentDialogIndex();
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
            view = new DialogCellAds(mContext);
        } else if (viewType == 1) {
            view = new LoadingCell(mContext);
        }
        view.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
        return new RecyclerListView.Holder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int i) {
        if (viewHolder.getItemViewType() == 0) {
            DialogCellAds cell = (DialogCellAds) viewHolder.itemView;
            cell.useSeparator = (i != getItemCount() - 1);
            TLRPC.TL_dialog dialog = getItem(i);
            if (dialogsType == 0) {
                if (AndroidUtilities.isTablet()) {
                    cell.setDialogSelected(dialog.id == openedDialogId);
                }
            }

            long dialog_id =    dialog.id;
            int chat_id = 0 ;
            int user_id = 0 ;
            int lower_part = (int) dialog_id;
            int high_id = (int) (dialog_id >> 32);

            if (lower_part != 0) {
                if (high_id == 1) {
                    chat_id = lower_part;
                } else {
                    if (lower_part > 0) {
                        user_id = lower_part;
                    } else if (lower_part < 0) {
                        chat_id = -lower_part;
                    }
                }
            }
            boolean isSelected = hashMapSelectedDialogs.get(chat_id) != null ;
            cell.setDialog(dialog, i, dialogsType , isSelected);

        }
    }
    @Override
    public int getItemViewType(int i) {
        if (i == getDialogsArray().size()) {
            return 1;
        }
        return 0;
    }
}
