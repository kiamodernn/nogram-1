package ir.nogram.users.activity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import ir.nogram.messanger.NotificationCenter;
import ir.nogram.messanger.R;
import ir.nogram.ui.ActionBar.ActionBar;
import ir.nogram.ui.ActionBar.ActionBarMenu;
import ir.nogram.ui.ActionBar.BaseFragment;
import ir.nogram.ui.Components.LayoutHelper;
import ir.nogram.ui.ThemeManager;
import ir.nogram.users.activity.colorPicker.ColorPickerPalette;
import ir.nogram.users.activity.colorPicker.ColorPickerSwatch;

/**
 * Created by MhkDeveloper on 2016-10-10.
 */
public class ActivityColorTheme extends BaseFragment implements NotificationCenter.NotificationCenterDelegate , ColorPickerSwatch.OnColorSelectedListener {
    static Context context;
    ColorPickerPalette mPallette ;

     public ActivityColorTheme( ) {
    }

    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.didChangedColorTheme);
    }

    @Override
    public boolean onFragmentCreate() {
        super.onFragmentCreate();
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.didChangedColorTheme);
        swipeBackEnabled = true;
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
    }
    View view;
    @Override
    public View createView(final Context context) {
        this.context = context;
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle(context.getString(R.string.changeColor));
//        actionBar.setBackgroundColor(ThemeManager.getColor());
        ActionBarMenu menu = actionBar.createMenu();
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });
//        menu.addItem(2 , R.drawable.ic_del) ;

        fragmentView = new FrameLayout(context);
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view =layoutInflater.inflate(R.layout.activity_change_color_theme , null) ;
        ((FrameLayout)fragmentView).addView(view);
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) view.getLayoutParams();
        layoutParams.width = LayoutHelper.MATCH_PARENT;
        layoutParams.height = LayoutHelper.MATCH_PARENT;
        view.setLayoutParams(layoutParams);
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        mPallette = (ColorPickerPalette) view.findViewById(R.id.welcome_themes);
        mPallette.init(114, 6, this);
//        mPallette.drawPalette(ThemeManager.PALETTE, ThemeManager.getColor());

        return fragmentView;
    }
    @Override
    public void didReceivedNotification(int id, Object... args) {

    }


    @Override
    public void onColorSelected(int color) {
        ThemeManager.setColor(context, color);
        mPallette.drawPalette(ThemeManager.PALETTE, color);
        actionBar.setBackgroundColor(color);
        NotificationCenter.getInstance().postNotificationName(NotificationCenter.didChangedColorTheme);
    }
}
