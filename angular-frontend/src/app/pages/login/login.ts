import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { Role } from '../../models';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './login.html',
  styleUrl: './login.css'
})
export class LoginComponent {

  role: Role = 'STUDENT';
  email = '';
  password = '';
  neptunCode = '';
  error = '';

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  selectRole(role: Role): void {
    this.role = role;
    this.error = '';
  }

  onSubmit(): void {
    this.error = '';

    this.authService.login({
      email: this.email,
      password: this.password,
      neptunCode: this.role === 'STUDENT' ? this.neptunCode : null
    }).subscribe({
      next: (user) => {
        if (user.role === 'TEACHER') {
          this.router.navigate(['/teacher']);
        } else {
          this.router.navigate(['/student']);
        }
      },
      error: (errorMessage: string) => {
        this.error = errorMessage;
      }
    });
  }
}
