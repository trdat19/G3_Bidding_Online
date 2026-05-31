package client.controller;

import client.service.ClientNetworkService;
import client.util.StageUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import shared.dto.request.BaseRequest;
import shared.dto.response.BaseResponse;
import shared.enums.Action;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class AdminDashboardController {
    private static final String HOME_PAGE = "/view/admin/admin-home.fxml";
    private static final String USERS_PAGE = "/view/admin/admin-users.fxml";
    private static final String AUCTIONS_PAGE = "/view/admin/admin-auctions.fxml";
    private static final String PRODUCTS_PAGE = "/view/admin/admin-products.fxml";
    private static final String REPORTS_PAGE = "/view/admin/admin-reports.fxml";
    private static final String SETTINGS_PAGE = "/view/admin/admin-settings.fxml";
    private static final String REQUEST_PAGE = "/view/admin/admin-request.fxml";

    @FXML private StackPane contentPane;

    private final Map<String, Parent> pageCache = new HashMap<>();
    private final Map<String, Object> pageControllerCache = new HashMap<>();
    private String currentPage;

    @FXML
    private void initialize() {
        loadPage(HOME_PAGE);
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            BaseResponse response = ClientNetworkService.getInstance()
                    .sendRequest(new BaseRequest(Action.LOGOUT, null));

            if (response != null && response.isSuccess()) {
                disposePages();
                Parent root = FXMLLoader.load(getClass().getResource("/view/login.fxml"));
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                StageUtils.setMaximizedScene(stage, root);
                stage.show();
                return;
            }

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Đăng xuất thất bại");
            alert.setHeaderText(null);
            alert.setContentText(response != null
                    ? response.getMessage()
                    : "Không kết nối được server");
            alert.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
            refreshCurrentPage();
            return;
        }

        try {
            Parent page = pageCache.get(fxmlPath);

            if (page == null) {
                URL pageUrl = getClass().getResource(fxmlPath);
                if (pageUrl == null) {
                    throw new IOException("FXML not found: " + fxmlPath);
                }

                FXMLLoader loader = new FXMLLoader(pageUrl);
                page = loader.load();
                pageCache.put(fxmlPath, page);
                pageControllerCache.put(fxmlPath, loader.getController());
            }

            pauseCurrentPage();
            contentPane.getChildren().setAll(page);
            currentPage = fxmlPath;
            activateCurrentPage();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void pauseCurrentPage() {
        Object controller = pageControllerCache.get(currentPage);
        if (controller instanceof AdminPageLifecycle) {
            ((AdminPageLifecycle) controller).onPageHidden();
        }
    }

    private void disposePages() {
        for (Object controller : pageControllerCache.values()) {
            if (controller instanceof AdminPageLifecycle) {
                ((AdminPageLifecycle) controller).dispose();
            }
        }
    }

    private void activateCurrentPage() {
        Object controller = pageControllerCache.get(currentPage);
        if (controller instanceof AdminPageLifecycle) {
            ((AdminPageLifecycle) controller).onPageShown();
        }
    }

    private void refreshCurrentPage() {
        Object controller = pageControllerCache.get(currentPage);
        if (controller instanceof AdminPageLifecycle) {
            ((AdminPageLifecycle) controller).onPageShown();
        }
    }
}
