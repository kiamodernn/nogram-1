package ir.nogram.users.activity;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.text.InputFilter;
import android.text.style.CharacterStyle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import ir.nogram.messanger.AndroidUtilities;
import ir.nogram.messanger.ApplicationLoader;
import ir.nogram.messanger.FileLog;
import ir.nogram.messanger.MessageObject;
import ir.nogram.messanger.R;
import ir.nogram.tgnet.TLRPC;
import ir.nogram.ui.ActionBar.ActionBar;
import ir.nogram.ui.ActionBar.ActionBarMenuItem;
import ir.nogram.ui.ActionBar.BaseFragment;
import ir.nogram.ui.ActionBar.Theme;
import ir.nogram.ui.Cells.ChatMessageCell;
import ir.nogram.ui.Cells.EmptyCell;
import ir.nogram.ui.ChatActivity;
import ir.nogram.ui.Components.ChatActivityEnterView;
import ir.nogram.ui.Components.LayoutHelper;
import ir.nogram.ui.Components.RecyclerListView;
import ir.nogram.ui.Components.ShareAlertAdvanced;
import ir.nogram.ui.Components.SizeNotifierFrameLayout;
import ir.nogram.users.activity.holder.HoldConst;

import java.util.ArrayList;


/**
 * Created by MhkDeveloper on 2016-10-10.
 */
public class ActivityForwardAdvanced extends BaseFragment {
    private ActionBarMenuItem editDoneItem;

    private final static int done_button = 1;
      Context context ;
    MessageObject messageObjectt ;
    private FrameLayout emptyViewContainer;
    EditText  editText ;
    String backupCaption  ;

    ChatActivityEnterView chatActivityEnterView ;
    public ActivityForwardAdvanced(MessageObject messageObject){
        this.messageObjectt = messageObject ;
    }
    public void onFragmentDestroy() {
        SharedPreferences sharedPreferences = ApplicationLoader.applicationContext.getSharedPreferences(HoldConst.PREF_MAIN , Context.MODE_PRIVATE) ;
        String cap = sharedPreferences.getString(HoldConst.PREF_CAPTION , null) ;
        if(cap != null && !cap.equals("null")){
            messageObjectt.caption = cap ;
        }
        super.onFragmentDestroy();
    }

    @Override
    public boolean onFragmentCreate() {
        super.onFragmentCreate();
        return true;
    }
//    RecyclerView recyclerView
    @Override
    public void onResume() {
        super.onResume();
    }
    View view ;
    @Override
    public View createView(final Context context) {
        this.context = context ;
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle(context.getString(R.string.advancedForward));
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }

            }
        });
        fragmentView = new SizeNotifierFrameLayout(context) {

            int inputFieldHeight = 0;

            @Override
            protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
                boolean result = super.drawChild(canvas, child, drawingTime);
                if (child == actionBar) {
                    parentLayout.drawHeaderShadow(canvas, actionBar.getMeasuredHeight());
                }
                return result;
            }

            @Override
            protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                int widthSize = MeasureSpec.getSize(widthMeasureSpec);
                int heightSize = MeasureSpec.getSize(heightMeasureSpec);

                setMeasuredDimension(widthSize, heightSize);
                heightSize -= getPaddingTop();

                measureChildWithMargins(actionBar, widthMeasureSpec, 0, heightMeasureSpec, 0);
                int actionBarHeight = actionBar.getMeasuredHeight();
                heightSize -= actionBarHeight;

                int keyboardSize = getKeyboardHeight();

                if (keyboardSize <= AndroidUtilities.dp(20) && !AndroidUtilities.isInMultiwindow) {
                    heightSize -= chatActivityEnterView.getEmojiPadding();
                }

                int childCount = getChildCount();

                measureChildWithMargins(chatActivityEnterView, widthMeasureSpec, 0, heightMeasureSpec, 0);
                inputFieldHeight = chatActivityEnterView.getMeasuredHeight();

                for (int i = 0; i < childCount; i++) {
                    View child = getChildAt(i);
                    if (child == null || child.getVisibility() == GONE || child == chatActivityEnterView || child == actionBar) {
                        continue;
                    }
                    if (child == chatListView ) {
                        int contentWidthSpec = MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY);
                        int contentHeightSpec = MeasureSpec.makeMeasureSpec(Math.max(AndroidUtilities.dp(10), heightSize - inputFieldHeight + AndroidUtilities.dp(2 + (chatActivityEnterView.isTopViewVisible() ? 48 : 0))), MeasureSpec.EXACTLY);
                        child.measure(contentWidthSpec, contentHeightSpec);
                    } else if (child == emptyViewContainer) {
                        int contentWidthSpec = MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY);
                        int contentHeightSpec = MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.EXACTLY);
                        child.measure(contentWidthSpec, contentHeightSpec);
                    } else if (chatActivityEnterView.isPopupView(child)) {
                        if (AndroidUtilities.isInMultiwindow) {
                            if (AndroidUtilities.isTablet()) {
                                child.measure(MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(Math.min(AndroidUtilities.dp(320), heightSize - inputFieldHeight + actionBarHeight - AndroidUtilities.statusBarHeight + getPaddingTop()), MeasureSpec.EXACTLY));
                            } else {
                                child.measure(MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(heightSize - inputFieldHeight + actionBarHeight - AndroidUtilities.statusBarHeight + getPaddingTop(), MeasureSpec.EXACTLY));
                            }
                        } else {
                            child.measure(MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(child.getLayoutParams().height, MeasureSpec.EXACTLY));
                        }
                    } else {
                        measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);
                    }
                }
            }

            @Override
            protected void onLayout(boolean changed, int l, int t, int r, int b) {
                final int count = getChildCount();

                int paddingBottom = getKeyboardHeight() <= AndroidUtilities.dp(20) && !AndroidUtilities.isInMultiwindow ? chatActivityEnterView.getEmojiPadding() : 0;
                setBottomClip(paddingBottom);
                for (int i = 0; i < count; i++) {
                    final View child = getChildAt(i);
                    if (child.getVisibility() == GONE) {
                        continue;
                    }
                    final LayoutParams lp = (LayoutParams) child.getLayoutParams();

                    final int width = child.getMeasuredWidth();
                    final int height = child.getMeasuredHeight();

                    int childLeft;
                    int childTop;

                    int gravity = lp.gravity;
                    if (gravity == -1) {
                        gravity = Gravity.TOP | Gravity.LEFT;
                    }

                    final int absoluteGravity = gravity & Gravity.HORIZONTAL_GRAVITY_MASK;
                    final int verticalGravity = gravity & Gravity.VERTICAL_GRAVITY_MASK;

                    switch (absoluteGravity & Gravity.HORIZONTAL_GRAVITY_MASK) {
                        case Gravity.CENTER_HORIZONTAL:
                            childLeft = (r - l - width) / 2 + lp.leftMargin - lp.rightMargin;
                            break;
                        case Gravity.RIGHT:
                            childLeft = r - width - lp.rightMargin;
                            break;
                        case Gravity.LEFT:
                        default:
                            childLeft = lp.leftMargin;
                    }

                    switch (verticalGravity) {
                        case Gravity.TOP:
                            childTop = lp.topMargin + getPaddingTop();
                            if (child != actionBar) {
                                childTop += actionBar.getMeasuredHeight();
                            }
                            break;
                        case Gravity.CENTER_VERTICAL:
                            childTop = ((b - paddingBottom) - t - height) / 2 + lp.topMargin - lp.bottomMargin;
                            break;
                        case Gravity.BOTTOM:
                            childTop = ((b - paddingBottom) - t) - height - lp.bottomMargin;
                            break;
                        default:
                            childTop = lp.topMargin;
                    }

                    if (child == emptyViewContainer) {
                        childTop -= inputFieldHeight / 2 - actionBar.getMeasuredHeight() / 2;
                    } else if (chatActivityEnterView.isPopupView(child)) {
                        if (AndroidUtilities.isInMultiwindow) {
                            childTop = chatActivityEnterView.getTop() - child.getMeasuredHeight() + AndroidUtilities.dp(1);
                        } else {
                            childTop = chatActivityEnterView.getBottom();
                        }
                    }   else if (child == chatListView  ) {
                        if (chatActivityEnterView.isTopViewVisible()) {
                            childTop -= AndroidUtilities.dp(48);
                        }
                    } else if (child == actionBar) {
                        childTop -= getPaddingTop();
                    }
                    child.layout(childLeft, childTop, childLeft + width, childTop + height);
                }
                 notifyHeightChanged();
            }
        };

        SizeNotifierFrameLayout contentView = (SizeNotifierFrameLayout) fragmentView;

        contentView.setBackgroundImage(Theme.getCachedWallpaper());

        emptyViewContainer = new FrameLayout(context);
        emptyViewContainer.setVisibility(View.INVISIBLE);
        contentView.addView(emptyViewContainer, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER));
        emptyViewContainer.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });


        chatListView = new RecyclerListView(context) {
            @Override
            protected void onLayout(boolean changed, int l, int t, int r, int b) {
                super.onLayout(changed, l, t, r, b);
//                forceScrollToTop = false;

            }
        };
        chatListView.setTag(1);
        chatListView.setVerticalScrollBarEnabled(true);

        chatListView.setClipToPadding(false);
        chatListView.setPadding(0, AndroidUtilities.dp(4), 0, AndroidUtilities.dp(3));
        chatListView.setItemAnimator(null);
        chatListView.setLayoutAnimation(null);
        chatLayoutManager = new ir.nogram.messanger.support.widget.LinearLayoutManager(context) {
            @Override
            public boolean supportsPredictiveItemAnimations() {
                return false;
            }
        };
        chatLayoutManager.setOrientation(ir.nogram.messanger.support.widget.LinearLayoutManager.VERTICAL);
        chatLayoutManager.setStackFromEnd(false);
        chatListView.setLayoutManager(chatLayoutManager);

        contentView.addView(chatListView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        chatActivityEnterView = new ChatActivityEnterView(getParentActivity(), contentView, null, true);
        chatActivityEnterView.setDialogId(messageObjectt.getDialogId());
        chatActivityEnterView.setAllowStickersAndGifs(false , false);
        editText = chatActivityEnterView.messageEditText ;
        type = new ChatActivity(null).getMessageType(messageObjectt) ;
        if(type == 3){
            limited = false ;
            editText.setText(messageObjectt.messageText);
        }else{
            InputFilter[] fa= new InputFilter[1];
            fa[0] = new InputFilter.LengthFilter(200);
            editText.setFilters(fa);
            if(messageObjectt.caption != null){
                editText.setText(this.messageObjectt.caption);
//                messageObjectt.caption = null ;
            }
        }
//        chatActivityEnterView.hideSendAudio();
        contentView.addView(chatActivityEnterView, contentView.getChildCount() - 1, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.BOTTOM));

        chatActivityEnterView.setDelegate(new ChatActivityEnterView.ChatActivityEnterViewDelegate() {
            @Override
            public void onMessageSend(CharSequence message) {

            }

            @Override
            public void needSendTyping() {

            }

            @Override
            public void onTextChanged(CharSequence text, boolean bigChange) {

            }

            @Override
            public void onAttachButtonHidden() {

            }

            @Override
            public void onAttachButtonShow() {

            }

            @Override
            public void onWindowSizeChanged(int size) {

            }

            @Override
            public void onStickersTab(boolean opened) {

            }

            @Override
            public void onMessageEditEnd(boolean loading) {

            }

            @Override
            public void didPressedAttachButton() {

            }



            @Override
            public void needStartRecordVideo(int state) {

            }

            @Override
            public void needChangeVideoPreviewState(int state, float seekProgress) {

            }

            @Override
            public void onSwitchRecordMode(boolean video) {

            }

            @Override
            public void onPreAudioVideoRecord() {

            }

            @Override
            public void needStartRecordAudio(int state) {

            }

            @Override
            public void needShowMediaBanHint() {

            }
        });

        chatActivityEnterView.audioSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        chatActivityEnterView.audioSendButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return false;
            }
        });
        chatActivityEnterView.audioSendButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                return false;
            }
        });
        chatActivityEnterView.sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                MessageObject messageObject = new MessageObject(message(messageObjectt.messageOwner) ,null , true) ;

                if(type != 3){
                    messageObject.messageOwner.media.caption = editText.getText().toString() ;
                    messageObject.caption = editText.getText().toString() +"" ;
                }else{
                    messageObject.messageOwner.message = editText.getText().toString() ;
                }
                SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences(HoldConst.PREF_MAIN, Activity.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean(HoldConst.PREF_DIRECT_SHARE_QUATE, false).apply();
                showDialog(new ShareAlertAdvanced(context, messageObject , null, false , null , type , editText.getText().toString() +""));

            }
        });

        ArrayList<MessageObject> messageObjects = new ArrayList<>() ;
        messageObjects.add(messageObjectt) ;

        ChatActivityAdapter chatAdapter = new ChatActivityAdapter(getParentActivity() , messageObjects ) ;
        chatListView.setAdapter(chatAdapter);
//       frameLayout = (FrameLayout) fragmentView;
         return  fragmentView ;
    }
    MessageObject backUpMessageObject ;
    LinearLayout layout_top ;
    private RecyclerListView chatListView;
    private ir.nogram.messanger.support.widget.LinearLayoutManager chatLayoutManager;
    boolean limited = true;
    int type ;

    private void initialWidget() {
//        edtCap = (EditText) view.findViewById(R.id.edt_advanced) ;
//        type = new ChatActivity(null).getMessageType(messageObjectt) ;
//        if(type == 3){
//            limited = false ;
//            edtCap.setText(messageObjectt.messageText);
//        }else{
//            InputFilter[] fa= new InputFilter[1];
//            fa[0] = new InputFilter.LengthFilter(200);
//            edtCap.setFilters(fa);
//            if(messageObjectt.caption != null){
//                edtCap.setText(this.messageObjectt.caption);
//                 messageObjectt.caption = null ;
//            }
//        }
//
//        layout_top = (LinearLayout) view.findViewById(R.id.layout_top) ;


    }

    public BaseFragment parentFragment = null;


    public class ChatActivityAdapter extends ir.nogram.messanger.support.widget.RecyclerView.Adapter {

        private Context mContext;

        ArrayList<MessageObject> messageObjects ;
        public ChatActivityAdapter(Context context , ArrayList<MessageObject> messageObjects) {
            mContext = context;
            this.messageObjects = messageObjects;
        }


        private class Holder extends ir.nogram.messanger.support.widget.RecyclerView.ViewHolder {

            public Holder(View itemView) {
                super(itemView);
            }
        }

        @Override
        public int getItemCount() {
            return messageObjects.size();
        }

        @Override
        public long getItemId(int i) {
            return messageObjects.get(i).getId() ;
        }

        @Override
        public ir.nogram.messanger.support.widget.RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view;
                if(type != 3){
                    view = new ChatMessageCell(mContext);
                    ChatMessageCell chatMessageCell = (ChatMessageCell) view;
                    chatMessageCell.setDelegate(new ChatMessageCell.ChatMessageCellDelegate() {
                        @Override
                        public void didPressedUserAvatar(ChatMessageCell cell, TLRPC.User user) {

                        }

                        @Override
                        public void didLongPressedUserAvatar(ChatMessageCell cell, TLRPC.User user) {

                        }

                        @Override
                        public void didPressedViaBot(ChatMessageCell cell, String username) {

                        }

                        @Override
                        public void didPressedChannelAvatar(ChatMessageCell cell, TLRPC.Chat chat, int postId) {

                        }

                        @Override
                        public void didPressedCancelSendButton(ChatMessageCell cell) {

                        }

                        @Override
                        public void didLongPressed(ChatMessageCell cell) {

                        }

                        @Override
                        public void didPressedReplyMessage(ChatMessageCell cell, int id) {

                        }

                        @Override
                        public void didPressedUrl(MessageObject messageObject, CharacterStyle url, boolean longPress) {

                        }


                        @Override
                        public void needOpenWebView(String url, String title, String description, String originalUrl, int w, int h) {

                        }

                        @Override
                        public void didPressedImage(ChatMessageCell cell) {

                        }

                        @Override
                        public void didPressedShare(ChatMessageCell cell) {

                        }

                        @Override
                        public void didPressedFave(ChatMessageCell cell) {

                        }

                        @Override
                        public void didPressedOther(ChatMessageCell cell) {

                        }

                        @Override
                        public void didPressedBotButton(ChatMessageCell cell, TLRPC.KeyboardButton button) {

                        }

                        @Override
                        public void didPressedInstantButton(ChatMessageCell cell, int type) {

                        }

                        @Override
                        public boolean needPlayMessage(MessageObject messageObject) {
                            return false;
                        }



                        @Override
                        public boolean canPerformActions() {
                            return false;
                        }
                    });
                    view.setLayoutParams(new ir.nogram.messanger.support.widget.RecyclerView.LayoutParams(ir.nogram.messanger.support.widget.RecyclerView.LayoutParams.MATCH_PARENT, ir.nogram.messanger.support.widget.RecyclerView.LayoutParams.WRAP_CONTENT));

                }else{
                    view = new EmptyCell(mContext) ;
                }
               return new Holder(view);
        }

        @Override
        public void onBindViewHolder(ir.nogram.messanger.support.widget.RecyclerView.ViewHolder holder, int position) {
                if(type != 3){
                    MessageObject message = messageObjects.get(position);
                    message.caption = null ;
                    View view = holder.itemView;
                    ( (ChatMessageCell)view).setMessageObject(message , false , false);
                    SharedPreferences sharedPreferences = ApplicationLoader.applicationContext.getSharedPreferences(HoldConst.PREF_MAIN , Context.MODE_PRIVATE) ;
                    String cap = sharedPreferences.getString(HoldConst.PREF_CAPTION , null) ;
                    if(cap != null && !cap.equals("null")){
                        message.caption  = cap ;
                    }

                }

            }


        @Override
        public int getItemViewType(int position) {
            return 0 ;
        }

        @Override
        public void onViewAttachedToWindow(ir.nogram.messanger.support.widget.RecyclerView.ViewHolder holder) {
            if (holder.itemView instanceof ChatMessageCell) {
                final ChatMessageCell messageCell = (ChatMessageCell) holder.itemView;
                messageCell.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        messageCell.getViewTreeObserver().removeOnPreDrawListener(this);

                        int height = chatListView.getMeasuredHeight();
                        int top = messageCell.getTop();
                        int bottom = messageCell.getBottom();
                        int viewTop = top >= 0 ? 0 : -top;
                        int viewBottom = messageCell.getMeasuredHeight();
                        if (viewBottom > height) {
                            viewBottom = viewTop + height;
                        }
                        messageCell.setVisiblePart(viewTop, viewBottom - viewTop);
                        return true;
                    }
                });
//                messageCell.setHighlighted(highlightMessageId != Integer.MAX_VALUE && messageCell.getMessageObject().getId() == highlightMessageId);
            }
        }

//        public void updateRowWithMessageObject(MessageObject messageObject) {
//            int index = messages.indexOf(messageObject);
//            if (index == -1) {
//                return;
//            }
//            notifyItemChanged(messagesStartRow + messages.size() - index - 1);
//        }

        @Override
        public void notifyDataSetChanged() {
//            updateRows();
            try {
                super.notifyDataSetChanged();
            } catch (Exception e) {
                FileLog.e("tmessages", e);
            }
        }

        @Override
        public void notifyItemChanged(int position) {
//            updateRows();
            try {
                super.notifyItemChanged(position);
            } catch (Exception e) {
                FileLog.e("tmessages", e);
            }
        }

        @Override
        public void notifyItemRangeChanged(int positionStart, int itemCount) {
//            updateRows();
            try {
                super.notifyItemRangeChanged(positionStart, itemCount);
            } catch (Exception e) {
                FileLog.e("tmessages", e);
            }
        }

        @Override
        public void notifyItemInserted(int position) {
//            updateRows();
            try {
                super.notifyItemInserted(position);
            } catch (Exception e) {
                FileLog.e("tmessages", e);
            }
        }

        @Override
        public void notifyItemMoved(int fromPosition, int toPosition) {
//            updateRows();
            try {
                super.notifyItemMoved(fromPosition, toPosition);
            } catch (Exception e) {
                FileLog.e("tmessages", e);
            }
        }

        @Override
        public void notifyItemRangeInserted(int positionStart, int itemCount) {
//            updateRows();
            try {
                super.notifyItemRangeInserted(positionStart, itemCount);
            } catch (Exception e) {
                FileLog.e("tmessages", e);
            }
        }

        @Override
        public void notifyItemRemoved(int position) {
//            updateRows();
            try {
                super.notifyItemRemoved(position);
            } catch (Exception e) {
                FileLog.e("tmessages", e);
            }
        }

        @Override
        public void notifyItemRangeRemoved(int positionStart, int itemCount) {
//            updateRows();
            try {
                super.notifyItemRangeRemoved(positionStart, itemCount);
            } catch (Exception e) {
                FileLog.e("tmessages", e);
            }
        }
    }

    private TLRPC.Message message(TLRPC.Message message){
        if (message == null)
        {
            return null;
        }
        TLRPC.Message  messageret = new TLRPC.Message();
        if ((message instanceof TLRPC.TL_message)){
            messageret.id = message.id;
            messageret.from_id = message.from_id;
            messageret.to_id = message.to_id;
            messageret.date = message.date;
            messageret.action = message.action;
            messageret.reply_to_msg_id = message.reply_to_msg_id;
            messageret.fwd_from = message.fwd_from;
            messageret.reply_to_random_id = message.reply_to_random_id;
            messageret.via_bot_name = message.via_bot_name;
            messageret.edit_date = message.edit_date;
            messageret.silent = message.silent;
            messageret.message = message.message;
            if (message.media != null) {
                messageret.media = media(message.media);
            }
            messageret.flags = message.flags;
            messageret.mentioned = message.mentioned;
            messageret.media_unread = message.media_unread;
            messageret.out = message.out;
            messageret.unread = message.unread;
            messageret.entities = message.entities;
            messageret.reply_markup = message.reply_markup;
            messageret.views = message.views;
            messageret.via_bot_id = message.via_bot_id;
            messageret.send_state = message.send_state;
            messageret.fwd_msg_id = message.fwd_msg_id;
            messageret.attachPath = message.attachPath;
            messageret.params = message.params;
            messageret.random_id = message.random_id;
            messageret.local_id = message.local_id;
            messageret.dialog_id = message.dialog_id;
            messageret.ttl = message.ttl;
            messageret.destroyTime = message.destroyTime;
            messageret.layer = message.layer;
            messageret.seq_in = message.seq_in;
            messageret.seq_out = message.seq_out;
             messageret.replyMessage = message.replyMessage;
        }
        return messageret ;
    }

    private TLRPC.MessageMedia media(TLRPC.MessageMedia paramMessageMedia) {
        TLRPC.MessageMedia   localObject = new TLRPC.MessageMedia();
              if ((paramMessageMedia instanceof TLRPC.TL_messageMediaUnsupported_old)){
                localObject = new TLRPC.TL_messageMediaUnsupported_old();
             }
        if ((paramMessageMedia instanceof TLRPC.TL_messageMediaAudio_layer45))
        {
            localObject = new TLRPC.TL_messageMediaAudio_layer45();

        }
        if ((paramMessageMedia instanceof TLRPC.TL_messageMediaPhoto_old))
        {
            localObject = new TLRPC.TL_messageMediaPhoto_old();

        }
        if ((paramMessageMedia instanceof TLRPC.TL_messageMediaUnsupported))
        {
            localObject = new TLRPC.TL_messageMediaUnsupported();

        }
        if ((paramMessageMedia instanceof TLRPC.TL_messageMediaEmpty))
        {
            localObject = new TLRPC.TL_messageMediaEmpty();
        }
        if ((paramMessageMedia instanceof TLRPC.TL_messageMediaVenue))
        {
            localObject = new TLRPC.TL_messageMediaVenue();
        }
        if ((paramMessageMedia instanceof TLRPC.TL_messageMediaVideo_old))
        {
            localObject = new TLRPC.TL_messageMediaVideo_old();
        }
        if ((paramMessageMedia instanceof TLRPC.TL_messageMediaDocument_old))
        {
            localObject = new TLRPC.TL_messageMediaDocument_old();
        }
        if ((paramMessageMedia instanceof TLRPC.TL_messageMediaDocument))
        {
            localObject = new TLRPC.TL_messageMediaDocument();
        }
        if ((paramMessageMedia instanceof TLRPC.TL_messageMediaContact))
        {
            localObject = new TLRPC.TL_messageMediaContact();
        }
        if ((paramMessageMedia instanceof TLRPC.TL_messageMediaPhoto))
        {
            localObject = new TLRPC.TL_messageMediaPhoto();
        }
        if ((paramMessageMedia instanceof TLRPC.TL_messageMediaVideo_layer45))
        {
            localObject = new TLRPC.TL_messageMediaVideo_layer45();
        }
        if ((paramMessageMedia instanceof TLRPC.TL_messageMediaWebPage))
        {
            localObject = new TLRPC.TL_messageMediaWebPage();
        }
        if ((paramMessageMedia instanceof TLRPC.TL_messageMediaGeo))
        {
            localObject = new TLRPC.TL_messageMediaGeo();
        }
            localObject.bytes = paramMessageMedia.bytes;
            localObject.caption = paramMessageMedia.caption;
            localObject.photo = paramMessageMedia.photo;
            localObject.audio_unused = paramMessageMedia.audio_unused;
            localObject.geo = paramMessageMedia.geo;
            localObject.title = paramMessageMedia.title;
            localObject.address = paramMessageMedia.address;
            localObject.provider = paramMessageMedia.provider;
            localObject.venue_id = paramMessageMedia.venue_id;
            localObject.document = paramMessageMedia.document;
            localObject.video_unused = paramMessageMedia.video_unused;
            localObject.phone_number = paramMessageMedia.phone_number;
            localObject.first_name = paramMessageMedia.first_name;
            localObject.last_name = paramMessageMedia.last_name;
            localObject.user_id = paramMessageMedia.user_id;
            localObject.webpage = paramMessageMedia.webpage;
        return localObject;
    }

}
