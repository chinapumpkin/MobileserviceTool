package oulu.university.smartglasses;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import org.linphone.core.*;

import java.util.logging.Handler;

import fi.tol.oulu.vgs4msc.R;

//I need to edit this one and combined it with my own activity
/**
 * Created by afirouzi on 10.9.2015.
 */
public class MainActivity extends Activity {
    public static Button ConnectionBtn;
    private Button MessageSettingBtn;

    @Override
    public void onCreate(Bundle savedInstanceState){
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);

        ConnectionBtn = (Button)findViewById(R.id.ConnectionBtn);
        MessageSettingBtn = (Button)findViewById(R.id.MessageSettingBtn);
        ConnectionBtn.getBackground().setColorFilter(Color.argb(255, 150, 100, 200), PorterDuff.Mode.DARKEN);
        MessageSettingBtn.getBackground().setColorFilter(Color.argb(255, 150, 100, 200), PorterDuff.Mode.DARKEN);

        ConnectionBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent bleIntent = new Intent(MainActivity.this, DeviceScanActivity.class);
                MainActivity.this.startActivity(bleIntent);
            }
        });

        MessageSettingBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent messageSettingIntent = new Intent(MainActivity.this, MessageSetting.class);
                MainActivity.this.startActivity(messageSettingIntent);
            }
        });
    }

    @Override
    public void onResume(){
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
    }

}
