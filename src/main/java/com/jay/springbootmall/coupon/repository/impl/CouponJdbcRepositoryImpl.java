package com.jay.springbootmall.coupon.repository.impl;

import com.jay.springbootmall.coupon.repository.CouponJdbcRepository;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public class CouponJdbcRepositoryImpl implements CouponJdbcRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public CouponJdbcRepositoryImpl(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public int incrementUsedQuantity(Long couponId) {
        String sql = "UPDATE coupon SET used_quantity = used_quantity + 1 " +
                "WHERE coupon_id = :couponId AND used_quantity < total_quantity";

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("couponId", couponId);

        return jdbcTemplate.update(sql, params);
    }

    @Override
    public int markCouponAsUsed(Long memberId, Long couponId) {
        String sql = "UPDATE user_coupon SET is_used = true, used_time = :now " +
                "WHERE member_id = :memberId AND coupon_id = :couponId AND is_used = false";

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("memberId", memberId);
        params.addValue("couponId", couponId);
        params.addValue("now", LocalDateTime.now());

        return jdbcTemplate.update(sql, params);
    }
}