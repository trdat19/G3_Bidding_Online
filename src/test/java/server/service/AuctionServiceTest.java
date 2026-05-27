package server.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import server.dao.AuctionDAO;
import server.dao.BidDAO;
import server.dao.ItemDAO;
import server.dao.UserDAO;
import server.model.core.Auction;
import server.model.core.Bid;
import server.model.item.Electronics;
import server.model.item.Item;
import server.model.user.Seller;
import server.model.user.User;
import shared.dto.common.AuctionDTO;
import shared.enums.AuctionStatus;
import shared.enums.ItemStatus;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuctionServiceTest {

    @Mock private AuctionDAO auctionDAO;
    @Mock private ItemDAO itemDAO;
    @Mock private UserDAO userDAO;
    @Mock private BidDAO bidDAO;

    private AuctionService auctionService;

    @BeforeEach
    public void setUp() throws Exception {
        SingletonTestUtil.resetSingleton(AuctionService.class);
        auctionService = AuctionService.getInstance();
        inject("auctionDAO", auctionDAO);
        inject("itemDAO", itemDAO);
        inject("userDAO", userDAO);
        inject("bidDAO", bidDAO);
    }

    private void inject(String name, Object mock) throws Exception {
        Field field = AuctionService.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(auctionService, mock);
    }

    //--------------CREATE----------------------
    @Test
    @DisplayName("createAuction - Sản phẩm được tạo phiên và đánh dấu chờ duyệt!")
    public void createAuction_success() {
        LocalDateTime start = LocalDateTime.now().plusHours(1);
        LocalDateTime end = start.plusHours(2);
        Item item = item(10L, 7L, ItemStatus.PENDING);
        User seller = user(7L, "Seller One");

        when(itemDAO.findById(10L)).thenReturn(item);
        when(auctionDAO.getAllAuctionsByItemId(10L)).thenReturn(List.of());
        when(auctionDAO.insertAuction(any(Auction.class))).thenAnswer(invocation -> {
            Auction auction = invocation.getArgument(0);
            auction.setId(99L);
            return true;
        });
        when(bidDAO.countBidByAuctionId(99L)).thenReturn(0);
        when(userDAO.findById(7L)).thenReturn(seller);

        AuctionDTO result = auctionService.createAuction(auctionData(10L, 7L, start, end));

        assertNotNull(result);
        assertEquals(99L, result.getId());
        assertEquals(10L, result.getItemId());
        assertEquals("Laptop", result.getItemName());
        assertEquals(new BigDecimal("100.00"), result.getStartPrice());
        assertEquals(AuctionStatus.WAITING_APPROVAL, result.getStatus());

        ArgumentCaptor<Auction> auctionCaptor = ArgumentCaptor.forClass(Auction.class);
        verify(auctionDAO).insertAuction(auctionCaptor.capture());
        assertEquals(10L, auctionCaptor.getValue().getItemId());
        assertEquals(7L, auctionCaptor.getValue().getSellerId());
        verify(itemDAO).updateStatus(10L, ItemStatus.WAITING_APPROVAL);
    }

    @Test
    @DisplayName("createAuction - tạo phiên cho sản phẩm thất bại, không tìm thấy item!")
    public void createAuction_missingItem() {
        LocalDateTime start = LocalDateTime.now().plusHours(1);
        LocalDateTime end = start.plusHours(2);
        when(itemDAO.findById(10L)).thenReturn(null);

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                auctionService.createAuction(auctionData(10L, 7L, start, end)));

        assertEquals("Sản phẩm không tồn tại!", exception.getMessage());
        verify(auctionDAO, never()).insertAuction(any(Auction.class));
        verify(itemDAO, never()).updateStatus(anyLong(), any(ItemStatus.class));
    }

    @Test
    @DisplayName("createAuction - tạo phiên cho sản phẩm thất bại, sản phẩm phải ở trạng thái PENDING!")
    public void createAuction_itemNotPending() {
        LocalDateTime start = LocalDateTime.now().plusHours(1);
        LocalDateTime end = start.plusHours(2);
        when(itemDAO.findById(10L)).thenReturn(item(10L, 7L, ItemStatus.ACTIVE));

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                auctionService.createAuction(auctionData(10L, 7L, start, end)));

        assertEquals("Chỉ có thể tạo đấu giá cho sản phẩm đang PENDING!", exception.getMessage());
        verify(auctionDAO, never()).insertAuction(any(Auction.class));
    }

    @Test
    @DisplayName("createAuction - tạo lại phiên cho sản phẩm CANCELLED không có bid")
    public void createAuction_cancelledNoBidItem_success() {
        LocalDateTime start = LocalDateTime.now().plusHours(1);
        LocalDateTime end = start.plusHours(2);
        Item item = item(10L, 7L, ItemStatus.CANCELLED);
        User seller = user(7L, "Seller One");
        Auction oldAuction = auction(
                5L,
                10L,
                7L,
                AuctionStatus.FINISHED,
                LocalDateTime.now().minusHours(3),
                LocalDateTime.now().minusHours(1));

        when(itemDAO.findById(10L)).thenReturn(item);
        when(auctionDAO.getAllAuctionsByItemId(10L)).thenReturn(List.of(oldAuction));
        when(bidDAO.countBidByAuctionId(5L)).thenReturn(0);
        when(auctionDAO.deleteAuctionsByItemId(10L)).thenReturn(true);
        when(auctionDAO.insertAuction(any(Auction.class))).thenAnswer(invocation -> {
            Auction auction = invocation.getArgument(0);
            auction.setId(99L);
            return true;
        });
        when(bidDAO.countBidByAuctionId(99L)).thenReturn(0);
        when(userDAO.findById(7L)).thenReturn(seller);

        AuctionDTO result = auctionService.createAuction(auctionData(10L, 7L, start, end));

        assertNotNull(result);
        assertEquals(99L, result.getId());
        verify(auctionDAO).deleteAuctionsByItemId(10L);
        verify(itemDAO).updateStatus(10L, ItemStatus.WAITING_APPROVAL);
    }

    @Test
    @DisplayName("createAuction - không tạo lại phiên nếu auction cũ đã có bid")
    public void createAuction_cancelledItemWithBid_failure() {
        LocalDateTime start = LocalDateTime.now().plusHours(1);
        LocalDateTime end = start.plusHours(2);
        Auction oldAuction = auction(
                5L,
                10L,
                7L,
                AuctionStatus.FINISHED,
                LocalDateTime.now().minusHours(3),
                LocalDateTime.now().minusHours(1));

        when(itemDAO.findById(10L)).thenReturn(item(10L, 7L, ItemStatus.CANCELLED));
        when(auctionDAO.getAllAuctionsByItemId(10L)).thenReturn(List.of(oldAuction));
        when(bidDAO.countBidByAuctionId(5L)).thenReturn(1);

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                auctionService.createAuction(auctionData(10L, 7L, start, end)));

        assertEquals("Chỉ có thể tạo đấu giá cho sản phẩm đang PENDING!", exception.getMessage());
        verify(auctionDAO, never()).insertAuction(any(Auction.class));
        verify(auctionDAO, never()).deleteAuctionsByItemId(10L);
    }

    @Test
    @DisplayName("createAuction - tạo phiên thất bại, khoảng thời gian không hợp lệ!")
    public void createAuction_invalidTimeRange() {
        LocalDateTime start = LocalDateTime.now().plusHours(2);
        LocalDateTime end = start.minusMinutes(1);
        when(itemDAO.findById(10L)).thenReturn(item(10L, 7L, ItemStatus.PENDING));

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                auctionService.createAuction(auctionData(10L, 7L, start, end)));

        assertEquals("Thời gian kết thúc phải sau thời gian bắt đầu", exception.getMessage());
        verify(auctionDAO, never()).insertAuction(any(Auction.class));
    }

    //----------------ACCEPT/REJECT--------------------
    @Test
    @DisplayName("approveCreateAuctionRequest - chấp nhận thành công request tạo phiên, OPEN!")
    public void approveCreateAuctionRequest_successOpen() {
        Auction auction = auction(
                5L,
                10L,
                7L,
                AuctionStatus.PREPARING,
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(3));
        Item item = item(10L, 7L, ItemStatus.WAITING_APPROVAL);
        User seller = user(7L, "Seller One");

        when(auctionDAO.findById(5L)).thenReturn(auction);
        when(itemDAO.findById(10L)).thenReturn(item);
        when(userDAO.findById(7L)).thenReturn(seller);

        AuctionDTO result = auctionService.approveCreateAuctionRequest(5L);

        assertNotNull(result);
        assertEquals(AuctionStatus.OPEN, result.getStatus());
        verify(auctionDAO).updateStatus(5L, AuctionStatus.OPEN);
        verify(itemDAO).updateStatus(10L, ItemStatus.ACTIVE);
    }

    @Test
    @DisplayName("approveCreateAuctionRequest - chấp nhận thành công request tạo phiên, RUNNING!")
    public void approveCreateAuctionRequest_successRunning() {
        Auction auction = auction(
                5L,
                10L,
                7L,
                AuctionStatus.PREPARING,
                LocalDateTime.now().minusMinutes(5),
                LocalDateTime.now().plusHours(1));
        Item item = item(10L, 7L, ItemStatus.WAITING_APPROVAL);

        when(auctionDAO.findById(5L)).thenReturn(auction);
        when(itemDAO.findById(10L)).thenReturn(item);
        when(userDAO.findById(7L)).thenReturn(user(7L, "Seller One"));

        AuctionDTO result = auctionService.approveCreateAuctionRequest(5L);

        assertEquals(AuctionStatus.RUNNING, result.getStatus());
        verify(auctionDAO).updateStatus(5L, AuctionStatus.RUNNING);
        verify(itemDAO).updateStatus(10L, ItemStatus.ACTIVE);
    }

    @Test
    @DisplayName("rejectCreateAuctionRequest - từ chối tạo phiên thành công!")
    public void rejectCreateAuctionRequest_success() {
        Auction auction = auction(
                5L,
                10L,
                7L,
                AuctionStatus.PREPARING,
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(3));
        Item item = item(10L, 7L, ItemStatus.WAITING_APPROVAL);

        when(auctionDAO.findById(5L)).thenReturn(auction);
        when(itemDAO.findById(10L)).thenReturn(item);
        when(userDAO.findById(7L)).thenReturn(user(7L, "Seller One"));

        AuctionDTO result = auctionService.rejectCreateAuctionRequest(5L);

        assertEquals(AuctionStatus.CANCELLED, result.getStatus());
        verify(auctionDAO).updateStatus(5L, AuctionStatus.CANCELLED);
        verify(itemDAO).updateStatus(10L, ItemStatus.CANCELLED);
    }

    //---------------FINISH--------------
    @Test
    @DisplayName("finishAuction - kết thúc phiên, xác định được người thắng, chuyển trạng thái Item!")
    public void finishAuction_withWinner() {
        Auction auction = auction(
                5L,
                10L,
                7L,
                AuctionStatus.RUNNING,
                LocalDateTime.now().minusHours(1),
                LocalDateTime.now().plusMinutes(5));
        Bid highestBid = new Bid(5L, 3L, new BigDecimal("150.00"));
        highestBid.setId(11L);

        when(auctionDAO.findById(5L)).thenReturn(auction);
        when(bidDAO.getHighestBidByAuctionId(5L)).thenReturn(highestBid);
        when(userDAO.findById(3L)).thenReturn(user(3L, "Bidder One"));

        Map<String, Object> result = auctionService.finishAuction(5L);

        assertNotNull(result);
        assertSame(highestBid, result.get("highestBid"));
        assertTrue(result.get("message").toString().contains("Bidder One"));
        verify(auctionDAO).updateStatus(5L, AuctionStatus.FINISHED);
        verify(itemDAO).updateStatus(10L, ItemStatus.SOLD);
    }

    @Test
    @DisplayName("finishAuction - kết thúc phiên, không ai Bid!")
    public void finishAuction_withoutWinner() {
        Auction auction = auction(
                5L,
                10L,
                7L,
                AuctionStatus.RUNNING,
                LocalDateTime.now().minusHours(1),
                LocalDateTime.now().plusMinutes(5));
        when(auctionDAO.findById(5L)).thenReturn(auction);
        when(bidDAO.getHighestBidByAuctionId(5L)).thenReturn(null);

        Map<String, Object> result = auctionService.finishAuction(5L);

        assertNotNull(result);
        assertNull(result.get("highestBid"));
        verify(auctionDAO).updateStatus(5L, AuctionStatus.FINISHED);
        verify(itemDAO).updateStatus(10L, ItemStatus.CANCELLED);
    }

    //---------------GET----------------
    @Test
    @DisplayName("getCreateAuctionRequests - maps only requests with existing items")
    public void getCreateAuctionRequests_skipsMissingItems() {
        Auction first = auction(
                5L,
                10L,
                7L,
                AuctionStatus.PREPARING,
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(3));
        Auction missingItem = auction(
                6L,
                11L,
                7L,
                AuctionStatus.PREPARING,
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(3));

        when(auctionDAO.getAllAuctionsByStatus(AuctionStatus.PREPARING))
                .thenReturn(List.of(first, missingItem));
        when(itemDAO.findById(10L)).thenReturn(item(10L, 7L, ItemStatus.WAITING_APPROVAL));
        when(itemDAO.findById(11L)).thenReturn(null);
        when(userDAO.findById(7L)).thenReturn(user(7L, "Seller One"));

        List<AuctionDTO> result = auctionService.getCreateAuctionRequests();

        assertEquals(1, result.size());
        assertEquals(5L, result.get(0).getId());
    }

    @Test
    @DisplayName("getApprovedAuctionsBySellerId - chỉ trả các phiên đã được duyệt")
    public void getApprovedAuctionsBySellerId_excludesPendingAndRejectedAuctions() {
        Auction openAuction = auction(
                5L, 10L, 7L, AuctionStatus.OPEN,
                LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(3));
        Auction finishedAuction = auction(
                6L, 11L, 7L, AuctionStatus.FINISHED,
                LocalDateTime.now().minusHours(3), LocalDateTime.now().minusHours(1));
        Auction pendingAuction = auction(
                7L, 12L, 7L, AuctionStatus.WAITING_APPROVAL,
                LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(3));
        Auction rejectedAuction = auction(
                8L, 13L, 7L, AuctionStatus.CANCELLED,
                LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(3));

        when(auctionDAO.getAllAuctionsBySellerId(7L)).thenReturn(List.of(
                openAuction, finishedAuction, pendingAuction, rejectedAuction));
        when(itemDAO.findById(10L)).thenReturn(item(10L, 7L, ItemStatus.ACTIVE));
        when(itemDAO.findById(11L)).thenReturn(item(11L, 7L, ItemStatus.SOLD));
        when(userDAO.findById(7L)).thenReturn(user(7L, "Seller One"));

        List<AuctionDTO> result = auctionService.getApprovedAuctionsBySellerId(7L);

        assertEquals(2, result.size());
        assertEquals(5L, result.get(0).getId());
        assertEquals(6L, result.get(1).getId());
        verify(itemDAO, never()).findById(12L);
        verify(itemDAO, never()).findById(13L);
    }

    private Map<String, Object> auctionData(Long itemId, Long sellerId,
                                            LocalDateTime startTime,
                                            LocalDateTime endTime) {
        Map<String, Object> data = new HashMap<>();
        data.put("itemId", itemId);
        data.put("sellerId", sellerId);
        data.put("startPrice", new BigDecimal("100.00"));
        data.put("minIncrement", new BigDecimal("10.00"));
        data.put("buyNowPrice", new BigDecimal("500.00"));
        data.put("startTime", startTime);
        data.put("endTime", endTime);
        return data;
    }

    private Auction auction(Long id, Long itemId, Long sellerId, AuctionStatus status,
                            LocalDateTime startTime, LocalDateTime endTime) {
        Auction auction = new Auction(
                itemId,
                sellerId,
                new BigDecimal("100.00"),
                new BigDecimal("100.00"),
                new BigDecimal("10.00"),
                new BigDecimal("500.00"),
                startTime,
                endTime);
        auction.setId(id);
        auction.setStatus(status);
        return auction;
    }

    private Item item(Long id, Long sellerId, ItemStatus status) {
        Item item = new Electronics("Laptop", "Gaming laptop", sellerId, status);
        item.setId(id);
        item.setImageUrl("laptop.png");
        return item;
    }

    private User user(Long id, String fullName) {
        User user = new Seller();
        user.setId(id);
        user.setFullname(fullName);
        return user;
    }
}
