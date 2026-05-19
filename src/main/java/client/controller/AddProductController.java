package client.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import shared.enums.ItemCategory;

import java.io.File;
import client.service.ClientNetworkService;
import shared.dto.request.BaseRequest;
import shared.dto.response.BaseResponse;
import shared.enums.Action;

import java.util.HashMap;
import java.util.Map;



public class AddProductController {
    @FXML private TextField nameField;
    @FXML private ComboBox<ItemCategory> categoryBox;
    @FXML private TextArea descriptionField;
    @FXML private Label errorLabel;
    @FXML private ImageView productImageView;
    @FXML private Label imageNameLabel;
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
        data.put("imageUrl", selectedImageFile.toURI().toString());


        BaseRequest request = new BaseRequest(Action.CREATE_ITEM, data);
        BaseResponse response = ClientNetworkService.getInstance().sendRequest(request);

        if (response != null && response.isSuccess() && response.getData() != null)  {
            if (sellerDashboardController != null) {
                sellerDashboardController.refreshProducts();
            }
            closeWindow();
        }
        else {
            errorLabel.setText(response != null ? response.getMessage() : "Không kết nối được server");
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }
    private void closeWindow() {
        Stage stage = (Stage) nameField.getScene().getWindow();
        stage.close();
    }
}
