package ir.nogram.users.activity.helper; /**
 * www.iranjavaref.ir
 */

import java.util.Calendar;

import ir.nogram.users.activity.holder.HoldDate;


public class ConvertDate {

    public static String gregorian_to_jalali(int gYear, int gMonth, int gDayOfMonth) {

        String d = "";
        try {
            CalendarTool calendarToolCurrent = new CalendarTool();
            CalendarTool calendarTool = new CalendarTool(gYear, gMonth + 1, gDayOfMonth);

            int currentIraniYear = calendarToolCurrent.getIranianYear();
            int IraniYear = calendarTool.getIranianYear();

            d += calendarTool.getIranianWeekDayStr();
            d += " " + calendarTool.getIranianDay() + " " + calendarTool.getIranianMonthStr();
            if (currentIraniYear != IraniYear) {
                d += " " + calendarTool.getIranianYear();
            }
        } catch (Exception e) {
            d = "-";
        }
        return d;

    }

    public static String gregorian_to_jalali_dialog(long date) {
        Calendar rightNow = Calendar.getInstance();
        int day = rightNow.get(Calendar.DAY_OF_YEAR);
        rightNow.setTimeInMillis(date);
        int dateYear = rightNow.get(Calendar.YEAR);
        int dateMonth = rightNow.get(Calendar.MONTH);
        int dateDayOfMonth = rightNow.get(Calendar.DAY_OF_MONTH);
        String d = "";
        CalendarTool calendarTool = null;
        try {
            calendarTool = new CalendarTool(dateYear, dateMonth + 1, dateDayOfMonth);
            if (Math.abs(System.currentTimeMillis() - date) >= 31536000000L) {
                return calendarTool.getIranianYear() + "";
            } else {
                int dateDay = rightNow.get(Calendar.DAY_OF_YEAR);
                int dayDiff = dateDay - day;
                if (dayDiff == 0 || dayDiff == -1 && System.currentTimeMillis() - date < 60 * 60 * 8 * 1000) {
                    return String.format("%02d" , rightNow.get(Calendar.HOUR) )+ ":" + String.format("%02d" ,rightNow.get(Calendar.MINUTE));
                } else if (dayDiff > -7 && dayDiff <= -1) {
                    return calendarTool.getIranianWeekDayStr();
                } else {
                    return calendarTool.getIranianDay() + " " +calendarTool.getIranianMonthStr();
                }
            }

        } catch (Exception e) {
            d = "-";
        }
        return d;
    }

    public static HoldDate gregorian_to_jalaliByItem(int gYear, int gMonth, int gDayOfMonth) {
        HoldDate holdDate = null;
        try {
            CalendarTool calendarToolCurrent = new CalendarTool();
            CalendarTool calendarTool = new CalendarTool(gYear, gMonth + 1, gDayOfMonth);

//			int currentIraniYear = calendarToolCurrent.getIranianYear() ;
//			int  IraniYear = calendarTool.getIranianYear() ;
            holdDate = new HoldDate(calendarTool.getIranianDay(), calendarTool.getIranianMonth(), calendarTool.getIranianYear()
                    , calendarTool.getIranianMonthStr()
            );

        } catch (Exception e) {

        }
        return holdDate;

    }

    public static int numMonthByName(String monthName) {
        int num = 1;
        switch (monthName) {
            case "فروردین":
            case "March":
                num = 1;
                break;
            case "اردیبهشت":
            case "April":
                num = 2;
                break;
            case "خرداد":
            case "May":
                num = 3;
                break;
            case "تیر":
            case "June":
                num = 4;
                break;
            case "مرداد":
            case "July":
                num = 5;
                break;
            case "شهریور":
            case "August":
                num = 6;
                break;
            case "مهر":
            case "September":
                num = 7;
                break;
            case "آبان":
            case "October":
                num = 8;
                break;
            case "آذر":
            case "November":
                num = 9;
                break;
            case "دی":
            case "December":
                num = 10;
                break;
            case "بهمن":
            case "January":
                num = 11;
                break;
            case "اسفند":
            case "February":
                num = 12;
                break;


        }
        return num;
    }

}
