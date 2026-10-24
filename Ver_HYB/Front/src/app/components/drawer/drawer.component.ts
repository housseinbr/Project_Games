import { Component, OnInit, Input, Output, EventEmitter, HostListener } from '@angular/core';
import { Category } from '../../models/models';

@Component({
  selector: 'app-drawer',
  templateUrl: './drawer.component.html',
  styleUrls: ['./drawer.component.scss']
})
export class DrawerComponent implements OnInit {
  @Input() isOpen = false;
  @Input() categories: Category[] = [];
  @Output() close = new EventEmitter<void>();
  @Output() categoryClick = new EventEmitter<string>();

  searchQuery = '';
  expandedGroup: number | null = null;

  quickLinks = [
    { label: '🌐 Promotion' },
    { label: '🎮 Jeux à Petit Prix' }
  ];

  constructor() {}

  ngOnInit(): void {}

  @HostListener('document:keydown.escape')
  onEscape(): void {
    if (this.isOpen) {
      this.onClose();
    }
  }

  onClose(): void {
    this.close.emit();
  }

  toggleGroup(index: number): void {
    if (this.expandedGroup === index) {
      this.expandedGroup = null;
    } else {
      this.expandedGroup = index;
    }
  }

  onSearch(): void {
    console.log('Drawer search:', this.searchQuery);
  }

  onCategoryClick(categoryId: string): void {
    this.categoryClick.emit(categoryId);
    this.onClose();
  }

  isGroupExpanded(index: number): boolean {
    return this.expandedGroup === index;
  }

  // Catégories de premier niveau uniquement (les sous-catégories sont affichées imbriquées)
  get topCategories(): Category[] {
    return this.categories.filter(c => !c.parentId);
  }

  getSubcategories(parentId: string): Category[] {
    return this.categories.filter(c => c.parentId === parentId);
  }
}
