package server.concurrency;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class AuctionLockManager {
    private static volatile AuctionLockManager instance;

    private final ConcurrentHashMap<Long, ReentrantLock> locks;

    private AuctionLockManager() {
        locks = new ConcurrentHashMap<>();
    }

    public static AuctionLockManager getInstance() {
        if (instance == null) {
            synchronized (AuctionLockManager.class) {
                if (instance == null) {
                    instance = new AuctionLockManager();
                }
            }
        }
        return instance;
    }

    public ReentrantLock getLock(Long auctionId) {
        // ReentrantLock(true): thread nào đến trước xử lý trước.
        return locks.computeIfAbsent(auctionId, id -> new ReentrantLock(true));
    }

    public void removeLock(Long auctionId) {
        locks.remove(auctionId);
    }
}