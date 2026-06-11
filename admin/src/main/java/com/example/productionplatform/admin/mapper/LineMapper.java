package com.example.productionplatform.admin.mapper;

import com.example.productionplatform.admin.model.ProductionLine;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface LineMapper {

    @Select("SELECT id, code, name, capacity, status, create_time AS createTime FROM production_line ORDER BY id")
    List<ProductionLine> findAll();

    @Select("SELECT COUNT(*) FROM production_line")
    int countAll();

    @Select("SELECT COUNT(*) FROM production_line WHERE status='active'")
    int countActive();

    @Insert("INSERT INTO production_line (code, name, capacity, status, create_time) VALUES (#{code}, #{name}, #{capacity}, #{status}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ProductionLine line);

    @Update("UPDATE production_line SET code=#{code}, name=#{name}, capacity=#{capacity}, status=#{status} WHERE id=#{id}")
    int update(ProductionLine line);

    @Delete("DELETE FROM production_line WHERE id=#{id}")
    int deleteById(Long id);
}
