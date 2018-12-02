package com.example.fooddelivery;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import model.Food;
import model.Order;

public class ViewOrderActivity extends AppCompatActivity {

    private ListView orderList;
    private ArrayList<Order> orders;
    private ArrayList<Map<String, String>> mylist;
    private SimpleAdapter adapter;
    private Map<Food, Integer> inventory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_order);
        orderList = (ListView) findViewById(R.id.orders_list);
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (Client.socket.isConnected()) {
                    mylist = new ArrayList<>();
                    orders = LoginActivity.client.queryOrder(LoginActivity.account);
                    inventory = LoginActivity.client.queryAllInventory();
//                    inventory = new HashMap<>();
                    for (Order order : orders) {
                        HashMap<String, String> map = new HashMap<>();
                        map.put("order_id", order.getID() + "");
                        Log.e("Test", order.getID() + "");
                        map.put("status", order.getStatus());
                        map.put("account", order.getAccount());
                        StringBuilder orderedFoods = new StringBuilder();
                        boolean available = true;
                        boolean partial = false;
                        for (Map.Entry<Food, Integer> food : order.getIngredients().entrySet()) {
                            orderedFoods.append(food.getKey().getName() + "    $");
                            orderedFoods.append(food.getKey().getPrice() + "    x");
                            orderedFoods.append(food.getValue());
                            for (Map.Entry<Food, Integer> item : inventory.entrySet()) {
                                if (item.getKey().getName().equals(food.getKey().getName())) {
                                    if (food.getValue() > item.getValue()) {
                                        available = false;
                                        if (item.getValue() > 0) {
                                            partial = true;
                                            orderedFoods.append("(" + item.getValue() + " is available)");
                                        } else {
                                            orderedFoods.append("(not available)");
                                        }
                                    } else {
                                        partial = true;
                                    }
                                }
                            }
                            orderedFoods.append("\n");
                        }
                        if (order.getStatus().equals("submitting") || order.getStatus().equals("receiving")) {
                            if (!available) {
                                if (partial) {
                                    map.put("available", "Partially NotAvailable");
                                } else {
                                    map.put("available", "Totally NotAvailable");
                                }
                            } else {
                                map.put("available", "Available");
                            }
                        } else {
                            map.put("available", "Available");
                        }
                        map.put("ordered_foods", orderedFoods.toString());
                        map.put("total", new DecimalFormat("#.00").format(order.getTotal()));
                        mylist.add(map);
                        adapter = new SimpleAdapter(ViewOrderActivity.this,
                                mylist,
                                R.layout.order_list_layout,
                                new String[]{"order_id", "status", "ordered_foods", "total", "available", "account"},
                                new int[]{R.id.order_id, R.id.status, R.id.ordered_foods, R.id.total, R.id.available, R.id.customer_name});
                        Message message = new Message();
                        message.what = 0;
                        handler.sendMessage(message);
                    }
                }
            }
        }).start();
        orderList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                final Map<String, String> temp = (Map<String, String>) orderList.getItemAtPosition(i);
                if (!temp.get("available").equals("Available")) {
                    if (temp.get("available").equals("Totally NotAvailable")) {
                        new AlertDialog.Builder(ViewOrderActivity.this).setTitle("Delete the order?")
                                .setIcon(android.R.drawable.ic_dialog_info)
                                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        new Thread(new Runnable() {
                                            @Override
                                            public void run() {
                                                LoginActivity.client.deleteOrder(Integer.parseInt(temp.get("order_id")));
                                                Message message = new Message();
                                                message.what = 1;
                                                handler.sendMessage(message);
                                            }
                                        }).start();
                                    }
                                })
                                .setNegativeButton("Cancel", null).show();
                    } else {
                        new AlertDialog.Builder(ViewOrderActivity.this).setTitle("Accept partially order or delet order?")
                                .setIcon(android.R.drawable.ic_dialog_info)
                                .setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        new Thread(new Runnable() {
                                            @Override
                                            public void run() {
                                                LoginActivity.client.updatePartialOrder(Integer.parseInt(temp.get("order_id")));
                                                Message message = new Message();
                                                message.what = 1;
                                                handler.sendMessage(message);
                                            }
                                        }).start();
                                    }
                                })
                                .setNegativeButton("Delete", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        new Thread(new Runnable() {
                                            @Override
                                            public void run() {
                                                LoginActivity.client.deleteOrder(Integer.parseInt(temp.get("order_id")));
                                                Message message = new Message();
                                                message.what = 1;
                                                handler.sendMessage(message);
                                            }
                                        }).start();
                                    }
                                }).show();
                    }
                } else if (temp.get("status").equals("Food Ready")) {
                    new AlertDialog.Builder(ViewOrderActivity.this).setTitle("Finish the order?")
                            .setIcon(android.R.drawable.ic_dialog_info)
                            .setPositiveButton("Finish", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            LoginActivity.client.updateFoodReadyOrder(Integer.parseInt(temp.get("order_id")));
                                            Message message = new Message();
                                            message.what = 1;
                                            handler.sendMessage(message);
                                        }
                                    }).start();
                                }
                            })
                            .setNegativeButton("Cancel", null).show();
                }
            }
        });
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    orderList.setAdapter(adapter);
//                    new Thread(new Runnable() {
//                        @Override
//                        public void run() {
//                            try {
//                                Thread.sleep(3000);
//                                inventory = LoginActivity.client.queryAllInventory();
//                            } catch (InterruptedException e) {
//                                e.printStackTrace();
//                            }
//                        }
//                    }).start();
                    break;
                case 1:
                    ViewOrderActivity.this.recreate();
                    break;
                case 3:
                    Toast.makeText(ViewOrderActivity.this, "The order is preparing.", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    };
}
