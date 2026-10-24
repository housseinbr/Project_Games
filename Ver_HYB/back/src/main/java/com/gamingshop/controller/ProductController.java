package com.gamingshop.controller;

import com.gamingshop.dto.ApiResponse;
import com.gamingshop.dto.CategoryDeletionInfoDTO;
import com.gamingshop.dto.ProductDTO;
import com.gamingshop.entity.Category;
import com.gamingshop.repository.CategoryRepository;
import com.gamingshop.repository.ProductRepository;
import com.gamingshop.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ProductController {

	private final ProductService productService;
	private final CategoryRepository categoryRepository;
	private final ProductRepository productRepository;

	// ── PUBLIC ────────────────────────────────────────────────

	@GetMapping("/products")
	public ResponseEntity<ApiResponse<List<ProductDTO>>> getAllProducts(
			@RequestParam(required = false) String categoryId, @RequestParam(required = false) String search) {
		List<ProductDTO> products;
		if (categoryId != null) {
			products = productService.getProductsByCategory(categoryId);
		} else if (search != null && !search.isEmpty()) {
			products = productService.searchProducts(search);
		} else {
			products = productService.getAllProducts();
		}
		return ResponseEntity.ok(ApiResponse.success(products));
	}

	@GetMapping("/products/{id}")
	public ResponseEntity<ApiResponse<ProductDTO>> getProduct(@PathVariable String id) {
		return ResponseEntity.ok(ApiResponse.success(productService.getProductById(id)));
	}

	@GetMapping("/categories")
	public ResponseEntity<ApiResponse<List<Category>>> getCategories() {
		return ResponseEntity.ok(ApiResponse.success(categoryRepository.findAll()));
	}

	// ── ADMIN PRODUCTS ────────────────────────────────────────

	@PostMapping("/admin/products")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponse<ProductDTO>> createProduct(@RequestBody ProductDTO dto) {
		return ResponseEntity.ok(ApiResponse.success("Product created", productService.createProduct(dto)));
	}

	@PutMapping("/admin/products/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponse<ProductDTO>> updateProduct(@PathVariable String id, @RequestBody ProductDTO dto) {
		return ResponseEntity.ok(ApiResponse.success("Product updated", productService.updateProduct(id, dto)));
	}

	@DeleteMapping("/admin/products/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable String id) {
		productService.deleteProduct(id);
		return ResponseEntity.ok(ApiResponse.success("Product deleted", null));
	}

	// ── ADMIN CATEGORIES ──────────────────────────────────────

	@PostMapping("/admin/categories")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponse<Category>> createCategory(@RequestBody Category category) {
		resolveParent(category);
		Category saved = categoryRepository.save(category);
		return ResponseEntity.ok(ApiResponse.success("Category created", saved));
	}

	@PutMapping("/admin/categories/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponse<Category>> updateCategory(@PathVariable String id,
			@RequestBody Category category) {
		return categoryRepository.findById(id).map(existing -> {
			existing.setName(category.getName());
			existing.setDescription(category.getDescription());
			existing.setIcon(category.getIcon());
			existing.setParentId(category.getParentId());
			resolveParent(existing);
			Category saved = categoryRepository.save(existing);
			return ResponseEntity.ok(ApiResponse.success("Category updated", saved));
		}).orElse(ResponseEntity.notFound().<ApiResponse<Category>>build());
	}

	// Résout parentName à partir de parentId (ou les vide si pas de parent)
	private void resolveParent(Category category) {
		if (category.getParentId() != null && !category.getParentId().isBlank()) {
			categoryRepository.findById(category.getParentId())
					.ifPresentOrElse(parent -> category.setParentName(parent.getName()), () -> {
						category.setParentId(null);
						category.setParentName(null);
					});
		} else {
			category.setParentId(null);
			category.setParentName(null);
		}
	}

	// Aperçu de ce qui sera supprimé (sous-catégories + produits liés), à afficher
	// AVANT confirmation
	@GetMapping("/categories/{id}/deletion-info")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponse<CategoryDeletionInfoDTO>> getCategoryDeletionInfo(@PathVariable String id) {
		if (!categoryRepository.existsById(id)) {
			return ResponseEntity.notFound().<ApiResponse<CategoryDeletionInfoDTO>>build();
		}
		List<String> allIds = collectCategoryAndDescendantIds(id);
		int productCount = allIds.stream().mapToInt(catId -> (int) productRepository.countByCategoryId(catId)).sum();

		CategoryDeletionInfoDTO dto = new CategoryDeletionInfoDTO();
		dto.setSubcategoryCount(allIds.size() - 1); // -1 : on exclut la catégorie elle-même
		dto.setProductCount(productCount);
		return ResponseEntity.ok(ApiResponse.success("OK", dto));
	}

	@DeleteMapping("/admin/categories/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable String id) {
		if (!categoryRepository.existsById(id)) {
			return ResponseEntity.notFound().<ApiResponse<Void>>build();
		}
		// Supprime en cascade : la catégorie, toutes ses sous-catégories, et tous les
		// produits liés à chacune
		List<String> allIds = collectCategoryAndDescendantIds(id);
		allIds.forEach(productRepository::deleteByCategoryId);
		categoryRepository.deleteAllById(allIds);
		return ResponseEntity.ok(ApiResponse.success("Category and linked products deleted", null));
	}

	// Récupère l'id de la catégorie + tous les ids de ses sous-catégories
	// (récursif, tous niveaux)
	private List<String> collectCategoryAndDescendantIds(String categoryId) {
		List<String> result = new ArrayList<>();
		result.add(categoryId);
		for (Category child : categoryRepository.findByParentId(categoryId)) {
			result.addAll(collectCategoryAndDescendantIds(child.getId()));
		}
		return result;
	}
}