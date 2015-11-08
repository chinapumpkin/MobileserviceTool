package oulu.university.smartglasses;

//Some change

import android.app.DialogFragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import fi.tol.oulu.vgs4msc.R;

/**
 * Created by afirouzi on 15.10.2015.
 */
public class BleCommandFragment extends DialogFragment implements View.OnClickListener, ICallback {
    String LeftCommand;
    String RightCommand;
    String ForwardCommand;
    String StopCommand;
    boolean TextSavedAsBinary;
    String DefaultCommandBinary = "0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000";
    Button onlineStatueBtn;
    TextView linphoneTextView;
    Button linphoneBtn;
    LinphoneMiniManager linphoneMiniManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.ble_command_fragment_layout, container, false);
        final EditText ed = (EditText) v.findViewById(R.id.charact_value);
        Button ok = (Button) v.findViewById(R.id.dialog_confirm);
        Button cancel = (Button) v.findViewById(R.id.dialog_cancel);
        Button leftBtn = (Button) v.findViewById(R.id.command_left_btn);
        leftBtn.setOnClickListener(this);
        Button rightBtn = (Button) v.findViewById(R.id.command_right_btn);
        rightBtn.setOnClickListener(this);
        Button forwardBtn = (Button) v.findViewById(R.id.command_forward_btn);
        forwardBtn.setOnClickListener(this);
        Button stopBtn = (Button) v.findViewById(R.id.command_stop_btn);
        stopBtn.setOnClickListener(this);
        linphoneBtn = (Button) v.findViewById(R.id.LinphoneBtn);
        linphoneBtn.setOnClickListener(this);
        onlineStatueBtn = (Button) v.findViewById(R.id.OnlineStatueBtn);
        onlineStatueBtn.getBackground().setColorFilter(Color.argb(255, 200, 0, 0), PorterDuff.Mode.DARKEN);
        linphoneTextView = (TextView) v.findViewById(R.id.LinphoneTextView);

        SharedPreferences loadSharedPreferences = getActivity().getSharedPreferences("SettingData", Context.MODE_PRIVATE);
        TextSavedAsBinary = loadSharedPreferences.getBoolean("TextSavedAsBinary", true);
        if (TextSavedAsBinary == true) {
            LeftCommand = MessageSetting.BinaryToHex(loadSharedPreferences.getString("LeftCommand", DefaultCommandBinary));
            RightCommand = MessageSetting.BinaryToHex(loadSharedPreferences.getString("RightCommand", DefaultCommandBinary));
            ForwardCommand = MessageSetting.BinaryToHex(loadSharedPreferences.getString("ForwardCommand", DefaultCommandBinary));
            StopCommand = MessageSetting.BinaryToHex(loadSharedPreferences.getString("StopCommand", DefaultCommandBinary));
        } else {
            LeftCommand = (loadSharedPreferences.getString("LeftCommand", DefaultCommandBinary));
            RightCommand = (loadSharedPreferences.getString("RightCommand", DefaultCommandBinary));
            ForwardCommand = (loadSharedPreferences.getString("ForwardCommand", DefaultCommandBinary));
            StopCommand = (loadSharedPreferences.getString("StopCommand", DefaultCommandBinary));
        }

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), "starts", Toast.LENGTH_SHORT).show();
                // write characterist here.
                String str = ed.getText().toString();
                SendValueToBleReceiver(str);

                dismiss();
                return;
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        return v;
    }

    public void onClick(View v) {
        switch (v.getTag().toString()) {
            case "left":
                SendValueToBleReceiver(LeftCommand);
                break;
            case "right":
                SendValueToBleReceiver(RightCommand);
                break;
            case "forward":
                SendValueToBleReceiver(ForwardCommand);
                break;
            case "stop":
                SendValueToBleReceiver(StopCommand);
                break;
            case "Connect":
                    linphoneMiniManager = new LinphoneMiniManager(getActivity(), "linphone", this);
                break;
            case "Disconnect":
                    linphoneMiniManager.destroy();
                break;
            default:
        }
    }

    public void SendValueToBleReceiver(String CommandValue) {
        //mWriteCharactristc.
        //byte[] strBytes = CommandValue.getBytes();
        byte[] strBytes = hexStringToByteArray(CommandValue);
        byte[] bytes = ((DeviceControlActivity) getActivity()).mWriteCharacteristic.getValue();
        //mWriteCharacteristic.
        if (strBytes == null) {
            Toast.makeText(getActivity(), "Cannot get Value from EditText Widget", Toast.LENGTH_SHORT).show();
            dismiss();
            return;
        }

        if (bytes == null) {
            // maybe just write a byte into GATT
            Toast.makeText(getActivity(), "Cannot get Values from mWriteCharacteristic", Toast.LENGTH_SHORT).show();
            dismiss();
            return;
        } else if (bytes.length <= strBytes.length) {
            for (int i = 0; i < bytes.length; i++) {
                bytes[i] = strBytes[i];
            }
        } else {
            for (int i = 0; i < strBytes.length; i++) {
                bytes[i] = strBytes[i];
            }
        }

        ((DeviceControlActivity) getActivity()).mWriteCharacteristic.setValue(bytes);
        ((DeviceControlActivity) getActivity()).writeCharacteristic(((DeviceControlActivity) getActivity()).mWriteCharacteristic);
        Toast.makeText(getActivity(), "Sent!!!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void callBackLinphoneMessageReceived(String linphoneMessageValue) {
        switch (linphoneMessageValue) {
            case "L":
                SendValueToBleReceiver("LeftCommand");
                break;
            case "R":
                SendValueToBleReceiver("RightCommand");
                break;
            case "S":
                SendValueToBleReceiver("StopCommand");
                break;
            case "F":
                SendValueToBleReceiver("ForwardCommand");
                break;
            default:
        }
        linphoneTextView.setText(linphoneMessageValue);
    }

    //get rid of this method
    @Override
    public void callBackLinphoneStatusChanged(String linphoneOnlineStatueValue) {
        linphoneOnlineStatueValue=  linphoneOnlineStatueValue.toUpperCase();
        switch (linphoneOnlineStatueValue) {
            case "REGISTRATION SUCCESSFUL":
                onlineStatueBtn.getBackground().setColorFilter(Color.argb(255, 0, 200, 0), PorterDuff.Mode.LIGHTEN);
                linphoneBtn.setText("Disconnect");
                linphoneBtn.setTag("Disconnect");
                break;
            default:
                onlineStatueBtn.getBackground().setColorFilter(Color.argb(255, 200, 0, 0), PorterDuff.Mode.DARKEN);
                linphoneBtn.setText("Connect");
                linphoneBtn.setTag("Connect");
        }

    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }
}
