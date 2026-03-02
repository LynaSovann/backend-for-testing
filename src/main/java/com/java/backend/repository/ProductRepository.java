package com.java.backend.repository;

import com.java.backend.model.Product;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ProductRepository {

    @Select("""
            SELECT * FROM products
            """)
    @Results(
            id = "productMapper",
            value = {
                    @Result(property = "productId", column = "product_id"),
                    @Result(property = "productName", column = "product_name"),
                    @Result(property = "imageUrl", column = "image_url"),
                    @Result(property = "description", column = "description")
            }
    )
    List<Product> getAllProducts();


    @Select("""
            SELECT * FROM products WHERE product_id = #{id}
            """)
    @ResultMap("productMapper")
    Product getProductById(@Param("id") Long id);


    @Select("""
            INSERT INTO products (product_name, image_url, description)
            VALUES (#{productName}, #{imageUrl}, #{description})
            RETURNING *
            """)
    @ResultMap("productMapper")
    Product createProduct(@Param("productName") String productName,
                          @Param("description") String description,
                          @Param("imageUrl") String imageUrl);


    @Update("""
            UPDATE products
            SET product_name = #{productName},
                image_url = #{imageUrl},
                description = #{description}
            WHERE product_id = #{id}
            """)
    Boolean updateProduct(@Param("id") Long id,
                          @Param("productName") String productName,
                          @Param("description") String description,
                          @Param("imageUrl") String imageUrl);


    @Delete("""
            DELETE FROM products WHERE product_id = #{id}
            """)
    Boolean deleteProduct(@Param("id") Long id);
}