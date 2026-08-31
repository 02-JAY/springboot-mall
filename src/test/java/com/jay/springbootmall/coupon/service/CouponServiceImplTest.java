package com.jay.springbootmall.coupon.service;

import com.jay.springbootmall.coupon.dto.CouponResponse;
import com.jay.springbootmall.coupon.dto.CreateCouponRequest;
import com.jay.springbootmall.coupon.model.Coupon;
import com.jay.springbootmall.coupon.model.DiscountType;
import com.jay.springbootmall.coupon.model.UserCoupon;
import com.jay.springbootmall.coupon.repository.CouponJdbcRepository;
import com.jay.springbootmall.coupon.repository.CouponRepository;
import com.jay.springbootmall.coupon.repository.UserCouponRepository;
import com.jay.springbootmall.coupon.service.impl.CouponServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CouponServiceImplTest {

    @Mock
    private CouponRepository couponRepository;

    @Mock
    private UserCouponRepository userCouponRepository;

    @Mock
    private CouponJdbcRepository couponJdbcRepository;

    @InjectMocks
    private CouponServiceImpl couponService;

    @Nested
    @DisplayName("建立優惠券測試 (createCoupon)")
    class CreateCouponTests {

        @Test
        @DisplayName("成功建立優惠券")
        void createCoupon_Success() {
            CreateCouponRequest request = new CreateCouponRequest();
            request.setCode("LINE_QUIZ_2026");
            request.setTitle("心理測驗限定9折券");
            request.setDiscountType(DiscountType.PERCENTAGE);
            request.setDiscountValue(new BigDecimal("0.90"));
            request.setMinSpending(new BigDecimal("500.00"));
            request.setStartTime(LocalDateTime.now().plusDays(1));
            request.setEndTime(LocalDateTime.now().plusDays(10));
            request.setTotalQuantity(100);

            when(couponRepository.existsByCode("LINE_QUIZ_2026")).thenReturn(false);
            when(couponRepository.save(any(Coupon.class))).thenAnswer(invocation -> {
                Coupon coupon = invocation.getArgument(0);
                coupon.setCouponId(1L);
                return coupon;
            });

            CouponResponse response = couponService.createCoupon(request);

            assertNotNull(response);
            assertEquals(1L, response.getCouponId());
            assertEquals("LINE_QUIZ_2026", response.getCode());
            assertEquals(new BigDecimal("0.90"), response.getDiscountValue());
            assertNull(response.getIsUsed());
            verify(couponRepository, times(1)).save(any(Coupon.class));
        }

        @Test
        @DisplayName("折扣碼重複時拋出 IllegalArgumentException")
        void createCoupon_DuplicateCode_ThrowsException() {
            CreateCouponRequest request = new CreateCouponRequest();
            request.setCode("EXIST_CODE");

            when(couponRepository.existsByCode("EXIST_CODE")).thenReturn(true);

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                    couponService.createCoupon(request)
            );
            assertEquals("折扣碼已存在: EXIST_CODE", exception.getMessage());
            verify(couponRepository, never()).save(any(Coupon.class));
        }

        @Test
        @DisplayName("生效時間晚於過期時間時拋出 IllegalArgumentException")
        void createCoupon_InvalidTimeRange_ThrowsException() {
            CreateCouponRequest request = new CreateCouponRequest();
            request.setCode("TIME_TEST");
            request.setStartTime(LocalDateTime.now().plusDays(5));
            request.setEndTime(LocalDateTime.now().plusDays(1)); // 結束時間早於開始時間

            when(couponRepository.existsByCode("TIME_TEST")).thenReturn(false);

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                    couponService.createCoupon(request)
            );
            assertEquals("生效時間不可晚於過期時間", exception.getMessage());
            verify(couponRepository, never()).save(any(Coupon.class));
        }
    }

    @Nested
    @DisplayName("發放/領取優惠券測試 (issueCouponToMember)")
    class IssueCouponTests {

        @Test
        @DisplayName("會員成功領取優惠券")
        void issueCoupon_Success() {
            Long memberId = 1L;
            String code = "LINE_QUIZ_2026";

            Coupon coupon = new Coupon();
            coupon.setCouponId(10L);
            coupon.setCode(code);
            coupon.setStartTime(LocalDateTime.now().minusDays(1));
            coupon.setEndTime(LocalDateTime.now().plusDays(5));
            coupon.setTotalQuantity(100);
            coupon.setUsedQuantity(0);

            when(couponRepository.findByCode(code)).thenReturn(Optional.of(coupon));
            when(userCouponRepository.existsByMemberIdAndCoupon_CouponId(memberId, 10L)).thenReturn(false);
            when(couponJdbcRepository.incrementUsedQuantity(10L)).thenReturn(1);

            couponService.issueCouponToMember(memberId, code);

            verify(userCouponRepository, times(1)).save(any(UserCoupon.class));
            verify(couponJdbcRepository, times(1)).incrementUsedQuantity(10L);
        }

        @Test
        @DisplayName("折扣碼不存在時拋出 IllegalArgumentException")
        void issueCoupon_NotFound_ThrowsException() {
            Long memberId = 1L;
            String code = "NOT_EXIST";

            when(couponRepository.findByCode(code)).thenReturn(Optional.empty());

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                    couponService.issueCouponToMember(memberId, code)
            );
            assertEquals("找不到對應的折扣碼", exception.getMessage());
        }

        @Test
        @DisplayName("優惠券已過期時拋出 IllegalStateException")
        void issueCoupon_Expired_ThrowsException() {
            Long memberId = 1L;
            String code = "EXPIRED_CODE";

            Coupon coupon = new Coupon();
            coupon.setCouponId(10L);
            coupon.setStartTime(LocalDateTime.now().minusDays(10));
            coupon.setEndTime(LocalDateTime.now().minusDays(1)); // 已過期

            when(couponRepository.findByCode(code)).thenReturn(Optional.of(coupon));

            IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                    couponService.issueCouponToMember(memberId, code)
            );
            assertEquals("該優惠券不在有效期限內", exception.getMessage());
            verify(userCouponRepository, never()).save(any(UserCoupon.class));
        }

        @Test
        @DisplayName("會員已領取過時拋出 IllegalStateException")
        void issueCoupon_AlreadyClaimed_ThrowsException() {
            Long memberId = 1L;
            String code = "CLAIMED_CODE";

            Coupon coupon = new Coupon();
            coupon.setCouponId(10L);
            coupon.setStartTime(LocalDateTime.now().minusDays(1));
            coupon.setEndTime(LocalDateTime.now().plusDays(5));

            when(couponRepository.findByCode(code)).thenReturn(Optional.of(coupon));
            when(userCouponRepository.existsByMemberIdAndCoupon_CouponId(memberId, 10L)).thenReturn(true);

            IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                    couponService.issueCouponToMember(memberId, code)
            );
            assertEquals("該會員已領取過此優惠券", exception.getMessage());
            verify(couponJdbcRepository, never()).incrementUsedQuantity(anyLong());
        }

        @Test
        @DisplayName("優惠券名額已滿扣減失敗時拋出 IllegalStateException")
        void issueCoupon_SoldOut_ThrowsException() {
            Long memberId = 1L;
            String code = "SOLDOUT_CODE";

            Coupon coupon = new Coupon();
            coupon.setCouponId(10L);
            coupon.setStartTime(LocalDateTime.now().minusDays(1));
            coupon.setEndTime(LocalDateTime.now().plusDays(5));

            when(couponRepository.findByCode(code)).thenReturn(Optional.of(coupon));
            when(userCouponRepository.existsByMemberIdAndCoupon_CouponId(memberId, 10L)).thenReturn(false);
            when(couponJdbcRepository.incrementUsedQuantity(10L)).thenReturn(0); // 原子更新回傳 0 筆

            IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                    couponService.issueCouponToMember(memberId, code)
            );
            assertEquals("該優惠券已被領取完畢", exception.getMessage());
            verify(userCouponRepository, never()).save(any(UserCoupon.class));
        }
    }

    @Nested
    @DisplayName("查詢與核銷測試")
    class QueryAndUseCouponTests {

        @Test
        @DisplayName("成功查詢會員優惠券清單")
        void getMemberCoupons_Success() {
            Long memberId = 1L;

            Coupon coupon = new Coupon();
            coupon.setCouponId(100L);
            coupon.setCode("DISCOUNT_100");
            coupon.setTitle("滿額現折");
            coupon.setDiscountType(DiscountType.FIXED_AMOUNT);
            coupon.setDiscountValue(new BigDecimal("100.00"));
            coupon.setMinSpending(new BigDecimal("500.00"));
            coupon.setStartTime(LocalDateTime.now().minusDays(1));
            coupon.setEndTime(LocalDateTime.now().plusDays(10));

            UserCoupon userCoupon = new UserCoupon();
            userCoupon.setUserCouponId(1L);
            userCoupon.setMemberId(memberId);
            userCoupon.setCoupon(coupon);
            userCoupon.setIsUsed(false);

            when(userCouponRepository.findAllByMemberIdWithCoupon(memberId)).thenReturn(List.of(userCoupon));

            List<CouponResponse> result = couponService.getMemberCoupons(memberId);

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(100L, result.get(0).getCouponId());
            assertEquals("DISCOUNT_100", result.get(0).getCode());
            assertFalse(result.get(0).getIsUsed());
        }

        @Test
        @DisplayName("成功核銷優惠券")
        void useCoupon_Success() {
            Long memberId = 1L;
            Long couponId = 100L;

            Coupon coupon = new Coupon();
            coupon.setCouponId(couponId);
            coupon.setStartTime(LocalDateTime.now().minusDays(1));
            coupon.setEndTime(LocalDateTime.now().plusDays(5));

            when(couponRepository.findById(couponId)).thenReturn(Optional.of(coupon));
            when(couponJdbcRepository.markCouponAsUsed(memberId, couponId)).thenReturn(1);

            assertDoesNotThrow(() -> couponService.useCoupon(memberId, couponId));
            verify(couponJdbcRepository, times(1)).markCouponAsUsed(memberId, couponId);
        }

        @Test
        @DisplayName("核銷已過期的優惠券時拋出 IllegalStateException")
        void useCoupon_Expired_ThrowsException() {
            Long memberId = 1L;
            Long couponId = 100L;

            Coupon coupon = new Coupon();
            coupon.setCouponId(couponId);
            coupon.setStartTime(LocalDateTime.now().minusDays(10));
            coupon.setEndTime(LocalDateTime.now().minusDays(1)); // 已過期

            when(couponRepository.findById(couponId)).thenReturn(Optional.of(coupon));

            IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                    couponService.useCoupon(memberId, couponId)
            );
            assertEquals("該優惠券已過期或尚未生效", exception.getMessage());
            verify(couponJdbcRepository, never()).markCouponAsUsed(anyLong(), anyLong());
        }

        @Test
        @DisplayName("核銷失敗（已使用過或非持有人）時拋出 IllegalStateException")
        void useCoupon_AlreadyUsedOrInvalid_ThrowsException() {
            Long memberId = 1L;
            Long couponId = 100L;

            Coupon coupon = new Coupon();
            coupon.setCouponId(couponId);
            coupon.setStartTime(LocalDateTime.now().minusDays(1));
            coupon.setEndTime(LocalDateTime.now().plusDays(5));

            when(couponRepository.findById(couponId)).thenReturn(Optional.of(coupon));
            when(couponJdbcRepository.markCouponAsUsed(memberId, couponId)).thenReturn(0);

            IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                    couponService.useCoupon(memberId, couponId)
            );
            assertEquals("優惠券核銷失敗，可能已使用過或尚未領取", exception.getMessage());
        }
    }
}