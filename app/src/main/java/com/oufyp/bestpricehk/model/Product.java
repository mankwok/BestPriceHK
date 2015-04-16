package com.oufyp.bestpricehk.model;

import com.oufyp.bestpricehk.R;

import java.io.Serializable;

public class Product implements Serializable {
    private int[] icons = {R.drawable.ic_baby, R.drawable.ic_beer, R.drawable.ic_beverage, R.drawable.ic_biscuit,
            R.drawable.ic_bread, R.drawable.ic_cakes, R.drawable.ic_dairy, R.drawable.ic_household,
            R.drawable.ic_powder, R.drawable.ic_noddles, R.drawable.ic_oil, R.drawable.ic_rice,
            R.drawable.ic_snack, R.drawable.ic_wine,
    };
    private String id;
    private String name;
    private String type;
    private String brand;
    private String[] price = new String[4];
    private String[] discount = new String[4];
    private int countFav;
    private int countShare;
    private String bestPrice;

    public Product() {
        //empty constructor
    }

    public Product(String id, String name, String type, String brand) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.brand = brand;
    }

    public Product(String id, String name, String type, String brand, String[] price, String[] discount,String bestPrice) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.brand = brand;
        setPrice(price);
        this.discount = discount;
        setBestPrice(bestPrice);
    }

    public Product(String id, String name, String type, String brand, int countFav, int countShare, String bestPrice) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.brand = brand;
        this.countFav = countFav;
        this.countShare = countShare;
        if (bestPrice == null || bestPrice.equals("null") || bestPrice.equals("9999.00")) {
            this.bestPrice = "none";
        } else {
            this.bestPrice = bestPrice;
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String[] getDiscount() {
        return discount;
    }

    public void setDiscount(String[] discount) {
        this.discount = discount;
    }

    public String[] getPrice() {
        return price;
    }

    public void setPrice(String[] price) {
        for (int i = 0; i < price.length; i++) {
            if (price[i].equals("9999.00")) { // 9999.00 is the tag of no price
                price[i] = "--";
            }
        }
        this.price = price;
    }

    public int getCountFav() {
        return countFav;
    }

    public void setCountFav(int countFav) {
        this.countFav = countFav;
    }

    public int getCountShare() {
        return countShare;
    }

    public void setCountShare(int countShare) {
        this.countShare = countShare;
    }

    public String getDescription() {
        return "Name: " + this.getName() + "\nType: " + this.getType() + "\nBrand: " + this.getBrand();
    }

    public int getImage(String type) {
        switch (type) {
            case "Baby care":
                return icons[0];
            case "Beer":
                return icons[1];
            case "Beverages":
                return icons[2];
            case "Biscuits":
                return icons[3];
            case "Bread":
                return icons[4];
            case "Cakes":
                return icons[5];
            case "Dairy":
                return icons[6];
            case "Household":
                return icons[7];
            case "Milk Powder":
                return icons[8];
            case "Noodles":
                return icons[9];
            case "Oil":
                return icons[10];
            case "Rice":
                return icons[11];
            case "Snacks":
                return icons[12];
            case "Wine":
                return icons[13];
            default:
                return icons[0];
        }
    }

    public void setBestPrice(String bestPrice) {
        this.bestPrice = bestPrice.equals("9999.00") ? "none" : bestPrice;
    }

    public String getBestPrice() {
        return bestPrice;
    }

    public int getBestStore() {
        String[] price = this.getPrice();
        String bestPrice = this.getBestPrice();
        for (int index = 0; index < price.length; index++) {
            if (price[index].equals(bestPrice)) {
                return index;
            }
        }
        return -1;
    }

}
