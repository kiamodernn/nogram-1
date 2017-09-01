package ir.nogram.users.activity.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.alirezaafkar.sundatepicker.DatePicker;
import com.alirezaafkar.sundatepicker.interfaces.DateSetListener;

import ir.nogram.messanger.R;

import java.util.Calendar;
import java.util.Date;


/**
 * Created by MhkDeveloper on 18/04/2017.
 */

public class CalendarMat extends Dialog {
    Date mDate ;
    Context context ;
    public CalendarMat(@NonNull Context context) {
        super(context);
        this.context = context ;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.dialog_calendar_search);

        DatePicker.Builder builder = new DatePicker
                .Builder()
                .theme(R.style.DialogTheme)
                .future(true);
        mDate = new Date();
        builder.date(mDate.getDay(), mDate.getMonth(), mDate.getYear());
        builder.build(new DateSetListener() {
            @Override
            public void onDateSet(int id, @Nullable Calendar calendar, int day, int month, int year) {

                //textView

            }
        }).show( ((AppCompatActivity)context).getSupportFragmentManager(), "");
    }
}
