package server.controller;

import server.network.ClientConnectionHandler;
import server.service.WalletService;
import shared.dto.common.SellerWalletDTO;
import shared.dto.request.BaseRequest;
import shared.dto.response.BaseResponse;

import java.math.BigDecimal;

public class WalletServerController {
    private static volatile WalletServerController instance;
    private final WalletService walletService = WalletService.getInstance();

    private WalletServerController() {}

    public static WalletServerController getInstance() {
        if (instance == null) {
            synchronized (WalletServerController.class) {
                if (instance == null) {
                    instance = new WalletServerController();
                }
            }
        }
        return instance;
    }

    public BaseResponse getWallet(ClientConnectionHandler handler) {
        Long userId = handler.getUser().getId();
        BigDecimal balance = walletService.getBalance(userId);

        return new BaseResponse(true, "Số dư ví:", balance);
    }

    public BaseResponse deposit(BaseRequest request, ClientConnectionHandler handler) {
        try {
            Long userId = handler.getUser().getId();
            BigDecimal amount = new BigDecimal(request.getData().toString());

            BigDecimal newBalance = walletService.deposit(userId, amount);

            return new BaseResponse(true, "Nạp tiền thành công!", newBalance);

        } catch (Exception e) {
            return new BaseResponse(false, e.getMessage(), null);
        }
    }

    public BaseResponse getSellerWalletSummary(ClientConnectionHandler handler) {
        Long sellerId = handler.getUser().getId();
        SellerWalletDTO summary = walletService.getSellerWalletSummary(sellerId);
        return new BaseResponse(true, "Lấy thông tin ví seller thành công.", summary);
    }

    public BaseResponse withdrawSellerWallet(BaseRequest request, ClientConnectionHandler handler) {
        try {
            Long sellerId = handler.getUser().getId();
            BigDecimal amount = new BigDecimal(request.getData().toString());
            SellerWalletDTO summary = walletService.withdrawSellerWallet(sellerId, amount);
            return new BaseResponse(true, "Rút tiền thành công.", summary);
        } catch (Exception e) {
            return new BaseResponse(false, e.getMessage(), null);
        }
    }
}
