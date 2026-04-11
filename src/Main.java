package server;

import server.model.Seller;
import server.model.Item;

public class Main {
    public static void main(String[] args) {
        //cái đoạn dưới này để test thôi
        Seller sl1 = new Seller("SL01", "TruongThanhDat");
        Item it1 = new Item("01", "Laptop", 100);
        Item it2 = new Item("02", "smartphone", 50);

        sl1.addItem(it1);
        sl1.addItem(it2);
        sl1.displayItems();

    }
}