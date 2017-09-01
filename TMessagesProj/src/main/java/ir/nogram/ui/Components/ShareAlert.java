/*
 * This is the source code of Telegram for Android v. 3.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2017.
 */

package ir.nogram.ui.Components;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.EditorInfo;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import ir.nogram.SQLite.SQLiteCursor;
import ir.nogram.messanger.AndroidUtilities;
import ir.nogram.messanger.ApplicationLoader;
import ir.nogram.messanger.ChatObject;
import ir.nogram.messanger.ContactsController;
import ir.nogram.messanger.FileLog;
import ir.nogram.messanger.LocaleController;
import ir.nogram.messanger.MessageObject;
import ir.nogram.messanger.MessagesController;
import ir.nogram.messanger.MessagesStorage;
import ir.nogram.messanger.NotificationCenter;
import ir.nogram.messanger.R;
import ir.nogram.messanger.SendMessagesHelper;
import ir.nogram.messanger.support.widget.GridLayoutManager;
import ir.nogram.messanger.support.widget.RecyclerView;
import ir.nogram.tgnet.ConnectionsManager;
import ir.nogram.tgnet.NativeByteBuffer;
import ir.nogram.tgnet.RequestDelegate;
import ir.nogram.tgnet.TLObject;
import ir.nogram.tgnet.TLRPC;
import ir.nogram.ui.ActionBar.BottomSheet;
import ir.nogram.ui.ActionBar.Theme;
import ir.nogram.ui.Cells.ShareDialogCell;
import ir.nogram.ui.DialogsActivity;
import ir.nogram.ui.ThemeManager;
import ir.nogram.users.activity.holder.HoldConst;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class ShareAlert extends BottomSheet implements NotificationCenter.NotificationCenterDelegate {

    private FrameLayout frameLayout;
    private FrameLayout frameLayout2;
    private TextView doneButtonBadgeTextView;
    private TextView doneButtonTextView;
    private LinearLayout doneButton;
    private EditText nameTextView;
    private EditText commentTextView;
    private View shadow;
    private View shadow2;
    private AnimatorSet animatorSet;
    private RecyclerListView gridView;
    private GridLayoutManager layoutManager;
    private ShareDialogsAdapter listAdapter;
    private ShareSearchAdapter searchAdapter;
    private MessageObject sendingMessageObject;
    private String sendingText;
    private EmptyTextProgressView searchEmptyView;
    private Drawable shadowDrawable;
    private HashMap<Long, TLRPC.TL_dialog> selectedDialogs = new HashMap<>();

    private TLRPC.TL_exportedMessageLink exportedMessageLink;
    private boolean loadingLink;
    private boolean copyLinkOnEnd;

    private boolean isPublicChannel;
    private String linkToCopy;

    private int scrollOffsetY;
    private int topBeforeSwitch;
    //HoseinKord
    private Switch quoteSwitch;
    //HoseinKord
    private float touchPositionDP;
    private  DialogsOnTouch onTouchListener = null;

    private FrameLayout tabsView;

    private LinearLayout tabsLayout;
    private int tabsHeight;
    private ImageView allTab;
    private ImageView usersTab;
    private ImageView groupsTab;
    private ImageView superGroupsTab;
    private ImageView channelsTab;
    private ImageView botsTab;
    private final int allTabDialogsType = 1 ;
    private final int usersTabDialogsType = 2;
    private final int groupsTabDialogsType = 3;
    private final int superGroupsTabDialogsType = 4;
    private final int channelsTabDialogsType = 5;
    private final int botsTabDialogsType = 6;

    int dialogsType = allTabDialogsType ;

    public ShareAlert(final Context context, MessageObject messageObject, final String text, boolean publicChannel, final String copyLink, boolean fullScreen) {
        super(context, true);

        shadowDrawable = context.getResources().getDrawable(R.drawable.sheet_shadow).mutate();
        shadowDrawable.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_dialogBackground), PorterDuff.Mode.MULTIPLY));

        linkToCopy = copyLink;
        sendingMessageObject = messageObject;
        searchAdapter = new ShareSearchAdapter(context);
        isPublicChannel = publicChannel;
        sendingText = text;

        if (publicChannel) {
            loadingLink = true;
            TLRPC.TL_channels_exportMessageLink req = new TLRPC.TL_channels_exportMessageLink();
            req.id = messageObject.getId();
            req.channel = MessagesController.getInputChannel(messageObject.messageOwner.to_id.channel_id);
            ConnectionsManager.getInstance().sendRequest(req, new RequestDelegate() {
                @Override
                public void run(final TLObject response, TLRPC.TL_error error) {
                    AndroidUtilities.runOnUIThread(new Runnable() {
                        @Override
                        public void run() {
                            if (response != null) {
                                exportedMessageLink = (TLRPC.TL_exportedMessageLink) response;
                                if (copyLinkOnEnd) {
                                    copyLink(context);
                                }
                            }
                            loadingLink = false;
                        }
                    });
                }
            });
        }

        containerView = new FrameLayout(context) {

            private boolean ignoreLayout = false;

            @Override
            public boolean onInterceptTouchEvent(MotionEvent ev) {
                if (ev.getAction() == MotionEvent.ACTION_DOWN && scrollOffsetY != 0 && ev.getY() < scrollOffsetY) {
                    dismiss();
                    return true;
                }
                return super.onInterceptTouchEvent(ev);
            }

            @Override
            public boolean onTouchEvent(MotionEvent e) {
                return !isDismissed() && super.onTouchEvent(e);
            }

            @Override
            protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                int height = MeasureSpec.getSize(heightMeasureSpec);
                if (Build.VERSION.SDK_INT >= 21) {
                    height -= AndroidUtilities.statusBarHeight;
                }
                int size = Math.max(searchAdapter.getItemCount(), listAdapter.getItemCount());
                int contentSize = AndroidUtilities.dp(48) + Math.max(3, (int) Math.ceil(size / 4.0f)) * AndroidUtilities.dp(100) + backgroundPaddingTop;
                int padding = contentSize < height ? 0 : height - (height / 5 * 3) + AndroidUtilities.dp(8);
                if (gridView.getPaddingTop() != padding) {
                    ignoreLayout = true;
                    gridView.setPadding(0, padding, 0, AndroidUtilities.dp(frameLayout2.getTag() != null ? 56 : 8));
                    ignoreLayout = false;
                }
                super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(Math.min(contentSize, height), MeasureSpec.EXACTLY));
            }

            @Override
            protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
                super.onLayout(changed, left, top, right, bottom);
                updateLayout();
            }

            @Override
            public void requestLayout() {
                if (ignoreLayout) {
                    return;
                }
                super.requestLayout();
            }

            @Override
            protected void onDraw(Canvas canvas) {
                shadowDrawable.setBounds(0, scrollOffsetY - backgroundPaddingTop, getMeasuredWidth(), getMeasuredHeight());
                shadowDrawable.draw(canvas);
            }
        };
        containerView.setWillNotDraw(false);
        containerView.setPadding(backgroundPaddingLeft, 0, backgroundPaddingLeft, 0);

        frameLayout = new FrameLayout(context);
        frameLayout.setBackgroundColor(Theme.getColor(Theme.key_dialogBackground));
        frameLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        doneButton = new LinearLayout(context);
        doneButton.setOrientation(LinearLayout.HORIZONTAL);
        doneButton.setBackgroundDrawable(Theme.createSelectorDrawable(Theme.getColor(Theme.key_dialogButtonSelector), 0));
        doneButton.setPadding(AndroidUtilities.dp(21), 0, AndroidUtilities.dp(21), 0);
        frameLayout.addView(doneButton, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.MATCH_PARENT, Gravity.TOP | Gravity.RIGHT));
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedDialogs.isEmpty() && (isPublicChannel || linkToCopy != null)) {
                    if (linkToCopy == null && loadingLink) {
                        copyLinkOnEnd = true;
                        Toast.makeText(ShareAlert.this.getContext(), LocaleController.getString("Loading", R.string.Loading), Toast.LENGTH_SHORT).show();
                    } else {
                        copyLink(ShareAlert.this.getContext());
                    }
                    dismiss();
                } else {
                    if (sendingMessageObject != null) {
                        ArrayList<MessageObject> arrayList = new ArrayList<>();
                        arrayList.add(sendingMessageObject);
                        for (HashMap.Entry<Long, TLRPC.TL_dialog> entry : selectedDialogs.entrySet()) {
                            if (frameLayout2.getTag() != null && commentTextView.length() > 0) {
                                SendMessagesHelper.getInstance().sendMessage(commentTextView.getText().toString(), entry.getKey(), null, null, true, null, null, null);
                            }
                            //HoseinKord
                            if (quoteSwitch.isChecked()) {
                                SendMessagesHelper.getInstance().sendMessage(arrayList, entry.getKey());
                            }else{
                                for (MessageObject object : arrayList) {
                                    SendMessagesHelper.getInstance().processForwardFromMyName(object, entry.getKey());
                                }
                            }

//                            SendMessagesHelper.getInstance().sendMessage(arrayList, entry.getKey());
                        }
                    } else if (sendingText != null) {
                        for (HashMap.Entry<Long, TLRPC.TL_dialog> entry : selectedDialogs.entrySet()) {
                            if (frameLayout2.getTag() != null && commentTextView.length() > 0) {
                                SendMessagesHelper.getInstance().sendMessage(commentTextView.getText().toString(), entry.getKey(), null, null, true, null, null, null);
                            }
                            SendMessagesHelper.getInstance().sendMessage(sendingText, entry.getKey(), null, null, true, null, null, null);
                        }
                    }
                    dismiss();
                }
            }
        });

        doneButtonBadgeTextView = new TextView(context);
        doneButtonBadgeTextView.setTypeface(AndroidUtilities.getTypeface(ThemeManager.getFont()));
        doneButtonBadgeTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
        doneButtonBadgeTextView.setTextColor(Theme.getColor(Theme.key_dialogBadgeText));
        doneButtonBadgeTextView.setGravity(Gravity.CENTER);
        doneButtonBadgeTextView.setBackgroundDrawable(Theme.createRoundRectDrawable(AndroidUtilities.dp(12.5f), Theme.getColor(Theme.key_dialogBadgeBackground)));
        doneButtonBadgeTextView.setMinWidth(AndroidUtilities.dp(23));
        doneButtonBadgeTextView.setPadding(AndroidUtilities.dp(8), 0, AndroidUtilities.dp(8), AndroidUtilities.dp(1));
        doneButton.addView(doneButtonBadgeTextView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, 23, Gravity.CENTER_VERTICAL, 0, 0, 10, 0));

        doneButtonTextView = new TextView(context);
        doneButtonTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        doneButtonTextView.setGravity(Gravity.CENTER);
        doneButtonTextView.setCompoundDrawablePadding(AndroidUtilities.dp(8));
        doneButtonTextView.setTypeface(AndroidUtilities.getTypeface(ThemeManager.getFont()));
        doneButton.addView(doneButtonTextView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_VERTICAL));

        //HoseinKord
        //quate
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences(HoldConst.PREF_MAIN , Context.MODE_PRIVATE);
        quoteSwitch = new Switch(context);
        quoteSwitch.setTag("chat");
        quoteSwitch.setDuplicateParentStateEnabled(false);
        quoteSwitch.setFocusable(false);
        quoteSwitch.setFocusableInTouchMode(false);
        quoteSwitch.setClickable(true);
        setCheck(preferences.getBoolean(HoldConst.PREF_DIRECT_SHARE_QUATE, true));
        frameLayout.addView(quoteSwitch, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.TOP, 0, 6, 0, 0));
        quoteSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences(HoldConst.PREF_MAIN, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean(HoldConst.PREF_DIRECT_SHARE_QUATE, isChecked).apply();
            }
        });
        TextView quoteTextView = new TextView(context);
        quoteTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 9);
        //quoteTextView.setTextColor(0xff979797);
        quoteTextView.setTextColor(0xff979797);
        quoteTextView.setGravity(Gravity.CENTER);
        quoteTextView.setCompoundDrawablePadding(AndroidUtilities.dp(8));
        quoteTextView.setText(LocaleController.getString("Quote", R.string.Quote).toUpperCase());
        quoteTextView.setTypeface(AndroidUtilities.getTypeface(ThemeManager.getFont()));
        frameLayout.addView(quoteTextView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.TOP , 12, 2, 0, 0));
        //


//        ImageView imageView = new ImageView(context);
//        imageView.setImageResource(R.drawable.ic_ab_search);
//        imageView.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_dialogIcon), PorterDuff.Mode.MULTIPLY));
//        imageView.setScaleType(ImageView.ScaleType.CENTER);
//        imageView.setPadding(0, AndroidUtilities.dp(2), 0, 0);
//        frameLayout.addView(imageView, LayoutHelper.createFrame(48, 48, Gravity.LEFT | Gravity.CENTER_VERTICAL));

        nameTextView = new EditText(context);
        nameTextView.setHint(LocaleController.getString("ShareSendTo", R.string.ShareSendTo));
        nameTextView.setMaxLines(1);
        nameTextView.setSingleLine(true);
        nameTextView.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
        nameTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        nameTextView.setBackgroundDrawable(null);
        nameTextView.setHintTextColor(Theme.getColor(Theme.key_dialogTextHint));
        nameTextView.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        nameTextView.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        AndroidUtilities.clearCursorDrawable(nameTextView);
        nameTextView.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        frameLayout.addView(nameTextView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 48, Gravity.TOP | Gravity.LEFT, 54, 2, 96, 0));
        nameTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String text = nameTextView.getText().toString();
                if (text.length() != 0) {
                    if (gridView.getAdapter() != searchAdapter) {
                        topBeforeSwitch = getCurrentTop();
                        gridView.setAdapter(searchAdapter);
                        searchAdapter.notifyDataSetChanged();
                    }
                    if (searchEmptyView != null) {
                        searchEmptyView.setText(LocaleController.getString("NoResult", R.string.NoResult));
                    }
                } else {
                    if (gridView.getAdapter() != listAdapter) {
                        int top = getCurrentTop();
                        searchEmptyView.setText(LocaleController.getString("NoChats", R.string.NoChats));
                        gridView.setAdapter(listAdapter);
                        listAdapter.notifyDataSetChanged();
                        if (top > 0) {
                            layoutManager.scrollToPositionWithOffset(0, -top);
                        }
                    }
                }
                if (searchAdapter != null) {
                    searchAdapter.searchDialogs(text);
                }
            }
        });

//HoseinKord
        tabsView = new FrameLayout(context);
        createTabs(context);
        frameLayout.addView(tabsView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, tabsHeight, Gravity.TOP, 0, 52, 0, 0));

        gridView = new RecyclerListView(context);
        gridView.setTag(13);
        gridView.setPadding(0, 0, 0, AndroidUtilities.dp(8));
        gridView.setClipToPadding(false);
        gridView.setLayoutManager(layoutManager = new GridLayoutManager(getContext(), 4));
        gridView.setHorizontalScrollBarEnabled(false);
        gridView.setVerticalScrollBarEnabled(false);
        gridView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(android.graphics.Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                RecyclerListView.Holder holder = (RecyclerListView.Holder) parent.getChildViewHolder(view);
                if (holder != null) {
                    int pos = holder.getAdapterPosition();
                    outRect.left = pos % 4 == 0 ? 0 : AndroidUtilities.dp(4);
                    outRect.right = pos % 4 == 3 ? 0 : AndroidUtilities.dp(4);
                } else {
                    outRect.left = AndroidUtilities.dp(4);
                    outRect.right = AndroidUtilities.dp(4);
                }
            }
        });
        containerView.addView(gridView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.TOP | Gravity.LEFT, 0, 100, 0, 0));
//        gridView.setAdapter(listAdapter = new ShareDialogsAdapter(context));
        gridView.setGlowColor(Theme.getColor(Theme.key_dialogScrollGlow));
        gridView.setOnItemClickListener(new RecyclerListView.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (position < 0) {
                    return;
                }
                TLRPC.TL_dialog dialog;
                if (gridView.getAdapter() == listAdapter) {
                    dialog = listAdapter.getItem(position);
                } else {
                    dialog = searchAdapter.getItem(position);
                }
                if (dialog == null) {
                    return;
                }
                ShareDialogCell cell = (ShareDialogCell) view;
                if (selectedDialogs.containsKey(dialog.id)) {
                    selectedDialogs.remove(dialog.id);
                    cell.setChecked(false, true);
                } else {
                    selectedDialogs.put(dialog.id, dialog);
                    cell.setChecked(true, true);
                }
                updateSelectedCount();
            }
        });
        gridView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                updateLayout();
            }
        });

        searchEmptyView = new EmptyTextProgressView(context);
        searchEmptyView.setShowAtCenter(true);
        searchEmptyView.showTextView();
        searchEmptyView.setText(LocaleController.getString("NoChats", R.string.NoChats));
        gridView.setEmptyView(searchEmptyView);

        onTouchListener = new DialogsOnTouch(context);
        gridView.setOnTouchListener(onTouchListener);
        searchEmptyView.setOnTouchListener(onTouchListener);

        containerView.addView(searchEmptyView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.TOP | Gravity.LEFT, 0, 48, 0, 0));

        containerView.addView(frameLayout, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 100, Gravity.LEFT | Gravity.TOP));

        shadow = new View(context);
        shadow.setBackgroundResource(R.drawable.header_shadow);
        containerView.addView(shadow, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 3, Gravity.TOP | Gravity.LEFT, 0, 48, 0, 0));

        frameLayout2 = new FrameLayout(context);
        frameLayout2.setBackgroundColor(Theme.getColor(Theme.key_dialogBackground));
        frameLayout2.setTranslationY(AndroidUtilities.dp(53));
        containerView.addView(frameLayout2, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 48, Gravity.LEFT | Gravity.BOTTOM));
        frameLayout2.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        commentTextView = new EditText(context);
        commentTextView.setHint(LocaleController.getString("ShareComment", R.string.ShareComment));
        commentTextView.setMaxLines(1);
        commentTextView.setSingleLine(true);
        commentTextView.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
        commentTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        commentTextView.setBackgroundDrawable(null);
        commentTextView.setHintTextColor(Theme.getColor(Theme.key_dialogTextHint));
        commentTextView.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        commentTextView.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        AndroidUtilities.clearCursorDrawable(commentTextView);
        commentTextView.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        frameLayout2.addView(commentTextView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.TOP | Gravity.LEFT, 8, 1, 8, 0));

        shadow2 = new View(context);
        shadow2.setBackgroundResource(R.drawable.header_shadow_reverse);
        shadow2.setTranslationY(AndroidUtilities.dp(53));
        containerView.addView(shadow2, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 3, Gravity.BOTTOM | Gravity.LEFT, 0, 0, 0, 48));

        updateSelectedCount();

        listAdapter = new ShareDialogsAdapter(context);
        gridView.setAdapter(listAdapter);
        listAdapter.notifyDataSetChanged();
        refreshTabs() ;

        if (!DialogsActivity.dialogsLoaded) {
            MessagesController.getInstance().loadDialogs(0, 100, true);
            ContactsController.getInstance().checkInviteText();
            DialogsActivity.dialogsLoaded = true;
        }
        if (listAdapter.dialogs.isEmpty()) {
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.dialogsNeedReload);
        }
    }

    private int getCurrentTop() {
        if (gridView.getChildCount() != 0) {
            View child = gridView.getChildAt(0);
            RecyclerListView.Holder holder = (RecyclerListView.Holder) gridView.findContainingViewHolder(child);
            if (holder != null) {
                return gridView.getPaddingTop() - (holder.getAdapterPosition() == 0 && child.getTop() >= 0 ? child.getTop() : 0);
            }
        }
        return -1000;
    }


    @Override
    public void didReceivedNotification(int id, Object... args) {
        if (id == NotificationCenter.dialogsNeedReload) {
            if (listAdapter != null) {
                listAdapter.fetchDialogs();
            }
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.dialogsNeedReload);
        }
    }

    @Override
    protected boolean canDismissWithSwipe() {
        return false;
    }

    @SuppressLint("NewApi")
    private void updateLayout() {
        if (gridView.getChildCount() <= 0) {
            return;
        }
        View child = gridView.getChildAt(0);
        RecyclerListView.Holder holder = (RecyclerListView.Holder) gridView.findContainingViewHolder(child);
        int top = child.getTop() - AndroidUtilities.dp(8);
        int newOffset = top > 0 && holder != null && holder.getAdapterPosition() == 0 ? top : 0;
        if (scrollOffsetY != newOffset) {
            gridView.setTopGlowOffset(scrollOffsetY = newOffset);
            frameLayout.setTranslationY(scrollOffsetY);
            shadow.setTranslationY(scrollOffsetY);
            searchEmptyView.setTranslationY(scrollOffsetY);
            containerView.invalidate();
        }
    }

    private void copyLink(Context context) {
        if (exportedMessageLink == null && linkToCopy == null) {
            return;
        }
        try {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) ApplicationLoader.applicationContext.getSystemService(Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText("label", linkToCopy != null ? linkToCopy : exportedMessageLink.link);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(context, LocaleController.getString("LinkCopied", R.string.LinkCopied), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            FileLog.e(e);
        }
    }

    private void showCommentTextView(final boolean show) {
        if (show == (frameLayout2.getTag() != null)) {
            return;
        }
        if (animatorSet != null) {
            animatorSet.cancel();
        }
        frameLayout2.setTag(show ? 1 : null);
        AndroidUtilities.hideKeyboard(commentTextView);
        animatorSet = new AnimatorSet();
        animatorSet.playTogether(
                ObjectAnimator.ofFloat(shadow2, "translationY", AndroidUtilities.dp(show ? 0 : 53)),
                ObjectAnimator.ofFloat(frameLayout2, "translationY", AndroidUtilities.dp(show ? 0 : 53)));
        animatorSet.setInterpolator(new DecelerateInterpolator());
        animatorSet.setDuration(180);
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (animation.equals(animatorSet)) {
                    gridView.setPadding(0, 0, 0, AndroidUtilities.dp(show ? 56 : 8));
                    animatorSet = null;
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                if (animation.equals(animatorSet)) {
                    animatorSet = null;
                }
            }
        });
        animatorSet.start();
    }

    public void updateSelectedCount() {
        if (selectedDialogs.isEmpty()) {
            showCommentTextView(false);
            doneButtonBadgeTextView.setVisibility(View.GONE);
            if (!isPublicChannel && linkToCopy == null) {
                doneButtonTextView.setTextColor(Theme.getColor(Theme.key_dialogTextGray4));
                doneButton.setEnabled(false);
                doneButtonTextView.setText(LocaleController.getString("Send", R.string.Send).toUpperCase());
            } else {
                doneButtonTextView.setTextColor(Theme.getColor(Theme.key_dialogTextBlue2));
                doneButton.setEnabled(true);
                doneButtonTextView.setText(LocaleController.getString("CopyLink", R.string.CopyLink).toUpperCase());
            }
        } else {
            showCommentTextView(true);
            doneButtonTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            doneButtonBadgeTextView.setVisibility(View.VISIBLE);
            doneButtonBadgeTextView.setText(String.format("%d", selectedDialogs.size()));
            doneButtonTextView.setTextColor(Theme.getColor(Theme.key_dialogTextBlue3));
            doneButton.setEnabled(true);
            doneButtonTextView.setText(LocaleController.getString("Send", R.string.Send).toUpperCase());
        }
    }

    @Override
    public void dismiss() {
        super.dismiss();
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.dialogsNeedReload);
    }

    private class ShareDialogsAdapter extends RecyclerListView.SelectionAdapter {

        private Context context;
        private int currentCount;
        private ArrayList<TLRPC.TL_dialog> dialogs = new ArrayList<>();

        public ShareDialogsAdapter(Context context) {
            this.context = context;
            fetchDialogs();
        }

        public void fetchDialogs() {
            dialogs.clear();
            int size = 0 ;
            ArrayList<TLRPC.TL_dialog> dialogss = null ;
            if (dialogsType == allTabDialogsType) {
                size =   MessagesController.getInstance().dialogsServerOnly.size() ;
                dialogss = MessagesController.getInstance().dialogsServerOnly;
            }
            else if (dialogsType == usersTabDialogsType) {
                size =   MessagesController.getInstance().dialogsUsers.size() ;
                dialogss = MessagesController.getInstance().dialogsUsers;
            } else if (dialogsType == groupsTabDialogsType) {
                size =   MessagesController.getInstance().dialogsGroupsOnly.size() ;
                dialogss = MessagesController.getInstance().dialogsGroupsOnly;
            } else if (dialogsType == channelsTabDialogsType) {
                size =   MessagesController.getInstance().dialogsChannels.size() ;
                dialogss = MessagesController.getInstance().dialogsChannels;
            } else if (dialogsType == botsTabDialogsType) {
                size =   MessagesController.getInstance().dialogsBots.size() ;
                dialogss = MessagesController.getInstance().dialogsBots;
            } else if (dialogsType == superGroupsTabDialogsType) {
                size =   MessagesController.getInstance().dialogsMegaGroups.size() ;
                dialogss = MessagesController.getInstance().dialogsMegaGroups;
            }
            for (int a = 0; a <size ; a++) {
                TLRPC.TL_dialog dialog =dialogss.get(a);
                int lower_id = (int) dialog.id;
                int high_id = (int) (dialog.id >> 32);
                if (lower_id != 0 && high_id != 1) {
                    if (lower_id > 0) {
                        dialogs.add(dialog);
                    } else {
                        TLRPC.Chat chat = MessagesController.getInstance().getChat(-lower_id);
                        if (!(chat == null || ChatObject.isNotInChat(chat) || ChatObject.isChannel(chat) && !chat.creator && (chat.admin_rights == null || !chat.admin_rights.post_messages) && !chat.megagroup)) {
                            dialogs.add(dialog);
                        }
                    }
                }
            }
            notifyDataSetChanged();
        }

        @Override
        public int getItemCount() {
            return dialogs.size();
        }

        public TLRPC.TL_dialog getItem(int i) {
            if (i < 0 || i >= dialogs.size()) {
                return null;
            }
            return dialogs.get(i);
        }

        @Override
        public boolean isEnabled(RecyclerView.ViewHolder holder) {
            return true;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = new ShareDialogCell(context);
            view.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, AndroidUtilities.dp(100)));
            return new RecyclerListView.Holder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            ShareDialogCell cell = (ShareDialogCell) holder.itemView;
            TLRPC.TL_dialog dialog = getItem(position);
            cell.setDialog((int) dialog.id, selectedDialogs.containsKey(dialog.id), null);
        }

        @Override
        public int getItemViewType(int i) {
            return 0;
        }
    }

    public class ShareSearchAdapter extends RecyclerListView.SelectionAdapter {

        private Context context;
        private Timer searchTimer;
        private ArrayList<DialogSearchResult> searchResult = new ArrayList<>();
        private String lastSearchText;
        private int reqId = 0;
        private int lastReqId;
        private int lastSearchId = 0;

        private class DialogSearchResult {
            public TLRPC.TL_dialog dialog = new TLRPC.TL_dialog();
            public TLObject object;
            public int date;
            public CharSequence name;
        }

        public ShareSearchAdapter(Context context) {
            this.context = context;
        }

        private void searchDialogsInternal(final String query, final int searchId) {
            MessagesStorage.getInstance().getStorageQueue().postRunnable(new Runnable() {
                @Override
                public void run() {
                    try {
                        String search1 = query.trim().toLowerCase();
                        if (search1.length() == 0) {
                            lastSearchId = -1;
                            updateSearchResults(new ArrayList<DialogSearchResult>(), lastSearchId);
                            return;
                        }
                        String search2 = LocaleController.getInstance().getTranslitString(search1);
                        if (search1.equals(search2) || search2.length() == 0) {
                            search2 = null;
                        }
                        String search[] = new String[1 + (search2 != null ? 1 : 0)];
                        search[0] = search1;
                        if (search2 != null) {
                            search[1] = search2;
                        }

                        ArrayList<Integer> usersToLoad = new ArrayList<>();
                        ArrayList<Integer> chatsToLoad = new ArrayList<>();
                        int resultCount = 0;

                        HashMap<Long, DialogSearchResult> dialogsResult = new HashMap<>();
                        SQLiteCursor cursor = MessagesStorage.getInstance().getDatabase().queryFinalized("SELECT did, date FROM dialogs ORDER BY date DESC LIMIT 400");
                        while (cursor.next()) {
                            long id = cursor.longValue(0);
                            DialogSearchResult dialogSearchResult = new DialogSearchResult();
                            dialogSearchResult.date = cursor.intValue(1);
                            dialogsResult.put(id, dialogSearchResult);

                            int lower_id = (int) id;
                            int high_id = (int) (id >> 32);
                            if (lower_id != 0 && high_id != 1) {
                                if (lower_id > 0) {
                                    if (!usersToLoad.contains(lower_id)) {
                                        usersToLoad.add(lower_id);
                                    }
                                } else {
                                    if (!chatsToLoad.contains(-lower_id)) {
                                        chatsToLoad.add(-lower_id);
                                    }
                                }
                            }
                        }
                        cursor.dispose();

                        if (!usersToLoad.isEmpty()) {
                            cursor = MessagesStorage.getInstance().getDatabase().queryFinalized(String.format(Locale.US, "SELECT data, status, name FROM users WHERE uid IN(%s)", TextUtils.join(",", usersToLoad)));
                            while (cursor.next()) {
                                String name = cursor.stringValue(2);
                                String tName = LocaleController.getInstance().getTranslitString(name);
                                if (name.equals(tName)) {
                                    tName = null;
                                }
                                String username = null;
                                int usernamePos = name.lastIndexOf(";;;");
                                if (usernamePos != -1) {
                                    username = name.substring(usernamePos + 3);
                                }
                                int found = 0;
                                for (String q : search) {
                                    if (name.startsWith(q) || name.contains(" " + q) || tName != null && (tName.startsWith(q) || tName.contains(" " + q))) {
                                        found = 1;
                                    } else if (username != null && username.startsWith(q)) {
                                        found = 2;
                                    }
                                    if (found != 0) {
                                        NativeByteBuffer data = cursor.byteBufferValue(0);
                                        if (data != null) {
                                            TLRPC.User user = TLRPC.User.TLdeserialize(data, data.readInt32(false), false);
                                            data.reuse();
                                            DialogSearchResult dialogSearchResult = dialogsResult.get((long) user.id);
                                            if (user.status != null) {
                                                user.status.expires = cursor.intValue(1);
                                            }
                                            if (found == 1) {
                                                dialogSearchResult.name = AndroidUtilities.generateSearchName(user.first_name, user.last_name, q);
                                            } else {
                                                dialogSearchResult.name = AndroidUtilities.generateSearchName("@" + user.username, null, "@" + q);
                                            }
                                            dialogSearchResult.object = user;
                                            dialogSearchResult.dialog.id = user.id;
                                            resultCount++;
                                        }
                                        break;
                                    }
                                }
                            }
                            cursor.dispose();
                        }

                        if (!chatsToLoad.isEmpty()) {
                            cursor = MessagesStorage.getInstance().getDatabase().queryFinalized(String.format(Locale.US, "SELECT data, name FROM chats WHERE uid IN(%s)", TextUtils.join(",", chatsToLoad)));
                            while (cursor.next()) {
                                String name = cursor.stringValue(1);
                                String tName = LocaleController.getInstance().getTranslitString(name);
                                if (name.equals(tName)) {
                                    tName = null;
                                }
                                for (int a = 0; a < search.length; a++) {
                                    String q = search[a];
                                    if (name.startsWith(q) || name.contains(" " + q) || tName != null && (tName.startsWith(q) || tName.contains(" " + q))) {
                                        NativeByteBuffer data = cursor.byteBufferValue(0);
                                        if (data != null) {
                                            TLRPC.Chat chat = TLRPC.Chat.TLdeserialize(data, data.readInt32(false), false);
                                            data.reuse();
                                            if (!(chat == null || ChatObject.isNotInChat(chat) || ChatObject.isChannel(chat) && !chat.creator && (chat.admin_rights == null || !chat.admin_rights.post_messages) && !chat.megagroup)) {
                                                DialogSearchResult dialogSearchResult = dialogsResult.get(-(long) chat.id);
                                                dialogSearchResult.name = AndroidUtilities.generateSearchName(chat.title, null, q);
                                                dialogSearchResult.object = chat;
                                                dialogSearchResult.dialog.id = -chat.id;
                                                resultCount++;
                                            }
                                        }
                                        break;
                                    }
                                }
                            }
                            cursor.dispose();
                        }

                        ArrayList<DialogSearchResult> searchResults = new ArrayList<>(resultCount);
                        for (DialogSearchResult dialogSearchResult : dialogsResult.values()) {
                            if (dialogSearchResult.object != null && dialogSearchResult.name != null) {
                                searchResults.add(dialogSearchResult);
                            }
                        }

                        cursor = MessagesStorage.getInstance().getDatabase().queryFinalized("SELECT u.data, u.status, u.name, u.uid FROM users as u INNER JOIN contacts as c ON u.uid = c.uid");
                        while (cursor.next()) {
                            int uid = cursor.intValue(3);
                            if (dialogsResult.containsKey((long) uid)) {
                                continue;
                            }
                            String name = cursor.stringValue(2);
                            String tName = LocaleController.getInstance().getTranslitString(name);
                            if (name.equals(tName)) {
                                tName = null;
                            }
                            String username = null;
                            int usernamePos = name.lastIndexOf(";;;");
                            if (usernamePos != -1) {
                                username = name.substring(usernamePos + 3);
                            }
                            int found = 0;
                            for (String q : search) {
                                if (name.startsWith(q) || name.contains(" " + q) || tName != null && (tName.startsWith(q) || tName.contains(" " + q))) {
                                    found = 1;
                                } else if (username != null && username.startsWith(q)) {
                                    found = 2;
                                }
                                if (found != 0) {
                                    NativeByteBuffer data = cursor.byteBufferValue(0);
                                    if (data != null) {
                                        TLRPC.User user = TLRPC.User.TLdeserialize(data, data.readInt32(false), false);
                                        data.reuse();
                                        DialogSearchResult dialogSearchResult = new DialogSearchResult();
                                        if (user.status != null) {
                                            user.status.expires = cursor.intValue(1);
                                        }
                                        dialogSearchResult.dialog.id = user.id;
                                        dialogSearchResult.object = user;
                                        if (found == 1) {
                                            dialogSearchResult.name = AndroidUtilities.generateSearchName(user.first_name, user.last_name, q);
                                        } else {
                                            dialogSearchResult.name = AndroidUtilities.generateSearchName("@" + user.username, null, "@" + q);
                                        }
                                        searchResults.add(dialogSearchResult);
                                    }
                                    break;
                                }
                            }
                        }
                        cursor.dispose();

                        Collections.sort(searchResults, new Comparator<DialogSearchResult>() {
                            @Override
                            public int compare(DialogSearchResult lhs, DialogSearchResult rhs) {
                                if (lhs.date < rhs.date) {
                                    return 1;
                                } else if (lhs.date > rhs.date) {
                                    return -1;
                                }
                                return 0;
                            }
                        });

                        updateSearchResults(searchResults, searchId);
                    } catch (Exception e) {
                        FileLog.e(e);
                    }
                }
            });
        }

        private void updateSearchResults(final ArrayList<DialogSearchResult> result, final int searchId) {
            AndroidUtilities.runOnUIThread(new Runnable() {
                @Override
                public void run() {
                    if (searchId != lastSearchId) {
                        return;
                    }
                    for (int a = 0; a < result.size(); a++) {
                        DialogSearchResult obj = result.get(a);
                        if (obj.object instanceof TLRPC.User) {
                            TLRPC.User user = (TLRPC.User) obj.object;
                            MessagesController.getInstance().putUser(user, true);
                        } else if (obj.object instanceof TLRPC.Chat) {
                            TLRPC.Chat chat = (TLRPC.Chat) obj.object;
                            MessagesController.getInstance().putChat(chat, true);
                        }
                    }
                    boolean becomeEmpty = !searchResult.isEmpty() && result.isEmpty();
                    boolean isEmpty = searchResult.isEmpty() && result.isEmpty();
                    if (becomeEmpty) {
                        topBeforeSwitch = getCurrentTop();
                    }
                    searchResult = result;
                    notifyDataSetChanged();
                    if (!isEmpty && !becomeEmpty && topBeforeSwitch > 0) {
                        layoutManager.scrollToPositionWithOffset(0, -topBeforeSwitch);
                        topBeforeSwitch = -1000;
                    }
                }
            });
        }

        public void searchDialogs(final String query) {
            if (query != null && lastSearchText != null && query.equals(lastSearchText)) {
                return;
            }
            lastSearchText = query;
            try {
                if (searchTimer != null) {
                    searchTimer.cancel();
                    searchTimer = null;
                }
            } catch (Exception e) {
                FileLog.e(e);
            }
            if (query == null || query.length() == 0) {
                searchResult.clear();
                topBeforeSwitch = getCurrentTop();
                notifyDataSetChanged();
            } else {
                final int searchId = ++lastSearchId;
                searchTimer = new Timer();
                searchTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        try {
                            cancel();
                            searchTimer.cancel();
                            searchTimer = null;
                        } catch (Exception e) {
                            FileLog.e(e);
                        }
                        searchDialogsInternal(query, searchId);
                    }
                }, 200, 300);
            }
        }

        @Override
        public int getItemCount() {
            return searchResult.size();
        }

        public TLRPC.TL_dialog getItem(int i) {
            if (i < 0 || i >= searchResult.size()) {
                return null;
            }
            return searchResult.get(i).dialog;
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public boolean isEnabled(RecyclerView.ViewHolder holder) {
            return true;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = new ShareDialogCell(context);
            view.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, AndroidUtilities.dp(100)));
            return new RecyclerListView.Holder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            ShareDialogCell cell = (ShareDialogCell) holder.itemView;
            DialogSearchResult result = searchResult.get(position);
            cell.setDialog((int) result.dialog.id, selectedDialogs.containsKey(result.dialog.id), result.name);
        }

        @Override
        public int getItemViewType(int i) {
            return 0;
        }
    }

    public void setCheck(boolean checked) {
        if (Build.VERSION.SDK_INT < 11) {
            quoteSwitch.resetLayout();
            quoteSwitch.requestLayout();
        }
        quoteSwitch.setChecked(checked);
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
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN: {
                    downX = Math.round(event.getX() / vDPI);
                    downY = Math.round(event.getY() / vDPI);
                    //Log.e("DialogsActivity", "view " + view.toString());
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
                        if(deltaX < 0 ){
                            if(dialogsType<= allTabDialogsType){
                                dialogsType = botsTabDialogsType ;
                            }else{
                                dialogsType = dialogsType - 1 ;
                            }
                        }else{
                            if(dialogsType == botsTabDialogsType){
                                dialogsType = allTabDialogsType ;
                            }else{
                                dialogsType = dialogsType + 1 ;
                            }
                        }
                        downX = Math.round(event.getX() / vDPI );
                        refreshAdapter(mContext);
                        //dialogsAdapter.notifyDataSetChanged();
                        //return true;
                    }

                    //downX = downY = upX = upY = 0;
                    return false;
                }
            }

            return false;
        }
    }

    private void createTabs(final Context context) {
        tabsHeight = 35 ;

        tabsLayout = new LinearLayout(context);
        tabsLayout.setOrientation(LinearLayout.HORIZONTAL);
        tabsLayout.setGravity(Gravity.CENTER);

        //1
        allTab = new ImageView(context);
        //allTab.setScaleType(ImageView.ScaleType.CENTER);
        allTab.setImageResource(R.drawable.tab_all);

        addTabView(context, allTab);

        //2
        usersTab = new ImageView(context);
        usersTab.setImageResource(R.drawable.tab_user);
        /*usersTab.setScaleType(ImageView.ScaleType.CENTER);
        if(!hideUsers) {
            tabsLayout.addView(usersTab, LayoutHelper.createLinear(0, LayoutHelper.MATCH_PARENT, 1.0f));
        }*/
        addTabView(context, usersTab);
        //3
        groupsTab = new ImageView(context);
        groupsTab.setImageResource(R.drawable.tab_group);
        /*groupsTab.setScaleType(ImageView.ScaleType.CENTER);
        if(!hideGroups) {
            tabsLayout.addView(groupsTab, LayoutHelper.createLinear(0, LayoutHelper.MATCH_PARENT, 1.0f));
        }*/
        addTabView(context, groupsTab);
        //4
        superGroupsTab = new ImageView(context);
        superGroupsTab.setImageResource(R.drawable.tab_supergroup);
        /*superGroupsTab.setScaleType(ImageView.ScaleType.CENTER);
        if(!hideSGroups){
            tabsLayout.addView(superGroupsTab, LayoutHelper.createLinear(0, LayoutHelper.MATCH_PARENT, 1.0f));
        }*/
        addTabView(context, superGroupsTab);
        //5
        channelsTab = new ImageView(context);
        channelsTab.setImageResource(R.drawable.tab_channel);
        /*channelsTab.setScaleType(ImageView.ScaleType.CENTER);
        if(!hideChannels){
            tabsLayout.addView(channelsTab, LayoutHelper.createLinear(0, LayoutHelper.MATCH_PARENT, 1.0f));
        }*/
        addTabView(context, channelsTab);
        //6
        botsTab = new ImageView(context);
        botsTab.setImageResource(R.drawable.tab_bot);
        /*botsTab.setScaleType(ImageView.ScaleType.CENTER);
        if(!hideBots){
            tabsLayout.addView(botsTab, LayoutHelper.createLinear(0, LayoutHelper.MATCH_PARENT, 1.0f));
        }*/
        addTabView(context, botsTab);
        //7

        tabsView.addView(tabsLayout, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        try {
            allTab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (dialogsType != allTabDialogsType) {
                        dialogsType = allTabDialogsType;
                        refreshAdapter(context);
                    }
                }
            });

            usersTab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (dialogsType != usersTabDialogsType) {
                        dialogsType = usersTabDialogsType;
                        refreshAdapter(context);
                    }
                }
            });
            groupsTab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (dialogsType != groupsTabDialogsType) {
                        dialogsType = groupsTabDialogsType;
                        refreshAdapter(context);
                    }
//                    if (dialogsType != 2) {
//                        dialogsType = 2 ;
//                        refreshAdapter(context);
//                    }
                }
            });
            superGroupsTab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (dialogsType != superGroupsTabDialogsType) {
                        dialogsType = superGroupsTabDialogsType;
                        refreshAdapter(context);
                    }
                }
            });

            channelsTab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (dialogsType != channelsTabDialogsType) {
                        dialogsType = channelsTabDialogsType;
                        refreshAdapter(context);
                    }
                }
            });

            botsTab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (dialogsType != botsTabDialogsType) {
                        dialogsType = botsTabDialogsType;
                        refreshAdapter(context);
                    }
                }
            });
        }catch (Exception e){
            Log.e("log" , e.getMessage() +"") ;
        }

        try {
            tabsLayout.setBackgroundColor(Theme.getColor(Theme.key_avatar_backgroundActionBarBlue));
        }catch (Exception e){

        }
    }

    private void addTabView(Context context, ImageView iv) {
        iv.setScaleType(ImageView.ScaleType.CENTER);
        GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.RECTANGLE);
        shape.setCornerRadius(AndroidUtilities.dp(32));
        shape.setColor(Theme.getColor(Theme.key_avatar_backgroundActionBarBlue));
        //tv.setPadding(AndroidUtilities.dp(size > 10 ? size - 7 : 4), 0, AndroidUtilities.dp(size > 10 ? size - 7 : 4), 0);
        RelativeLayout layout = new RelativeLayout(context);
        layout.addView(iv, LayoutHelper.createRelative(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
        tabsLayout.addView(layout, LayoutHelper.createLinear(0, LayoutHelper.MATCH_PARENT, 1.0f));
    }



    private void refreshAdapter(Context context){
        refreshAdapterAndTabs(new ShareDialogsAdapter(context));
    }
    private void refreshAdapterAndTabs(ShareDialogsAdapter adapter){
        listAdapter = adapter ;
        gridView.setAdapter(listAdapter);
        listAdapter.notifyDataSetChanged();
        refreshTabs();
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
        channelsTab.setBackgroundResource(0);
        botsTab.setBackgroundResource(0);

        allTab.setColorFilter(iColor, PorterDuff.Mode.SRC_IN);
        usersTab.setColorFilter(iColor, PorterDuff.Mode.SRC_IN);
        groupsTab.setColorFilter(iColor, PorterDuff.Mode.SRC_IN);
        superGroupsTab.setColorFilter(iColor, PorterDuff.Mode.SRC_IN);
        channelsTab.setColorFilter(iColor, PorterDuff.Mode.SRC_IN);
        botsTab.setColorFilter(iColor, PorterDuff.Mode.SRC_IN);
        Drawable selected =ApplicationLoader.applicationContext.getResources().getDrawable(R.drawable.tab_selected);
//        selected.setColorFilter(iconColor, PorterDuff.Mode.SRC_IN);

        switch(dialogsType) {
            case usersTabDialogsType:
                usersTab.setColorFilter(iconColor, PorterDuff.Mode.SRC_IN);
                usersTab.setBackgroundDrawable(selected);
                break;
            case groupsTabDialogsType:
                groupsTab.setColorFilter(iconColor, PorterDuff.Mode.SRC_IN);
                groupsTab.setBackgroundDrawable(selected);
                break;
            case channelsTabDialogsType:
                channelsTab.setColorFilter(iconColor, PorterDuff.Mode.SRC_IN);
                channelsTab.setBackgroundDrawable(selected);
                break;
            case botsTabDialogsType:
                botsTab.setColorFilter(iconColor, PorterDuff.Mode.SRC_IN);
                botsTab.setBackgroundDrawable(selected);
                break;
            case superGroupsTabDialogsType:
                superGroupsTab.setColorFilter(iconColor, PorterDuff.Mode.SRC_IN);
                superGroupsTab.setBackgroundDrawable(selected);
                break;
            default:
                allTab.setColorFilter(iconColor, PorterDuff.Mode.SRC_IN);
                allTab.setBackgroundDrawable(selected);
        }
    }

}
