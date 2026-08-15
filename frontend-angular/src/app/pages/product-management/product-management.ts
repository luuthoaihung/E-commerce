import { Component, OnInit, ChangeDetectorRef, Inject, PLATFORM_ID } from '@angular/core'; // Thêm ChangeDetectorRef
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { Router } from '@angular/router';
import { ProductService } from '../../services/product.service';
import { Product } from '../../models/product.model';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-product-management',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './product-management.html',
  styleUrl: './product-management.css'
})
export class ProductManagement implements OnInit {
  products: Product[] = [];
  isLoading = false;
  errorMessage = '';

  constructor(
    private productService: ProductService,
    public router: Router,
    private cdr: ChangeDetectorRef,
    @Inject(PLATFORM_ID) private platformId: Object
  ) {}

  ngOnInit(): void {
    if (isPlatformBrowser(this.platformId)) {
      this.loadProducts();
    }
  }

  loadProducts(): void {
    this.isLoading = true;
    this.productService.getProducts().subscribe({
      next: (res: any) => {
        console.log('Dữ liệu API trả về:', res);
        
        // Lấy mảng sản phẩm từ content của phân trang
        this.products = res.result?.content || []; 
        
        this.isLoading = false;       // Tắt trạng thái đang tải
        this.cdr.detectChanges();     // <--- 2. Ép Angular cập nhật giao diện ngay lập tức
      },
      error: (err) => {
        console.error('Lỗi tải danh sách sản phẩm:', err);
        this.isLoading = false;
        this.cdr.detectChanges();     // <--- 3. Ép cập nhật nếu có lỗi xảy ra
      }
    });
  }

  editProduct(id: any) {
    this.router.navigate([`/products/edit`, id]);
  }

  deleteProduct(id: string | undefined): void {
    if (!id) return;
    if (confirm('Bạn có chắc chắn muốn xóa sản phẩm này không?')) {
      this.productService.deleteProduct(id).subscribe({
        next: () => {
          alert('Xóa mềm sản phẩm thành công!');
          this.loadProducts();
        },
        error: (err) => {
          alert('Lỗi khi xóa sản phẩm!');
          console.error(err);
        }
      });
    }
  }
}