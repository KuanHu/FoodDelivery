package com.example.fooddelivery;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.io.IOException;
import java.nio.DoubleBuffer;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import model.Food;
import model.Order;
import model.User;

public class MainActivity extends AppCompatActivity {

    private ListView foodsList;
    private Button submit;
    private TextView totalPrice;
    private SimpleAdapter adapter;
    private double total;
    private ArrayList<Map<String, String>> mylist;
    private Map<Food, Integer> foodsOrdered;
    private ArrayList<Food> foods;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        String[] names = new String[]{"Burgers", "Chickens", "French Fries", "Onion Rings"};
//        double[] prices = new double[] {5, 7, 2.5, 3.5};
        foodsList = (ListView) findViewById(R.id.foods_list);
        submit = (Button) findViewById(R.id.finish);
        totalPrice = (TextView) findViewById(R.id.total_value);
        total = 0;
        mylist = new ArrayList<>();
        foodsOrdered = new HashMap<>();
        new Thread(new Runnable() {
            @Override
            public void run() {
                foods = LoginActivity.client.queryAllFoods();
                Message msg = new Message();
                msg.what = 0;
                handler.sendMessage(msg);
            }
        }).start();
//        foodsList.setAdapter(adapter);
        foodsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                final Map<String, String> temp = (Map<String, String>) foodsList.getItemAtPosition(arg2);
                LinearLayout layout = (LinearLayout) adapter.getView(arg2, null, null);
                final String name = ((TextView) layout.findViewById(R.id.name)).getText().toString();
//                Log.e("test", name);
//                Toast.makeText(MainActivity.this, "name: " + temp.get("name"), Toast.LENGTH_SHORT).show();
//                        Toast.makeText(MainActivity.this, map.get("price"), Toast.LENGTH_SHORT).show();
                final EditText input = new EditText(MainActivity.this);
                input.setInputType(InputType.TYPE_CLASS_NUMBER);
                new AlertDialog.Builder(MainActivity.this).setTitle("Enter the quantity: ")
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .setView(input)
                        .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (!input.getText().toString().trim().equals("")) {
                                    total -= (Double.parseDouble(temp.get("price")) * Integer.parseInt(temp.get("quantity")) * 1.3);
                                    temp.put("quantity", Integer.parseInt(input.getText().toString()) + "");
                                    if (Integer.parseInt(input.getText().toString()) == 0) {
                                        for (Map.Entry<Food, Integer> entry : foodsOrdered.entrySet()) {
                                            if (entry.getKey().getName().equals(name)) {
                                                foodsOrdered.remove(entry);
                                            }
                                        }
                                    } else {
                                        foodsOrdered.put(new Food(name, Double.parseDouble(temp.get("price"))), Integer.parseInt(input.getText().toString()));
                                    }
                                    total += (Double.parseDouble(temp.get("price")) * Integer.parseInt(temp.get("quantity")) * 1.3);
                                    totalPrice.setText("Total Price: $" + new DecimalFormat("#.00").format(total));
                                    adapter.notifyDataSetChanged();
                                }
                            }
                        })
                        .setNegativeButton("Cancel", null).show();
            }
        });
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Client.socket.isConnected()) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            User user = new User(LoginActivity.accountId);
                            Order order = new Order(0, user, "submitting", foodsOrdered, total);
                            LoginActivity.client.insertOrder(order);
                            Intent intent = new Intent(MainActivity.this, ViewOrderActivity.class);
                            MainActivity.this.finish();
                            startActivity(intent);
                        }
                    }).start();
                }
            }
        });
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    for (int i = 0; i < foods.size(); i++) {
                        HashMap<String, String> map = new HashMap<>();
                        Log.e("Test", foods.get(i).getName());
                        map.put("name", foods.get(i).getName());
                        map.put("price", foods.get(i).getPrice() + "");
                        map.put("quantity", 0 + "");
                        mylist.add(map);
                    }
                    adapter = new SimpleAdapter(MainActivity.this,
                            mylist,
                            R.layout.foods_list_layout,
                            new String[]{"name", "price", "quantity"},
                            new int[]{R.id.name, R.id.price, R.id.quantity});
                    foodsList.setAdapter(adapter);
//                    adapter.notifyDataSetChanged();
                    break;
                default:
                    break;
            }
        }
    };
}
