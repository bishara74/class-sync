import { Component, ChangeDetectorRef } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../services/auth.service';
import { AttendanceService } from '../../services/attendance.service';

@Component({
  selector: 'app-teacher-dashboard',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './teacher-dashboard.html',
  styleUrl: './teacher-dashboard.css'
})
export class TeacherDashboardComponent {

  courseName = '';
  validFor = 10;
  generatedCode: string | null = null;
  error = '';
  loading = false;

  constructor(
    private authService: AuthService,
    private attendanceService: AttendanceService,
    private cdr: ChangeDetectorRef
  ) {}

  onGenerate(): void {
    this.error = '';
    this.generatedCode = null;
    this.loading = true;

    const user = this.authService.currentUser;
    if (!user) return;

    this.attendanceService.createSession({
      teacherId: user.id,
      courseName: this.courseName,
      validForMinutes: Number(this.validFor)
    }).subscribe({
      next: (session) => {
        this.generatedCode = session.generatedCode;
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: (errorMessage: string) => {
        this.error = errorMessage;
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }
}
