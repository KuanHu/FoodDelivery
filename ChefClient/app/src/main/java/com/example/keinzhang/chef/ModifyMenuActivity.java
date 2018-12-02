package com.example.keinzhang.chef;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Map;

import model.Food;

public class ModifyMenuActivity extends AppCompatActivity {

    private ArrayList<Food> foods;
    private EditText edit;
    private Button save;
    private Button cancel;
    private Map<Food, Integer> inventory;
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_menu);
        edit = (EditText) findViewById(R.id.foods);
        save = (Button) findViewById(R.id.save);
        cancel = (Button) findViewById(R.id.cancel);
        new Thread(new Runnable() {
            @Override
            public void run() {
                foods = LoginActivity.client.queryAllFoods();
                Message msg = new Message();
                msg.what = 0;
                handler.sendMessage(msg);
            }
        }).start();
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    ArrayList<Food> modify = new ArrayList<>();
                    String[] content = edit.getText().toString().split("\n");
                    for (int i = 0; i < content.length; i++) {
                        String[] temp = content[i].split(",");
                        modify.add(new Food(temp[0], Double.parseDouble(temp[1])));
                    }
                    LoginActivity.client.updateInventory(modify);
                    ModifyMenuActivity.this.finish();
                } catch (NumberFormatException | IndexOutOfBoundsException e) {
                    Message msg = new Message();
                    msg.what = 1;
                    handler.sendMessage(msg);
                }
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ModifyMenuActivity.this.finish();
            }
        });
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    StringBuilder content = new StringBuilder();
                    for (Food food : foods) {
                        content.append(food.getName() + "," + food.getPrice() + "\n");
                    }
                    edit.setText(content.toString());
                    break;
                case 1:
                    Toast.makeText(ModifyMenuActivity.this, "The file format is incorrect.", Toast.LENGTH_SHORT).show();;
                default:
                    break;
            }
        }
    };
}
