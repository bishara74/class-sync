import { Component, ChangeDetectorRef } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../services/auth.service';
import { AttendanceService } from '../../services/attendance.service';
import { AttendanceStatus } from '../../models';

@Component({
  selector: 'app-student-dashboard',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './student-dashboard.html',
  styleUrl: './student-dashboard.css'
})
export class StudentDashboardComponent {

  code = '';
  status: AttendanceStatus | null = null;
  error = '';
  loading = false;

  constructor(
    private authService: AuthService,
    private attendanceService: AttendanceService,
    private cdr: ChangeDetectorRef
  ) {}

  onCheckIn(): void {
    this.error = '';
    this.status = null;
    this.loading = true;

    const user = this.authService.currentUser;
    if (!user) return;

    this.attendanceService.checkIn({
      studentId: user.id,
      code: this.code
    }).subscribe({
      next: (record) => {
        this.status = record.status;
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
