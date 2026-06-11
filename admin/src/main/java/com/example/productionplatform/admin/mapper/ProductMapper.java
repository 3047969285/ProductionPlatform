package com.example.productionplatform.admin.mapper;

import com.example.productionplatform.admin.model.Product;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ProductMapper {

    @Select("SELECT id, code, name, spec, unit, create_time AS createTime FROM product ORDER BY id")
    List<Product> findAll();

    @Select("SELECT COUNT(*) FROM product")
    int countAll();

    @Insert("INSERT INTO product (code, name, spec, unit, create_time) VALUES (#{code}, #{name}, #{spec}, #{unit}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Product product);

    @Update("UPDATE product SET code=#{code}, name=#{name}, spec=#{spec}, unit=#{unit} WHERE id=#{id}")
    int update(Product product);

    @Delete("DELETE FROM product WHERE id=#{id}")
    int deleteById(Long id);
}
