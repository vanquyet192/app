package com.example.weather_forecast;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class IntroActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        // Xử lý chuyển tiếp khi người dùng nhấn nút "Tiếp tục"
        Button continueButton = findViewById(R.id.continueButton);
        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Tạo intent để chuyển đến MainActivity
                Intent intent = new Intent(IntroActivity.this, MainActivity.class);
                startActivity(intent);

                // Đóng activity giới thiệu sau khi chuyển tiếp
                finish();
            }
        });
    }
}
