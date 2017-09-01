package ir.nogram.users.activity.holder;

/**
 * Created by MhkDeveloper on 2016-10-05.
 */
public class HoldChangeContact {

    public int user_id ;
    public int id ;
    public int type ;
    public int date ;

    public HoldChangeContact(int user_id, int id, int type , int date) {
        this.user_id = user_id;
        this.id = id;
        this.type = type;
        this.date = date;
    }
}
