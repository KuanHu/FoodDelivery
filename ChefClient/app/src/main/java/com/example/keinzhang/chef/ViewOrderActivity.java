package com.example.keinzhang.chef;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.keinzhang.chef.R;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import model.Food;
import model.Order;

public class ViewOrderActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private ListView orderList;
    private Map<String, String> temp;
    private Spinner spinner;
    private ArrayList<Order> orders;
    private ArrayList<Map<String, String>> mylist;
    private SimpleAdapter adapter;
    private Map<Food, Integer> inventory;

    private PopupWindow mPopupWindow;


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


//        orderList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//                final Map<String, String> temp = (Map<String, String>) orderList.getItemAtPosition(i);
//                if (temp.get("available").equals("Available")) {
//                    new AlertDialog.Builder(ViewOrderActivity.this).setTitle("Update the status?")
//                            .setIcon(android.R.drawable.ic_dialog_info)
//                            .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialogInterface, int i) {
//                                    new Thread(new Runnable() {
//                                        @Override
//                                        public void run() {
//                                            if (temp.get("status").equals("submitting")) {
//                                                LoginActivity.client.updateSubmittingOrder(Integer.parseInt(temp.get("order_id")));
//                                            } else if (temp.get("status").equals("receiving")) {
//                                                LoginActivity.client.updateReceivingOrder(Integer.parseInt(temp.get("order_id")));
//                                                LoginActivity.client.updateStock(Integer.parseInt(temp.get("order_id")));
//                                            } else {
//                                                Message message = new Message();
//                                                message.what = 3;
//                                                handler.sendMessage(message);
//                                            }
//                                            Message message = new Message();
//                                            message.what = 1;
//                                            handler.sendMessage(message);
//                                        }
//                                    }).start();
//                                }
//                            })
//                            .setNegativeButton("Cancel", null).show();
//                } else {
//                    Message msg = new Message();
//                    msg.what = 2;
//                    handler.sendMessage(msg);
//                }
//            }
//        });


        registerForContextMenu(orderList);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo)
    {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        ListView lv = (ListView) orderList;
        AdapterView.AdapterContextMenuInfo acmi = (AdapterView.AdapterContextMenuInfo) menuInfo;
        temp = (Map<String, String>) lv.getItemAtPosition(acmi.position);
        inflater.inflate(R.menu.menu_orderlist, menu);
        menu.setHeaderTitle("Select the action :");
//        menu.setHeaderTitle(temp.get("status"));
    }
    @Override
    public boolean onContextItemSelected(MenuItem item){
        if(item.getItemId()==R.id.update){
//            Toast.makeText(getApplicationContext(),"current status :" + temp.get("status"),Toast.LENGTH_LONG).show();
            showUpdateDialog();
        }
        else if(item.getItemId()==R.id.delete){
            deleteOrder(temp);
            Toast.makeText(getApplicationContext(),"deleted",Toast.LENGTH_LONG).show();
        }else{
            return false;
        }
        return true;
    }

    private void deleteOrder(Map<String,String> temp) {
    }




    private void showUpdateDialog() {
        if(!temp.get("available").equals("Available")){
            Message msg = new Message();
            msg.what = 2;
            handler.sendMessage(msg);
            return;
        }
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(ViewOrderActivity.this);
        alertDialog.setTitle("Update Order");
        alertDialog.setMessage("Please choose status");

        LayoutInflater inflater = this.getLayoutInflater();
        final View view = inflater.inflate(R.layout.update_order,null);

        spinner = (Spinner)view.findViewById(R.id.statusSpinner);

        final String[] statusArray={"receiving","preparing","Finish"};
        final String[] statusArrayForWindow={"receiving","preparing","packaging","Food Ready","Finish"};
        spinner.setOnItemSelectedListener(this);

        //Creating the ArrayAdapter instance having the status list
        ArrayAdapter aa = new ArrayAdapter(this,android.R.layout.simple_spinner_item,statusArray);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //Setting the ArrayAdapter data on the Spinner
        spinner.setAdapter(aa);

        alertDialog.setView(view);


        final Handler mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message message) {
                // This is where you do your work in the UI thread.
                // Your worker tells you in the message what to do.
                Toast.makeText(getApplicationContext(), "You cannot update to previous/current status", Toast.LENGTH_LONG).show();
            }
        };

        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String currentStatus = temp.get("status");
                        String settingStatus = String.valueOf(spinner.getSelectedItem());
                        int settingIndex = spinner.getSelectedItemPosition();
                        int currentIndex = findIndexOfArray(currentStatus, statusArrayForWindow);
                        if (settingIndex <= currentIndex) {

                            Message msg = new Message();
                            msg.what = 4;
                            handler.sendMessage(msg);
//                            new AlertDialog.Builder(ViewOrderActivity.this).setTitle("You cannot set to the previous status!")
//                                    .setIcon(android.R.drawable.ic_dialog_info)
//                                    .setNegativeButton("Cancel", null).show();
//                            Context mContext = getApplicationContext();
////                            Toast.makeText(getApplicationContext(), "You cannot update to previous/current status", Toast.LENGTH_LONG).show();
//                            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);
//
//
//                            // Get the application context
//                            // Inflate the custom layout/view
//                            View customView = inflater.inflate(R.layout.popup_window,null);
//
//                            mPopupWindow = new PopupWindow(
//                                    customView,
//                                    ActionBar.LayoutParams.WRAP_CONTENT,
//                                    ActionBar.LayoutParams.WRAP_CONTENT
//                            );
//
//                            // Set an elevation value for popup window
//                            // Call requires API level 21
//                            if(Build.VERSION.SDK_INT>=21){
//                                mPopupWindow.setElevation(5.0f);
//                            }
//
//                            // Get a reference for the custom view close button
//                            ImageButton closeButton = (ImageButton) customView.findViewById(R.id.ib_close);
//
//                            // Set a click listener for the popup window close button
//                            closeButton.setOnClickListener(new View.OnClickListener() {
//                                @Override
//                                public void onClick(View view) {
//                                    // Dismiss the popup window
//                                    mPopupWindow.dismiss();
//                                }
//                            });
//
//                            mPopupWindow.showAtLocation(view, Gravity.CENTER,0,0);
                        } else if (currentStatus.equals("submitting")) {
                            if (settingStatus == "receiving") {
                                LoginActivity.client.updateSubmittingOrder(Integer.parseInt(temp.get("order_id")));
                                Message message = new Message();
                                message.what = 1;
                                handler.sendMessage(message);
                            } else if (settingStatus == "preparing") {
                                LoginActivity.client.updateReceivingOrder(Integer.parseInt(temp.get("order_id")));
                                LoginActivity.client.updateStock(Integer.parseInt(temp.get("order_id")));
                                Message message2 = new Message();
                                message2.what = 1;
                                handler.sendMessage(message2);
                            } else {
                                Message message2 = new Message();
                                message2.what = 5;
                                handler.sendMessage(message2);
                            }
                        } else if (currentStatus.equals("preparing")) {
                            Message message = new Message();
                            message.what = 6;
                            handler.sendMessage(message);
                        } else if (currentIndex >= 1) {
                            int step = settingIndex - currentIndex;
                            int start = 0;
                            while (start < step) {
                                Message message = new Message();
                                message.what = 3;
                                handler.sendMessage(message);
                                Message message2 = new Message();
                                message2.what = 1;
                                handler.sendMessage(message2);
                                start++;
                            }
                        }else if(currentStatus.equals("receiving")){
                            if(settingIndex > 2){
                                return;
                            }
                            LoginActivity.client.updateReceivingOrder(Integer.parseInt(temp.get("order_id")));
                            LoginActivity.client.updateStock(Integer.parseInt(temp.get("order_id")));
                            Message message2 = new Message();
                            message2.what = 1;
                            handler.sendMessage(message2);
                        }else {
                            Message message = new Message();
                            message.what = 1;
                            handler.sendMessage(message);
                        }
//                        finish();
//                        startActivity(getIntent());
//                if (temp.get("status").equals("submitting")) {
//                    LoginActivity.client.updateSubmittingOrder(Integer.parseInt(temp.get("order_id")));
//                } else if (temp.get("status").equals("receiving")) {
//                    LoginActivity.client.updateReceivingOrder(Integer.parseInt(temp.get("order_id")));
//                    LoginActivity.client.updateStock(Integer.parseInt(temp.get("order_id")));
//                } else {
//                    Message message = new Message();
//                    message.what = 3;
//                    handler.sendMessage(message);
//                }
//                Message message = new Message();
//                message.what = 1;
//                handler.sendMessage(message);
                    }
                }).start();
            }
        });

        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        alertDialog.show();
    }

    public int findIndexOfArray(String currentStatus, String[] statusArray) {
        int index = -1;
        for (int i=0;i<statusArray.length;i++) {
            if (statusArray[i].equals(currentStatus)) {
                return i;
            }
        }
        return -1;
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
                case 4:
                    Toast.makeText(ViewOrderActivity.this, "You cannot set to the previous status", Toast.LENGTH_SHORT).show();
                    break;
                case 5:
                    Toast.makeText(getApplicationContext(), "You cannot update to this status, you need to wait about 120-240 seconds for food to be prepared1", Toast.LENGTH_LONG).show();
                    break;
                case 6:
//                    Toast.makeText(getApplicationContext(), "You cannot update to this status, you need to wait for the food ready to be pick up!", Toast.LENGTH_LONG).show();
                    Toast toast = Toast.makeText(getApplicationContext(), "You cannot update to this status, you need to wait for the food ready to be pick up!", Toast.LENGTH_LONG);
                    View view = toast.getView();
//                    view.setBackgroundResource(R.drawable.pigicon);
                    TextView text = (TextView) view.findViewById(android.R.id.message);
                    text.setTextColor(Color.parseColor("#FFF72600"));
                    /*Here you can do anything with above textview like text.setTextColor(Color.parseColor("#000000"));*/
                    toast.show();
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
