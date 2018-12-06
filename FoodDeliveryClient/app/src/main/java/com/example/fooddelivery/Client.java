package com.example.fooddelivery;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import model.Food;
import model.Order;

/**
 * Created by lesli on 2018/11/31.
 */

public class Client
{
    public static Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    public Client() {
        try {
            socket = new Socket("104.196.71.17", 8080);
            InputStream is = socket.getInputStream();
            OutputStream os = socket.getOutputStream();
//            reader = new BufferedReader(new InputStreamReader(is));
//            writer = new PrintWriter(new OutputStreamWriter(os), true);
            oos = new ObjectOutputStream(os);
            ois = new ObjectInputStream(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int CheckLogin(String account, String pw) {
        try {
            oos.writeUTF("loginOrRegister");
            oos.writeUTF(account);
            oos.writeUTF(pw);
            oos.flush();
            int result = ois.readInt();
            return result;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -2;
    }

    public void insertOrder(Order order) {
        try {
            oos.writeUTF("insertOrder");
            oos.writeObject(order);
            oos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<Order> queryOrder(int accountID) {
        try {
            oos.writeUTF("queryOrderByAccountID");
            oos.writeUTF(accountID +"");
            oos.flush();
            ArrayList<Order> orders =  (ArrayList<Order>) ois.readObject();
            for (Order order : orders) {
                if (order.getStatus().equals("Food Ready")) {
                    orders.remove(order);
                    orders.add(0, order);
                }
            }
            return orders;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public void deleteOrder(int orderID) {
        try {
            oos.writeUTF("deleteOrder");
            oos.writeInt(orderID);
            oos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void updatePartialOrder(int orderID) {
        try {
            oos.writeUTF("updatePartialOrder");
            oos.writeInt(orderID);
            oos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<Food> queryAllFoods() {
        try {
            oos.writeUTF("queryAllFoods");
            oos.flush();
            return (ArrayList<Food>) ois.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public Map<Food, Integer> queryAllInventory() {
        try {
            oos.writeUTF("queryAllInventory");
            oos.writeUTF(LoginActivity.account);
            oos.flush();
            return (Map<Food, Integer>) ois.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return new HashMap<>();
    }

    public void updateFoodReadyOrder(int orderID) {
        try {
            oos.writeUTF("updateFoodReady");
            oos.writeInt(orderID);
            oos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
