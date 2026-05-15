package server.model.item.factory;

import server.model.item.Item;
import server.model.item.Vehicle;

public class VehicleFactory implements ItemFactory {

    @Override
    public Item createItem() {
        return new Vehicle();
    }
}