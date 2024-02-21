package com.example.securitypatrol.Interfaces.UIComponents;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.EditText;

import com.example.securitypatrol.Interfaces.UIComponentCreator;

public class Create_UI_Edittext implements UIComponentCreator {

    @Override
    public View createView(Context context) {
        EditText editText = new EditText(context);
        editText.setHint("Enter value");
        editText.setTextColor(Color.BLACK);
        editText.setTextSize(16);

        return editText;
    }
}
