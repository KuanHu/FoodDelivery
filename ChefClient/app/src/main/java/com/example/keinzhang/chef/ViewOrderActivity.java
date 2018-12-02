package com.example.keinzhang.chef;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.example.keinzhang.chef.R;

import java.io.IOException;
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
                    orders = LoginActivity.client.queryAllOrder();
                    inventory = LoginActivity.client.queryAllInventory();
//                    inventory = new HashMap<>();
                    for (Order order : orders) {
                        HashMap<String, String> map = new HashMap<>();
                        map.put("order_id", order.getID() + "");
                        map.put("status", order.getStatus());
                        map.put("account", order.getUser().getAccount());
                        map.put("customer_id",  1000000 + order.getUser().getId() +"");
                        StringBuilder orderedFoods = new StringBuilder();
                        boolean available = true;
                        boolean partial = false;
                        for (Map.Entry<Food, Integer> food : order.getIngredients().entrySet()) {
                            orderedFoods.append(food.getKey().getName() + "    $");
                            orderedFoods.append(food.getKey().getPrice() + "    x");
                            orderedFoods.append(food.getValue());
                            if (order.getStatus().equals("submitting") || order.getStatus().equals("receiving")) {
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
                                new String[]{"order_id", "status", "ordered_foods", "total", "available", "account", "customer_id"},
                                new int[]{R.id.order_id, R.id.status, R.id.ordered_foods, R.id.total, R.id.available, R.id.customer_name, R.id.customer_id});
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
                if (temp.get("available").equals("Available")) {
                    new AlertDialog.Builder(ViewOrderActivity.this).setTitle("Update the status?")
                            .setIcon(android.R.drawable.ic_dialog_info)
                            .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (temp.get("status").equals("submitting")) {
                                                LoginActivity.client.updateSubmittingOrder(Integer.parseInt(temp.get("order_id")));
                                            } else if (temp.get("status").equals("receiving")) {
                                                LoginActivity.client.updateReceivingOrder(Integer.parseInt(temp.get("order_id")));
                                                LoginActivity.client.updateStock(Integer.parseInt(temp.get("order_id")));
                                            } else {
                                                Message message = new Message();
                                                message.what = 3;
                                                handler.sendMessage(message);
                                            }
                                            Message message = new Message();
                                            message.what = 1;
                                            handler.sendMessage(message);
                                        }
                                    }).start();
                                }
                            })
                            .setNegativeButton("Cancel", null).show();
                } else {
                    Message msg = new Message();
                    msg.what = 2;
                    handler.sendMessage(msg);
                }
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
                Intent intent = new Intent(ViewOrderActivity.this, LoginActivity.class);
                ViewOrderActivity.this.finish();
                startActivity(intent);
                return true;
            case R.id.edit_inventory:
                intent = new Intent(ViewOrderActivity.this, ModifyMenuActivity.class);
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
                    orderList.setAdapter(adapter);
                    break;
                case 1:
                    ViewOrderActivity.this.recreate();
                    break;
                case 2:
                    Toast.makeText(ViewOrderActivity.this, "Wait for user to confirm.", Toast.LENGTH_SHORT).show();
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
