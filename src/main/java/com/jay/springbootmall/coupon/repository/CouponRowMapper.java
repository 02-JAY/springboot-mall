package com.jay.springbootmall.coupon.repository;

import com.jay.springbootmall.coupon.model.Coupon;
import com.jay.springbootmall.coupon.model.DiscountType;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class CouponRowMapper implements RowMapper<Coupon> {
    @Override
    public Coupon mapRow(ResultSet rs, int rowNum) throws SQLException {
        Coupon coupon = new Coupon();
        coupon.setCouponId(rs.getLong("coupon_id"));
        coupon.setCode(rs.getString("code"));
        coupon.setTitle(rs.getString("title"));
        coupon.setDiscountType(DiscountType.valueOf(rs.getString("discount_type")));
        coupon.setDiscountValue(rs.getBigDecimal("discount_value"));
        coupon.setMinSpending(rs.getBigDecimal("min_spending"));

        if (rs.getTimestamp("start_time") != null) {
            coupon.setStartTime(rs.getTimestamp("start_time").toLocalDateTime());
        }
        if (rs.getTimestamp("end_time") != null) {
            coupon.setEndTime(rs.getTimestamp("end_time").toLocalDateTime());
        }

        coupon.setTotalQuantity(rs.getInt("total_quantity"));
        coupon.setUsedQuantity(rs.getInt("used_quantity"));

        if (rs.getTimestamp("created_date") != null) {
            coupon.setCreatedDate(rs.getTimestamp("created_date").toLocalDateTime());
        }
        if (rs.getTimestamp("last_modified_date") != null) {
            coupon.setLastModifiedDate(rs.getTimestamp("last_modified_date").toLocalDateTime());
        }

        return coupon;
    }
}
