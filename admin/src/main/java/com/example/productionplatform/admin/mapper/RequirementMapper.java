package com.example.productionplatform.admin.mapper;

import com.example.productionplatform.admin.model.Requirement;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface RequirementMapper {

    String JOIN_SELECT = """
            SELECT r.id, r.req_no AS reqNo, r.title, r.description,
                   r.product_id AS productId, r.order_id AS orderId,
                   p.name AS productName, o.order_no AS orderNo,
                   r.priority, r.status, r.proposer,
                   r.create_time AS createTime, r.update_time AS updateTime
            FROM requirement r
            LEFT JOIN product p ON r.product_id = p.id
            LEFT JOIN production_order o ON r.order_id = o.id
            """;

    @Select(JOIN_SELECT + " ORDER BY r.create_time DESC")
    List<Requirement> findAll();

    @Select(JOIN_SELECT + " ORDER BY r.create_time DESC LIMIT #{limit}")
    List<Requirement> findRecent(int limit);

    @Select("SELECT COUNT(*) FROM requirement")
    int countAll();

    @Select("SELECT COUNT(*) FROM requirement WHERE status = #{status}")
    int countByStatus(String status);

    @Insert("""
            INSERT INTO requirement (req_no, title, description, product_id, order_id, priority, status, proposer, create_time, update_time)
            VALUES (#{reqNo}, #{title}, #{description}, #{productId}, #{orderId}, #{priority}, #{status}, #{proposer}, #{createTime}, #{updateTime})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Requirement req);

    @Update("""
            UPDATE requirement SET title=#{title}, description=#{description}, product_id=#{productId},
            order_id=#{orderId}, priority=#{priority}, status=#{status}, proposer=#{proposer}, update_time=#{updateTime}
            WHERE id=#{id}
            """)
    int update(Requirement req);

    @Update("UPDATE requirement SET status=#{status}, update_time=NOW() WHERE id=#{id}")
    int updateStatus(@Param("id") Long id, @Param("status") String status);

    @Delete("DELETE FROM requirement WHERE id=#{id}")
    int deleteById(Long id);
}
