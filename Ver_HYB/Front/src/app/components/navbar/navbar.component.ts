import { Component, OnInit, Output, EventEmitter } from '@angular/core';
import { Router } from '@angular/router';
import { Category, Product } from '../../models/models';
import { ApiService } from '../../services/services';

interface CategoryMegaMenu {
  category: Category;
  groups: { subcategory: Category; products: Product[] }[];
  singles: Product[]; // produits rattachés directement à la catégorie, sans sous-catégorie
}

@Component({
  selector: 'app-navbar',
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.scss']
})
export class NavbarComponent implements OnInit {
  @Output() burgerClick = new EventEmitter<void>();
  @Output() categoryClick = new EventEmitter<string>();
  // Émet la catégorie/sous-catégorie choisie dans un méga-menu
  @Output() megaMenuSelect = new EventEmitter<{ categoryId: string; name: string }>();

  categories: Category[] = [];
  // Catégories de premier niveau (celles affichées dans la barre du haut)
  topCategories: Category[] = [];
  // Une entrée par catégorie de premier niveau QUI A des sous-catégories avec des produits
  megaMenus: Map<string, CategoryMegaMenu> = new Map();
  // Id de la catégorie dont le méga-menu est actuellement ouvert (null = aucun)
  openMegaMenuId: string | null = null;

  specialLinks = [
    { label: '🌐 Promotion', icon: 'fa-globe' },
    { label: '🎮 Jeux à Petit Prix', icon: 'fa-gamepad' }
  ];

  constructor(
    public router: Router,
    private api: ApiService
  ) {}

  ngOnInit(): void {
    this.loadCategories();
  }

  loadCategories() {
    this.api.getCategories().subscribe(cats => {
      this.categories = cats;
      this.topCategories = cats.filter(c => !c.parentId);
      this.buildMegaMenus();
    });
  }

  // Construit un méga-menu pour CHAQUE catégorie de premier niveau qui a des sous-catégories
  // (généralisation : plus besoin de coder en dur "Carte cadeau", ça marche pour n'importe quelle catégorie)
  private buildMegaMenus() {
    const subcatsByParent = new Map<string, Category[]>();
    this.categories.forEach(c => {
      if (c.parentId) {
        if (!subcatsByParent.has(c.parentId)) subcatsByParent.set(c.parentId, []);
        subcatsByParent.get(c.parentId)!.push(c);
      }
    });

    const parentsWithChildren = this.topCategories.filter(c => subcatsByParent.has(c.id));
    this.megaMenus.clear();
    if (parentsWithChildren.length === 0) { return; }

    this.api.getProducts().subscribe(allProducts => {
      parentsWithChildren.forEach(cat => {
        const subs = subcatsByParent.get(cat.id) || [];
        const groups = subs
          .map(sub => ({
            subcategory: sub,
            products: allProducts.filter(p => p.categoryId === sub.id)
          }))
          .filter(g => g.products.length > 0);
        const singles = allProducts.filter(p => p.categoryId === cat.id);
        this.megaMenus.set(cat.id, { category: cat, groups, singles });
      });
    });
  }

  hasMegaMenu(categoryId: string): boolean {
    return this.megaMenus.has(categoryId);
  }

  getMegaMenu(categoryId: string): CategoryMegaMenu | undefined {
    return this.megaMenus.get(categoryId);
  }

  toggleMegaMenu(categoryId: string) {
    this.openMegaMenuId = this.openMegaMenuId === categoryId ? null : categoryId;
  }

  closeMegaMenu() {
    this.openMegaMenuId = null;
  }

  // Clic sur une catégorie SANS sous-catégorie -> filtre direct
  selectCategory(categoryId: string) {
    this.openMegaMenuId = null;
    this.categoryClick.emit(categoryId);
  }

  // Clic sur un produit/sous-catégorie À L'INTÉRIEUR d'un méga-menu ouvert
  selectMegaMenuItem(categoryId: string, name: string) {
    this.openMegaMenuId = null;
    this.megaMenuSelect.emit({ categoryId, name });
  }

  onBurgerClick(): void {
    this.burgerClick.emit();
  }
}
