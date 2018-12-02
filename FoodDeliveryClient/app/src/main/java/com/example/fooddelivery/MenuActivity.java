package com.example.fooddelivery;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.util.Calendar;

public class MenuActivity extends AppCompatActivity {

    private Button order;
    private Button viewOrderList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        order = (Button) findViewById(R.id.order);
        viewOrderList = (Button) findViewById(R.id.view_order_list);
        order.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
                if (hour >= 0 && hour <= 24) {
                    Intent intent = new Intent(MenuActivity.this, MainActivity.class);
                    startActivity(intent);
                } else {
                    Message msg = new Message();
                    msg.what = 0;
                    handler.sendMessage(msg);
                }
            }
        });
        viewOrderList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MenuActivity.this, ViewOrderActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout:
                try {
                    Client.socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                LoginActivity.client = null;
                Intent intent = new Intent(MenuActivity.this, LoginActivity.class);
                MenuActivity.this.finish();
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    Toast.makeText(MenuActivity.this, "Only can order foods during 11:00am~7:00pm", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    };
}
