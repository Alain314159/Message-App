import { authService } from '@/services/supabase/auth.service';
import { supabase } from '@/services/supabase/config';

jest.mock('@/services/supabase/config', () => ({
  supabase: {
    auth: {
      signInWithPassword: jest.fn(),
      signUp: jest.fn(),
      signOut: jest.fn(),
      resetPasswordForEmail: jest.fn(),
      getUser: jest.fn(),
      setSession: jest.fn(),
      onAuthStateChange: jest.fn(),
    },
  },
}));

describe('authService', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('signIn', () => {
    it('should sign in with email and password', async () => {
      const mockUser = { id: '1', email: 'test@test.com' };
      const mockSession = { access_token: 'token' };
      (supabase.auth.signInWithPassword as jest.Mock).mockResolvedValue({
        data: { user: mockUser, session: mockSession },
        error: null,
      });

      const result = await authService.signIn('test@test.com', 'password123');

      expect(supabase.auth.signInWithPassword).toHaveBeenCalledWith({
        email: 'test@test.com',
        password: 'password123',
      });
      expect(result.user).toEqual(mockUser);
      expect(result.session).toEqual(mockSession);
      expect(result.error).toBeNull();
    });

    it('should return error on sign in failure', async () => {
      (supabase.auth.signInWithPassword as jest.Mock).mockResolvedValue({
        data: { user: null, session: null },
        error: { message: 'Invalid credentials' },
      });

      const result = await authService.signIn('test@test.com', 'wrong');

      expect(result.user).toBeNull();
      expect(result.error).toBe('Invalid credentials');
    });
  });

  describe('signUp', () => {
    it('should sign up with email, password, and display name', async () => {
      const mockUser = { id: '1', email: 'new@test.com' };
      (supabase.auth.signUp as jest.Mock).mockResolvedValue({
        data: { user: mockUser },
        error: null,
      });

      const result = await authService.signUp('new@test.com', 'password123', 'New User');

      expect(supabase.auth.signUp).toHaveBeenCalledWith({
        email: 'new@test.com',
        password: 'password123',
        options: {
          data: { display_name: 'New User' },
        },
      });
      expect(result.user).toEqual(mockUser);
      expect(result.error).toBeNull();
    });

    it('should return error on sign up failure', async () => {
      (supabase.auth.signUp as jest.Mock).mockResolvedValue({
        data: { user: null },
        error: { message: 'Email already registered' },
      });

      const result = await authService.signUp('dup@test.com', 'pass', 'Dup');
      expect(result.error).toBe('Email already registered');
    });
  });

  describe('signOut', () => {
    it('should sign out', async () => {
      (supabase.auth.signOut as jest.Mock).mockResolvedValue({ error: null });

      await authService.signOut();

      expect(supabase.auth.signOut).toHaveBeenCalled();
    });
  });

  describe('resetPassword', () => {
    it('should reset password for email', async () => {
      (supabase.auth.resetPasswordForEmail as jest.Mock).mockResolvedValue({ error: null });

      const result = await authService.resetPassword('test@test.com', 'https://example.com');

      expect(supabase.auth.resetPasswordForEmail).toHaveBeenCalledWith(
        'test@test.com',
        { redirectTo: 'https://example.com' }
      );
      expect(result.error).toBeNull();
    });
  });

  describe('getCurrentUser', () => {
    it('should get current user', async () => {
      const mockUser = { id: '1', email: 'test@test.com' };
      (supabase.auth.getUser as jest.Mock).mockResolvedValue({
        data: { user: mockUser },
        error: null,
      });

      const result = await authService.getCurrentUser();
      expect(result.user).toEqual(mockUser);
    });
  });

  describe('setSession', () => {
    it('should set session with tokens', async () => {
      const mockSession = { access_token: 'new', refresh_token: 'refresh' };
      (supabase.auth.setSession as jest.Mock).mockResolvedValue({
        data: { session: mockSession },
        error: null,
      });

      const result = await authService.setSession('access', 'refresh');
      expect(result.session).toEqual(mockSession);
    });
  });
});
