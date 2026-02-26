import { User } from './user.model';
import { AttendanceSession } from './attendance-session.model';

export type AttendanceStatus = 'PRESENT' | 'LATE' | 'ABSENT';

export interface AttendanceRecord {
  id: number;
  student: User;
  session: AttendanceSession;
  checkInTime: string;
  status: AttendanceStatus;
}
