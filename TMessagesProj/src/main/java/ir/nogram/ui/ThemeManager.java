package ir.nogram.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Typeface;

import ir.nogram.messanger.ApplicationLoader;
import ir.nogram.users.activity.holder.HoldConst;


/**
 * Created by MhkDeveloper on 04/03/2017.
 */
public class ThemeManager {

    public static Context mContext ;
    public static SharedPreferences mPrefs;
    private static int mColor;
    private static Resources mResources;
    private static int mActiveColor;
    private static String mActiveFont;
    public static final int DEFAULT_COLOR = 0xff009688;
    public static final String DEFAULT_FONT= "fonts/IRANSansMobile.ttf";
    // Colors copied from http://www.google.com/design/spec/style/color.html#color-ui-color-palette
    private static final int[][] COLORS = {{
            // Red
            0xfffde0dc, 0xfff9bdbb, 0xfff69988, 0xfff36c60,
            0xffe84e40, 0xffe51c23, 0xffdd191d, 0xffd01716,
            0xffc41411, 0xffb0120a
    }, {    // Pink
            0xfffce4ec, 0xfff8bbd0, 0xfff48fb1, 0xfff06292,
            0xffec407a, 0xffe91e63, 0xffd81b60, 0xffc2185b,
            0xffad1457, 0xff880e4f
    }, {    // Purple
            0xfff3e5f5, 0xffe1bee7, 0xffce93d8, 0xffba68c8,
            0xffab47bc, 0xff9c27b0, 0xff8e24aa, 0xff7b1fa2,
            0xff6a1b9a, 0xff4a148c
    }, {    // Deep Purple
            0xffede7f6, 0xffd1c4e9, 0xffb39ddb, 0xff9575cd,
            0xff7e57c2, 0xff673ab7, 0xff5e35b1, 0xff512da8,
            0xff4527a0, 0xff311b92
    }, {    // Indigo
            0xffe8eaf6, 0xffc5cae9, 0xff9fa8da, 0xff7986cb,
            0xff5c6bc0, 0xff3f51b5, 0xff3949ab, 0xff303f9f,
            0xff283593, 0xff1a237e
    }, {    // Blue
            0xffe7e9fd, 0xffd0d9ff, 0xffafbfff, 0xff91a7ff,
            0xff738ffe, 0xff5677fc, 0xff4e6cef, 0xff455ede,
            0xff3b50ce, 0xff2a36b1
    }, {    // Light Blue
            0xffe1f5fe, 0xffb3e5fc, 0xff81d4fa, 0xff4fc3f7,
            0xff29b6f6, 0xff03a9f4, 0xff039be5, 0xff0288d1,
            0xff0277bd, 0xff01579b
    }, {    // Cyan
            0xffe0f7fa, 0xffb2ebf2, 0xff80deea, 0xff4dd0e1,
            0xff26c6da, 0xff00bcd4, 0xff00acc1, 0xff0097a7,
            0xff00838f, 0xff006064
    }, {    // Teal
            0xffe0f2f1, 0xffb2dfdb, 0xff80cbc4, 0xff4db6ac,
            0xff26a69a, 0xff009688, 0xff00897b, 0xff00796b,
            0xff00695c, 0xff004d40
    }, {    // Green
            0xffd0f8ce, 0xffa3e9a4, 0xff72d572, 0xff42bd41,
            0xff2baf2b, 0xff259b24, 0xff0a8f08, 0xff0a7e07,
            0xff056f00, 0xff0d5302
    }, {    // Light Green
            0xfff1f8e9, 0xffdcedc8, 0xffc5e1a5, 0xffaed581,
            0xff9ccc65, 0xff8bc34a, 0xff7cb342, 0xff689f38,
            0xff558b2f, 0xff33691e
    }, {    // Lime
            0xfff9fbe7, 0xfff0f4c3, 0xffe6ee9c, 0xffdce775,
            0xffd4e157, 0xffcddc39, 0xffc0ca33, 0xffafb42b,
            0xff9e9d24, 0xff827717
    }, {    // Yellow
            0xfffffde7, 0xfffff9c4, 0xfffff59d, 0xfffff176,
            0xffffee58, 0xffffeb3b, 0xfffdd835, 0xfffbc02d,
            0xfff9a825, 0xfff57f17
    }, {    // Amber
            0xfffff8e1, 0xffffecb3, 0xffffe082, 0xffffd54f,
            0xffffca28, 0xffffc107, 0xffffb300, 0xffffa000,
            0xffff8f00, 0xffff6f00
    }, {    // Orange
            0xfffff3e0, 0xffffe0b2, 0xffffcc80, 0xffffb74d,
            0xffffa726, 0xffff9800, 0xfffb8c00, 0xfff57c00,
            0xffef6c00, 0xffe65100
    }, {    // Deep Orange
            0xfffbe9e7, 0xffffccbc, 0xffffab91, 0xffff8a65,
            0xffff7043, 0xffff5722, 0xfff4511e, 0xffe64a19,
            0xffd84315, 0xffbf360c
    }, {    // Brown
            0xffefebe9, 0xffd7ccc8, 0xffbcaaa4, 0xffa1887f,
            0xff8d6e63, 0xff795548, 0xff6d4c41, 0xff5d4037,
            0xff4e342e, 0xff3e2723
    }, {    // Grey
            0xfffafafa, 0xfff5f5f5, 0xffeeeeee, 0xffe0e0e0,
            0xffbdbdbd, 0xff9e9e9e, 0xff757575, 0xff616161,
            0xff424242, 0xff212121, 0xff000000, 0xffffffff
    }, {    // Blue Grey
            0xffeceff1, 0xffeceff1, 0xffb0bec5, 0xff90a4ae,
            0xff78909c, 0xff607d8b, 0xff546e7a, 0xff455a64,
            0xff37474f, 0xff263238
    }};


    /**
     * These are the colors that go in the initial palette.
     */
    public static final int[] PALETTE = {
            COLORS[0][7], // Red
            COLORS[0][6], // Red
            COLORS[0][5], // Red
            COLORS[0][4], // Red
            COLORS[0][3], // Red
            COLORS[0][2], // Red

            COLORS[1][7], // Pink
            COLORS[1][6], // Pink
            COLORS[1][5], // Pink
            COLORS[1][4], // Pink
            COLORS[1][3], // Pink
            COLORS[1][2], // Pink

            COLORS[2][7], // Purple
            COLORS[2][6], // Purple
            COLORS[2][5], // Purple
            COLORS[2][4], // Purple
            COLORS[2][3], // Purple
            COLORS[2][2], // Purple

            COLORS[3][7], // Deep purple
            COLORS[3][6], // Deep purple
            COLORS[3][5], // Deep purple
            COLORS[3][4], // Deep purple
            COLORS[3][3], // Deep purple
            COLORS[3][2], // Deep purple

            COLORS[4][7], // Indigo
            COLORS[4][6], // Indigo
            COLORS[4][5], // Indigo
            COLORS[4][4], // Indigo
            COLORS[4][3], // Indigo
            COLORS[4][2], // Indigo

            COLORS[5][7], // Blue
            COLORS[5][6], // Blue
            COLORS[5][5], // Blue
            COLORS[5][4], // Blue
            COLORS[5][3], // Blue
            COLORS[5][2], // Blue

            COLORS[6][7], // Light Blue
            COLORS[6][6], // Light Blue
            COLORS[6][5], // Light Blue
            COLORS[6][4], // Light Blue
            COLORS[6][3], // Light Blue
            COLORS[6][2], // Light Blue

            COLORS[7][7], // Cyan
            COLORS[7][6], // Cyan
            COLORS[7][5], // Cyan
            COLORS[7][4], // Cyan
            COLORS[7][3], // Cyan
            COLORS[7][2], // Cyan

            COLORS[8][7], // Teal
            COLORS[8][6], // Teal
            COLORS[8][5], // Teal
            COLORS[8][4], // Teal
            COLORS[8][3], // Teal
            COLORS[8][2], // Teal

            COLORS[9][7], // Green
            COLORS[9][6], // Green
            COLORS[9][5], // Green
            COLORS[9][4], // Green
            COLORS[9][3], // Green
            COLORS[9][2], // Green

            COLORS[10][7], // Light Green
            COLORS[10][6], // Light Green
            COLORS[10][5], // Light Green
            COLORS[10][4], // Light Green
            COLORS[10][3], // Light Green
            COLORS[10][2], // Light Green

            COLORS[11][7], // Lime
            COLORS[11][6], // Lime
            COLORS[11][5], // Lime
            COLORS[11][4], // Lime
            COLORS[11][3], // Lime
            COLORS[11][2], // Lime

            COLORS[12][7], // Yellow
            COLORS[12][6], // Yellow
            COLORS[12][5], // Yellow
            COLORS[12][4], // Yellow
            COLORS[12][3], // Yellow
            COLORS[12][2], // Yellow

            COLORS[13][7], // Amber
            COLORS[13][6], // Amber
            COLORS[13][5], // Amber
            COLORS[13][4], // Amber
            COLORS[13][3], // Amber
            COLORS[13][2], // Amber

            COLORS[14][7], // Orange
            COLORS[14][6], // Orange
            COLORS[14][5], // Orange
            COLORS[14][4], // Orange
            COLORS[14][3], // Orange
            COLORS[14][2], // Orange

            COLORS[15][7], // Deep Orange
            COLORS[15][6], // Deep Orange
            COLORS[15][5], // Deep Orange
            COLORS[15][4], // Deep Orange
            COLORS[15][3], // Deep Orange
            COLORS[15][2], // Deep Orange

            COLORS[16][7], // Brown
            COLORS[16][6], // Brown
            COLORS[16][5], // Brown
            COLORS[16][4], // Brown
            COLORS[16][3], // Brown
            COLORS[16][2], // Brown

            COLORS[17][7], // Grey
            COLORS[17][6], // Grey
            COLORS[17][5], // Grey
            COLORS[17][4], // Grey
            COLORS[17][3], // Grey
            COLORS[17][2], // Grey

            COLORS[18][7], // Blue Grey
            COLORS[18][6], // Blue Grey
            COLORS[18][5], // Blue Grey
            COLORS[18][4] ,// Blue Grey
            COLORS[18][3] , // Blue Grey
            COLORS[18][2] // Blue Grey
    };
    public static int getColor(){
        return mActiveColor ;
    }
    public static void setColor(Context context, int color) {
        mActiveColor = color ;
        mPrefs.edit().putInt(HoldConst.PREF_BackColor , color).apply();
    }
    public static String getFont(){
        return mActiveFont ;
    }
    public static void setFont(String font) {
        mActiveFont = font ;
        mPrefs.edit().putString(HoldConst.PREF_DefaultFont , font).apply();
    }
    public static void init(Context context) {
        mContext = context ;
        mPrefs = ApplicationLoader.applicationContext.getSharedPreferences(HoldConst.PREF_MAIN , Context.MODE_PRIVATE) ;
        mResources = context.getResources() ;
        mColor = mPrefs.getInt(HoldConst.PREF_BackColor,  DEFAULT_COLOR);
        mActiveFont = mPrefs.getString(HoldConst.PREF_DefaultFont,  DEFAULT_FONT);
        mActiveColor = mColor;
    }

    public static Typeface getTypeFace(String assetPath){
        Typeface t = Typeface.createFromAsset(ApplicationLoader.applicationContext.getAssets(), assetPath);
        return  t ;
    }
}
