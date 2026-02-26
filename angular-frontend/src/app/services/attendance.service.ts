import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { AttendanceSession, AttendanceRecord, CreateSessionRequest, CheckInRequest } from '../models';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class AttendanceService {

  private baseUrl = `${environment.apiBaseUrl}/api/attendance`;

  constructor(private http: HttpClient) {}

  createSession(request: CreateSessionRequest): Observable<AttendanceSession> {
    return this.http.post<AttendanceSession>(`${this.baseUrl}/sessions`, request).pipe(
      catchError(this.handleError)
    );
  }

  checkIn(request: CheckInRequest): Observable<AttendanceRecord> {
    return this.http.post<AttendanceRecord>(`${this.baseUrl}/check-in`, request).pipe(
      catchError(this.handleError)
    );
  }

  private handleError(error: HttpErrorResponse): Observable<never> {
    let errorMessage = 'An unexpected error occurred.';
    if (error.error && typeof error.error === 'string') {
      errorMessage = error.error;
    } else if (error.error?.message) {
      errorMessage = error.error.message;
    }
    return throwError(() => errorMessage);
  }
}
