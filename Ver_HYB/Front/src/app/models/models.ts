// ============================================
// models/product.model.ts
// ============================================
export interface Product {
  id: string;
  name: string;
  description: string;
  price: number;
  stock: number;
  imageUrl: string;
  categoryId: string;
  categoryName: string;
  // ===== NOUVEAUX =====
  regions?: string;           // "Global,EU,US,TR"
  valuesAvailable?: string;   // "10$,20$,50$"
  pricesByValue?: string;     // JSON string
  regionList?: string[];      // parsé par le backend
  valueList?: string[];       // parsé par le backend
}

export interface Category {
  id: string;
  name: string;
  description: string;
  icon: string;
  parentId?: string | null;
  parentName?: string | null;
}

export interface CartItem {
  product: Product;
  quantity: number;
}

// ============================================
// models/chat.model.ts
// ============================================
export interface ChatSession {
  id: string;
  clientName: string;
  clientPhone: string;
  status: 'PENDING' | 'ACTIVE' | 'CLOSED';
  isReadByAdmin: boolean;
  orderItems: OrderItem[];
  messages: ChatMessage[];
  createdAt: string;
  unreadCount: number;
}

export interface OrderItem {
  productId: string;
  productName: string;
  quantity: number;
  unitPrice: number;
}

export interface ChatMessage {
  id?: string;
  senderType: 'CLIENT' | 'ADMIN';
  senderName: string;
  message: string;
  isRead: boolean;
  edited?: boolean;   // ← AJOUTER
  sentAt: string;
}

export interface CreateSessionRequest {
  clientName: string;
  clientPhone: string;
  items: {
    productId: string;
    productName: string;
    quantity: number;
    unitPrice: number;
  }[];
}

export interface SendMessageRequest {
  sessionId: string;
  message: string;
  senderType: string;
  senderName: string;
}

// ============================================
// Profil du compte connecté (admin ou client)
// ============================================
export interface UserProfile {
  id: string;
  username: string;
  email: string;
  role: string;
  token?: string; // présent uniquement après une mise à jour réussie
}

export interface UpdateProfileRequest {
  username?: string;
  email?: string;
  currentPassword?: string;
  newPassword?: string;
}

// Aperçu de ce qu'entraîne la suppression d'une catégorie (sous-catégories + produits liés)
export interface CategoryDeletionInfo {
  subcategoryCount: number;
  productCount: number;
}

// ============================================
// models/auth.model.ts
// ============================================
export interface AuthRequest {
  username: string;
  password: string;
}

export interface AuthResponse {
  token: string;
  username: string;
  message: string;
  roles?: string[]; // ← AJOUTE

}

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
}
export interface SubCategory {
  name: string;
  items: string[];
}

export interface CardCategory {
  name: string;
  subCategories: SubCategory[];
}
