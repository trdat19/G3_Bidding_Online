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
    SEND_CREATE_AUCTION_REQUEST,

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

    ACCEPT_CREATE_AUCTION_REQUEST,
    EJECT_CREATE_AUCTION_REQUEST,

    GET_BID_HISTORY,
    GET_AUCTION_LIST,
    GET_AUCTION_DETAILS,
    GET_USER_PROFILE,
    UPDATE_USER_PROFILE,
    GET_LEADERBOARD,
    GET_NOTIFICATIONS,

}
