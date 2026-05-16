package server.controller;

import server.network.ClientConnectionHandler;
import server.service.ItemService;
import shared.dto.request.item.CreateItemRequest;
import shared.dto.request.item.DeleteItemRequest;
import shared.dto.response.BaseResponse;

public class SellerServerController {
    private final ItemService itemService = new ItemService();
    public BaseResponse createItem(CreateItemRequest request, ClientConnectionHandler handler)
    {
        if (handler.getUser() == null) {
            return new BaseResponse(false, "Bạn cần đăng nhập");
        }

        return itemService.createItem(request, handler.getUser());

    }
    public BaseResponse getSellerItems(ClientConnectionHandler handler) {
        if (handler.getUser() == null) {
            return new BaseResponse(false, "Bạn cần đăng nhập");
        }

        return itemService.findBySeller(handler.getUser());
    }
    public void updateItem()
    {

    }
    public BaseResponse deleteItem(DeleteItemRequest request, ClientConnectionHandler handler)
    {
        if (handler.getUser() == null) {
            return new BaseResponse(false, "Bạn cần đăng nhập");
        }

        return itemService.deleteItem(request.getItemId(), handler.getUser());

    }
}
