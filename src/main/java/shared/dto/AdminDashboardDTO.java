package shared.dto;

import java.io.Serializable;

public class AdminDashboardDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private long totalUsers;
    private long blockedUsers;
    private long runningAuctions;
    private long finishedAuctions;
    private long pendingRequests;
    private long totalProducts;
    private long activeProducts;

    public long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public long getBlockedUsers() {
        return blockedUsers;
    }

    public void setBlockedUsers(long blockedUsers) {
        this.blockedUsers = blockedUsers;
    }

    public long getRunningAuctions() {
        return runningAuctions;
    }

    public void setRunningAuctions(long runningAuctions) {
        this.runningAuctions = runningAuctions;
    }

    public long getFinishedAuctions() {
        return finishedAuctions;
    }

    public void setFinishedAuctions(long finishedAuctions) {
        this.finishedAuctions = finishedAuctions;
    }

    public long getPendingRequests() {
        return pendingRequests;
    }

    public void setPendingRequests(long pendingRequests) {
        this.pendingRequests = pendingRequests;
    }

    public long getTotalProducts() {
        return totalProducts;
    }

    public void setTotalProducts(long totalProducts) {
        this.totalProducts = totalProducts;
    }

    public long getActiveProducts() {
        return activeProducts;
    }

    public void setActiveProducts(long activeProducts) {
        this.activeProducts = activeProducts;
    }
}
