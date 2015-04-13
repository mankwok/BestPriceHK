package com.oufyp.bestpricehk.model;

import android.util.Log;

import com.oufyp.bestpricehk.R;

public class FavProduct extends Product {
    private int displayFlag;
    private int qty;

    public FavProduct(String id, String name, String type, String brand, int qty, String[] price, String[] discount, String bestPrice, int displayFlag) {
        super(id, name, type, brand, price, discount, bestPrice);
        this.qty = qty;
        this.displayFlag = displayFlag;
    }

    public int getDisplayFlag() {
        return displayFlag;
    }

    public void setDisplayFlag(int displayFlag) {
        this.displayFlag = displayFlag;
    }

    public int getQty() {
        return qty;
    }

    public void setQty(int qty) {
        this.qty = qty;
    }

    public String getAvailable(int displayFlag) {
        String[] prices = this.getPrice();
        String[] storeName = {"ParknShop", "Wellcome", "Jusco", "Market Place"};
        String available = "";
        if (prices[0].equals(prices[1]) && prices[0].equals(prices[2]) && prices[0].equals(prices[1]) && prices[0].equals("--")) {
            return "Not available in all stores";
        }
        switch (displayFlag) {
            case 0:
                if (!prices[0].equals("--")) {
                    available = "Available in ParknShop";
                } else {
                    available = "Not available in ParknShop";
                }
                break;
            case 1:
                if (!prices[1].equals("--")) {
                    available = "Available in Wellcome";
                } else {
                    available = "Not available in Wellcome";
                }
                break;
            case 2:
                if (!prices[2].equals("--")) {
                    available = "Available in Jusco";
                } else {
                    available = "Not available in Jusco";
                }
                break;
            case 3:
                if (!prices[3].equals("--")) {
                    available = "Available in Market Place";
                } else {
                    available = "Not available in Market Place";
                }
                break;
            case 4:
                available = "Available in " + storeName[this.getBestStore()];
        }
        return available;
    }

    public double getUnitPrice(int displayFlag) {
        String stringPrice;
        if (displayFlag < 4) {
            stringPrice = this.getPrice()[displayFlag];
        } else {
            stringPrice = this.getBestPrice();
        }
        if (stringPrice.equals("9999.00")) {
            return 0.0;
        }
        try {
            return Double.parseDouble(stringPrice);
        } catch (Exception ex) {
            return 0.0;
        }
    }

    public double getSubTotal(int displayFlag) {
        String discount = this.getDisplayDiscount(displayFlag);
        if(!discount.equals("--")){
            try {
                if (discount.contains("save")) {
                    int qty = this.getQty();
                    int saveQty = Integer.parseInt(discount.substring(discount.indexOf(" ")+ 1, discount.indexOf(" ", 5)));
                    double savePrice = Double.parseDouble(discount.substring(discount.indexOf("$") + 1));
                    return (getUnitPrice(displayFlag) * qty) - ((qty/saveQty)* savePrice);
                }else if(discount.contains("at")){
                    int qty = this.getQty();
                    int saveQty = Integer.parseInt(discount.substring(discount.indexOf(" ")+ 1, discount.indexOf(" ", 5)));
                    double savePrice = Double.parseDouble(discount.substring(discount.indexOf("$") + 1));
                    return (getUnitPrice(displayFlag) * (qty % saveQty)) + ((qty/saveQty)* savePrice);
                }
            }catch (NumberFormatException ex){
                return 0.0;
            }
        }
        return getUnitPrice(displayFlag) * getQty();
    }

    public String getDisplayDiscount(int displayFlag) {
        if (displayFlag < 4) {
            return this.getDiscount()[displayFlag];
        } else {
            return this.getDiscount()[this.getBestStore()];
        }
    }

}
