import { Routes } from '@angular/router';
import { LoginComponent } from './pages/login/login';
import { TeacherDashboardComponent } from './pages/teacher-dashboard/teacher-dashboard';
import { StudentDashboardComponent } from './pages/student-dashboard/student-dashboard';
import { roleGuard } from './guards/auth.guard';

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'teacher', component: TeacherDashboardComponent, canActivate: [roleGuard('TEACHER')] },
  { path: 'student', component: StudentDashboardComponent, canActivate: [roleGuard('STUDENT')] },
  { path: '', redirectTo: '/login', pathMatch: 'full' },
  { path: '**', redirectTo: '/login' }
];
