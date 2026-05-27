package com.example.vitaai.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import com.example.vitaai.data.HealthSnapshot;

public class MainActivity extends Activity {
    private FrameLayout contentFrame;
    private LinearLayout dashboardView;
    private LinearLayout chatView;
    private LinearLayout settingsView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.parseColor("#0A0A0F"));

        // Content Area
        contentFrame = new FrameLayout(this);
        LinearLayout.LayoutParams frameParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1.0f);
        root.addView(contentFrame, frameParams);

        // Bottom Navigation
        LinearLayout navBar = new LinearLayout(this);
        navBar.setOrientation(LinearLayout.HORIZONTAL);
        navBar.setBackgroundColor(Color.parseColor("#12121A"));
        navBar.setPadding(0, 20, 0, 20);
        navBar.setGravity(Gravity.CENTER);

        navBar.addView(createNavButton("Home", 0));
        navBar.addView(createNavButton("Coach", 1));
        navBar.addView(createNavButton("Settings", 2));

        root.addView(navBar);

        setupViews();
        showView(0); // Show Home by default

        setContentView(root);
    }

    private void setupViews() {
        dashboardView = createDashboardView();
        chatView = createChatView();
        settingsView = createSettingsView();
    }

    private void showView(int index) {
        contentFrame.removeAllViews();
        switch (index) {
            case 0: contentFrame.addView(dashboardView); break;
            case 1: contentFrame.addView(chatView); break;
            case 2: contentFrame.addView(settingsView); break;
        }
    }

    private TextView createNavButton(final String text, final int index) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextColor(Color.WHITE);
        tv.setPadding(60, 20, 60, 20);
        tv.setGravity(Gravity.CENTER);
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showView(index);
            }
        });
        return tv;
    }

    private LinearLayout createDashboardView() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 40, 40, 40);
        
        TextView title = new TextView(this);
        title.setText("Today's Summary");
        title.setTextSize(22);
        title.setTextColor(Color.WHITE);
        title.setPadding(0, 0, 0, 30);
        layout.addView(title);

        HealthSnapshot snapshot = new HealthSnapshot(8210, 68.0, 7.5);
        
        layout.addView(createStatCard("Steps", String.valueOf(snapshot.steps), "#00FFD1"));
        layout.addView(createStatCard("Heart Rate", snapshot.avgHeartRate + " bpm", "#FFB347"));
        layout.addView(createStatCard("Sleep Score", "85/100", "#9D6FFF"));

        return layout;
    }

    private LinearLayout createChatView() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 40, 40, 40);

        TextView title = new TextView(this);
        title.setText("VitaAI Coach");
        title.setTextSize(22);
        title.setTextColor(Color.parseColor("#00FFD1"));
        title.setPadding(0, 0, 0, 20);
        layout.addView(title);

        final LinearLayout chatHistory = new LinearLayout(this);
        chatHistory.setOrientation(LinearLayout.VERTICAL);
        ScrollView scrollView = new ScrollView(this);
        scrollView.addView(chatHistory);
        layout.addView(scrollView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1.0f));

        // Initial Message
        addChatMessage(chatHistory, "Hello! I'm your VitaAI coach. Based on your 7.5h sleep, you're well-recovered. Ready for a workout?", false);

        // Input Area
        LinearLayout inputArea = new LinearLayout(this);
        final EditText input = new EditText(this);
        input.setHint("Ask anything...");
        input.setHintTextColor(Color.GRAY);
        input.setTextColor(Color.WHITE);
        input.setBackgroundColor(Color.parseColor("#12121A"));
        
        Button send = new Button(this);
        send.setText("Send");
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = input.getText().toString();
                if (!msg.isEmpty()) {
                    addChatMessage(chatHistory, msg, true);
                    input.setText("");
                    addChatMessage(chatHistory, "That's a great question! Research suggests consistency is key for wellbeing.", false);
                }
            }
        });

        inputArea.addView(input, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f));
        inputArea.addView(send);
        layout.addView(inputArea);

        return layout;
    }

    private void addChatMessage(LinearLayout history, String text, boolean isUser) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextColor(isUser ? Color.BLACK : Color.WHITE);
        tv.setPadding(30, 20, 30, 20);
        
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(Color.parseColor(isUser ? "#00FFD1" : "#12121A"));
        gd.setCornerRadius(20);
        tv.setBackground(gd);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 10, 0, 10);
        params.gravity = isUser ? Gravity.RIGHT : Gravity.LEFT;
        
        history.addView(tv, params);
    }

    private LinearLayout createSettingsView() {
        LinearLayout layout = new LinearLayout(this);
        layout.setPadding(40, 40, 40, 40);
        TextView tv = new TextView(this);
        tv.setText("Settings & Profile\n\n- Connect Health Connect\n- Linked Devices\n- Privacy Policy");
        tv.setTextColor(Color.WHITE);
        layout.addView(tv);
        return layout;
    }

    private LinearLayout createStatCard(String label, String value, String color) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(30, 30, 30, 30);
        card.setElevation(4);
        
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(Color.parseColor("#12121A"));
        gd.setCornerRadius(15);
        card.setBackground(gd);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, 20);
        card.setLayoutParams(params);

        TextView labelTv = new TextView(this);
        labelTv.setText(label);
        labelTv.setTextColor(Color.GRAY);
        card.addView(labelTv);

        TextView valueTv = new TextView(this);
        valueTv.setText(value);
        valueTv.setTextSize(28);
        valueTv.setTextColor(Color.parseColor(color));
        card.addView(valueTv);

        return card;
    }
}