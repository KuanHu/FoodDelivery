package model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Order implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	private int id;
	private User user;

	/**
	 * 1. submitting, 2. receiving, 3. preparing, 4. packaging, 5. FoodReady, 6. Finish
	 */
	private String status;
	private Map<Food, Integer> ingredients;
	private double total;

	public Order(){

	}

	public Order(int id, User user, String status, Map<Food, Integer> ingredients, double total) {
		this.id = id;
		this.user = user;
		this.status = status;
		this.ingredients = ingredients;
		this.total = total;
	}

	public Order(int id, User user, String status, double total) {
		this.id = id;
		this.user = user;
		this.status = status;
		this.ingredients = new HashMap<>();
		this.total = total;
	}

	public int getID() {
		return this.id;
	}

	public void setID(int id) {
		this.id = id;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public String getStatus() {
		return this.status;
	}

	public Map<Food, Integer> getIngredients() {
		return this.ingredients;
	}

	public void addIngredient(Food food, int quantity) {
		ingredients.put(food, quantity);
	}

	public double getTotal() {
		return this.total;
	}
}
