package server.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import server.dao.AuctionDAO;
import server.dao.BidDAO;
import server.dao.UserDAO;

import server.model.core.Auction;
import server.model.core.Bid;
import server.model.user.Bidder;
import server.model.user.User;
import shared.enums.AuctionStatus;
import shared.exception.AuctionClosedException;
import shared.exception.AuctionNotFoundException;
import shared.exception.BidTooLowException;
import shared.exception.InvalidAuctionTimeException;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.util.HashMap;

@ExtendWith(MockitoExtension.class)
public class BidServiceTest {

    @Mock private AuctionService auctionService;
    @Mock private AuctionDAO auctionDAO;
    @Mock private BidDAO bidDAO;
    @Mock private UserDAO userDAO;
    @Mock private WalletService walletService;

    private BidService bidService;

    @BeforeEach
    public void setup() throws Exception {
        SingletonTestUtil.resetSingleton(BidService.class);
        bidService = BidService.getInstance();
        inject("auctionService", auctionService);
        inject("auctionDAO", auctionDAO);
        inject("bidDAO", bidDAO);
        inject("userDAO", userDAO);
        inject("walletService", walletService);
    }

    private void inject(String name, Object mock) throws Exception {
        Field field = BidService.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(bidService, mock);
    }

    //---------------PLACE-BID------------------
    @Test
    @DisplayName("placeBid - Đặt giá thành công và chạm giá mua ngay")
    public void placeBid_success_buyNow() {
        Auction auction = auction(
                AuctionStatus.OPEN,
                LocalDateTime.now().minusMinutes(5),
                LocalDateTime.now().plusHours(1)
        );

        User bidder = new Bidder();
        bidder.setId(3L);
        bidder.setFullname("Bidder One");

        when(auctionDAO.findById(5L)).thenReturn(auction);
        when(bidDAO.getHighestBidByAuctionId(5L)).thenReturn(null);
        doNothing().when(walletService).checkCanBid(3L, new BigDecimal("500.00"));
        when(bidDAO.insertBid(any(Bid.class))).thenReturn(true);
        when(userDAO.findById(3L)).thenReturn(bidder);
        when(auctionService.finishAuction(5L)).thenReturn(new HashMap<>());

        assertTrue(bidService.placeBid(5L, 3L, new BigDecimal("500.00")));

        verify(auctionDAO).updateStatus(5L, AuctionStatus.RUNNING);
        verify(auctionDAO).updateMaxPrice(5L, new BigDecimal("500.00"));
        verify(bidDAO).insertBid(any(Bid.class));
        verify(auctionService).finishAuction(5L);
    }

    @Test
    @DisplayName("placeBid - Thất bại khi không tìm thấy phiên đấu giá")
    public void placeBid_auctionNotFound() {
        when(auctionDAO.findById(5L)).thenReturn(null);

        assertThrows(AuctionNotFoundException.class, () ->
                bidService.placeBid(5L, 3L, new BigDecimal("120.00")));

        verify(bidDAO, never()).insertBid(any(Bid.class));
    }

    @Test
    @DisplayName("placeBid - Thất bại khi phiên chưa tới thời gian bắt đầu")
    public void placeBid_auctionNotStarted() {
        Auction auction = auction(
                AuctionStatus.OPEN,
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(2)
        );

        when(auctionDAO.findById(5L)).thenReturn(auction);

        assertThrows(InvalidAuctionTimeException.class, () ->
                bidService.placeBid(5L, 3L, new BigDecimal("120.00")));

        verify(bidDAO, never()).insertBid(any(Bid.class));
    }

    @Test
    @DisplayName("placeBid - Thất bại khi phiên đã quá thời gian kết thúc")
    public void placeBid_auctionEnded() {
        Auction auction = auction(
                AuctionStatus.OPEN,
                LocalDateTime.now().minusHours(2),
                LocalDateTime.now().minusHours(1)
        );

        when(auctionDAO.findById(5L)).thenReturn(auction);
        when(auctionService.finishAuction(5L)).thenReturn(new HashMap<>());

        assertThrows(InvalidAuctionTimeException.class, () ->
                bidService.placeBid(5L, 3L, new BigDecimal("120.00")));

        verify(auctionService).finishAuction(5L);
        verify(bidDAO, never()).insertBid(any(Bid.class));
    }

    @Test
    @DisplayName("placeBid - Thất bại khi phiên không ở trạng thái OPEN hoặc RUNNING")
    public void placeBid_auctionClosed() {
        Auction auction = auction(
                AuctionStatus.CLOSED,
                LocalDateTime.now().minusMinutes(5),
                LocalDateTime.now().plusHours(1)
        );

        when(auctionDAO.findById(5L)).thenReturn(auction);

        assertThrows(AuctionClosedException.class, () ->
                bidService.placeBid(5L, 3L, new BigDecimal("120.00")));

        verify(bidDAO, never()).insertBid(any(Bid.class));
    }

    @Test
    @DisplayName("placeBid - Thất bại khi số tiền bid thấp hơn mức tối thiểu")
    public void placeBid_amountTooLow() {
        Auction auction = auction(
                AuctionStatus.OPEN,
                LocalDateTime.now().minusMinutes(5),
                LocalDateTime.now().plusHours(1)
        );

        when(auctionDAO.findById(5L)).thenReturn(auction);
        when(bidDAO.getHighestBidByAuctionId(5L)).thenReturn(null);

        assertThrows(BidTooLowException.class, () ->
                bidService.placeBid(5L, 3L, new BigDecimal("105.00")));

        verify(walletService, never()).checkCanBid(anyLong(), any(BigDecimal.class));
        verify(bidDAO, never()).insertBid(any(Bid.class));
    }

    @Test
    @DisplayName("placeBid - Trả về false khi lưu bid thất bại")
    public void placeBid_insertBidFailed() {
        Auction auction = auction(
                AuctionStatus.OPEN,
                LocalDateTime.now().minusMinutes(5),
                LocalDateTime.now().plusHours(1)
        );

        when(auctionDAO.findById(5L)).thenReturn(auction);
        when(bidDAO.getHighestBidByAuctionId(5L)).thenReturn(null);
        doNothing().when(walletService).checkCanBid(3L, new BigDecimal("120.00"));
        when(bidDAO.insertBid(any(Bid.class))).thenReturn(false);

        assertFalse(bidService.placeBid(5L, 3L, new BigDecimal("120.00")));

        verify(bidDAO).insertBid(any(Bid.class));
        verify(auctionDAO, never()).updateMaxPrice(anyLong(), any(BigDecimal.class));
    }

    //----------------AMOUNT-VALID------------------
    @Test
    @DisplayName("isAmountValid - Hợp lệ khi amount >= minBid")
    public void isAmountValid_success() {
        assertTrue(bidService.isAmountValid(new BigDecimal("120.00"), new BigDecimal("100.00")));
    }

    @Test
    @DisplayName("isAmountValid - Không hợp lệ khi amount nhỏ hơn minBid")
    public void isAmountValid_amountTooLow() {
        assertFalse(bidService.isAmountValid(new BigDecimal("90.00"), new BigDecimal("100.00")));
    }

    @Test
    @DisplayName("isAmountValid - Không hợp lệ khi amount null")
    public void isAmountValid_nullAmount() {
        assertFalse(bidService.isAmountValid(null, new BigDecimal("100.00")));
    }

    @Test
    @DisplayName("isAmountValid - Không hợp lệ khi minBid null")
    public void isAmountValid_nullMinBid() {
        assertFalse(bidService.isAmountValid(new BigDecimal("100.00"), null));
    }

    private Auction auction(AuctionStatus status, LocalDateTime startTime, LocalDateTime endTime) {
        Auction auction = new Auction(
                10L,
                7L,
                new BigDecimal("100.00"),
                new BigDecimal("100.00"),
                new BigDecimal("10.00"),
                new BigDecimal("500.00"),
                startTime,
                endTime
        );
        auction.setId(5L);
        auction.setStatus(status);
        return auction;
    }
}