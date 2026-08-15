import { inject, PLATFORM_ID } from '@angular/core';
import { Router, CanActivateFn } from '@angular/router';
import { isPlatformBrowser } from '@angular/common';

export const authGuard: CanActivateFn = (route, state) => {
  const router = inject(Router);
  const platformId = inject(PLATFORM_ID);

  // 1. Nếu đang chạy ở môi trường Server (SSR), cho phép qua để render trang tĩnh ban đầu
  if (!isPlatformBrowser(platformId)) {
    return true;
  }

  // 2. Chạy trên trình duyệt: Kiểm tra token trong localStorage
  const token = localStorage.getItem('token'); 
  
  if (token && token.trim() !== '') {
    return true; // Token tồn tại -> Cho phép truy cập
  }

  // 3. Nếu không có token -> Điều hướng về trang login và chặn truy cập
  router.navigate(['/login']);
  return false;
};