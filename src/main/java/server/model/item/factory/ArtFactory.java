package server.model.item.factory;

import server.model.item.Art;
import server.model.item.Item;

public class ArtFactory implements ItemFactory{

    @Override
    public Item createItem() {
        return new Art();
    }
}
