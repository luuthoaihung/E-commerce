import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';

@Injectable({
  providedIn: 'root' // Tự động đăng ký vào root, không cần khai báo trong providers
})
export class AuthService {
  private baseUrl = 'http://localhost:8081'; // URL backend của bạn

  constructor(private http: HttpClient) {}

  // Hàm đăng nhập
  login(credentials: any): Observable<any> {
    return this.http.post(`${this.baseUrl}/auth/login`, credentials).pipe(
      tap((response: any) => {
        // Lưu token ngay khi nhận được response
        const token = response.token || response.accessToken || response.result;
        if (token) {
          localStorage.setItem('token', token);
        }
      })
    );
  }

  // Kiểm tra xem đã đăng nhập chưa
  isLoggedIn(): boolean {
    return !!localStorage.getItem('token');
  }

  // Đăng xuất
  logout() {
    localStorage.removeItem('token');
  }

  // Lấy token hiện tại
  getToken(): string | null {
    return localStorage.getItem('token');
  }
}