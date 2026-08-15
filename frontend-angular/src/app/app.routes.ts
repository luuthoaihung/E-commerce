import { Routes } from '@angular/router';
import { Login } from './login/login';
import { ProductManagement } from './pages/product-management/product-management';
import { ProductForm } from './pages/product-form/product-form';
import { AdminLayout } from './layouts/admin-layout/admin-layout';
import { authGuard } from './guards/auth.guard';

export const routes: Routes = [
  { path: 'login', component: Login },

  // Nhóm các trang quản lý nằm chung trong khung Sidebar của AdminLayout
  {
    path: '',
    component: AdminLayout,
    canActivate: [authGuard], // Bảo vệ toàn bộ khu vực admin bằng Guard
    children: [
      { path: 'products', component: ProductManagement },
      { path: 'products/add', component: ProductForm },
      { path: 'products/edit/:id', component: ProductForm },
      { path: '', redirectTo: 'products', pathMatch: 'full' }
    ]
  },

  // Dự phòng chuyển hướng về sản phẩm nếu gõ sai URL
  { path: '**', redirectTo: 'products' }
];