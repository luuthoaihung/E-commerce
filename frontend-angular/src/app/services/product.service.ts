import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Product } from '../models/product.model';

@Injectable({
  providedIn: 'root'
})
export class ProductService {
  private apiUrl = 'http://localhost:8081/products';

  constructor(private http: HttpClient) {}

  // Lấy danh sách sản phẩm (Khớp với GET /products/list)
  getProducts(name?: string, min?: number, max?: number): Observable<any> {
    let params = new HttpParams();
    if (name) params = params.set('name', name);
    if (min) params = params.set('min', min);
    if (max) params = params.set('max', max);

    return this.http.get<any>(`${this.apiUrl}/list`, { params });
  }

  // Lấy thông tin chi tiết 1 sản phẩm (Khớp với GET /products/info/{id})
  getProductById(id: string): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/info/${id}`);
  }

  // Tạo mới sản phẩm (Khớp với POST /products/add)
 addProduct(productData: any): Observable<any> {
  // Nếu dùng FormData, Angular HttpClient sẽ tự động nhận diện Content-Type là multipart/form-data
  return this.http.post(`${this.apiUrl}/products/add`, productData);
}

  // Cập nhật sản phẩm (Khớp với PUT /products/edit/{id})
  updateProduct(id: string, product: Product): Observable<any> {
    return this.http.put<any>(`${this.apiUrl}/edit/${id}`, product);
  }

  // Xóa mềm sản phẩm (Khớp với DELETE /products/delete/{id})
  deleteProduct(id: string): Observable<any> {
    return this.http.delete<any>(`${this.apiUrl}/delete/${id}`);
  }

  
}