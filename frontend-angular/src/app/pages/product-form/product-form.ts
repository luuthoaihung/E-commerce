import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { ProductService } from '../../services/product.service';
import { CategoryService } from '../../services/category.service';

@Component({
  selector: 'app-product-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule], // Bắt buộc phải có ReactiveFormsModule
  templateUrl: './product-form.html',
  styleUrl: './product-form.css'
})
export class ProductForm implements OnInit {
  productForm!: FormGroup;
  isEditMode = false;
  categories: any[] = [];
  
  selectedFile: File | null = null;
  imagePreview: string | null = null;
  isLoading = false;

  constructor(
    private fb: FormBuilder,
    private productService: ProductService,
    private categoryService: CategoryService,
    private router: Router,
    private route: ActivatedRoute
    
  ) {}

  ngOnInit(): void {
    // 1. Khởi tạo FormGroup khớp với các formControlName trong HTML
    this.productForm = this.fb.group({
      name: ['', Validators.required],
      price: [0, [Validators.required, Validators.min(0)]],
      stockQuantity: [0, [Validators.required, Validators.min(0)]],
      categoryId: ['', Validators.required]
    });

    this.loadCategories();
  }

  loadCategories() {
    // Giả sử bạn có hàm này trong service
    this.categoryService.getCategories().subscribe(res => {
      this.categories = res.result || res;
    });
  }

  // 2. Xử lý khi người dùng chọn file ảnh
  onFileSelected(event: any) {
    const file = event.target.files[0];
    if (file) {
      this.selectedFile = file;

      // Đọc file để tạo ảnh xem trước (Preview)
      const reader = new FileReader();
      reader.onload = () => {
        this.imagePreview = reader.result as string;
      };
      reader.readAsDataURL(file);
    }
  }

  // 3. Hàm xử lý khi bấm nút submit form
  onSubmit() {
    if (this.productForm.invalid) {
      alert('Vui lòng điền đầy đủ thông tin hợp lệ!');
      return;
    }

    // Đóng gói dữ liệu vào FormData để gửi lên Backend kèm theo file ảnh
    const formData = new FormData();
    formData.append('name', this.productForm.get('name')?.value);
    formData.append('price', this.productForm.get('price')?.value);
    formData.append('stockQuantity', this.productForm.get('stockQuantity')?.value);
    formData.append('categoryId', this.productForm.get('categoryId')?.value);

    if (this.selectedFile) {
      formData.append('file', this.selectedFile); // Chữ 'file' này phải trùng khớp với @RequestParam ở bên Backend Java
    }

    // Gọi API thêm sản phẩm
    this.productService.addProduct(formData as any).subscribe({
      next: () => {
        alert('Thêm sản phẩm thành công!');
        this.router.navigate(['/products']);
      },
      error: (err) => {
        console.error('Lỗi khi thêm sản phẩm:', err);
        alert('Thêm sản phẩm thất bại!');
      }
    });
  }

  onCancel() {
    if (confirm('Bạn có chắc muốn hủy? Mọi thay đổi sẽ không được lưu.')) {
      this.router.navigate(['/products']);
    }
  }

  removeImage() {
    this.selectedFile = null;
    this.imagePreview = null;
    // Nếu form đang dùng Reactive Forms và có trường chứa path ảnh, cần reset nó ở đây
    // ví dụ: this.productForm.patchValue({ imagePath: '' });
  }

}