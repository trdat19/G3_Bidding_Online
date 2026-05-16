# G3 Bidding Online - Chat Notes

## Register/Login

- Server da co nhan request socket qua `SocketSever`, `ClientConnectionHandler`, `RequestRouter`.
- `REGISTER` da route ve `AuthServerController.register`.
- `AuthService.register` da goi `UserDAO.insertUser` de luu DB.
- `LoginController` da sua theo huong gui `BaseRequest("LOGIN", loginData)` len server.
- Can tranh loi `response == null` khi server chua chay hoac mat ket noi:

```java
String message = response != null ? response.getMessage() : "Khong ket noi duoc server";
```

## Loi compile da sua

- `ItemFactory.createItemFromDb(...)` bi loi vi goi constructor `Art/Electronics/Vehicle` khong ton tai.
- Da sua bang cach tao item qua `createItem(...)`, roi `setId(...)`, `setCreatedAtItem(...)`.
- Sau sua, lenh sau da compile thanh cong:

```powershell
.\mvnw.cmd -q -DskipTests compile
```

## Seller them/xoa san pham

`ItemDAO` da co nen tang DB:

- `insertItem(Item item)`
- `deleteItem(long idItem)`
- `findBySellerId(long sellerId)`

Nhung server chua san sang hoan toan cho GUI seller them/xoa san pham.

Con thieu:

- `RequestRouter` case `"CREATE_ITEM"`, `"DELETE_ITEM"`, `"GET_SELLER_ITEMS"`
- `SellerServerController` xu ly request va tra `BaseResponse`
- `server.service.ItemService` goi `ItemDAO`
- GUI `AddProductController` gui request len server qua `ClientNetworkService`
- GUI `SellerDashboardController` xoa san pham qua server
- Neu san pham co thoi gian dau gia `startDateTime/endDateTime`, can noi them voi `AuctionDAO`, vi `ItemDAO.insertItem(...)` hien chua luu thoi gian phien dau gia.

## Cach tiep tuc lan sau

Noi voi Codex:

> Doc `CHAT_NOTES.md` roi lam tiep phan seller them/xoa san pham.
