-- ================================================
-- Migration V4: Seed Profile Functionalities
-- Description: Associates functionalities with profiles applying hierarchy inheritance
-- Total: STUDENT(17) + TEACHER(45) + ADMIN(83)
-- ================================================

-- ====================================
-- STUDENT Profile (17 functionalities)
-- ====================================
-- Own Profile (7)
INSERT INTO profile_functionalities (profile_id, functionality_id, granted_by_inheritance)
SELECT p.id, f.id, FALSE
FROM profiles p
CROSS JOIN functionalities f
WHERE p.code = 'STUDENT'
AND f.code IN (
    'view_profile',
    'update_profile',
    'view_progress',
    'view_certificates',
    'download_certificates',
    'view_notifications',
    'manage_preferences'
);

-- Courses (2)
INSERT INTO profile_functionalities (profile_id, functionality_id, granted_by_inheritance)
SELECT p.id, f.id, FALSE
FROM profiles p
CROSS JOIN functionalities f
WHERE p.code = 'STUDENT'
AND f.code IN (
    'view_courses',
    'enroll_course'
);

-- Lessons (2)
INSERT INTO profile_functionalities (profile_id, functionality_id, granted_by_inheritance)
SELECT p.id, f.id, FALSE
FROM profiles p
CROSS JOIN functionalities f
WHERE p.code = 'STUDENT'
AND f.code IN (
    'view_lessons',
    'complete_lessons'
);

-- Students (2)
INSERT INTO profile_functionalities (profile_id, functionality_id, granted_by_inheritance)
SELECT p.id, f.id, FALSE
FROM profiles p
CROSS JOIN functionalities f
WHERE p.code = 'STUDENT'
AND f.code IN (
    'submit_exercises',
    'view_feedback'
);

-- AI (4)
INSERT INTO profile_functionalities (profile_id, functionality_id, granted_by_inheritance)
SELECT p.id, f.id, FALSE
FROM profiles p
CROSS JOIN functionalities f
WHERE p.code = 'STUDENT'
AND f.code IN (
    'chat_with_ai',
    'view_ai_history',
    'clear_ai_history',
    'view_ai_analytics'
);

-- ====================================
-- TEACHER Profile (45 functionalities)
-- = 17 inherited from STUDENT + 28 own
-- ====================================

-- Inherit all from STUDENT (17 functionalities)
INSERT INTO profile_functionalities (profile_id, functionality_id, granted_by_inheritance)
SELECT t.id, pf.functionality_id, TRUE
FROM profiles t
CROSS JOIN profiles s
INNER JOIN profile_functionalities pf ON pf.profile_id = s.id
WHERE t.code = 'TEACHER'
AND s.code = 'STUDENT';

-- Courses (5 additional)
INSERT INTO profile_functionalities (profile_id, functionality_id, granted_by_inheritance)
SELECT p.id, f.id, FALSE
FROM profiles p
CROSS JOIN functionalities f
WHERE p.code = 'TEACHER'
AND f.code IN (
    'create_courses',
    'edit_courses',
    'delete_courses',
    'publish_courses',
    'duplicate_courses'
);

-- Lessons (10 additional)
INSERT INTO profile_functionalities (profile_id, functionality_id, granted_by_inheritance)
SELECT p.id, f.id, FALSE
FROM profiles p
CROSS JOIN functionalities f
WHERE p.code = 'TEACHER'
AND f.code IN (
    'create_lessons',
    'edit_lessons',
    'delete_lessons',
    'reorder_lessons',
    'upload_lesson_materials',
    'delete_lesson_materials',
    'preview_lessons',
    'schedule_lessons',
    'view_lesson_analytics',
    'manage_lesson_prerequisites'
);

-- Students (8 additional)
INSERT INTO profile_functionalities (profile_id, functionality_id, granted_by_inheritance)
SELECT p.id, f.id, FALSE
FROM profiles p
CROSS JOIN functionalities f
WHERE p.code = 'TEACHER'
AND f.code IN (
    'view_students',
    'view_student_details',
    'view_student_progress',
    'view_student_grades',
    'grade_exercises',
    'provide_feedback',
    'view_attendance',
    'mark_attendance'
);

-- WhatsApp (5 additional)
INSERT INTO profile_functionalities (profile_id, functionality_id, granted_by_inheritance)
SELECT p.id, f.id, FALSE
FROM profiles p
CROSS JOIN functionalities f
WHERE p.code = 'TEACHER'
AND f.code IN (
    'send_whatsapp_messages',
    'receive_whatsapp_messages',
    'view_whatsapp_history',
    'manage_whatsapp_templates',
    'manage_whatsapp_contacts'
);

-- ====================================
-- ADMIN Profile (83 functionalities)
-- = 45 inherited from TEACHER + 38 own
-- ====================================

-- Inherit all from TEACHER (45 functionalities - includes STUDENT inheritance)
INSERT INTO profile_functionalities (profile_id, functionality_id, granted_by_inheritance)
SELECT a.id, pf.functionality_id, TRUE
FROM profiles a
CROSS JOIN profiles t
INNER JOIN profile_functionalities pf ON pf.profile_id = t.id
WHERE a.code = 'ADMIN'
AND t.code = 'TEACHER';

-- Courses (3 additional)
INSERT INTO profile_functionalities (profile_id, functionality_id, granted_by_inheritance)
SELECT p.id, f.id, FALSE
FROM profiles p
CROSS JOIN functionalities f
WHERE p.code = 'ADMIN'
AND f.code IN (
    'archive_courses',
    'manage_course_categories',
    'unenroll_course'
);

-- Students (5 additional)
INSERT INTO profile_functionalities (profile_id, functionality_id, granted_by_inheritance)
SELECT p.id, f.id, FALSE
FROM profiles p
CROSS JOIN functionalities f
WHERE p.code = 'ADMIN'
AND f.code IN (
    'communicate_with_students',
    'manage_student_enrollment',
    'export_student_data',
    'issue_certificates',
    'revoke_certificates'
);

-- Teachers (8)
INSERT INTO profile_functionalities (profile_id, functionality_id, granted_by_inheritance)
SELECT p.id, f.id, FALSE
FROM profiles p
CROSS JOIN functionalities f
WHERE p.code = 'ADMIN'
AND f.code IN (
    'view_teachers',
    'view_teacher_details',
    'assign_teachers',
    'remove_teachers',
    'view_teacher_performance',
    'manage_teacher_permissions',
    'view_teacher_schedule',
    'export_teacher_reports'
);

-- AI (6 additional)
INSERT INTO profile_functionalities (profile_id, functionality_id, granted_by_inheritance)
SELECT p.id, f.id, FALSE
FROM profiles p
CROSS JOIN functionalities f
WHERE p.code = 'ADMIN'
AND f.code IN (
    'configure_ai_settings',
    'manage_ai_prompts',
    'train_ai_models',
    'test_ai_responses',
    'manage_ai_costs',
    'export_ai_conversations'
);

-- WhatsApp (3 additional)
INSERT INTO profile_functionalities (profile_id, functionality_id, granted_by_inheritance)
SELECT p.id, f.id, FALSE
FROM profiles p
CROSS JOIN functionalities f
WHERE p.code = 'ADMIN'
AND f.code IN (
    'configure_whatsapp',
    'broadcast_whatsapp',
    'view_whatsapp_analytics'
);

-- System (13)
INSERT INTO profile_functionalities (profile_id, functionality_id, granted_by_inheritance)
SELECT p.id, f.id, FALSE
FROM profiles p
CROSS JOIN functionalities f
WHERE p.code = 'ADMIN'
AND f.code IN (
    'manage_users',
    'create_users',
    'edit_users',
    'delete_users',
    'view_system_stats',
    'manage_settings',
    'manage_integrations',
    'view_audit_logs',
    'manage_permissions',
    'view_system_health',
    'manage_backups',
    'configure_notifications',
    'manage_api_keys'
);
