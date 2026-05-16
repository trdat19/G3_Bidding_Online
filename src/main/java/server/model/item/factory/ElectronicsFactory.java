package server.model.item.factory;

import server.model.item.Electronics;
import server.model.item.Item;

public class ElectronicsFactory implements ItemFactory {

    @Override
    public Item createItem() {
        return new Electronics();
    }
}