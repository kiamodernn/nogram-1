package ir.nogram.users.activity.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.TextView;

import ir.nogram.messanger.R;


public class CustomTextView extends TextView {

    private int DefalultfontSize = 18 ;
    private String DefaultFontName= "fonts/BNazaninBold.ttf";
    public CustomTextView(Context context) {
        super(context);
        setFontsSetting(context , null);

    }

    public CustomTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setFontsSetting(context , attrs);

    }

    public CustomTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setFontsSetting(context , attrs) ;

    }


    private void setFontsSetting(Context context , AttributeSet attrs){


        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CustomTextView);
        String fontName = a.getString(R.styleable.CustomTextView_fontName);
        Typeface t;
        if(fontName!=null)
            t= Typeface.createFromAsset(context.getAssets(),fontName);
        else
            t= Typeface.createFromAsset(context.getAssets(),DefaultFontName);

        this.setTypeface(t);


        int fontSize =   a.getInt(R.styleable.CustomTextView_fontSize , DefalultfontSize);

        this.setTextSize(TypedValue.COMPLEX_UNIT_DIP, fontSize);


        int fontColor =a.getColor(R.styleable.CustomTextView_fontColor,context.getResources().getColor(R.color.default_text_color)) ;
       if(fontColor != 0){
        this.setTextColor(fontColor);
       }
        a.recycle();
    }
//	public CustomTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
//		super(context, attrs, defStyleAttr, defStyleRes);
//	}
}
