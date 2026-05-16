package server.model.item;

import shared.enums.ItemCategory;
import shared.enums.ItemStatus;
import java.math.BigDecimal;

public class Electronics extends Item {

    private static final long serialVersionUID = 1L;

    private String brand;
    private int warrantyMonth;
    private String model;
    private int year;

    public Electronics() {}

    public Electronics(String nameItem, String description, Long sellerId,
                       ItemStatus statusItem) {
        super(nameItem, description, sellerId, ItemCategory.ELECTRONICS, statusItem);
        //set thuoc tinh rieng
    }

    //getter
    public String getBrand() { return brand; }
    public int getWarrantyMonth() { return warrantyMonth; }
    public String getModel() { return model; }
    public int getYear() { return year; }

    //setter
    public void setBrand(String brand) { this.brand = brand; }
    public void setWarrantyMonth(int warrantyMonth) { this.warrantyMonth = warrantyMonth; }
    public void setModel(String model) { this.model = model; }
    public void setYear(int year) { this.year = year; }

    @Override
    public String getInfo() {
        return String.format("Electronic: %s | Brand: %s | Model: %s | WarrantyMonth: %d | Year: %d",
                nameItem, brand, model, warrantyMonth, year);
    }
}