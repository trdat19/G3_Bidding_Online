package server.network;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import server.model.user.Admin;
import server.model.user.Bidder;
import server.model.user.Seller;
import server.model.user.User;
import shared.dto.request.BaseRequest;
import shared.dto.response.BaseResponse;
import shared.enums.Action;
import shared.enums.UserRole;
import shared.enums.UserStatus;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class RequestRouterTest {

    @Test
    @DisplayName("route - Tra ve false khi request null")
    public void route_nullRequest() {
        ClientConnectionHandler handler = mock(ClientConnectionHandler.class);

        BaseResponse response = RequestRouter.route(null, handler);

        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertNotNull(response.getMessage());
    }

    @Test
    @DisplayName("route - Tra ve false khi action null")
    public void route_nullAction() {
        ClientConnectionHandler handler = mock(ClientConnectionHandler.class);
        BaseRequest request = new BaseRequest(null, null);

        BaseResponse response = RequestRouter.route(request, handler);

        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertNotNull(response.getMessage());
    }

    @Test
    @DisplayName("route - Seller action that bai khi chua dang nhap")
    public void route_createItem_withoutLogin() {
        ClientConnectionHandler handler = mock(ClientConnectionHandler.class);
        when(handler.getUser()).thenReturn(null);

        BaseRequest request = new BaseRequest(Action.CREATE_ITEM, null);

        BaseResponse response = RequestRouter.route(request, handler);

        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().contains("Server"));
    }

    @Test
    @DisplayName("route - Bidder action that bai khi chua dang nhap")
    public void route_placeBid_withoutLogin() {
        ClientConnectionHandler handler = mock(ClientConnectionHandler.class);
        when(handler.getUser()).thenReturn(null);

        BaseRequest request = new BaseRequest(Action.PLACE_BID, null);

        BaseResponse response = RequestRouter.route(request, handler);

        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().contains("Server"));
    }

    @Test
    @DisplayName("route - Admin action that bai khi chua dang nhap")
    public void route_getUsersList_withoutLogin() {
        ClientConnectionHandler handler = mock(ClientConnectionHandler.class);
        when(handler.getUser()).thenReturn(null);

        BaseRequest request = new BaseRequest(Action.GET_USERS_LIST, null);

        BaseResponse response = RequestRouter.route(request, handler);

        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().contains("Server"));
    }

    @Test
    @DisplayName("route - Seller action that bai khi user la Bidder")
    public void route_createItem_wrongRole() {
        ClientConnectionHandler handler = mock(ClientConnectionHandler.class);
        when(handler.getUser()).thenReturn(user(UserRole.BIDDER));

        BaseRequest request = new BaseRequest(Action.CREATE_ITEM, null);

        BaseResponse response = RequestRouter.route(request, handler);

        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().contains("Server"));
    }

    @Test
    @DisplayName("route - Bidder action that bai khi user la Seller")
    public void route_placeBid_wrongRole() {
        ClientConnectionHandler handler = mock(ClientConnectionHandler.class);
        when(handler.getUser()).thenReturn(user(UserRole.SELLER));

        BaseRequest request = new BaseRequest(Action.PLACE_BID, null);

        BaseResponse response = RequestRouter.route(request, handler);

        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().contains("Server"));
    }

    @Test
    @DisplayName("route - Admin action that bai khi user la Seller")
    public void route_getUsersList_wrongRole() {
        ClientConnectionHandler handler = mock(ClientConnectionHandler.class);
        when(handler.getUser()).thenReturn(user(UserRole.SELLER));

        BaseRequest request = new BaseRequest(Action.GET_USERS_LIST, null);

        BaseResponse response = RequestRouter.route(request, handler);

        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().contains("Server"));
    }

    @Test
    @DisplayName("route - DELETE_ITEM that bai khi thieu itemId")
    public void route_deleteItem_missingItemId() {
        ClientConnectionHandler handler = mock(ClientConnectionHandler.class);
        when(handler.getUser()).thenReturn(user(UserRole.SELLER));

        BaseRequest request = new BaseRequest(Action.DELETE_ITEM, null);

        BaseResponse response = RequestRouter.route(request, handler);

        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().contains("itemId"));
    }

    @Test
    @DisplayName("route - DELETE_ITEM that bai khi itemId khong phai so")
    public void route_deleteItem_invalidItemId() {
        ClientConnectionHandler handler = mock(ClientConnectionHandler.class);
        when(handler.getUser()).thenReturn(user(UserRole.SELLER));

        BaseRequest request = new BaseRequest(Action.DELETE_ITEM, "abc");

        BaseResponse response = RequestRouter.route(request, handler);

        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().contains("itemId"));
    }

    @Test
    @DisplayName("route - ENABLE_USER that bai khi thieu userId")
    public void route_enableUser_missingUserId() {
        ClientConnectionHandler handler = mock(ClientConnectionHandler.class);
        when(handler.getUser()).thenReturn(user(UserRole.ADMIN));

        BaseRequest request = new BaseRequest(Action.ENABLE_USER, null);

        BaseResponse response = RequestRouter.route(request, handler);

        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().contains("userId"));
    }

    @Test
    @DisplayName("route - DISABLE_USER that bai khi thieu userId")
    public void route_disableUser_missingUserId() {
        ClientConnectionHandler handler = mock(ClientConnectionHandler.class);
        when(handler.getUser()).thenReturn(user(UserRole.ADMIN));

        BaseRequest request = new BaseRequest(Action.DISABLE_USER, null);

        BaseResponse response = RequestRouter.route(request, handler);

        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().contains("userId"));
    }

    @Test
    @DisplayName("route - SUBSCRIBE_AUCTION that bai khi thieu auctionId")
    public void route_subscribeAuction_missingAuctionId() {
        ClientConnectionHandler handler = mock(ClientConnectionHandler.class);
        when(handler.getUser()).thenReturn(user(UserRole.BIDDER));

        BaseRequest request = new BaseRequest(Action.SUBSCRIBE_AUCTION, null);

        BaseResponse response = RequestRouter.route(request, handler);

        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertNotNull(response.getMessage());
    }

    @Test
    @DisplayName("route - SUBSCRIBE_AUCTION that bai khi auctionId khong phai so")
    public void route_subscribeAuction_invalidAuctionId() {
        ClientConnectionHandler handler = mock(ClientConnectionHandler.class);
        when(handler.getUser()).thenReturn(user(UserRole.BIDDER));

        BaseRequest request = new BaseRequest(Action.SUBSCRIBE_AUCTION, "abc");

        BaseResponse response = RequestRouter.route(request, handler);

        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertNotNull(response.getMessage());
    }

    @Test
    @DisplayName("route - Action ton tai trong enum nhung chua duoc router xu ly")
    public void route_actionNotHandled() {
        ClientConnectionHandler handler = mock(ClientConnectionHandler.class);
        BaseRequest request = new BaseRequest(Action.GET_AUTO_BID_RULE, null);

        BaseResponse response = RequestRouter.route(request, handler);

        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertNotNull(response.getMessage());
        assertTrue(response.getMessage().contains("khong") || response.getMessage().contains("kh"));
    }

    private User user(UserRole role) {
        User user;

        if (role == UserRole.ADMIN) {
            user = new Admin();
        } else if (role == UserRole.SELLER) {
            user = new Seller();
        } else {
            user = new Bidder();
        }

        user.setId(1L);
        user.setUsername("test_user");
        user.setFullname("Test User");
        user.setEmail("test@example.com");
        user.setRole(role);
        user.setStatus(UserStatus.ACTIVE);

        return user;
    }
}