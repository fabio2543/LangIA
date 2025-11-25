-- ================================================
-- Migration V3: Seed Functionalities
-- Description: Inserts all system functionalities organized by module
-- ================================================

-- Module: OWN_PROFILE (7 functionalities)
INSERT INTO functionalities (code, description, module, active) VALUES
('view_profile', 'View own profile information', 'OWN_PROFILE', TRUE),
('update_profile', 'Update own profile information', 'OWN_PROFILE', TRUE),
('view_progress', 'View own learning progress', 'OWN_PROFILE', TRUE),
('view_certificates', 'View own certificates', 'OWN_PROFILE', TRUE),
('download_certificates', 'Download own certificates', 'OWN_PROFILE', TRUE),
('view_notifications', 'View own notifications', 'OWN_PROFILE', TRUE),
('manage_preferences', 'Manage own preferences and settings', 'OWN_PROFILE', TRUE);

-- Module: COURSES (10 functionalities)
INSERT INTO functionalities (code, description, module, active) VALUES
('view_courses', 'View available courses', 'COURSES', TRUE),
('enroll_course', 'Enroll in courses', 'COURSES', TRUE),
('unenroll_course', 'Unenroll from courses', 'COURSES', TRUE),
('create_courses', 'Create new courses', 'COURSES', TRUE),
('edit_courses', 'Edit existing courses', 'COURSES', TRUE),
('delete_courses', 'Delete courses', 'COURSES', TRUE),
('publish_courses', 'Publish courses', 'COURSES', TRUE),
('archive_courses', 'Archive courses', 'COURSES', TRUE),
('duplicate_courses', 'Duplicate existing courses', 'COURSES', TRUE),
('manage_course_categories', 'Manage course categories', 'COURSES', TRUE);

-- Module: LESSONS (12 functionalities)
INSERT INTO functionalities (code, description, module, active) VALUES
('view_lessons', 'View lesson content', 'LESSONS', TRUE),
('complete_lessons', 'Mark lessons as completed', 'LESSONS', TRUE),
('create_lessons', 'Create new lessons', 'LESSONS', TRUE),
('edit_lessons', 'Edit existing lessons', 'LESSONS', TRUE),
('delete_lessons', 'Delete lessons', 'LESSONS', TRUE),
('reorder_lessons', 'Reorder lessons within courses', 'LESSONS', TRUE),
('upload_lesson_materials', 'Upload lesson materials', 'LESSONS', TRUE),
('delete_lesson_materials', 'Delete lesson materials', 'LESSONS', TRUE),
('preview_lessons', 'Preview lessons before publishing', 'LESSONS', TRUE),
('schedule_lessons', 'Schedule lesson availability', 'LESSONS', TRUE),
('view_lesson_analytics', 'View lesson engagement analytics', 'LESSONS', TRUE),
('manage_lesson_prerequisites', 'Manage lesson prerequisites', 'LESSONS', TRUE);

-- Module: STUDENTS (15 functionalities)
INSERT INTO functionalities (code, description, module, active) VALUES
('view_students', 'View student list', 'STUDENTS', TRUE),
('view_student_details', 'View detailed student information', 'STUDENTS', TRUE),
('view_student_progress', 'View student learning progress', 'STUDENTS', TRUE),
('view_student_grades', 'View student grades', 'STUDENTS', TRUE),
('grade_exercises', 'Grade student exercises', 'STUDENTS', TRUE),
('provide_feedback', 'Provide feedback to students', 'STUDENTS', TRUE),
('submit_exercises', 'Submit exercises for grading', 'STUDENTS', TRUE),
('view_feedback', 'View teacher feedback', 'STUDENTS', TRUE),
('communicate_with_students', 'Send messages to students', 'STUDENTS', TRUE),
('manage_student_enrollment', 'Manage student enrollments', 'STUDENTS', TRUE),
('export_student_data', 'Export student data and reports', 'STUDENTS', TRUE),
('view_attendance', 'View student attendance', 'STUDENTS', TRUE),
('mark_attendance', 'Mark student attendance', 'STUDENTS', TRUE),
('issue_certificates', 'Issue completion certificates', 'STUDENTS', TRUE),
('revoke_certificates', 'Revoke issued certificates', 'STUDENTS', TRUE);

-- Module: TEACHERS (8 functionalities)
INSERT INTO functionalities (code, description, module, active) VALUES
('view_teachers', 'View teacher list', 'TEACHERS', TRUE),
('view_teacher_details', 'View detailed teacher information', 'TEACHERS', TRUE),
('assign_teachers', 'Assign teachers to courses', 'TEACHERS', TRUE),
('remove_teachers', 'Remove teachers from courses', 'TEACHERS', TRUE),
('view_teacher_performance', 'View teacher performance metrics', 'TEACHERS', TRUE),
('manage_teacher_permissions', 'Manage teacher-specific permissions', 'TEACHERS', TRUE),
('view_teacher_schedule', 'View teacher schedules', 'TEACHERS', TRUE),
('export_teacher_reports', 'Export teacher activity reports', 'TEACHERS', TRUE);

-- Module: AI (10 functionalities)
INSERT INTO functionalities (code, description, module, active) VALUES
('chat_with_ai', 'Chat with AI assistant', 'AI', TRUE),
('view_ai_history', 'View AI conversation history', 'AI', TRUE),
('clear_ai_history', 'Clear AI conversation history', 'AI', TRUE),
('configure_ai_settings', 'Configure AI assistant settings', 'AI', TRUE),
('manage_ai_prompts', 'Manage AI system prompts', 'AI', TRUE),
('view_ai_analytics', 'View AI usage analytics', 'AI', TRUE),
('train_ai_models', 'Train and fine-tune AI models', 'AI', TRUE),
('test_ai_responses', 'Test AI responses and quality', 'AI', TRUE),
('manage_ai_costs', 'Monitor and manage AI costs', 'AI', TRUE),
('export_ai_conversations', 'Export AI conversation logs', 'AI', TRUE);

-- Module: WHATSAPP (8 functionalities)
INSERT INTO functionalities (code, description, module, active) VALUES
('send_whatsapp_messages', 'Send messages via WhatsApp', 'WHATSAPP', TRUE),
('receive_whatsapp_messages', 'Receive messages via WhatsApp', 'WHATSAPP', TRUE),
('view_whatsapp_history', 'View WhatsApp message history', 'WHATSAPP', TRUE),
('configure_whatsapp', 'Configure WhatsApp integration', 'WHATSAPP', TRUE),
('manage_whatsapp_templates', 'Manage WhatsApp message templates', 'WHATSAPP', TRUE),
('broadcast_whatsapp', 'Send broadcast messages via WhatsApp', 'WHATSAPP', TRUE),
('view_whatsapp_analytics', 'View WhatsApp engagement analytics', 'WHATSAPP', TRUE),
('manage_whatsapp_contacts', 'Manage WhatsApp contact lists', 'WHATSAPP', TRUE);

-- Module: SYSTEM (13 functionalities)
INSERT INTO functionalities (code, description, module, active) VALUES
('manage_users', 'Manage system users', 'SYSTEM', TRUE),
('create_users', 'Create new users', 'SYSTEM', TRUE),
('edit_users', 'Edit user information', 'SYSTEM', TRUE),
('delete_users', 'Delete users from system', 'SYSTEM', TRUE),
('view_system_stats', 'View system statistics', 'SYSTEM', TRUE),
('manage_settings', 'Manage system settings', 'SYSTEM', TRUE),
('manage_integrations', 'Manage external integrations', 'SYSTEM', TRUE),
('view_audit_logs', 'View system audit logs', 'SYSTEM', TRUE),
('manage_permissions', 'Manage user permissions', 'SYSTEM', TRUE),
('view_system_health', 'View system health metrics', 'SYSTEM', TRUE),
('manage_backups', 'Manage system backups', 'SYSTEM', TRUE),
('configure_notifications', 'Configure system notifications', 'SYSTEM', TRUE),
('manage_api_keys', 'Manage API keys and integrations', 'SYSTEM', TRUE);
