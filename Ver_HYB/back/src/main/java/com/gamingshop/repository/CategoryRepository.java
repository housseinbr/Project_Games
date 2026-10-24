package com.gamingshop.repository;

import com.gamingshop.entity.Category;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface CategoryRepository extends MongoRepository<Category, String> {
	List<Category> findByParentId(String parentId);
}
