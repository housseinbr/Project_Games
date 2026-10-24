package com.gamingshop.dto;

import lombok.Data;

// Aperçu de ce que la suppression d'une catégorie va entraîner (sous-catégories + produits liés)
@Data
public class CategoryDeletionInfoDTO {
	private int subcategoryCount;
	private int productCount;
}
