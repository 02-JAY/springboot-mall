package com.jay.springbootmall.coupon.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jay.springbootmall.coupon.dto.CouponResponse;
import com.jay.springbootmall.coupon.model.DiscountType;
import com.jay.springbootmall.coupon.service.CouponService;
import com.jay.springbootmall.util.JwtUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CouponClientController.class) // 指向 CouponClientController
@AutoConfigureMockMvc(addFilters = false) // 關閉測試時的 Security 過濾器
public class CouponClientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CouponService couponService;

    @MockitoBean
    private JwtUtils jwtUtils; // Mock 掉以防止 JwtAuthenticationFilter 初始化報錯

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext; // 避開 JPA Metamodel 檢查

    @Test
    @DisplayName("測試發放優惠券給指定會員 - 成功 (200 OK)")
    public void issueCouponToMember_Success() throws Exception {
        doNothing().when(couponService).issueCouponToMember(anyLong(), anyString());

        // API 路徑：POST /api/v1/coupons/issue?memberId=1&couponCode=LINE_QUIZ_2026
        mockMvc.perform(post("/api/v1/coupons/issue")
                        .param("memberId", "1")
                        .param("couponCode", "LINE_QUIZ_2026"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("測試查詢會員擁有的優惠券清單 - 成功 (200 OK)")
    public void getMemberCoupons_Success() throws Exception {
        CouponResponse coupon = new CouponResponse();
        coupon.setCouponId(1L);
        coupon.setCode("LINE_QUIZ_2026");
        coupon.setTitle("心理測驗限定9折券");
        coupon.setDiscountType(DiscountType.PERCENTAGE);
        coupon.setDiscountValue(new BigDecimal("0.90"));
        coupon.setMinSpending(new BigDecimal("500.00"));
        coupon.setStartTime(LocalDateTime.now().minusDays(1));
        coupon.setEndTime(LocalDateTime.now().plusDays(10));
        coupon.setIsUsed(false);

        when(couponService.getMemberCoupons(1L)).thenReturn(List.of(coupon));

        // API 路徑：GET /api/v1/coupons/my-coupons?memberId=1
        mockMvc.perform(get("/api/v1/coupons/my-coupons")
                        .param("memberId", "1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].couponId").value(1))
                .andExpect(jsonPath("$[0].code").value("LINE_QUIZ_2026"))
                .andExpect(jsonPath("$[0].title").value("心理測驗限定9折券"))
                .andExpect(jsonPath("$[0].discountValue").value(0.90))
                .andExpect(jsonPath("$[0].isUsed").value(false));
    }

    @Test
    @DisplayName("測試核銷/使用優惠券 - 成功 (200 OK)")
    public void useCoupon_Success() throws Exception {
        doNothing().when(couponService).useCoupon(anyLong(), anyLong());

        // API 路徑：POST /api/v1/coupons/use?memberId=1&couponId=10
        mockMvc.perform(post("/api/v1/coupons/use")
                        .param("memberId", "1")
                        .param("couponId", "10"))
                .andExpect(status().isOk());
    }
}