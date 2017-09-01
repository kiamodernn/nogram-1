/*
 * This is the source code of Telegram for Android v. 3.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2016.
 */

package ir.nogram.users.activity.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import ir.nogram.SQLite.FaveHide;
import ir.nogram.messanger.AndroidUtilities;
import ir.nogram.messanger.MessagesController;
import ir.nogram.messanger.support.widget.RecyclerView;
import ir.nogram.tgnet.TLRPC;
import ir.nogram.ui.Cells.LoadingCell;
import ir.nogram.users.activity.cell.DialogCellLock;

public class DialogsAdapterHide extends RecyclerView.Adapter {

    private Context mContext;
    private int dialogsType;
    private long openedDialogId;
    private int currentCount;
    private class Holder extends RecyclerView.ViewHolder {
        public Holder(View itemView) {
            super(itemView);
        }
    }

    public DialogsAdapterHide(Context context, int type) {
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
            return MessagesController.getInstance().dialogsBackUp ;
    }
    @Override
    public int getItemCount() {
        int count = getDialogsArray().size();
        if (count == 0 && MessagesController.getInstance().loadingDialogs) {
            return 0 ;
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
        if (holder.itemView instanceof DialogCellLock) {
            ((DialogCellLock) holder.itemView).checkCurrentDialogIndex();
        }
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
         View view = null;
        if (viewType == 0) {
            view = new DialogCellLock(mContext);
        } else if (viewType == 1) {
            view = new LoadingCell(mContext);
        }
        view.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int i) {
        if (viewHolder.getItemViewType() == 0) {
            DialogCellLock cell = (DialogCellLock) viewHolder.itemView;
            cell.useSeparator = (i != getItemCount() - 1);
            TLRPC.TL_dialog dialog = getItem(i);
            if (dialogsType == 0) {
                if (AndroidUtilities.isTablet()) {
                    cell.setDialogSelected(dialog.id == openedDialogId);
                }
            }
            boolean isLoced = false ;
            if(FaveHide.isHidenDialog((int) dialog.id)) {
                isLoced = true ;
            }else {
                isLoced = false ;
            }
            cell.setDialog(dialog, i, dialogsType , isLoced);
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
