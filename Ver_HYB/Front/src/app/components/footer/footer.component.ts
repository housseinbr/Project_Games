import { Component, Input, Output, EventEmitter } from '@angular/core';
import { Category } from '../../models/models';

@Component({
  selector: 'app-footer',
  templateUrl: './footer.component.html',
  styleUrls: ['./footer.component.scss']
})
export class FooterComponent {
  @Input() categories: Category[] = [];
  @Output() categoryClick = new EventEmitter<string>();

  onCategoryClick(categoryId: string): void {
    this.categoryClick.emit(categoryId);
  }
}
