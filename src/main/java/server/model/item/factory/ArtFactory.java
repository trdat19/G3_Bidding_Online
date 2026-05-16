package server.model.item.factory;

import server.model.item.Art;
import server.model.item.Item;

public class ArtFactory implements ItemFactory{

    public Item createItem() {
        Art art = new Art();
        return art;
    }
}
