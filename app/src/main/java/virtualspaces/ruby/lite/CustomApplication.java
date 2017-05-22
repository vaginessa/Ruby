package virtualspaces.ruby.lite;

import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by LUCASVENTURES on 5/18/2016.
 */
public class CustomApplication extends Application {


    @Override
    public void onCreate() {
        super.onCreate();

        SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(this);
        if (preferences.getString("token", null) != null) {
            Intent intent = new Intent(this, MyLauncher.class);
            startActivity(intent);
        }


    }
}
