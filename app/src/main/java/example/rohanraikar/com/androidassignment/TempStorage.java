package example.rohanraikar.com.androidassignment;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by rohan.raikar on 23/03/2017.
 */

public class TempStorage  {
    Context context;
    SharedPreferences myStore;
    public TempStorage(Context context) {
        this.context=context;
    }

    public void setMyStore(String storeName,String key,String value){
        myStore=context.getSharedPreferences(storeName,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=myStore.edit();
        editor.putString(key,value);
        editor.commit();
    }

    public String getValueFromMystore(String storeName,String key){
        myStore=context.getSharedPreferences(storeName,Context.MODE_PRIVATE);
        String value=myStore.getString(key,"");
        return value;
    }

}
