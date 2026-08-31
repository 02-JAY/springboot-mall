# 🛒 Spring Boot Shopping Mall (購物商城後端 API 系統)

本專案為一個基於 **Spring Boot 3** 與 **Java 21** 開發的高效能 RESTful 購物商城後端系統。整合 **LINE OAuth 2.0** 第三方快速授權連動、**Python LINE Bot** 智慧推薦與心理測驗行銷介面、**JWT** 身份安全驗證機制、**Spring Data JPA** 與 **MySQL** 資料庫，並包含高併發防護設計與完整的 Controller / Service 層單元測試（JUnit 5 / Mockito）。

---

## 🚀 核心技術棧 (Tech Stack)

* **核心框架**：Java 21, Spring Boot 3.4.x
* **資安與身份驗證**：Spring Security, JWT (JSON Web Token), LINE OAuth 2.0 (OpenID Connect), BCrypt 加密
* **資料庫與 ORM**：MySQL 8.0, Spring Data JPA, Hibernate, NamedParameterJdbcTemplate (原子操作)
* **API 文件化與規格**：OpenAPI 3 / Swagger UI
* **單元測試**：JUnit 5, Mockito (@MockitoBean), MockMvc
* **建置與輔助工具**：Apache Maven, Jackson, Jakarta Validation

---

## ✨ 系統模組與 API 規格 (System Architecture & APIs)

### 👤 1. 會員模組 (Member Module)

**會員前台 API (`/api/v1/members`)**
* `POST /api/v1/members/register` - 會員註冊（BCrypt 密碼加密雜湊）
* `POST /api/v1/members/login` - 會員登入（發放 JWT 通行證）
* `GET /api/v1/members/{id}` - 取得個人資料詳情
* `PUT /api/v1/members/{id}/password` - 修改會員密碼
* `POST /api/v1/members/{id}/bind-line` - 手動連動綁定 LINE 帳號
* `GET /api/v1/members/callback` - LINE OAuth 自動化回傳進入點 (Callback)

**會員管理後台 API (`/api/v1/admin/members`)**
* `DELETE /api/v1/admin/members/{id}` - 停用/封鎖會員帳號（軟刪除機制 `status=0`，保留歷史消費資料）

---

### 📦 2. 商品模組 (Product Module)

**商品前台 API (`/api/v1/products`)**
* `GET /api/v1/products` - 多功能條件搜尋商品（支援關鍵字、條件篩選與分頁）
* `GET /api/v1/products/{productId}` - 取得單一商品詳情（僅限上架商品）
* `GET /api/v1/products/categories` - 獲取所有商品分類清單

**商品管理後台 API (`/api/v1/admin/products`)**
* `GET /api/v1/admin/products` - 後台：獲取所有商品列表（含已下架商品）
* `GET /api/v1/admin/products/{productId}` - 後台：取得單一商品詳情（含已下架商品）
* `POST /api/v1/admin/products` - 建立新商品
* `PUT /api/v1/admin/products/{productId}` - 更新商品資訊
* `DELETE /api/v1/admin/products/{productId}` - 下架/軟刪除商品
* `PATCH /api/v1/admin/products/{productId}/restore` - 恢復已軟刪除的商品

---

### 🎟️ 3. 優惠券與行銷模組 (Coupon Module)

**優惠券前台 API (`/api/v1/coupons`)**
* `POST /api/v1/coupons/issue` - 發放/綁定優惠券給指定會員（支援 LINE 心理測驗或行銷活動觸發）
* `GET /api/v1/coupons/my-coupons` - 查詢會員所擁有的優惠券清單與使用狀態（DTO 扁平化輸出）
* `POST /api/v1/coupons/use` - 結帳核銷優惠券（原子狀態更新）

**優惠券管理後台 API (`/api/v1/admin/coupons`)**
* `POST /api/v1/admin/coupons` - 建立新優惠券規則（支援固定金額 `FIXED_AMOUNT` 與百分比 `PERCENTAGE` 折扣、生效過期時間與數量上限）

**高併發與資料完整性設計**
* **資料庫級防超賣/超發**：利用 SQL 條件更新（`WHERE used_quantity < total_quantity`）實現原子扣減，防止 Race Condition。
* **唯一性防護**：`user_coupon` 表設定 `(member_id, coupon_id)` 聯合唯一約束（Unique Constraint），杜絕同會員重複領取。
* **N+1 查詢優化**：查詢個人優惠券採用 JPA `JOIN FETCH`，一次性載入關聯資料。

---

### 🤖 4. LINE Bot 整合專用 API

**LINE Bot 服務介面 (`/api/v1/bot/products`)**
* `GET /api/v1/bot/products/recommendations` - 提供給 Python LINE Bot 呼叫的智慧商品推薦介面

---

## 📖 API 文件 (Swagger API Documentation)

本專案已整合 **OpenAPI 3 (Swagger UI)**。專案啟動後，可直接透過瀏覽器存取以下路徑進行 API 測試與調用預覽：

* 🌐 **Swagger UI 介面 URL**：`http://localhost:8080/swagger-ui.html`
* 📄 **OpenAPI Docs (JSON) URL**：`http://localhost:8080/v3/api-docs`

---

## 🛠️ 本地開發環境設定 (Getting Started)

### 1. 複製專案庫
```bash
git clone [https://github.com/02-JAY/springboot-mall.git](https://github.com/02-JAY/springboot-mall.git)
cd springboot-mall