package shared.dto;

import java.io.Serializable;

public class AdminDashboardDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private long totalUsers;
    private long totalProducts;
    private long runningAuctions;
    private long finishedAuctions;
    private long waitingAuctions;

    public AdminDashboardDTO(long totalUsers, long totalProducts, long runningAuctions, long finishedAuctions, long waitingAuctions)
    {
        this.totalUsers = totalUsers;
        this.totalProducts = totalProducts;
        this.runningAuctions = runningAuctions;
        this.finishedAuctions = finishedAuctions;
        this.waitingAuctions = waitingAuctions;
    };
    public long getTotalUsers() {
        return totalUsers;
    }

    public long getTotalProducts() {
        return totalProducts;
    }

    public long getRunningAuctions() {
        return runningAuctions;
    }

    public long getFinishedAuctions() {
        return finishedAuctions;
    }
    public long getWaitingAuctions() {
        return waitingAuctions;
    }
}