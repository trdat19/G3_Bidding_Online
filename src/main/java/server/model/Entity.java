package server.model;

import java.io.Serial;
import java.io.Serializable;

public abstract class Entity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    protected Long id; // dùng Long, không phải String

    public Entity() {}

    public Entity(Long id) {
        this.id = id;
    }

    // getter
    public Long getId() {
        return id;
    }

    // setter (CẦN thiết khi dùng AUTO_INCREMENT)
    public void setId(Long id) {
        this.id = id;
    }

    public abstract String getInfo();

    @Override
    public String toString() {
        return getInfo();
    }
}