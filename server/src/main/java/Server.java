import model.Food;
import model.Order;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server {

    private ServerSocket server;
    private Database database;

    public Server() {
        try {
            database = new Database();
            server = new ServerSocket(8080);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start() {
        System.out.println("Server starts");
        Thread serverThread = new Thread(new Runnable(){

            @Override
            public void run() {
                try{
                    while(true) {
                        Thread.sleep(1000 * 60 * 60);
                        database.updateInventory();
                    }
                }catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        serverThread.start();
        while(true) {
            Socket client;
            try {
                client = server.accept();
                System.out.println(client.getLocalAddress() + " is connected.");
                ReplyThread replyThread = new ReplyThread(client);
                new Thread(replyThread).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class ReplyThread implements Runnable {
        Socket client;
        private ObjectOutputStream writer;
        private ObjectInputStream reader;
        public ReplyThread(Socket client) {
            this.client = client;
        }

        @Override
        public void run() {
            try {
                InputStream is = client.getInputStream();
                reader = new ObjectInputStream(is);
                writer = new ObjectOutputStream(client.getOutputStream());
                String message = null;
                while (client.isConnected()) {
                    if (is.available() != 0) {
                        message = reader.readUTF();
                        if (message != null) {
                            System.out.println(message);
                        }
                        if (message.equals("loginOrRegister")) {
                            String account = reader.readUTF();
                            String pw = reader.readUTF();
                            int result = database.loginOrRegister(account, pw);
                            writer.writeInt(result);
                            writer.flush();
                        } else if (message.equals("insertOrder")) {
                            Order order = (Order) reader.readObject();
                            int accountId = order.getUser().getId();
                            System.out.println(database.insertOrder(accountId, order));
                        } else if (message.equals("queryOrderByAccountID")) {
                            // todo query order by account
                            int accountId = Integer.parseInt(reader.readUTF());
                            ArrayList<Order> orders = database.queryOrderByAccountID(accountId);
                            writer.writeObject(orders);
                            writer.flush();
                        } else if (message.equals("queryAllOrder")) {
                            ArrayList<Order> orders = database.queryOrderByStatus("submitting");
                            writer.writeObject(orders);
                            orders = database.queryOrderByStatus("receiving");
                            writer.writeObject(orders);
                            orders = database.queryOrderByStatus("preparing");
                            writer.writeObject(orders);
                            orders = database.queryOrderByStatus("packaging");
                            writer.writeObject(orders);
                            orders = database.queryOrderByStatus("Food Ready");
                            writer.writeObject(orders);
                            writer.flush();
                        } else if (message.equals("updateSubmitting")) {
                            int orderID = reader.readInt();
                            database.updateOrderSubmitting(orderID);
                        } else if (message.equals("updateReceiving")) {
                            final int orderID = reader.readInt();
                            database.updateOrderReceiving(orderID);
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        Thread.sleep((int)(Math.random() * 10 + 20) * 1000);
                                        database.updateOrderPreparing(orderID);
                                        Thread.sleep((int)(Math.random() * 10 + 20) * 1000);
                                        database.updateOrderPackaging(orderID);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }

                            }).start();
                        } else if (message.equals("updateFoodReady")) {
                            int orderID = reader.readInt();
                            database.updateOrderFoodReady(orderID);
                        } else if (message.equals("queryAllFoods")) {
                            ArrayList<Food> foods = database.queryAllFoods();
                            System.out.println(foods.size());
                            writer.writeObject(foods);
                            writer.flush();
                        } else if (message.equals("queryAllInventory")) {
                            writer.writeObject(database.queryAllInventory());
                            writer.flush();
                        } else if (message.equals("deleteOrder")) {
                            int orderID = reader.readInt();
                            database.deleteOrder(orderID);
                        } else if (message.equals("updatePartialOrder")) {
                            int orderID = reader.readInt();
                            database.updatePartialOrder(orderID);
                        } else if (message.equals("updateStock")) {
                            int orderID = reader.readInt();
                            database.updateStock(orderID);
                        }
                    }
                }
                System.out.println("Client disconect  1");
                client.close();
            } catch (IOException | ClassNotFoundException e) {
                try {
                    client.close();
                    System.out.println("Client disconect  2");
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                e.printStackTrace();
            } finally {
                if (!client.isClosed()) {
                    try {
                        client.close();
                        System.out.println("Client disconect  3");
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        }
    }

    public static void main(String[] args) {
        new Server().start();
    }
}
