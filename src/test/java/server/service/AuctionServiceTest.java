package server.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import server.dao.AuctionDAO;
import server.dao.AutoBidRuleDAO;
import server.dao.BidDAO;
import server.dao.ItemDAO;
import server.dao.UserDAO;
import server.model.core.Auction;
import server.model.core.AutoBidRule;
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
    @Mock private AutoBidRuleDAO autoBidRuleDAO;

    private AuctionService auctionService;
    private WalletService walletService;

    @BeforeEach
    public void setUp() throws Exception {
        SingletonTestUtil.resetSingleton(AuctionService.class);
        SingletonTestUtil.resetSingleton(WalletService.class);
        auctionService = AuctionService.getInstance();
        walletService = WalletService.getInstance();
        inject(AuctionService.class, auctionService, "auctionDAO", auctionDAO);
        inject(AuctionService.class, auctionService, "itemDAO", itemDAO);
        inject(AuctionService.class, auctionService, "userDAO", userDAO);
        inject(AuctionService.class, auctionService, "bidDAO", bidDAO);
        inject(AuctionService.class, auctionService, "autoBidRuleDAO", autoBidRuleDAO);
        inject(WalletService.class, walletService, "userDAO", userDAO);
    }

    private void inject(Class<?> type, Object target, String name, Object mock) throws Exception {
        Field field = type.getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, mock);
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
        when(auctionDAO.insertAuction(any(Auction.class))).thenAnswer(invocation -> {
            Auction auction = invocation.getArgument(0);
            auction.setId(99L);
            return true;
        });
        when(bidDAO.countBidByAuctionId(99L)).thenReturn(0L);
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

        assertEquals("Chỉ có thể tạo đấu giá cho sản phẩm đang PENDING hoặc CANCELLED!", exception.getMessage());
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
        when(bidDAO.countBidByAuctionId(5L)).thenReturn(0L);
        when(auctionDAO.deleteAuctionsByItemId(10L)).thenReturn(true);
        when(auctionDAO.insertAuction(any(Auction.class))).thenAnswer(invocation -> {
            Auction auction = invocation.getArgument(0);
            auction.setId(99L);
            return true;
        });
        when(bidDAO.countBidByAuctionId(99L)).thenReturn(0L);
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
        when(bidDAO.countBidByAuctionId(5L)).thenReturn(1L);

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                auctionService.createAuction(auctionData(10L, 7L, start, end)));

        assertEquals("Sản phẩm đã có lịch sử đấu giá, không thể tạo lại phiên mới!", exception.getMessage());
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
                AuctionStatus.WAITING_APPROVAL,
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(3));

        when(auctionDAO.findById(5L)).thenReturn(auction);
        when(auctionDAO.updateStatus(5L, AuctionStatus.OPEN)).thenReturn(true);
        when(itemDAO.updateStatus(10L, ItemStatus.ACTIVE)).thenReturn(true);

        boolean result = auctionService.approveCreateAuctionRequest(5L);

        assertTrue(result);
        verify(auctionDAO).updateStatus(5L, AuctionStatus.OPEN);
        verify(itemDAO).updateStatus(10L, ItemStatus.ACTIVE);
    }

    @Test
    @DisplayName("approveCreateAuctionRequest - phiên đã tới giờ vẫn chuyển sang OPEN để scheduler xử lý")
    public void approveCreateAuctionRequest_pastStartTime_stillOpen() {
        Auction auction = auction(
                5L,
                10L,
                7L,
                AuctionStatus.WAITING_APPROVAL,
                LocalDateTime.now().minusMinutes(5),
                LocalDateTime.now().plusHours(1));

        when(auctionDAO.findById(5L)).thenReturn(auction);
        when(auctionDAO.updateStatus(5L, AuctionStatus.OPEN)).thenReturn(true);
        when(itemDAO.updateStatus(10L, ItemStatus.ACTIVE)).thenReturn(true);

        boolean result = auctionService.approveCreateAuctionRequest(5L);

        assertTrue(result);
        verify(auctionDAO).updateStatus(5L, AuctionStatus.OPEN);
        verify(itemDAO).updateStatus(10L, ItemStatus.ACTIVE);
    }

    @Test
    @DisplayName("rejectCreateAuctionRequest - từ chối tạo phiên thành công!")
    public void rejectCreateAuctionRequest_success() {
        Auction auction = auction(
                5L,
                10L,
                7L,
                AuctionStatus.WAITING_APPROVAL,
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(3));

        when(auctionDAO.findById(5L)).thenReturn(auction);
        when(auctionDAO.updateStatus(5L, AuctionStatus.CANCELLED)).thenReturn(true);
        when(itemDAO.updateStatus(10L, ItemStatus.PENDING)).thenReturn(true);

        boolean result = auctionService.rejectCreateAuctionRequest(5L);

        assertTrue(result);
        verify(auctionDAO).updateStatus(5L, AuctionStatus.CANCELLED);
        verify(itemDAO).updateStatus(10L, ItemStatus.PENDING);
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
        AutoBidRule autoBidRule = new AutoBidRule(
                5L, 3L, new BigDecimal("200.00"), new BigDecimal("10.00"));

        when(auctionDAO.findById(5L)).thenReturn(auction);
        when(bidDAO.getHighestBidByAuctionId(5L)).thenReturn(highestBid);
        when(userDAO.findById(3L)).thenReturn(user(3L, "Bidder One"));
        when(itemDAO.findById(10L)).thenReturn(item(10L, 7L, ItemStatus.ACTIVE));
        when(autoBidRuleDAO.getActiveRulesByAuctionId(5L)).thenReturn(List.of(autoBidRule));
        when(userDAO.transferBalanceIfEnough(3L, 7L, new BigDecimal("150.00"))).thenReturn(true);
        when(itemDAO.updateStatus(10L, ItemStatus.SOLD)).thenReturn(true);
        when(auctionDAO.updateStatus(5L, AuctionStatus.FINISHED)).thenReturn(true);

        Map<String, Object> result = auctionService.finishAuction(5L);

        assertNotNull(result);
        assertSame(highestBid, result.get("highestBid"));
        assertTrue(result.get("message").toString().contains("Bidder One"));
        verify(auctionDAO).updateStatus(5L, AuctionStatus.FINISHED);
        verify(itemDAO).updateStatus(10L, ItemStatus.SOLD);
        verify(autoBidRuleDAO).switchRuleByAuctionIdAndBidderId(5L, 3L, false);
        verify(userDAO).transferBalanceIfEnough(3L, 7L, new BigDecimal("150.00"));
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
        when(itemDAO.findById(10L)).thenReturn(item(10L, 7L, ItemStatus.ACTIVE));
        when(itemDAO.updateStatus(10L, ItemStatus.CANCELLED)).thenReturn(true);
        when(auctionDAO.updateStatus(5L, AuctionStatus.FINISHED)).thenReturn(true);

        Map<String, Object> result = auctionService.finishAuction(5L);

        assertNotNull(result);
        assertNull(result.get("highestBid"));
        verify(auctionDAO).updateStatus(5L, AuctionStatus.FINISHED);
        verify(itemDAO).updateStatus(10L, ItemStatus.CANCELLED);
    }

    //---------------GET----------------
    @Test
    @DisplayName("getAuctionApprovalRequests - trả các yêu cầu đang chờ admin duyệt")
    public void getAuctionApprovalRequests_returnsWaitingApprovalAuctions() {
        Auction waitingApproval = auction(
                5L,
                10L,
                7L,
                AuctionStatus.WAITING_APPROVAL,
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(3));

        when(auctionDAO.getAllAuctionsByStatus(AuctionStatus.WAITING_APPROVAL))
                .thenReturn(List.of(waitingApproval));
        when(itemDAO.findById(10L)).thenReturn(item(10L, 7L, ItemStatus.WAITING_APPROVAL));
        when(userDAO.findById(7L)).thenReturn(user(7L, "Seller One"));

        List<AuctionDTO> result = auctionService.getAuctionApprovalRequests();

        assertEquals(1, result.size());
        assertEquals(5L, result.getFirst().getId());
    }

    @Test
    @DisplayName("getApprovedAuctionsBySellerId - chỉ trả các phiên đã được duyệt")
    public void getApprovedAuctionsBySellerId_excludesPendingAndRejectedAuctions() {
        AuctionDTO openAuction = mock(AuctionDTO.class);
        AuctionDTO finishedAuction = mock(AuctionDTO.class);
        List<AuctionDTO> summaries = List.of(openAuction, finishedAuction);

        when(auctionDAO.getSellerAuctionSummaries(7L)).thenReturn(summaries);

        List<AuctionDTO> result = auctionService.getApprovedAuctionsBySellerId(7L);

        assertSame(summaries, result);
        verify(auctionDAO).getSellerAuctionSummaries(7L);
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
