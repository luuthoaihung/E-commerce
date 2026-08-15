import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);
  
  // Lấy token từ localStorage (đảm bảo không bị xuống dòng)
  const token = localStorage.getItem('token'); 

  let clonedReq = req;
  if (token) {
    clonedReq = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
  }

  return next(clonedReq).pipe(
    catchError((error: HttpErrorResponse) => {
      // Nếu Backend trả về lỗi 401 (Unauthorized) hoặc 403
      if (error.status === 401) {
        console.warn('Token đã hết hạn hoặc không hợp lệ. Đang chuyển về trang đăng nhập...');
        
        // 1. Xóa token hỏng khỏi bộ nhớ
        localStorage.removeItem('token');
        
        // 2. Tự động đá văng về trang login
        router.navigate(['/login']);
      }
      return throwError(() => error);
    })
  );
};