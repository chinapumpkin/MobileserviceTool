package oulu.university.smartglasses;

import android.app.DialogFragment;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.PorterDuff;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

import fi.tol.oulu.vgs4msc.R;

/**
 * Created by afirouzi on 13.10.2015.
 */
public class PickColorFragment extends DialogFragment implements View.OnClickListener {
    int r = 0;
    int g = 0;
    int b = 0;
    int r_shorten = 0;
    int g_shorten = 0;
    int b_shorten = 0;
    private List<LedProperty> ledProperties = new ArrayList<>();
    EditText frequencyEditText;
    EditText dutycycleEditText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.pick_color_fragment_layout, container, false);

        final String commandName = getArguments().getString("command_name");
        String commandBinary = getArguments().getString("command_binary");
        getDialog().setTitle("Set blinking pattern");
        LinearGradient test = new LinearGradient(0.f, 0.f, 800.f, 0.0f,
                new int[]{0xFF000000, 0xFF0000FF, 0xFF00FF00, 0xFF00FFFF,
                        0xFFFF0000, 0xFFFF00FF, 0xFFFFFF00, 0xFFFFFFFF},
                null, Shader.TileMode.CLAMP);
        ShapeDrawable shape = new ShapeDrawable(new RectShape());
        shape.getPaint().setShader(test);

        final SeekBar seekBarFont = (SeekBar) v.findViewById(R.id.seekbar_font);
        seekBarFont.setProgressDrawable((Drawable) shape);
        seekBarFont.setMax(256 * 7 - 1);
        seekBarFont.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    if (progress < 256) {
                        b = progress;
                    } else if (progress < 256 * 2) {
                        g = progress % 256;
                        b = 256 - progress % 256;
                    } else if (progress < 256 * 3) {
                        g = 255;
                        b = progress % 256;
                    } else if (progress < 256 * 4) {
                        r = progress % 256;
                        g = 256 - progress % 256;
                        b = 256 - progress % 256;
                    } else if (progress < 256 * 5) {
                        r = 255;
                        g = 0;
                        b = progress % 256;
                    } else if (progress < 256 * 6) {
                        r = 255;
                        g = progress % 256;
                        b = 256 - progress % 256;
                    } else if (progress < 256 * 7) {
                        r = 255;
                        g = 255;
                        b = progress % 256;
                    }

                    seekBarFont.setBackgroundColor(Color.argb(255, r, g, b));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        Button ledbtn1 = (Button) v.findViewById(R.id.led1);
        ledbtn1.setOnClickListener(this);
        Button ledbtn2 = (Button) v.findViewById(R.id.led2);
        ledbtn2.setOnClickListener(this);
        Button ledbtn3 = (Button) v.findViewById(R.id.led3);
        ledbtn3.setOnClickListener(this);
        Button ledbtn4 = (Button) v.findViewById(R.id.led4);
        ledbtn4.setOnClickListener(this);
        Button ledbtn5 = (Button) v.findViewById(R.id.led5);
        ledbtn5.setOnClickListener(this);
        Button ledbtn6 = (Button) v.findViewById(R.id.led6);
        ledbtn6.setOnClickListener(this);
        Button ledbtn7 = (Button) v.findViewById(R.id.led7);
        ledbtn7.setOnClickListener(this);
        Button ledbtn8 = (Button) v.findViewById(R.id.led8);
        ledbtn8.setOnClickListener(this);
        Button ledbtn9 = (Button) v.findViewById(R.id.led9);
        ledbtn9.setOnClickListener(this);
        Button ledbtn10 = (Button) v.findViewById(R.id.led10);
        ledbtn10.setOnClickListener(this);
        Button ledbtn11 = (Button) v.findViewById(R.id.led11);
        ledbtn11.setOnClickListener(this);
        Button ledbtn12 = (Button) v.findViewById(R.id.led12);
        ledbtn12.setOnClickListener(this);
        Button ledbtn13 = (Button) v.findViewById(R.id.led13);
        ledbtn13.setOnClickListener(this);
        Button ledbtn14 = (Button) v.findViewById(R.id.led14);
        ledbtn14.setOnClickListener(this);

        Button cancelBtn = (Button) v.findViewById(R.id.cancel_button);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                return;
            }
        });
        Button setBtn = (Button) v.findViewById(R.id.set_button);
        setBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                r_shorten = r / 32;
                g_shorten = g / 32;
                b_shorten = b / 32;
                String command = DecimalTextToBinaryFourValue(frequencyEditText.getText().toString());
                command = command + DecimalTextToBinaryFourValue(dutycycleEditText.getText().toString());
                for (int i = 1; i <= 14; i++) {
                    if (LedPropertyId(i) != -1) {
                        command = command + DecimalTextToBinaryThreeValue(ledProperties.get(LedPropertyId(i)).redProperties);
                        command = command + DecimalTextToBinaryThreeValue(ledProperties.get(LedPropertyId(i)).greenProperties);
                        command = command + DecimalTextToBinaryThreeValue(ledProperties.get(LedPropertyId(i)).blueProperties);
                    } else {
                        command = command + "000000000";
                    }
                }
                // Add 26 bit which is not in use
                command = command + "00000000000000000000000000";
                switch (commandName) {
                    case "left":
                        ((MessageSetting) getActivity()).LeftCommand.setText(command);
                        break;
                    case "right":
                        ((MessageSetting) getActivity()).RightCommand.setText(command);
                        break;
                    case "forward":
                        ((MessageSetting) getActivity()).ForwardCommand.setText(command);
                        break;
                    case "stop":
                        ((MessageSetting) getActivity()).StopCommand.setText(command);
                        break;
                    default:
                }
                dismiss();
                return;
            }
        });

        frequencyEditText = (EditText) v.findViewById(R.id.frequency_text);
        if (frequencyEditText.getText().toString() == "") {
            frequencyEditText.setText("0");
        }
        frequencyEditText.addTextChangedListener(frequencyEditTextWatcher);
        dutycycleEditText = (EditText) v.findViewById(R.id.duty_cycle_text);
        if (dutycycleEditText.getText().toString() == "") {
            dutycycleEditText.setText("0");
        }
        dutycycleEditText.addTextChangedListener(dutycycleEditTextWatcher);

        SetSeekBarAutomatically(commandBinary, v);

        return v;
    }

    @Override
    public void onClick(View v) {
        if (LedPropertyId((Button) v) == -1) {
            v.getBackground().setColorFilter(Color.argb(255, r, g, b), PorterDuff.Mode.OVERLAY);
            LedProperty ledProperty = new LedProperty();
            ledProperty.number = Integer.parseInt(((Button) v).getText().toString());
            ledProperty.myButton = (Button) v;
            ledProperty.redProperties = r;
            ledProperty.greenProperties = g;
            ledProperty.blueProperties = b;
            ledProperties.add(ledProperty);
        } else {
            ledProperties.remove(LedPropertyId((Button) v));
            Drawable d = v.getBackground();
            v.invalidateDrawable(d);
            d.clearColorFilter();
        }
    }

    private final TextWatcher frequencyEditTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (s.length() != 0) {
                if ((Integer.parseInt(s.toString())) < 0) {
                    frequencyEditText.setText("0");
                }
                if ((Integer.parseInt(s.toString())) > 15) {
                    frequencyEditText.setText("15");
                }
            }
        }
    };

    private final TextWatcher dutycycleEditTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (s.length() != 0) {
                if ((Integer.parseInt(s.toString())) < 0) {
                    dutycycleEditText.setText("0");
                }
                if ((Integer.parseInt(s.toString())) > 15) {
                    dutycycleEditText.setText("15");
                }
            }
        }
    };

    String DecimalTextToBinaryThreeValue(int decimalValue) {
        int decimalValueDivideByThirtyTwo = decimalValue / 32;
        String HexdecimalValueText = Integer.toHexString(decimalValueDivideByThirtyTwo);
        String bin = new BigInteger(HexdecimalValueText, 16).toString(2);
        int inb = Integer.parseInt(bin);
        bin = String.format("%03d", inb);
        return bin;
    }

    String DecimalTextToBinaryFourValue(String decimalValueText) {
        int decimalValue = Integer.parseInt(decimalValueText);
        String HexdecimalValueText = Integer.toHexString(decimalValue);
        String bin = new BigInteger(HexdecimalValueText, 16).toString(2);
        int inb = Integer.parseInt(bin);
        bin = String.format("%04d", inb);
        return bin;
    }

    int LedPropertyId(Button btn) {
        for (LedProperty ledProperty : ledProperties) {
            if (ledProperty.myButton == btn) {
                return ledProperties.indexOf(ledProperty);
            }
        }
        return -1;
    }

    int LedPropertyId(int number) {
        for (LedProperty ledProperty : ledProperties) {
            if (ledProperty.number == number) {
                return ledProperties.indexOf(ledProperty);
            }
        }
        return -1;
    }

    void SetSeekBarAutomatically(String binaryCommand, View view) {
        if (binaryCommand.length() > 40) {
            frequencyEditText.setText(String.valueOf(Integer.parseInt(binaryCommand.substring(0, 4), 2)));
            dutycycleEditText.setText(String.valueOf(Integer.parseInt(binaryCommand.substring(4, 8), 2)));
            for (int i = 1; i <= 14; i++) {
                r = (Integer.parseInt(binaryCommand.substring(8 + (i - 1) * 9, 11 + (i - 1) * 9), 2)) * 32;
                g = (Integer.parseInt(binaryCommand.substring(11 + (i - 1) * 9, 14 + (i - 1) * 9), 2)) * 32;
                b = (Integer.parseInt(binaryCommand.substring(14 + (i - 1) * 9, 17 + (i - 1) * 9), 2)) * 32;
                if (r != 0 || g != 0 || b != 0) {
                    LedProperty ledProperty = new LedProperty();
                    ledProperty.number = i;
                    ledProperty.redProperties = r;
                    ledProperty.greenProperties = g;
                    ledProperty.blueProperties = b;
                    ledProperty.myButton = (Button) view.findViewWithTag(String.valueOf(i));
                    ledProperties.add(ledProperty);
                    ledProperty.myButton.getBackground().setColorFilter(Color.argb(255, r, g, b), PorterDuff.Mode.OVERLAY);
                }
            }
        }
    }
}

