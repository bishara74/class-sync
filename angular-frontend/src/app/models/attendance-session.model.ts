import { User } from './user.model';

export interface AttendanceSession {
  id: number;
  teacher: User;
  courseName: string;
  generatedCode: string;
  expirationTime: string;
}
