package com.example.securitypatrol.Interfaces.UIComponents;

import android.content.Context;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import com.example.securitypatrol.Interfaces.UIComponentCreator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Create_UI_Checkboxes implements UIComponentCreator {

    private final List<String> options;

    // Fallback default constructor (rarely used, but ensures backward compatibility)
    public Create_UI_Checkboxes() {
        this.options = Arrays.asList("Da", "Nu");
    }

    // Constructor with CSV values for dynamic options
    public Create_UI_Checkboxes(String valoriCsv) {
        this.options = parseOptions(valoriCsv);
    }

    private List<String> parseOptions(String valoriCsv) {
        List<String> list = new ArrayList<>();
        if (valoriCsv != null) {
            for (String raw : valoriCsv.split(",")) {
                String val = raw.trim();
                if (!val.isEmpty()) {
                    list.add(capitalize(val));
                }
            }
        }
        if (list.isEmpty()) {
            list.add("Da");
            list.add("Nu");
        }
        return list;
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase(java.util.Locale.ROOT) + s.substring(1);
    }

    @Override
    public View createView(Context context) {
        LinearLayout container = new LinearLayout(context);
        container.setOrientation(LinearLayout.VERTICAL);

        for (String opt : options) {
            CheckBox cb = new CheckBox(context);
            cb.setText(opt);
            container.addView(cb);
        }
        return container;
    }
}
