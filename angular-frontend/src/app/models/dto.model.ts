export interface LoginRequest {
  email: string;
  password: string;
  neptunCode: string | null;
}

export interface CreateSessionRequest {
  teacherId: number;
  courseName: string;
  validForMinutes: number;
}

export interface CheckInRequest {
  studentId: number;
  code: string;
}

export interface LoginResponse {
  token: string;
  user: import('./user.model').User;
}
