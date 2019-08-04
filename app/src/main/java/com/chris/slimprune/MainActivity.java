package com.chris.slimprune;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.leanplum.Leanplum;
import com.leanplum.Var;
import com.leanplum.callbacks.StartCallback;
import com.leanplum.callbacks.VariableCallback;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.chris.slimprune.ApplicationClass.bgColor;
import static com.chris.slimprune.ApplicationClass.drawableColor;

public class MainActivity extends AppCompatActivity {

    private ImageView bg;
    private EditText setUserIdEditText;
    private Button setUserIdButton;
    private EditText trackEventNameEditText;
    private EditText trackEventValueEditText;
    private EditText trackEventParamsEditText;
    private Button trackButton;
    private EditText setUserAttributeNameEditText;
    private EditText setUserAttributeValueEditText;
    private Button setUserAttributeButton;
    private Switch stateSwitch;
    private TextView state1TextView;
    private TextView state2TextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        bg = findViewById(R.id.main_imageView);
        setUserIdEditText = findViewById(R.id.main_setUserId_editText);
        setUserIdButton = findViewById(R.id.main_setUserId_button);
        trackEventNameEditText = findViewById(R.id.main_trackEventName_editText);
        trackEventValueEditText = findViewById(R.id.main_trackEventValue_editText);
        trackEventParamsEditText = findViewById(R.id.main_trackEventParams_editText);
        trackButton = findViewById(R.id.main_track_button);
        setUserAttributeNameEditText = findViewById(R.id.main_attributeName_editText);
        setUserAttributeValueEditText = findViewById(R.id.main_attributeValue_editText);
        setUserAttributeButton = findViewById(R.id.main_attribute_button);
        stateSwitch = findViewById(R.id.main_state_switch);


        /** SET BACKGROUND COLOR FROM LEANPLUM VARIABLE */
        if (bgColor.value() != null && drawableColor.value() != null) {
            bg.setBackgroundColor(Color.parseColor(bgColor.value()));
            bg.setColorFilter(Color.parseColor(drawableColor.value()), PorterDuff.Mode.MULTIPLY);
        }
        bgColor.addValueChangedHandler(new VariableCallback<String>() {
            @Override
            public void handle(Var<String> var) {

                if (bgColor.value() != null && drawableColor.value() != null) {
                    bg.setBackgroundColor(Color.parseColor(bgColor.value()));
                    bg.setColorFilter(Color.parseColor(drawableColor.value()), PorterDuff.Mode.MULTIPLY);
                }
            }
        });

        /** SET CURRENT USER ID IN HINT */
        Leanplum.addStartResponseHandler(new StartCallback() {
            @Override
            public void onResponse(boolean success) {
                setUserIdEditText.setHint(Leanplum.getUserId());
            }
        });

        /** EVENT LISTENERS */

        setUserIdButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newUserId = setUserIdEditText.getText().toString().trim();
                if (newUserId != null && newUserId.length() > 0) {
                    Leanplum.setUserId(newUserId);
                    Leanplum.forceContentUpdate();
                    setUserIdEditText.setText("");
                    setUserIdEditText.setHint(newUserId);
                    setUserIdEditText.clearFocus();
                    Log.d("New User ID: ", newUserId);
                }
            }
        });

        trackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String eventName = null;
                Double eventValue = null;
                Map<String, Object> eventParams;

                try {
                    eventParams = new Gson().fromJson(trackEventParamsEditText.getText().toString().trim(), Map.class);
                    if (!trackEventValueEditText.getText().toString().isEmpty())
                        eventValue = Double.parseDouble(trackEventValueEditText.getText().toString().trim());
                    if (!trackEventNameEditText.getText().toString().isEmpty())
                        eventName = trackEventNameEditText.getText().toString().trim();

                    Leanplum.track(eventName, eventValue, eventParams);

                } catch (JsonSyntaxException e) {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, "Your JSON sucks :(", Toast.LENGTH_SHORT).show();
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, "Value invalid.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        trackButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                trackEventNameEditText.setText("");
                trackEventValueEditText.setText("");
                trackEventParamsEditText.setText("");
                return true;
            }
        });

        trackEventParamsEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                final EditText view = (EditText) v;

                if (hasFocus) {
                    if (trackEventParamsEditText.getText().length() == 0)
                        trackEventParamsEditText.setText("{}");

                    // only working way to move the cursor
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            trackEventParamsEditText.setSelection(trackEventParamsEditText.getText().length() - 1);
                        }
                    }, 10);
                } else {
                    if (trackEventParamsEditText.getText().length() <= 2)
                        trackEventParamsEditText.setText("");
                }
            }
        });

        setUserAttributeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String attributeName = setUserAttributeNameEditText.getText().toString().trim();
                final String attributeValue = setUserAttributeValueEditText.getText().toString().trim();
                Map<String, Object> attribute = new HashMap<>();

                if (!attributeName.isEmpty() && !attributeValue.isEmpty()) {
                    attribute.put(attributeName, attributeValue);
                    Leanplum.setUserAttributes(attribute);
                } else {
                    Toast.makeText(MainActivity.this, "Nope", Toast.LENGTH_SHORT).show();
                }
            }
        });

        setUserAttributeButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                setUserAttributeNameEditText.setText("");
                setUserAttributeValueEditText.setText("");
                return true;
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        //START ANIMATION
        bg = findViewById(R.id.main_imageView);
        final Drawable d = bg.getDrawable();

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            AnimatedVectorDrawableCompat avd = (AnimatedVectorDrawableCompat) d;
            avd.start();
        } else {
            AnimatedVectorDrawable avd = (AnimatedVectorDrawable) d;
            avd.start();
        }
    }
}
