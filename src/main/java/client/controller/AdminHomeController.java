package client.controller;

import client.service.ClientNetworkService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import shared.dto.AdminDashboardDTO;
import shared.dto.request.BaseRequest;
import shared.dto.response.BaseResponse;
import shared.enums.Action;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class AdminHomeController implements AdminPageLifecycle {
    @FXML private Label totalUsersLabel;
    @FXML private Label runningAuctionsLabel;
    @FXML private Label finishedAuctionsLabel;
    @FXML private Label totalProductsLabel;
    @FXML private Label pendingRequestsLabel;
    @FXML private Label runningSummaryLabel;
    @FXML private Label finishedSummaryLabel;
    @FXML private Label requestTaskLabel;
    @FXML private Label auctionTaskLabel;
    @FXML private Label userTaskLabel;
    @FXML private Label productTaskLabel;
    @FXML private Label dashboardStatusLabel;

    private final ExecutorService requestExecutor = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "Admin-Home-Request");
        thread.setDaemon(true);
        return thread;
    });
    private final AtomicBoolean loading = new AtomicBoolean();
    private final Consumer<BaseResponse> realtimeListener = this::handleRealtimeEvent;
    private volatile boolean pageVisible;

    @FXML
    private void initialize() {
        setUnavailable();
        dashboardStatusLabel.setText("Đang tải dữ liệu tổng quan...");
        ClientNetworkService.getInstance().addEventListener(realtimeListener);
    }

    @Override
    public void onPageShown() {
        pageVisible = true;
        refreshData();
    }

    @Override
    public void onPageHidden() {
        pageVisible = false;
    }

    @Override
    public void dispose() {
        pageVisible = false;
        ClientNetworkService.getInstance().removeEventListener(realtimeListener);
        requestExecutor.shutdown();
    }

    @FXML
    public void handleRefresh() {
        refreshData();
    }

    public void refreshData() {
        if (!pageVisible || !loading.compareAndSet(false, true)) {
            return;
        }

        dashboardStatusLabel.setText("Đang tải dữ liệu tổng quan...");
        requestExecutor.execute(() -> {
            BaseResponse response = ClientNetworkService.getInstance()
                    .sendRequest(new BaseRequest(Action.GET_ADMIN_DASHBOARD_SUMMARY, null));

            Platform.runLater(() -> {
                loading.set(false);
                if (!pageVisible) {
                    return;
                }

                if (response == null
                        || !response.isSuccess()
                        || !(response.getData() instanceof AdminDashboardDTO)) {
                    setUnavailable();
                    dashboardStatusLabel.setText("Không tải được dữ liệu tổng quan từ server.");
                    return;
                }

                applySummary((AdminDashboardDTO) response.getData());
            });
        });
    }

    private void handleRealtimeEvent(BaseResponse response) {
        if (!"ADMIN_REQUESTS_CHANGED".equals(response.getAction())) {
            return;
        }

        Platform.runLater(() -> {
            if (pageVisible) {
                refreshData();
            }
        });
    }

    private void applySummary(AdminDashboardDTO summary) {
        totalUsersLabel.setText(String.valueOf(summary.getTotalUsers()));
        runningAuctionsLabel.setText(String.valueOf(summary.getRunningAuctions()));
        finishedAuctionsLabel.setText(String.valueOf(summary.getFinishedAuctions()));
        totalProductsLabel.setText(String.valueOf(summary.getTotalProducts()));
        pendingRequestsLabel.setText(String.valueOf(summary.getPendingRequests()));
        runningSummaryLabel.setText(String.valueOf(summary.getRunningAuctions()));
        finishedSummaryLabel.setText(String.valueOf(summary.getFinishedAuctions()));

        requestTaskLabel.setText(summary.getPendingRequests() + " yêu cầu đang chờ duyệt");
        auctionTaskLabel.setText(summary.getRunningAuctions() + " phiên đang diễn ra");
        userTaskLabel.setText(summary.getBlockedUsers() + " tài khoản đang bị khóa");
        productTaskLabel.setText(summary.getActiveProducts() + " sản phẩm đang hoạt động");
        dashboardStatusLabel.setText("Số liệu được cập nhật trực tiếp từ hệ thống.");
    }

    private void setUnavailable() {
        totalUsersLabel.setText("--");
        runningAuctionsLabel.setText("--");
        finishedAuctionsLabel.setText("--");
        totalProductsLabel.setText("--");
        pendingRequestsLabel.setText("--");
        runningSummaryLabel.setText("--");
        finishedSummaryLabel.setText("--");
        requestTaskLabel.setText("Chưa lấy được dữ liệu yêu cầu duyệt");
        auctionTaskLabel.setText("Chưa lấy được dữ liệu phiên đấu giá");
        userTaskLabel.setText("Chưa lấy được dữ liệu người dùng");
        productTaskLabel.setText("Chưa lấy được dữ liệu sản phẩm");
    }
}
