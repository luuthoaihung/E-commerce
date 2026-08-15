import { Component, HostListener } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-admin-layout',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive, CommonModule],
  templateUrl: './admin-layout.html',
  styleUrl: './admin-layout.css'
})
export class AdminLayout {
  isCollapsed = false;
  isDropdownOpen = false;

  // Thông tin user hiển thị (có thể thay thế bằng dữ liệu thực tế từ service của bạn)
  userName = 'Lưu Thoại Hưng';
  userEmail = 'hunggokai@gmail.com';

  constructor(private authService: AuthService, private router: Router) {}

  toggleSidebar() {
    this.isCollapsed = !this.isCollapsed;
  }

  toggleDropdown(event: Event) {
    event.stopPropagation(); // Ngăn sự kiện nổi bọt
    this.isDropdownOpen = !this.isDropdownOpen;
  }

  // Tự động đóng dropdown khi click ra ngoài màn hình
  @HostListener('document:click')
  onDocumentClick() {
    this.isDropdownOpen = false;
  }

  logout() {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}