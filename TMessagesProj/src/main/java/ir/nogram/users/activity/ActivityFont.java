package ir.nogram.users.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import ir.nogram.messanger.ApplicationLoader;
import ir.nogram.messanger.LocaleController;
import ir.nogram.messanger.NotificationCenter;
import ir.nogram.messanger.R;
import ir.nogram.ui.ActionBar.ActionBar;
import ir.nogram.ui.ActionBar.ActionBarMenu;
import ir.nogram.ui.ActionBar.BaseFragment;
import ir.nogram.ui.Components.LayoutHelper;
import ir.nogram.ui.ThemeManager;

/**
 * Created by MhkDeveloper on 2016-10-10.
 */
public class ActivityFont extends BaseFragment implements NotificationCenter.NotificationCenterDelegate, View.OnClickListener {
    static Context context;
    View view;
     private  TextView
            txt_font_default_tel,
            txt_font_faraz,
            txt_font_iran_sans,
            txt_font_iran_sans_bold,
            txt_font_iran_sans_light,
            txt_font_iran_sans_medium,
            txt_font_iran_sans_ultra_light,
            txt_font_bnazanin,
            txt_font_byekan;

    String defaultFont = ThemeManager.getFont();
    String Bnazanin = "fonts/BNazaninBold.ttf";
    String BYekan = "fonts/BYekan.ttf";
    String Far_Nazanin = "fonts/Far_Nazanin.ttf";
    String faraz = "fonts/faraz.ttf";
    String IRANSansMobile = "fonts/IRANSansMobile.ttf";
    String IRANSansMobile_Bold = "fonts/IRANSansMobile_Bold.ttf";
    String IRANSansMobile_Light = "fonts/IRANSansMobile_Light.ttf";
    String IRANSansMobile_Medium = "fonts/IRANSansMobile_Medium.ttf";
    String IRANSansIRANSansMobile_UltraLight = "fonts/IRANSansMobile_UltraLight.ttf";


    public ActivityFont() {
    }

    public void onFragmentDestroy() {
        super.onFragmentDestroy();
//        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.refreshTabs);

    }

    @Override
    public boolean onFragmentCreate() {
        super.onFragmentCreate();
//        NotificationCenter.getInstance().addObserver(this, NotificationCenter.refreshTabs);
        swipeBackEnabled = true;
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
    }


    @Override
    public View createView(final Context context) {
        this.context = context;
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle(context.getString(R.string.changeFont));
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
        view = layoutInflater.inflate(R.layout.activity_change_font, null);
        ((FrameLayout) fragmentView).addView(view);
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

        initialWidget();
        return fragmentView;
    }

    private void initialWidget() {
        txt_font_default_tel = (TextView) view.findViewById(R.id.txt_font_default_tel);
        txt_font_faraz = (TextView) view.findViewById(R.id.txt_font_faraz);
        txt_font_iran_sans = (TextView) view.findViewById(R.id.txt_font_iran_sans);
        txt_font_iran_sans_bold = (TextView) view.findViewById(R.id.txt_font_iran_sans_bold);
        txt_font_iran_sans_light = (TextView) view.findViewById(R.id.txt_font_iran_sans_light);
        txt_font_iran_sans_medium = (TextView) view.findViewById(R.id.txt_font_iran_sans_medium);
        txt_font_iran_sans_ultra_light = (TextView) view.findViewById(R.id.txt_font_iran_sans_ultra_light);
        txt_font_bnazanin = (TextView) view.findViewById(R.id.txt_font_bnazanin);
        txt_font_byekan = (TextView) view.findViewById(R.id.txt_font_byekan);

        txt_font_default_tel.setOnClickListener(this);
        txt_font_faraz.setOnClickListener(this);
        txt_font_iran_sans.setOnClickListener(this);
        txt_font_iran_sans_bold.setOnClickListener(this);
        txt_font_iran_sans_light.setOnClickListener(this);
        txt_font_iran_sans_medium.setOnClickListener(this);
        txt_font_iran_sans_ultra_light.setOnClickListener(this);
        txt_font_bnazanin.setOnClickListener(this);
        txt_font_byekan.setOnClickListener(this);

        txt_font_default_tel.setTypeface(getTypeFace(defaultFont));
        txt_font_faraz.setTypeface(getTypeFace(faraz));
        txt_font_iran_sans.setTypeface(getTypeFace(IRANSansMobile));
        txt_font_iran_sans_bold.setTypeface(getTypeFace(IRANSansMobile_Bold));
        txt_font_iran_sans_light.setTypeface(getTypeFace(IRANSansMobile_Light));
        txt_font_iran_sans_medium.setTypeface(getTypeFace(IRANSansMobile_Medium));
        txt_font_iran_sans_ultra_light.setTypeface(getTypeFace(IRANSansIRANSansMobile_UltraLight));
        txt_font_bnazanin.setTypeface(getTypeFace(Bnazanin));
        txt_font_byekan.setTypeface(getTypeFace(BYekan));


        setSelectedBack(ThemeManager.getFont());
    }

    @Override
    public void didReceivedNotification(int id, Object... args) {

    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        String tag = (String) view.getTag();
        ThemeManager.setFont(tag);
        Toast.makeText(context, LocaleController.getString("exit_to_cahnge", R.string.exit_to_cahnge), Toast.LENGTH_LONG).show();
        setSelectedBack(tag);
        showAlertExit() ;
    }

    private void showAlertExit() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context) ;
        builder.setMessage(LocaleController.getString("exit_to_cahnge" , R.string.exit_to_cahnge)) ;
        builder.setPositiveButton(
                LocaleController.getString("OK", R.string.OK), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int p) {
//                        Intent intent = new Intent(context , LaunchActivity.class) ;
//                        intent.putExtra("exit" , "exit") ;
//                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        Intent i = context.getPackageManager()
                                .getLaunchIntentForPackage( context.getPackageName() );
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        System.exit(1);
                        context.startActivity(i);
                    }
                }
        );
        builder.setCancelable(false) ;
        builder.show() ;
    }

    private void setSelectedBack(String tag) {

        int green;
        int normal;
        if (Build.VERSION.SDK_INT >= 23) {
            green = ContextCompat.getColor(context, R.color.green_light);
            normal = ContextCompat.getColor(context, R.color.transparent);
        } else {
            green = context.getResources().getColor(R.color.green_light);
            normal = context.getResources().getColor(R.color.transparent);
        }
        txt_font_default_tel.setBackgroundColor(normal);
        txt_font_faraz.setBackgroundColor(normal);
        txt_font_iran_sans.setBackgroundColor(normal);
        txt_font_iran_sans_bold.setBackgroundColor(normal);
        txt_font_iran_sans_light.setBackgroundColor(normal);
        txt_font_iran_sans_medium.setBackgroundColor(normal);
        txt_font_iran_sans_ultra_light.setBackgroundColor(normal);
        txt_font_bnazanin.setBackgroundColor(normal);
        txt_font_byekan.setBackgroundColor(normal);


        TextView textviewSelected = (TextView) view.findViewWithTag(tag);
//        textviewSelected.setTextColor(Color.WHITE);
        textviewSelected.setBackgroundColor(green);
    }


//    @Override
//    public void onColorSelected(int color) {
//        ThemeManager.setColor(context, color);
//        mPallette.drawPalette(ThemeManager.PALETTE, color);
//        actionBar.setBackgroundColor(color);
//        NotificationCenter.getInstance().postNotificationName(NotificationCenter.didChangedColorTheme);
//    }

    private Typeface getTypeFace(String patch) {

        Typeface t = Typeface.createFromAsset(ApplicationLoader.applicationContext.getAssets(), patch);
        return t;

    }
}
