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

import java.lang.reflect.Field;

@ExtendWith(MockitoExtension.class)
public class BidServiceTest {

    @Mock private AuctionService auctionService;
    @Mock private AuctionDAO auctionDAO;
    @Mock private BidDAO bidDAO;
    @Mock private UserDAO userDAO;

    private BidService bidService;

    @BeforeEach
    public void setup() throws Exception {
        SingletonTestUtil.resetSingleton(BidService.class);
        bidService = BidService.getInstance();
        inject("auctionService", auctionService);
        inject("auctionDAO", auctionDAO);
        inject("bidDAO", bidDAO);
        inject("userDAO", userDAO);
    }

    private void inject(String name, Object mock) throws Exception {
        Field field = BidService.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(bidService, mock);
    }

    //---------------PLACE-BID------------------
    @Test
    @DisplayName("placeBid - Đặt giá thành công!")
    public void placeBid_success() {

    }
}
