package com.example.securitypatrol.Interfaces.UIComponents;

import android.content.Context;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;

import com.example.securitypatrol.Interfaces.UIComponentCreator;

public class Create_UI_RadioButtons implements UIComponentCreator {

    @Override
    public View createView(Context context) {
        RadioGroup radioGroup = new RadioGroup(context);

        RadioButton functionalRadioButton = new RadioButton(context);
        functionalRadioButton.setText("Functional");

        RadioButton nonFunctionalRadioButton = new RadioButton(context);
        nonFunctionalRadioButton.setText("Nefunctional");

        radioGroup.addView(functionalRadioButton);
        radioGroup.addView(nonFunctionalRadioButton);

        return radioGroup;
    }
}
