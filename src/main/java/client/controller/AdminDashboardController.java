package client.controller;
import client.service.ClientNetworkService;
import client.util.StageUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import client.model.Item;
import shared.dto.request.BaseRequest;
import shared.dto.response.BaseResponse;
import shared.enums.Action;

public class AdminDashboardController {
    private static final String HOME_PAGE = "/view/admin/admin-home.fxml";
    private static final String USERS_PAGE = "/view/admin/admin-users.fxml";
    private static final String AUCTIONS_PAGE = "/view/admin/admin-auctions.fxml";
    private static final String PRODUCTS_PAGE = "/view/admin/admin-products.fxml";
    private static final String REPORTS_PAGE = "/view/admin/admin-reports.fxml";
    private static final String SETTINGS_PAGE = "/view/admin/admin-settings.fxml";
    private static final String REQUEST_PAGE = "/view/admin/admin-request.fxml";

    @FXML private StackPane contentPane;
    @FXML private TableView<Item> auctionTable;
    @FXML private TableColumn<Item, String> productColumn;
    @FXML private TableColumn<Item, String> sellerColumn;
    @FXML private TableColumn<Item, Double> currentPriceColumn;
    @FXML private TableColumn<Item, String> statusColumn;
    @FXML private TableColumn<Item, String> endTimeColumn;

    private final Map<String, Parent> pageCache = new HashMap<>();
    private String currentPage;

    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            BaseRequest logoutRequest = new BaseRequest(Action.LOGOUT, null);
            BaseResponse response = ClientNetworkService.getInstance().sendRequest(logoutRequest);

            if (response != null && response.isSuccess()) {
                Parent root = FXMLLoader.load(getClass().getResource("/view/login.fxml"));
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                StageUtils.setMaximizedScene(stage, root);
                stage.show();
            }
            else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Đăng xuất thất bại");
                alert.setHeaderText(null);
                alert.setContentText(response != null
                        ? response.getMessage()
                        : "Không kết nối được server");
                alert.showAndWait();
            }
        }catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void  initialize() {
        loadPage(HOME_PAGE);

    }
    @FXML
    private void showDashboard() {
        loadPage(HOME_PAGE);
    }
    @FXML
    private void showUsers() {
        loadPage(USERS_PAGE);

    }
    @FXML
    private void showAuctions() {
        loadPage(AUCTIONS_PAGE);

    }
    @FXML
    private void showProducts() {
        loadPage(PRODUCTS_PAGE);

    }
    @FXML
    private void showReports() {
        loadPage(REPORTS_PAGE);
    }
    @FXML
    private void showSettings() {
        loadPage(SETTINGS_PAGE);
    }
    @FXML
    private void showProductRequests() {
        loadPage(REQUEST_PAGE);
    }

    private void loadPage(String fxmlPath) {
        if (fxmlPath.equals(currentPage)) {
            return;
        }

        try {
            Parent page = pageCache.get(fxmlPath);
            if (page == null) {
                URL pageUrl = getClass().getResource(fxmlPath);
                if (pageUrl == null) {
                    throw new IOException("FXML not found: " + fxmlPath);
                }
                page = FXMLLoader.load(pageUrl);
                pageCache.put(fxmlPath, page);
            }

            contentPane.getChildren().clear();
            contentPane.getChildren().add(page);
            currentPage = fxmlPath;
        }catch (IOException e) {
            e.printStackTrace();
        }
    }
}
