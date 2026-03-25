package server.model;

public class Seller extends User{
    private Item[] items;
    private int currentItemOnSale = 0; //số lượng sp đang bán

    //constructor
    public Seller(String id, String name) {
        super(id, name);
        this.items = new Item[20]; //giả sử cơ chế seller chỉ đăng bán đc 20 sản phẩm cùng lúc, muốn thêm phải mua
    }

    //thêm, xoá sản phẩm trong ds bán
    public void addItem(Item newItem) {
        Item deepCopy = new Item(newItem.getId(), newItem.getName(), newItem.getCurrentPrice());
        items[currentItemOnSale] = deepCopy;
        currentItemOnSale += 1;
    }
    public void removeItem(){};

    //đưa ra ds sp
    public void displayItems() {
        for (int i = 0; i < currentItemOnSale; i ++) {
            items[i].display();
        }
    }


}
