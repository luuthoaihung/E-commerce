import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth.service'; 

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './login.html',
  styleUrl: './login.css'
})
export class Login {
  username = '';
  password = '';

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  onLogin() {
    this.authService.login({ username: this.username, password: this.password }).subscribe({
      next: () => {
        console.log('Đăng nhập thành công!');
        this.router.navigate(['/products']);
      },
      error: (err) => {
        console.error(err);
        alert('Đăng nhập thất bại, vui lòng kiểm tra lại!');
      }
    });
  }
}