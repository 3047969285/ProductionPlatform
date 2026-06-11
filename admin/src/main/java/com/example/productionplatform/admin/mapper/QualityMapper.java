package com.example.productionplatform.admin.mapper;

import com.example.productionplatform.admin.model.QualityRecord;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface QualityMapper {

    @Select("""
            SELECT q.id, q.order_id AS orderId, o.order_no AS orderNo, p.name AS productName,
                   q.result, q.defect_count AS defectCount, q.inspector, q.remark, q.inspect_time AS inspectTime
            FROM quality_record q
            LEFT JOIN production_order o ON q.order_id = o.id
            LEFT JOIN product p ON o.product_id = p.id
            ORDER BY q.inspect_time DESC
            """)
    List<QualityRecord> findAll();

    @Select("SELECT COUNT(*) FROM quality_record")
    int countAll();

    @Select("SELECT COUNT(*) FROM quality_record WHERE result = #{result}")
    int countByResult(String result);

    @Insert("""
            INSERT INTO quality_record (order_id, result, defect_count, inspector, remark, inspect_time)
            VALUES (#{orderId}, #{result}, #{defectCount}, #{inspector}, #{remark}, #{inspectTime})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(QualityRecord record);

    @Delete("DELETE FROM quality_record WHERE id=#{id}")
    int deleteById(Long id);
}
