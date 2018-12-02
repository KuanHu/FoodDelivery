import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import model.Food;
import model.Order;
import model.User;

public class Database {
    Connection conn;
    Statement stat;

    public Database () {
        try {
            Class.forName(DatabaseConstant.EMBEDDED_DRIVER).newInstance();
            File file = new File(DatabaseConstant.DATABASE_PATH);
            //Check if the user use this project at first time.
            if (!file.exists()) {
                file.mkdirs();
                conn = DriverManager.getConnection(DatabaseConstant.PROTOCAL + file+"/mydb.db"
                        + ";create=true");
                stat = conn.createStatement();
                create();
            } else {
                conn = DriverManager.getConnection(DatabaseConstant.PROTOCAL + file+"/mydb.db"
                        + ";create=false");
                stat = conn.createStatement();
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public void create() {
        try {
            stat.execute(DatabaseConstant.CREATE);
            stat.execute(DatabaseConstant.CREATE_DETAIL);
            stat.execute(DatabaseConstant.CREATE_USER);
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public ArrayList<Food> queryAllFoods() {
        ArrayList<Food> foods = new ArrayList<Food>();
        Scanner scanner;
        try {
            scanner = new Scanner(new FileReader(DatabaseConstant.INVENTORY));
            while (scanner.hasNextLine()) {
                String[] temp = scanner.nextLine().split(",");
                foods.add(new Food(temp[0], Double.parseDouble(temp[1])));
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return foods;
    }

    public Map<Food, Integer> queryAllInventory() {
        Map<Food, Integer> foods = new HashMap<Food, Integer>();
        Scanner scanner;
        try {
            scanner = new Scanner(new FileReader(DatabaseConstant.INVENTORY));
            while (scanner.hasNextLine()) {
                String[] temp = scanner.nextLine().split(",");
                foods.put(new Food(temp[0], Double.parseDouble(temp[1])), Integer.parseInt(temp[2]));
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return foods;
    }

    public void updateInventory() {
        try {
            Map<Food, Integer> stocks = new HashMap<Food, Integer>();
            Scanner scanner = new Scanner(new FileReader(DatabaseConstant.INVENTORY));
            while (scanner.hasNextLine()) {
                String[] temp = scanner.nextLine().split(",");
                int left = Integer.parseInt(temp[2]) + 50;
                stocks.put(new Food(temp[0], Double.parseDouble(temp[1])), left);
            }
            scanner.close();
            PrintWriter pw = new PrintWriter(new FileWriter(DatabaseConstant.INVENTORY));
            for (Food food : stocks.keySet()) {
                pw.println(food.getName() + "," + food.getPrice() + "," + stocks.get(food));
            }
            pw.flush();
            pw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void updateStock(int orderID) {
        try {
            ResultSet result = stat.executeQuery("SELECT * FROM order_detail WHERE order_id=" + orderID);
            Map<String, Integer> use = new HashMap<String, Integer>();
            while (result.next()) {
                use.put(result.getString("name"), result.getInt("quantity"));
            }
            Map<Food, Integer> stocks = new HashMap<Food, Integer>();
            Scanner scanner = new Scanner(new FileReader(DatabaseConstant.INVENTORY));
            while (scanner.hasNextLine()) {
                String[] temp = scanner.nextLine().split(",");
                int left = Integer.parseInt(temp[2]);
                if (use.containsKey(temp[0])) {
                    left -= use.get(temp[0]);
                }
                stocks.put(new Food(temp[0], Double.parseDouble(temp[1])), left);
            }
            scanner.close();
            PrintWriter pw = new PrintWriter(new FileWriter(DatabaseConstant.INVENTORY));
            for (Food food : stocks.keySet()) {
                pw.println(food.getName() + "," + food.getPrice() + "," + stocks.get(food));
            }
            pw.flush();
            pw.close();
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }


    public int insertUser(String account, String password) {
        try {
            ResultSet check = stat.executeQuery("SELECT account FROM users WHERE account='" + account + "'");
            if (check != null && check.next()) {
                return -2;
            } else {
                return stat.executeUpdate("INSERT INTO users (account, password) VALUES('" + account + "', '" + password + "')");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int loginOrRegister(String account, String password) {
        try {
            ResultSet check = stat.executeQuery("SELECT * FROM users WHERE account='" + account + "'");
            if (check != null && check.next()) {
                if (check.getString("password").equals(password)) {
                    return check.getInt("account_id");
                } else {
                    return -1;
                }
            } else {
                insertUser(account, password);
                return 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public int insertOrder(int accountId, Order order) {
        try {
            ResultSet check = stat.executeQuery("SELECT * FROM orders WHERE order_id=(SELECT max(order_id) FROM orders)");
            if (check != null && check.next()) {
                order.setID(check.getInt("order_id") + 1);
            } else {
                order.setID(0);
            }
            stat.executeUpdate("INSERT INTO orders VALUES(" + order.getID() + ", '" + accountId + "', " + order.getTotal() +", '" + order.getStatus() + "')");
            for (Map.Entry<Food, Integer> entry : order.getIngredients().entrySet()) {
                stat.executeUpdate("INSERT INTO order_detail VALUES(" + order.getID() + ", '" + entry.getKey().getName() + "', " + entry.getKey().getPrice() + ", " + entry.getValue() + ")");
            }
            return 1;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void deleteOrder(int order_id) {
        try {
            stat.executeUpdate("DELETE FROM orders order_detail WHERE order_id=" + order_id);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Order queryOrder(int orderID) {
        try {
            ResultSet check = stat.executeQuery("SELECT * FROM orders WHERE order_id=" + orderID);
            Order order = null;
            if(check.next()) {
                int accountId = check.getInt("account_id");
                ResultSet userCheck = stat.executeQuery("SELECT * FROM users WHERE account_id='" + accountId + "'");
                userCheck.next();
                User user = new User(userCheck.getInt("account_id"), userCheck.getString("account"), userCheck.getString("password"));
                order = new Order(check.getInt("order_id"), user, check.getString("status"), check.getDouble("total"));
            }
            if (order != null) {
                ResultSet single = stat.executeQuery("SELECT * FROM order_detail WHERE order_id=" + order.getID());
                while(single.next()) {
                    order.addIngredient(new Food(single.getString("name"), single.getDouble("price")), single.getInt("quantity"));
                }
            }
            return order;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void updatePartialOrder(int orderID) {
        try {
            stat.executeUpdate("UPDATE orders SET status='submitting' WHERE order_id=" + orderID);
            Map<Food, Integer> inventory = queryAllInventory();
            Order order = queryOrder(orderID);
            double total = 0;
            if (order != null) {
                for(Food food : order.getIngredients().keySet()) {
                    for (Food stock : inventory.keySet()) {
                        if (stock.equals(food)) {
                            if (inventory.get(stock) == 0) {
                                stat.executeUpdate("DELETE FROM order_detail WHERE order_id=" + orderID + " and name='" + stock.getName() + "'");
                            } else if (inventory.get(stock) < order.getIngredients().get(food)) {
                                total += stock.getPrice() * inventory.get(stock);
                                stat.executeUpdate("UPDATE order_detail SET quantity=" + inventory.get(stock) + " WHERE order_id=" + orderID + " and name='" + stock.getName() + "'");
                            } else {
                                total += food.getPrice() * order.getIngredients().get(food);
                            }
                        }
                    }
                }
                stat.executeUpdate("UPDATE orders SET total=" + (total * 1.3) + " WHERE order_id=" + orderID);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<Order> queryOrderByAccountID(int accountId) {
        ArrayList<Order> orders = new ArrayList<Order>();
        User user = null;
        try {
            ResultSet check = stat.executeQuery("SELECT * FROM users WHERE account_id='" + accountId + "'");
            check.next();
            user = new User(check.getInt("account_id"), check.getString("account"), check.getString("password"));
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try {
            ResultSet check = stat.executeQuery("SELECT * FROM orders WHERE account_id='" + accountId + "' ORDER BY order_id");
            while(check.next()) {
                orders.add(new Order(check.getInt("order_id"), user, check.getString("status"), check.getDouble("total")));
            }
            for (Order order : orders) {
                ResultSet single = stat.executeQuery("SELECT * FROM order_detail WHERE order_id=" + order.getID());
                while(single.next()) {
                    order.addIngredient(new Food(single.getString("name"), single.getDouble("price")), single.getInt("quantity"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }

    public int updateOrderSubmitting(int orderID) {
        try {
            return stat.executeUpdate("UPDATE orders SET status='receiving' WHERE order_id=" + orderID);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public ArrayList<Order> queryOrderByStatus(String status) {
        ArrayList<Order> orders = new ArrayList<Order>();
        User user = null;
        try {
            ResultSet check = stat.executeQuery("SELECT * FROM orders WHERE status=" + status + " ORDER BY order_id");
            while(check.next()) {
                int accountId = check.getInt("account_id");
                ResultSet userCheck = stat.executeQuery("SELECT * FROM users WHERE account_id='" + accountId + "'");
                userCheck.next();
                user = new User(userCheck.getInt("account_id"), userCheck.getString("account"), userCheck.getString("password"));
                orders.add(new Order(check.getInt("order_id"), user, check.getString("status"), check.getDouble("total")));
            }
            for (Order order : orders) {
                ResultSet single = stat.executeQuery("SELECT * FROM order_detail WHERE order_id=" + order.getID());
                while(single.next()) {
                    order.addIngredient(new Food(single.getString("name"), single.getDouble("price")), single.getInt("quantity"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }

    public int updateOrderReceiving(int orderID) {
        try {
            return stat.executeUpdate("UPDATE orders SET status='preparing' WHERE order_id=" + orderID);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int updateOrderPreparing(int orderID) {
        try {
            return stat.executeUpdate("UPDATE orders SET status='packaging' WHERE order_id=" + orderID);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int updateOrderFoodReady(int orderID) {
        try {
            return stat.executeUpdate("UPDATE orders SET status='Finish' WHERE order_id=" + orderID);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int updateOrderPackaging(int orderID) {
        try {
            return stat.executeUpdate("UPDATE orders SET status='Food Ready' WHERE order_id=" + orderID);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
    