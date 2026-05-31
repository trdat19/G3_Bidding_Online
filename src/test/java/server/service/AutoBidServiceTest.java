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
import server.model.core.Auction;
import server.model.core.AutoBidRule;
import server.model.core.Bid;
import shared.enums.AuctionStatus;
import shared.exception.AuctionNotFoundException;
import shared.exception.InvalidBidException;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AutoBidServiceTest {

    @Mock private AutoBidRuleDAO autoBidRuleDAO;
    @Mock private AuctionDAO auctionDAO;
    @Mock private BidDAO bidDAO;
    @Mock private BidService bidService;

    private AutoBidService autoBidService;

    @BeforeEach
    public void setup() throws Exception {
        SingletonTestUtil.resetSingleton(AutoBidService.class);
        SingletonTestUtil.resetSingleton(BidService.class);

        autoBidService = AutoBidService.getInstance();
        inject(autoBidService, AutoBidService.class, "autoBidRuleDAO", autoBidRuleDAO);
        inject(autoBidService, AutoBidService.class, "auctionDAO", auctionDAO);
        inject(autoBidService, AutoBidService.class, "bidDAO", bidDAO);
        inject(autoBidService, AutoBidService.class, "bidService", bidService);
        setStatic(BidService.class, "instance", bidService);
    }

    private void inject(Object target, Class<?> clazz, String name, Object mock) throws Exception {
        Field field = clazz.getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, mock);
    }

    private void setStatic(Class<?> clazz, String name, Object value) throws Exception {
        Field field = clazz.getDeclaredField(name);
        field.setAccessible(true);
        field.set(null, value);
    }

    @Test
    @DisplayName("registerAutoBidRule - dang ky rule thanh cong")
    public void registerAutoBidRule_success() {
        Auction auction = auction();
        when(auctionDAO.findById(5L)).thenReturn(auction);
        when(bidDAO.getHighestBidByAuctionId(5L)).thenReturn(null);
        when(autoBidRuleDAO.saveOrUpdateRule(any(AutoBidRule.class))).thenReturn(true);

        assertTrue(autoBidService.registerAutoBidRule(
                5L,
                10L,
                new BigDecimal("150.00"),
                new BigDecimal("10.00")
        ));

        ArgumentCaptor<AutoBidRule> captor = ArgumentCaptor.forClass(AutoBidRule.class);
        verify(autoBidRuleDAO).saveOrUpdateRule(captor.capture());

        AutoBidRule rule = captor.getValue();
        assertEquals(5L, rule.getAuctionId());
        assertEquals(10L, rule.getBidderId());
        assertEquals(new BigDecimal("150.00"), rule.getMaxAmount());
        assertEquals(new BigDecimal("10.00"), rule.getStepAmount());
        assertTrue(rule.getIsActive());
    }

    @Test
    @DisplayName("registerAutoBidRule - that bai khi khong tim thay phien dau gia")
    public void registerAutoBidRule_auctionNotFound() {
        when(auctionDAO.findById(5L)).thenReturn(null);

        assertThrows(AuctionNotFoundException.class, () ->
                autoBidService.registerAutoBidRule(
                        5L,
                        10L,
                        new BigDecimal("150.00"),
                        new BigDecimal("10.00")
                ));

        verify(autoBidRuleDAO, never()).saveOrUpdateRule(any(AutoBidRule.class));
    }

    @Test
    @DisplayName("registerAutoBidRule - that bai khi maxAmount nho hon gia toi thieu")
    public void registerAutoBidRule_maxAmountTooLow() {
        Auction auction = auction();
        when(auctionDAO.findById(5L)).thenReturn(auction);
        when(bidDAO.getHighestBidByAuctionId(5L)).thenReturn(new Bid(5L, 20L, new BigDecimal("130.00")));

        assertThrows(InvalidBidException.class, () ->
                autoBidService.registerAutoBidRule(
                        5L,
                        10L,
                        new BigDecimal("135.00"),
                        new BigDecimal("10.00")
                ));

        verify(autoBidRuleDAO, never()).saveOrUpdateRule(any(AutoBidRule.class));
    }

    @Test
    @DisplayName("registerAutoBidRule - that bai khi stepAmount nho hon minIncrement")
    public void registerAutoBidRule_stepAmountTooLow() {
        Auction auction = auction();
        when(auctionDAO.findById(5L)).thenReturn(auction);
        when(bidDAO.getHighestBidByAuctionId(5L)).thenReturn(null);

        assertThrows(InvalidBidException.class, () ->
                autoBidService.registerAutoBidRule(
                        5L,
                        10L,
                        new BigDecimal("150.00"),
                        new BigDecimal("5.00")
                ));

        verify(autoBidRuleDAO, never()).saveOrUpdateRule(any(AutoBidRule.class));
    }

    @Test
    @DisplayName("reactToIncomingBidLocked - auto bid vuot rule manh thu hai")
    public void reactToIncomingBidLocked_placesBidAboveSecondRule() {
        Auction auction = auction();
        Bid currentHighest = new Bid(5L, 20L, new BigDecimal("120.00"));
        AutoBidRule topRule = rule(1L, 5L, 10L, "200.00", "10.00", LocalDateTime.now().minusMinutes(2));
        AutoBidRule secondRule = rule(2L, 5L, 30L, "160.00", "10.00", LocalDateTime.now().minusMinutes(1));

        when(auctionDAO.findById(5L)).thenReturn(auction);
        when(bidDAO.getHighestBidByAuctionId(5L)).thenReturn(currentHighest);
        when(autoBidRuleDAO.getActiveRulesByAuctionId(5L)).thenReturn(List.of(topRule, secondRule));
        when(bidService.placeAutoBid(anyLong(), anyLong(), any(BigDecimal.class))).thenReturn(true);

        autoBidService.reactToIncomingBidLocked(5L);

        verify(bidService).placeAutoBid(5L, 10L, new BigDecimal("170.00"));
    }

    @Test
    @DisplayName("reactToIncomingBidLocked - khong auto bid khi rule manh nhat la nguoi dang dan dau")
    public void reactToIncomingBidLocked_topRuleAlreadyHighestBidder() {
        Auction auction = auction();
        Bid currentHighest = new Bid(5L, 10L, new BigDecimal("120.00"));
        AutoBidRule topRule = rule(1L, 5L, 10L, "200.00", "10.00", LocalDateTime.now().minusMinutes(2));

        when(auctionDAO.findById(5L)).thenReturn(auction);
        when(bidDAO.getHighestBidByAuctionId(5L)).thenReturn(currentHighest);
        when(autoBidRuleDAO.getActiveRulesByAuctionId(5L)).thenReturn(List.of(topRule));

        autoBidService.reactToIncomingBidLocked(5L);

        verify(bidService, never()).placeAutoBid(anyLong(), anyLong(), any(BigDecimal.class));
    }

    @Test
    @DisplayName("reactToIncomingBidLocked - tat rule khi maxAmount khong du gia toi thieu")
    public void reactToIncomingBidLocked_disablesUnusableRule() {
        Auction auction = auction();
        Bid currentHighest = new Bid(5L, 20L, new BigDecimal("120.00"));
        AutoBidRule weakRule = rule(1L, 5L, 10L, "125.00", "10.00", LocalDateTime.now().minusMinutes(1));

        when(auctionDAO.findById(5L)).thenReturn(auction);
        when(bidDAO.getHighestBidByAuctionId(5L)).thenReturn(currentHighest);
        when(autoBidRuleDAO.getActiveRulesByAuctionId(5L)).thenReturn(List.of(weakRule));

        autoBidService.reactToIncomingBidLocked(5L);

        verify(autoBidRuleDAO).updateStatus(1L, false);
        verify(bidService, never()).placeAutoBid(anyLong(), anyLong(), any(BigDecimal.class));
    }

    @Test
    @DisplayName("switchRuleByAuctionIdAndBidderId - bat/tat rule cua bidder")
    public void switchAutoBid_success() {
        when(autoBidRuleDAO.switchRuleByAuctionIdAndBidderId(5L, 10L, false))
                .thenReturn(true);

        assertTrue(autoBidService.switchAutoBid(5L, 10L, false));

        verify(autoBidRuleDAO).switchRuleByAuctionIdAndBidderId(5L, 10L, false);
    }

    @Test
    @DisplayName("getRule - lay rule theo auctionId va bidderId")
    public void getRule_success() {
        AutoBidRule expected = rule(1L, 5L, 10L, "200.00", "10.00", LocalDateTime.now());
        when(autoBidRuleDAO.findByAuctionIdAndBidderId(5L, 10L)).thenReturn(expected);

        AutoBidRule actual = autoBidService.getRule(5L, 10L);

        assertSame(expected, actual);
    }

    private Auction auction() {
        Auction auction = new Auction(
                1L,
                2L,
                new BigDecimal("100.00"),
                new BigDecimal("100.00"),
                new BigDecimal("10.00"),
                new BigDecimal("500.00"),
                LocalDateTime.now().minusMinutes(5),
                LocalDateTime.now().plusHours(1)
        );
        auction.setId(5L);
        auction.setStatus(AuctionStatus.RUNNING);
        return auction;
    }

    private AutoBidRule rule(Long id, Long auctionId, Long bidderId, String maxAmount,
                             String stepAmount, LocalDateTime createdAt) {
        AutoBidRule rule = new AutoBidRule(
                auctionId,
                bidderId,
                new BigDecimal(maxAmount),
                new BigDecimal(stepAmount)
        );
        rule.setId(id);
        rule.setCreatedAt(createdAt);
        return rule;
    }
}
