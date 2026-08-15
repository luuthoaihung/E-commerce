export interface Product {
  id?: string;
  name: string;
  description?: string;
  price: number;
  stockQuantity: number;
  imageUrl?: string;
  categoryId: string;
  categoryName?: string;
  deleted?: boolean;
}