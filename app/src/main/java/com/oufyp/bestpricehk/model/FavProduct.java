package com.oufyp.bestpricehk.model;

import com.oufyp.bestpricehk.R;

public class FavProduct {
    private int[] icons = {R.drawable.ic_baby, R.drawable.ic_beer, R.drawable.ic_beverage, R.drawable.ic_biscuit,
            R.drawable.ic_bread, R.drawable.ic_cakes, R.drawable.ic_dairy, R.drawable.ic_household,
            R.drawable.ic_powder, R.drawable.ic_noddles, R.drawable.ic_oil, R.drawable.ic_rice,
            R.drawable.ic_snack, R.drawable.ic_wine,
    };
    private String id;
    private String name;
    private String[] discount = new String[4];
    private String[] price = new String[4];
    private String displayFlag;
    private String unitPrice;
    private String subTotal;

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
        this.price = price;
    }

    public String getDisplayFlag() {
        return displayFlag;
    }

    public void setDisplayFlag(String displayFlag) {
        this.displayFlag = displayFlag;
    }

    public String getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(String unitPrice) {
        this.unitPrice = unitPrice;
    }

    public String getSubTotal() {
        return subTotal;
    }

    public void setSubTotal(String subTotal) {
        this.subTotal = subTotal;
    }


}
