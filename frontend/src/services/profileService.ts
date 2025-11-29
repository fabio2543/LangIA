import api from './api';
import type {
  UserProfileDetails,
  UpdatePersonalDataRequest,
  LearningPreferences,
  SkillAssessment,
  SkillAssessmentResponse,
  NotificationSettings,
} from '../types';

// ============================================
// Profile Details Endpoints
// ============================================

export const profileService = {
  // Get user profile details
  getProfileDetails: async (): Promise<UserProfileDetails> => {
    const response = await api.get<UserProfileDetails>('/profile/details');
    return response.data;
  },

  // Update user profile details
  updateProfileDetails: async (data: UpdatePersonalDataRequest): Promise<UserProfileDetails> => {
    const response = await api.patch<UserProfileDetails>('/profile/details', data);
    return response.data;
  },
};

// ============================================
// Email Change Endpoints
// ============================================

export const emailChangeService = {
  // Request email change (sends verification code to new email)
  requestEmailChange: async (newEmail: string): Promise<{ message: string }> => {
    const response = await api.post<{ message: string }>('/profile/email/change-request', {
      newEmail,
    });
    return response.data;
  },

  // Verify email change with 6-digit code
  verifyEmailChange: async (code: string): Promise<{ message: string }> => {
    const response = await api.post<{ message: string }>('/profile/email/verify', {
      code,
    });
    return response.data;
  },
};

// ============================================
// Learning Preferences Endpoints
// ============================================

export const learningPreferencesService = {
  // Get learning preferences
  getLearningPreferences: async (): Promise<LearningPreferences> => {
    const response = await api.get<LearningPreferences>('/profile/learning-preferences');
    return response.data;
  },

  // Update learning preferences
  updateLearningPreferences: async (data: LearningPreferences): Promise<LearningPreferences> => {
    const response = await api.put<LearningPreferences>('/profile/learning-preferences', data);
    return response.data;
  },
};

// ============================================
// Skill Assessment Endpoints
// ============================================

export const skillAssessmentService = {
  // Get all skill assessments
  getSkillAssessments: async (): Promise<SkillAssessmentResponse[]> => {
    const response = await api.get<SkillAssessmentResponse[]>('/profile/skill-assessments');
    return response.data;
  },

  // Create new skill assessment
  createSkillAssessment: async (data: SkillAssessment): Promise<SkillAssessmentResponse> => {
    const response = await api.post<SkillAssessmentResponse>('/profile/skill-assessments', data);
    return response.data;
  },
};

// ============================================
// Notification Settings Endpoints
// ============================================

export const notificationSettingsService = {
  // Get notification settings
  getNotificationSettings: async (): Promise<NotificationSettings> => {
    const response = await api.get<NotificationSettings>('/profile/notification-settings');
    return response.data;
  },

  // Update notification settings
  updateNotificationSettings: async (data: NotificationSettings): Promise<NotificationSettings> => {
    const response = await api.put<NotificationSettings>('/profile/notification-settings', data);
    return response.data;
  },
};
