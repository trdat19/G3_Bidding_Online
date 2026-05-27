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
import shared.dto.common.SellerWalletDTO;
import shared.enums.AuctionStatus;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class WalletServiceTest {

    @Mock private UserDAO userDAO;
    @Mock private BidDAO bidDAO;
    @Mock private AuctionDAO auctionDAO;

    private WalletService walletService;

    @BeforeEach
    public void setup() throws Exception {
        SingletonTestUtil.resetSingleton(WalletService.class);
        walletService = WalletService.getInstance();
        inject("userDAO", userDAO);
        inject("bidDAO", bidDAO);
        inject("auctionDAO", auctionDAO);
    }

    private void inject(String name, Object mock) throws Exception {
        Field field = WalletService.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(walletService, mock);
    }

    @Test
    @DisplayName("getSellerWalletSummary - chỉ tính các phiên đã bán thành công")
    public void getSellerWalletSummary_sumsFinishedAuctionsWithWinner() {
        Auction soldAuction = auction(1L, AuctionStatus.FINISHED);
        Auction noBidAuction = auction(2L, AuctionStatus.FINISHED);
        Auction runningAuction = auction(3L, AuctionStatus.RUNNING);

        when(userDAO.getBalance(7L)).thenReturn(new BigDecimal("80.00"));
        when(auctionDAO.getAllAuctionsBySellerId(7L))
                .thenReturn(List.of(soldAuction, noBidAuction, runningAuction));
        when(bidDAO.getHighestBidByAuctionId(1L))
                .thenReturn(new Bid(1L, 5L, new BigDecimal("150.00")));
        when(bidDAO.getHighestBidByAuctionId(2L)).thenReturn(null);

        SellerWalletDTO result = walletService.getSellerWalletSummary(7L);

        assertEquals(new BigDecimal("80.00"), result.getAvailableBalance());
        assertEquals(new BigDecimal("150.00"), result.getTotalRevenue());
        assertEquals(1, result.getSoldProductCount());
        verify(bidDAO, never()).getHighestBidByAuctionId(3L);
    }

    @Test
    @DisplayName("withdrawSellerWallet - rút tiền và trả về số dư mới")
    public void withdrawSellerWallet_success() {
        when(userDAO.decreaseBalanceIfEnough(7L, new BigDecimal("25.00"))).thenReturn(true);
        when(userDAO.getBalance(7L)).thenReturn(new BigDecimal("55.00"));
        when(auctionDAO.getAllAuctionsBySellerId(7L)).thenReturn(List.of());

        SellerWalletDTO result = walletService.withdrawSellerWallet(
                7L, new BigDecimal("25.00"));

        assertEquals(new BigDecimal("55.00"), result.getAvailableBalance());
        verify(userDAO).decreaseBalanceIfEnough(7L, new BigDecimal("25.00"));
    }

    @Test
    @DisplayName("withdrawSellerWallet - từ chối khi số dư không đủ")
    public void withdrawSellerWallet_insufficientBalance() {
        when(userDAO.decreaseBalanceIfEnough(7L, new BigDecimal("100.00"))).thenReturn(false);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                walletService.withdrawSellerWallet(7L, new BigDecimal("100.00")));

        assertEquals("Số dư không đủ để rút số tiền này.", exception.getMessage());
    }

    private Auction auction(Long id, AuctionStatus status) {
        Auction auction = new Auction();
        auction.setId(id);
        auction.setStatus(status);
        return auction;
    }
}
