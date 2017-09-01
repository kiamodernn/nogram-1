/*
 * This is the source code of Telegram for Android v. 3.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2017.
 */

package ir.nogram.ui.Cells;

import android.content.Context;
import android.view.Gravity;
import android.widget.FrameLayout;

import ir.nogram.messanger.AndroidUtilities;
import ir.nogram.messanger.LocaleController;
import ir.nogram.messanger.MessagesController;
import ir.nogram.messanger.R;
import ir.nogram.messanger.UserConfig;
import ir.nogram.messanger.UserObject;
import ir.nogram.tgnet.ConnectionsManager;
import ir.nogram.tgnet.TLRPC;
import ir.nogram.ui.ActionBar.SimpleTextView;
import ir.nogram.ui.ActionBar.Theme;
import ir.nogram.ui.Components.AvatarDrawable;
import ir.nogram.ui.Components.BackupImageView;
import ir.nogram.ui.Components.GroupCreateCheckBox;
import ir.nogram.ui.Components.LayoutHelper;
import ir.nogram.ui.ThemeManager;

public class GroupCreateUserCell extends FrameLayout {

    private BackupImageView avatarImageView;
    private SimpleTextView nameTextView;
    private SimpleTextView statusTextView;
    private GroupCreateCheckBox checkBox;
    private AvatarDrawable avatarDrawable;
    private TLRPC.User currentUser;
    private CharSequence currentName;
    private CharSequence currentStatus;

    private String lastName;
    private int lastStatus;
    private TLRPC.FileLocation lastAvatar;

    public GroupCreateUserCell(Context context, boolean needCheck) {
        super(context);
        avatarDrawable = new AvatarDrawable();

        avatarImageView = new BackupImageView(context);
        avatarImageView.setRoundRadius(AndroidUtilities.dp(24));
        addView(avatarImageView, LayoutHelper.createFrame(50, 50, (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.TOP, LocaleController.isRTL ? 0 : 11, 11, LocaleController.isRTL ? 11 : 0, 0));

        nameTextView = new SimpleTextView(context);
        nameTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        nameTextView.setTypeface(AndroidUtilities.getTypeface(ThemeManager.getFont()));
        nameTextView.setTextSize(17);
        nameTextView.setGravity((LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.TOP);
        addView(nameTextView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 20, (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.TOP, LocaleController.isRTL ? 28 : 72, 14, LocaleController.isRTL ? 72 : 28, 0));

        statusTextView = new SimpleTextView(context);
        statusTextView.setTextSize(16);
        statusTextView.setGravity((LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.TOP);
        addView(statusTextView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 20, (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.TOP, LocaleController.isRTL ? 28 : 72, 39, LocaleController.isRTL ? 72 : 28, 0));

        if (needCheck) {
            checkBox = new GroupCreateCheckBox(context);
            checkBox.setVisibility(VISIBLE);
            addView(checkBox, LayoutHelper.createFrame(24, 24, (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.TOP, LocaleController.isRTL ? 0 : 41, 41, LocaleController.isRTL ? 41 : 0, 0));
        }
    }

    public void setUser(TLRPC.User user, CharSequence name, CharSequence status) {
        currentUser = user;
        currentStatus = status;
        currentName = name;
        update(0);
    }

    public void setChecked(boolean checked, boolean animated) {
        checkBox.setChecked(checked, animated);
    }

    public TLRPC.User getUser() {
        return currentUser;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(72), MeasureSpec.EXACTLY));
    }

    public void recycle() {
        avatarImageView.getImageReceiver().cancelLoadImage();
    }

    public void update(int mask) {
        if (currentUser == null) {
            return;
        }
        TLRPC.FileLocation photo = null;
        String newName = null;
        if (currentUser.photo != null) {
            photo = currentUser.photo.photo_small;
        }

        if (mask != 0) {
            boolean continueUpdate = false;
            if ((mask & MessagesController.UPDATE_MASK_AVATAR) != 0) {
                if (lastAvatar != null && photo == null || lastAvatar == null && photo != null && lastAvatar != null && photo != null && (lastAvatar.volume_id != photo.volume_id || lastAvatar.local_id != photo.local_id)) {
                    continueUpdate = true;
                }
            }
            if (currentUser != null && currentStatus == null && !continueUpdate && (mask & MessagesController.UPDATE_MASK_STATUS) != 0) {
                int newStatus = 0;
                if (currentUser.status != null) {
                    newStatus = currentUser.status.expires;
                }
                if (newStatus != lastStatus) {
                    continueUpdate = true;
                }
            }
            if (!continueUpdate && currentName == null && lastName != null && (mask & MessagesController.UPDATE_MASK_NAME) != 0) {
                newName = UserObject.getUserName(currentUser);
                if (!newName.equals(lastName)) {
                    continueUpdate = true;
                }
            }
            if (!continueUpdate) {
                return;
            }
        }

        avatarDrawable.setInfo(currentUser);
        lastStatus = currentUser.status != null ? currentUser.status.expires : 0;

        if (currentName != null) {
            lastName = null;
            nameTextView.setText(currentName, true);
        } else {
            lastName = newName == null ? UserObject.getUserName(currentUser) : newName;
            nameTextView.setText(lastName);
        }

        if (currentStatus != null) {
            statusTextView.setText(currentStatus, true);
            statusTextView.setTag(Theme.key_groupcreate_offlineText);
            statusTextView.setTextColor(Theme.getColor(Theme.key_groupcreate_offlineText));
        } else {
            if (currentUser.bot) {
                statusTextView.setTag(Theme.key_groupcreate_offlineText);
                statusTextView.setTextColor(Theme.getColor(Theme.key_groupcreate_offlineText));
                statusTextView.setText(LocaleController.getString("Bot", R.string.Bot));
            } else {
                if (currentUser.id == UserConfig.getClientUserId() || currentUser.status != null && currentUser.status.expires > ConnectionsManager.getInstance().getCurrentTime() || MessagesController.getInstance().onlinePrivacy.containsKey(currentUser.id)) {
                    statusTextView.setTag(Theme.key_groupcreate_offlineText);
                    statusTextView.setTextColor(Theme.getColor(Theme.key_groupcreate_onlineText));
                    statusTextView.setText(LocaleController.getString("Online", R.string.Online));
                } else {
                    statusTextView.setTag(Theme.key_groupcreate_offlineText);
                    statusTextView.setTextColor(Theme.getColor(Theme.key_groupcreate_offlineText));
                    statusTextView.setText(LocaleController.formatUserStatus(currentUser));
                }
            }
        }

        avatarImageView.setImage(photo, "50_50", avatarDrawable);
    }

    @Override
    public boolean hasOverlappingRendering() {
        return false;
    }
}