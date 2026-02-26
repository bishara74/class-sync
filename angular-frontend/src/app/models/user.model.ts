export type Role = 'TEACHER' | 'STUDENT';

export interface User {
  id: number;
  name: string;
  email: string;
  role: Role;
  password?: string;
  neptunCode?: string;
}
