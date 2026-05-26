package client.controller;

import client.model.Item;
import client.service.ClientNetworkService;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import shared.dto.request.BaseRequest;
import shared.dto.response.BaseResponse;
import shared.enums.Action;
import shared.enums.ItemCategory;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class EditProductController {
    @FXML private TextField nameField;
    @FXML private ComboBox<ItemCategory> categoryBox;
    @FXML private TextArea descriptionField;
    @FXML private Label errorLabel;
    @FXML private ImageView productImageView;
    @FXML private Label imageNameLabel;
    private Item editingItem;
    private File selectedImageFile;


    private SellerDashboardController sellerDashboardController;
    public void setSellerDashboardController(SellerDashboardController sellerDashboardController) {
        this.sellerDashboardController = sellerDashboardController;
    }
    @FXML
    private void initialize() {     //tao lua chon ngay gio cho nguoi dung
        errorLabel.setText("");
        categoryBox.getItems().setAll(ItemCategory.values());
    }

    @FXML
    private void handleChooseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Chọn ảnh sản phẩm");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter(
                        "Image Files",
                        "*.png",
                        "*.jpg",
                        "*.jpeg",
                        "*.gif"
                )
        );
        Stage stage = (Stage) nameField.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {

            selectedImageFile = file;

            Image image = new Image(file.toURI().toString());

            productImageView.setImage(image);

            imageNameLabel.setText(file.getName());
        }
    }
    @FXML
    private void handleSave() {
        errorLabel.setText("");

        String title = nameField.getText().trim();
        ItemCategory category = categoryBox.getValue();
        String description = descriptionField.getText().trim();

        if (title.isEmpty()
                || category == null
                || description.isEmpty()) {

            errorLabel.setText("Vui lòng nhập đầy đủ thông tin");
            return;
        }
        if (selectedImageFile == null) {
            errorLabel.setText("Vui lòng chọn ảnh sản phẩm");
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("name", title);
        data.put("category", category.name());
        data.put("description", description);
        data.put("id", editingItem.getId());
        data.put("imageUrl", selectedImageFile.toURI().toString());
        BaseResponse response = ClientNetworkService.getInstance()
                .sendRequest(new BaseRequest(Action.UPDATE_ITEM, data));

        if (response == null || !response.isSuccess()) {
            errorLabel.setText(response != null ? response.getMessage() : "Khong ket noi duoc server");
            return;
        }

        if (sellerDashboardController != null) {
            sellerDashboardController.refreshProducts();
        }

        closeWindow();
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }
    private void closeWindow() {
        Stage stage = (Stage) nameField.getScene().getWindow();
        stage.close();
    }

    public void setData(Item item, SellerDashboardController sellerDashboardController) {
        this.editingItem = item;
        this.sellerDashboardController = sellerDashboardController;

        nameField.setText(item.getTitle());
        categoryBox.setValue(ItemCategory.valueOf(item.getCategory()));
        descriptionField.setText(item.getDescription());
        imageNameLabel.setText("Giữ ảnh hiện tại");
    }
}