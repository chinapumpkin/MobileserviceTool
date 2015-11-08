package oulu.university.smartglasses;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import java.math.BigInteger;

import fi.tol.oulu.vgs4msc.R;


/**
 * Created by afirouzi on 12.10.2015.
 */
public class MessageSetting extends Activity implements View.OnClickListener {
    EditText LeftCommand;
    EditText RightCommand;
    EditText ForwardCommand;
    EditText StopCommand;
    Button SaveSetting;
    Button SetLeft;
    Button SetRight;
    Button SetForward;
    Button SetStop;
    Button HexaBinaryButton;
    boolean TextSavedAsBinary = true;
    String DefaultCommandBinary = "0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000";


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.message_setting);
        getActionBar().setTitle("Set the commands pattern");


        LeftCommand = (EditText) findViewById(R.id.left_text);
        RightCommand = (EditText) findViewById(R.id.right_text);
        ForwardCommand = (EditText) findViewById(R.id.forward_text);
        StopCommand = (EditText) findViewById(R.id.stop_text);
        SaveSetting = (Button) findViewById(R.id.save_setting);
        SetLeft = (Button) findViewById(R.id.set_setting_left);
        SetLeft.setOnClickListener(this);
        SetRight = (Button) findViewById(R.id.set_setting_right);
        SetRight.setOnClickListener(this);
        SetForward = (Button) findViewById(R.id.set_setting_forward);
        SetForward.setOnClickListener(this);
        SetStop = (Button) findViewById(R.id.set_setting_stop);
        SetStop.setOnClickListener(this);


        SharedPreferences loadSharedPreferences = getSharedPreferences("SettingData", Context.MODE_PRIVATE);
        LeftCommand.setText(loadSharedPreferences.getString("LeftCommand", DefaultCommandBinary));
        RightCommand.setText(loadSharedPreferences.getString("RightCommand", DefaultCommandBinary));
        ForwardCommand.setText(loadSharedPreferences.getString("ForwardCommand", DefaultCommandBinary));
        StopCommand.setText(loadSharedPreferences.getString("StopCommand", DefaultCommandBinary));
        TextSavedAsBinary = loadSharedPreferences.getBoolean("TextSavedAsBinary",true);


        SaveSetting.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                SharedPreferences saveSharedPreferences = getSharedPreferences("SettingData", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = saveSharedPreferences.edit();
                editor.putString("LeftCommand", LeftCommand.getText().toString());
                editor.putString("RightCommand", RightCommand.getText().toString());
                editor.putString("ForwardCommand", ForwardCommand.getText().toString());
                editor.putString("StopCommand", StopCommand.getText().toString());
                editor.putBoolean("TextSavedAsBinary", TextSavedAsBinary);
                editor.commit();
                Toast.makeText(MessageSetting.this, "Data is saved successfully ", Toast.LENGTH_LONG).show();
            }
        });

        HexaBinaryButton = (Button) findViewById(R.id.hexa_binary_button);
        HexaBinaryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextSavedAsBinary ==true) {
                    Toast.makeText(MessageSetting.this,"Bin To Hex",Toast.LENGTH_SHORT).show();
                    ConvertAllToHex();
                } else {
                    Toast.makeText(MessageSetting.this,"Hex To Bin",Toast.LENGTH_SHORT).show();
                    ConvertAllToBinary();
                }
            }
        });
        if(TextSavedAsBinary ==true){HexaBinaryButton.setText("Hex");HexaBinaryButton.getBackground().setColorFilter(Color.argb(255, 150, 50, 150), PorterDuff.Mode.DARKEN);}
        else{HexaBinaryButton.setText("Bin");HexaBinaryButton.getBackground().setColorFilter(Color.argb(255, 50, 200, 150), PorterDuff.Mode.DARKEN);}
    }

    @Override
    public void onResume() {
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

    @Override
    public void onClick(View v) {
        ConvertAllToBinary();
        String command_name = "";
        String command_binary = "";
        switch ((v).getTag().toString()) {
            case "left":
                command_name = "left";
                command_binary = LeftCommand.getText().toString();
                break;
            case "right":
                command_name = "right";
                command_binary = RightCommand.getText().toString();
                break;
            case "forward":
                command_name = "forward";
                command_binary = ForwardCommand.getText().toString();
                break;
            case "stop":
                command_name = "stop";
                command_binary = StopCommand.getText().toString();
                break;
            default:
        }
        FragmentManager pickColorFragmentManager = getFragmentManager();
        DialogFragment pickColorFragment = new PickColorFragment();
        Bundle args = new Bundle();
        args.putString("command_name", command_name);
        args.putString("command_binary", command_binary);
        pickColorFragment.setArguments(args);
        pickColorFragment.show(pickColorFragmentManager, "pick_color_fragment");
    }

    void ConvertAllToBinary(){
        if(TextSavedAsBinary ==false){
            LeftCommand.setText(HexToBinary(LeftCommand.getText().toString()));
            RightCommand.setText(HexToBinary(RightCommand.getText().toString()));
            ForwardCommand.setText(HexToBinary(ForwardCommand.getText().toString()));
            StopCommand.setText(HexToBinary(StopCommand.getText().toString()));
            TextSavedAsBinary =true;
            HexaBinaryButton.setText("Hex");
            HexaBinaryButton.getBackground().setColorFilter(Color.argb(255, 150, 50, 150), PorterDuff.Mode.DARKEN);
        }
    }

    void ConvertAllToHex(){
        if(TextSavedAsBinary ==true){
            LeftCommand.setText(BinaryToHex(LeftCommand.getText().toString()));
            RightCommand.setText(BinaryToHex(RightCommand.getText().toString()));
            ForwardCommand.setText(BinaryToHex(ForwardCommand.getText().toString()));
            StopCommand.setText(BinaryToHex(StopCommand.getText().toString()));
            TextSavedAsBinary =false;
            HexaBinaryButton.setText("Bin");
            HexaBinaryButton.getBackground().setColorFilter(Color.argb(255, 50, 200, 150), PorterDuff.Mode.DARKEN);
        }
    }


    String HexToBinary(String HexValue){
        String BinaryValue="";
        for(int i =0; i<40; i++)
        {
            String bin =  new BigInteger(HexValue.substring(0 + i, 1 + i), 16).toString(2);
            int inb = Integer.parseInt(bin);
            BinaryValue = BinaryValue+ String.format("%04d", inb);
        }
        return BinaryValue;
    }

    public static String BinaryToHex(String BinaryValue){
        String HexValue="";
        for(int i =0; i<40; i++)
        {
            HexValue=HexValue+String.valueOf(Integer.toHexString(Integer.parseInt(BinaryValue.substring(0 + 4 * i, 4 + 4 * i), 2)));
        }
        return HexValue;
    }
}
