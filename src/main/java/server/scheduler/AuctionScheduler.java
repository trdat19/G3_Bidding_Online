package server.scheduler;

import server.dao.AuctionDAO;
import server.model.core.Auction;
import server.service.AuctionService;
import shared.enums.AuctionStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * AuctionScheduler - chạy nền, kiểm tra tự động
 *  1. Mở phiên khi đến startTime
 *  2. Đóng phiên khi quá endTime
 *
 * Khởi động cùng Server, chạy mỗi 10 giây
 *
 * Singleton
 */
public class AuctionScheduler {
    private static final int CHECK_INTERVAL_MS = 10 * 1000; // 10 giây

    private static AuctionScheduler instance;

    private final AuctionDAO auctionDAO = new AuctionDAO();
    private final AuctionService auctionService = AuctionService.getInstance();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    private AuctionScheduler() {}

    public static AuctionScheduler getInstance() {
        if (instance == null) {
            synchronized (AuctionScheduler.class) {
                if (instance == null) {
                    instance = new AuctionScheduler();
                }
            }
        }
        return instance;
    }

    //-----Khi mới khởi động Server, gọi hàm này để bắt đầu chạy Scheduler
    public void start() {
        scheduler.scheduleAtFixedRate(this::checkAuctions, 0, CHECK_INTERVAL_MS, TimeUnit.MILLISECONDS);
        System.out.println(">>> AuctionScheduler đã bắt đầu chạy...");
    }

    private void checkAuctions() {
        try {
            LocalDateTime now = LocalDateTime.now();

            // 1. Lấy các phiên tới startTime, mở
            List<Auction> approved = auctionDAO.getAllAuctionsByStatus(AuctionStatus.OPEN);

            for (Auction auction : approved) {
                if (auction.getStartTime() != null && !now.isBefore(auction.getStartTime())) {
                    auctionDAO.updateStatus(auction.getId(),  AuctionStatus.RUNNING);
                    System.out.println(">>> Đã mở phiên đấu giá: " + auction.getId());
                }
            }

            // 2. Đóng phiên quá endTime
            // Lấy các phiên đang chạy hoặc đã mở, kiểm tra endTime
            List<Auction> activeAuctions = auctionDAO.getAllAuctionsByStatus(AuctionStatus.OPEN);
            activeAuctions.addAll(auctionDAO.getAllAuctionsByStatus(AuctionStatus.RUNNING));

            for (Auction auction : activeAuctions) {
                if (auction.getEndTime() != null && now.isAfter(auction.getEndTime())) {
                    auctionService.finishAuction(auction.getId());
                    System.out.println(">>> Đã đóng phiên đấu giá: " + auction.getId());
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi kiểm tra phiên đấu giá: " + e.getMessage());
        }
    }

    //----------Hàm này để dừng-------------------
    public void stop() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
            System.out.println(">>> AuctionScheduler đã dừng.");
        }
        catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
