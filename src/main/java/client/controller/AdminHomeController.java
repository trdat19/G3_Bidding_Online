package client.controller;

import client.service.ClientNetworkService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import shared.dto.common.AdminDashboardDTO;
import shared.dto.request.BaseRequest;
import shared.dto.response.BaseResponse;
import shared.enums.Action;

public class AdminHomeController {
    @FXML private Label totalUsersLabel;
    @FXML private Label runningAuctionsLabel;
    @FXML private Label finishedAuctionsLabel;
    @FXML private Label totalProductsLabel;
    @FXML private Label runningAuctions;
    @FXML private Label finishedAuctions;
    @FXML private Label preparingAuctions;

    @FXML
    private void initialize() {
        loadDashboardStats();
    }

    private void loadDashboardStats() {
        BaseResponse response = ClientNetworkService.getInstance()
                .sendRequest(new BaseRequest(Action.GET_ADMIN_DASHBOARD_STATS, null));

        if (response != null && response.isSuccess()) {
            AdminDashboardDTO dto = (AdminDashboardDTO) response.getData();

            totalUsersLabel.setText(String.valueOf(dto.getTotalUsers()));
            runningAuctionsLabel.setText(String.valueOf(dto.getRunningAuctions()));
            finishedAuctionsLabel.setText(String.valueOf(dto.getFinishedAuctions()));
            totalProductsLabel.setText(String.valueOf(dto.getTotalProducts()));

            runningAuctions.setText(String.valueOf(dto.getRunningAuctions()));
            finishedAuctions.setText(String.valueOf(dto.getFinishedAuctions()));
            preparingAuctions.setText(String.valueOf(dto.getWaitingAuctions()));
        }
    }
}
