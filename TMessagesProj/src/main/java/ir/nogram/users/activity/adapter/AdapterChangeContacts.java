package ir.nogram.users.activity.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ir.nogram.messanger.AndroidUtilities;
import ir.nogram.messanger.MessageObject;
import ir.nogram.messanger.MessagesController;
import ir.nogram.messanger.R;
import ir.nogram.messanger.VideoEditedInfo;
import ir.nogram.tgnet.TLRPC;
import ir.nogram.ui.Cells.DialogCell;
import ir.nogram.ui.Components.BackupImageView;
import ir.nogram.ui.PhotoViewer;
import ir.nogram.users.activity.holder.HoldChangeContact;
import ir.nogram.users.activity.holder.HoldConst;
import ir.nogram.users.activity.views.CustomTextView;

import java.util.List;

public class AdapterChangeContacts extends RecyclerView.Adapter<AdapterChangeContacts.MyViewHolder> implements PhotoViewer.PhotoViewerProvider {
    public List<HoldChangeContact> holdChangeContacts;
    Context context;

    private BackupImageView avatarImage;
int user_id = 0;
    public AdapterChangeContacts(List<HoldChangeContact> pr, Context context) {
        this.holdChangeContacts = pr;
        this.context = context;
        avatarImage = new BackupImageView(context);
        avatarImage.setRoundRadius(AndroidUtilities.dp(30));

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
    public class MyViewHolder extends RecyclerView.ViewHolder {
        public CustomTextView ctxt_type_change;
        ViewGroup layout_root;
        DialogCell dialogCell;
        public MyViewHolder(View view) {
            super(view);
            layout_root = (ViewGroup) view.findViewById(R.id.layout_root);
            ctxt_type_change = (CustomTextView) view.findViewById(R.id.ctx_change);
            dialogCell = new DialogCell(context);
            layout_root.addView(dialogCell);
        }
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_contacts_changed, parent, false);

        return new MyViewHolder(itemView);
    }

    public void add(int position, HoldChangeContact item) {
        holdChangeContacts.add(position, item);
        notifyItemInserted(position);
//		notifyDataSetChanged();
    }

    public void remove(HoldChangeContact item) {
        int position = holdChangeContacts.indexOf(item);
        holdChangeContacts.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {

        final HoldChangeContact change = holdChangeContacts.get(position);
        try {
            holder.dialogCell.setDialog(change.user_id, null, change.date);
            final TLRPC.User user = MessagesController.getInstance().getUser(change.user_id);


                String typeChange = getChange(change.type);
                holder.ctxt_type_change.setText(typeChange);


            holder.dialogCell.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        user_id = change.user_id ;
                        if (user.photo != null && user.photo.photo_big != null) {
                            PhotoViewer.getInstance().setParentActivity((Activity) context);
                            PhotoViewer.getInstance().openPhoto(user.photo.photo_big, AdapterChangeContacts.this);
                        }
                    } catch (Exception e) {
                        Log.e("log", e.getMessage() + "");
                    }

                    return;
                }
            });
        } catch (Exception e) {
            Log.e("log", e.getMessage());
        }
    }

    private String getChange(int type) {
        switch (type) {
            case HoldConst.CONTACT_CHANGE_PHOTO:
                return context.getResources().getString(R.string.change_add_photo);
            case HoldConst.CONTACT_CHANGE_ADD_CONTACTLINK:
                return context.getResources().getString(R.string.change_add_you_to_contact);
            case HoldConst.CONTACT_CHANGE_DELETE_CONTACTLINK:
                return context.getResources().getString(R.string.change_delete_you_from_contact);
            case HoldConst.CONTACT_CHANGE_PHONE:
                return context.getResources().getString(R.string.change_phone);
            case HoldConst.CONTACT_CHANGE_USERNAME:
                return context.getResources().getString(R.string.change_user_name);
        }
        return "";
    }

    @Override
    public int getItemCount() {
        return holdChangeContacts.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }
}
