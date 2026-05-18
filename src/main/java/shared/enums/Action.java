package shared.enums;

public enum Action {
    /**
     * Xác thực
     */
    LOGIN,
    REGISTER,
    LOGOUT,

    /**
     * Seller
     */
    CREATE_ITEM,
    UPDATE_ITEM,
    DELETE_ITEM,
    GET_SELLER_ITEMS,
    CREATE_AUCTION,

    /**
     * Bidder
     */
    PLACE_BID,
    SUBSCRIBE_AUCTION,
    UNSUBSCRIBE_AUCTION,

    /**
     * Admin
     */
    GET_USERS_LIST,
    ENABLE_USER,
    DISABLE_USER,
    GET_CREATE_AUCTION_REQUESTS,
    ACCEPT_CREATE_AUCTION_REQUEST,
    REJECT_CREATE_AUCTION_REQUEST,

    GET_BID_HISTORY,
    GET_AUCTION_LIST,
    GET_AUCTION_DETAILS,
    GET_USER_PROFILE,
    UPDATE_USER_PROFILE,
    GET_LEADERBOARD,
    GET_NOTIFICATIONS,
}