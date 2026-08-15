import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class CategoryService {
  private apiUrl = 'http://localhost:8081/categories';

  constructor(private http: HttpClient) {}

  // Lấy danh sách: GET /categories/list
  getCategories(): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/list`);
  }

  // Thêm mới: POST /categories/add
  createCategory(categoryData: any): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/add`, categoryData);
  }

  // Cập nhật: PUT /categories/edit/{id}
  updateCategory(id: number, categoryData: any): Observable<any> {
    return this.http.put<any>(`${this.apiUrl}/edit/${id}`, categoryData);
  }

  // Xóa: DELETE /categories/delete/{id}
  deleteCategory(id: number): Observable<any> {
    return this.http.delete<any>(`${this.apiUrl}/delete/${id}`);
  }
}