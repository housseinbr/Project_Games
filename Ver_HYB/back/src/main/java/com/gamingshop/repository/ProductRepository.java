package com.gamingshop.repository;

import com.gamingshop.entity.Product;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface ProductRepository extends MongoRepository<Product, String> {

	List<Product> findByCategoryId(String categoryId);

	long countByCategoryId(String categoryId);

	void deleteByCategoryId(String categoryId);

	// Recherche insensible à la casse sur name OU description (équivalent LIKE
	// '%keyword%')
	@Query("{ '$or': [ { 'name': { '$regex': ?0, '$options': 'i' } }, { 'description': { '$regex': ?0, '$options': 'i' } } ] }")
	List<Product> searchByKeyword(String keyword);

	@Query("{ 'stock': { '$gt': 0 } }")
	List<Product> findAvailableProducts();
}
