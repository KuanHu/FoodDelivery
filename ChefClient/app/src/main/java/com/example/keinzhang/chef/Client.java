package com.example.keinzhang.chef;

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

    public ArrayList<Order> queryAllOrder() {
        try {
            oos.writeUTF("queryAllOrder");
            oos.flush();
            ArrayList<Order> orders = (ArrayList<Order>) ois.readObject();
            orders.addAll((ArrayList<Order>) ois.readObject());
            orders.addAll((ArrayList<Order>) ois.readObject());
            orders.addAll((ArrayList<Order>) ois.readObject());
            orders.addAll((ArrayList<Order>) ois.readObject());
            //orders.addAll((ArrayList<Order>) ois.readObject());
            return orders;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public void updateSubmittingOrder(int orderID) {
        try {
            oos.writeUTF("updateSubmitting");
            oos.writeInt(orderID);
            oos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void updateReceivingOrder(int orderID) {
        try {
            oos.writeUTF("updateReceiving");
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
            oos.flush();
            return (Map<Food, Integer>) ois.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return new HashMap<>();
    }

    public void updateInventory(ArrayList<Food> foods) {
        try {
            oos.writeUTF("updateInventory");
            oos.writeObject(foods);
            oos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void updateStock(int orderID) {
        try {
            oos.writeUTF("updateStock");
            oos.writeInt(orderID);
            oos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void deleteOrder(int order_id) {
    }
}
