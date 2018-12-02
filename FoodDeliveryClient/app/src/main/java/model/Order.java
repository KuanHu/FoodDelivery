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
	private String account;
	private String status;
	private Map<Food, Integer> ingredients;
	private double total;
	
	public Order(int id, String account, String status, Map<Food, Integer> ingredients, double total) {
		this.id = id;
		this.account = account;
		this.status = status;
		this.ingredients = ingredients;
		this.total = total;
	}

	public Order(){

	}
	
	public Order(int id, String account, String status, double total) {
		this.id = id;
		this.account = account;
		this.status = status;
		this.ingredients = new HashMap<Food, Integer>();
		this.total = total;
	}
	
	public int getID() {
		return this.id;
	}
	
	public void setID(int id) {
		this.id = id;
	}
	
	public String getAccount() {
		return this.account;
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
