package com.example.productionplatform.admin.mapper;

import com.example.productionplatform.admin.model.ProductionOrder;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ProductionOrderMapper {

    String JOIN_SELECT = """
            SELECT o.id, o.order_no AS orderNo, o.product_id AS productId, o.line_id AS lineId,
                   p.name AS productName, l.name AS lineName, o.quantity, o.completed_qty AS completedQty,
                   o.status, o.create_time AS createTime, o.update_time AS updateTime
            FROM production_order o
            LEFT JOIN product p ON o.product_id = p.id
            LEFT JOIN production_line l ON o.line_id = l.id
            """;

    @Select(JOIN_SELECT + " ORDER BY o.create_time DESC")
    List<ProductionOrder> findAll();

    @Select(JOIN_SELECT + " ORDER BY o.create_time DESC LIMIT #{limit}")
    List<ProductionOrder> findRecent(int limit);

    @Select("SELECT COUNT(*) FROM production_order")
    int countAll();

    @Select("SELECT COUNT(*) FROM production_order WHERE status = #{status}")
    int countByStatus(String status);

    @Insert("""
            INSERT INTO production_order (order_no, product_id, line_id, quantity, completed_qty, status, create_time, update_time)
            VALUES (#{orderNo}, #{productId}, #{lineId}, #{quantity}, #{completedQty}, #{status}, #{createTime}, #{updateTime})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ProductionOrder order);

    @Update("""
            UPDATE production_order SET product_id=#{productId}, line_id=#{lineId}, quantity=#{quantity},
            completed_qty=#{completedQty}, status=#{status}, update_time=#{updateTime} WHERE id=#{id}
            """)
    int update(ProductionOrder order);

    @Update("UPDATE production_order SET status=#{status}, update_time=NOW() WHERE id=#{id}")
    int updateStatus(@Param("id") Long id, @Param("status") String status);

    @Delete("DELETE FROM production_order WHERE id=#{id}")
    int deleteById(Long id);
}
