package com.langia.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.langia.backend.dto.ProfileWithPermissionsDTO;
import com.langia.backend.model.Functionality;
import com.langia.backend.model.LessonContext;
import com.langia.backend.model.Profile;
import com.langia.backend.model.ProfileFunctionality;
import com.langia.backend.model.StudentContext;
import com.langia.backend.model.User;
import com.langia.backend.model.UserProfile;
import com.langia.backend.repository.ProfileFunctionalityRepository;
import com.langia.backend.repository.ProfileRepository;

@ExtendWith(MockitoExtension.class)
class PermissionServiceTest {

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private ProfileFunctionalityRepository profileFunctionalityRepository;

    @InjectMocks
    private PermissionService permissionService;

    private Profile studentProfile;
    private Profile teacherProfile;
    private Profile adminProfile;

    private Functionality viewProfile;
    private Functionality editOwn;

    @BeforeEach
    void setUp() {
        studentProfile = profile(UserProfile.STUDENT);
        teacherProfile = profile(UserProfile.TEACHER);
        adminProfile = profile(UserProfile.ADMIN);

        viewProfile = functionality("visualizar_proprio_perfil", Functionality.Module.SELF_PROFILE);
        editOwn = functionality("editar_proprias_aulas", Functionality.Module.CLASSES);
    }

    @Test
    void shouldIncludeInheritedPermissions() {
        stubProfileLookups(UserProfile.STUDENT, UserProfile.TEACHER);
        when(profileFunctionalityRepository.findByProfileId(studentProfile.getId()))
                .thenReturn(List.of(link(studentProfile, viewProfile)));
        when(profileFunctionalityRepository.findByProfileId(teacherProfile.getId()))
                .thenReturn(List.of(link(teacherProfile, editOwn)));

        Set<String> teacherPermissions = permissionService.getPermissions(UserProfile.TEACHER);

        assertThat(teacherPermissions)
                .containsExactlyInAnyOrder(viewProfile.getCode(), editOwn.getCode());

        // cache hit should avoid requery
        permissionService.getPermissions(UserProfile.TEACHER);
        verify(profileFunctionalityRepository, times(1)).findByProfileId(studentProfile.getId());
        verify(profileFunctionalityRepository, times(1)).findByProfileId(teacherProfile.getId());
    }

    @Test
    void refreshShouldReloadPermissions() {
        stubProfileLookups(UserProfile.STUDENT, UserProfile.TEACHER);
        when(profileFunctionalityRepository.findByProfileId(studentProfile.getId()))
                .thenReturn(List.of(link(studentProfile, viewProfile)));

        Functionality newPerm = functionality("nova_permissao", Functionality.Module.SYSTEM);
        when(profileFunctionalityRepository.findByProfileId(teacherProfile.getId()))
                .thenReturn(List.of(link(teacherProfile, editOwn)))
                .thenReturn(List.of(link(teacherProfile, editOwn), link(teacherProfile, newPerm)));

        assertThat(permissionService.hasPermission(UserProfile.TEACHER, editOwn.getCode())).isTrue();

        permissionService.refreshPermissions(UserProfile.TEACHER);

        assertThat(permissionService.hasPermission(UserProfile.TEACHER, newPerm.getCode())).isTrue();
    }

    @Test
    void shouldBuildProfileWithPermissionsDto() {
        stubProfileLookups(UserProfile.STUDENT);
        when(profileFunctionalityRepository.findByProfileId(studentProfile.getId()))
                .thenReturn(List.of(link(studentProfile, viewProfile)));

        ProfileWithPermissionsDTO dto = permissionService.getPerfilComPermissoes(UserProfile.STUDENT);

        assertThat(dto.getCode()).isEqualTo(UserProfile.STUDENT);
        assertThat(dto.getPermissions()).hasSize(1);
        assertThat(dto.getPermissions().get(0).getCode()).isEqualTo(viewProfile.getCode());
    }

    @Test
    void canEditAulaShouldAllowOwnerOrAdmin() {
        User teacher = User.builder().id(UUID.randomUUID()).profile(UserProfile.TEACHER).build();
        User admin = User.builder().id(UUID.randomUUID()).profile(UserProfile.ADMIN).build();
        assertThat(permissionService.canEditAula(teacher, new TestLesson(teacher.getId()))).isTrue();
        assertThat(permissionService.canEditAula(teacher, new TestLesson(UUID.randomUUID()))).isFalse();
        assertThat(permissionService.canEditAula(admin, new TestLesson(UUID.randomUUID()))).isTrue();
    }

    @Test
    void canViewAlunoShouldRespectOwnership() {
        User teacher = User.builder().id(UUID.randomUUID()).profile(UserProfile.TEACHER).build();
        User admin = User.builder().id(UUID.randomUUID()).profile(UserProfile.ADMIN).build();

        assertThat(permissionService.canViewAluno(teacher, new TestStudent(teacher.getId()))).isTrue();
        assertThat(permissionService.canViewAluno(teacher, new TestStudent(UUID.randomUUID()))).isFalse();
        assertThat(permissionService.canViewAluno(admin, new TestStudent(UUID.randomUUID()))).isTrue();
    }

    @Test
    void hasAllAndAnyShouldEvaluateSets() {
        stubProfileLookups(UserProfile.STUDENT);
        when(profileFunctionalityRepository.findByProfileId(studentProfile.getId()))
                .thenReturn(List.of(link(studentProfile, viewProfile)));

        assertThat(permissionService.hasAllPermissions(UserProfile.STUDENT, viewProfile.getCode())).isTrue();
        assertThat(permissionService.hasAnyPermission(UserProfile.STUDENT, "missing", viewProfile.getCode()))
                .isTrue();
        assertThat(permissionService.hasAllPermissions(UserProfile.STUDENT, "missing")).isFalse();
        assertThat(permissionService.hasAnyPermission(UserProfile.STUDENT, "missing")).isFalse();
    }

    private Profile profile(UserProfile userProfile) {
        return Profile.builder()
                .id(UUID.randomUUID())
                .code(userProfile)
                .name(userProfile.name())
                .hierarchyLevel(userProfile.getHierarchyLevel())
                .active(true)
                .build();
    }

    private Functionality functionality(String code, Functionality.Module module) {
        return Functionality.builder()
                .id(UUID.randomUUID())
                .code(code)
                .description(code)
                .module(module)
                .active(true)
                .build();
    }

    private ProfileFunctionality link(Profile profile, Functionality functionality) {
        return ProfileFunctionality.builder()
                .profile(profile)
                .functionality(functionality)
                .build();
    }

    private void stubProfileLookups(UserProfile... profiles) {
        for (UserProfile profile : profiles) {
            Profile entity = switch (profile) {
                case STUDENT -> studentProfile;
                case TEACHER -> teacherProfile;
                case ADMIN -> adminProfile;
            };
            when(profileRepository.findByCode(profile)).thenReturn(Optional.of(entity));
        }
    }

    private static class TestLesson implements LessonContext {

        private final UUID ownerId;

        TestLesson(UUID ownerId) {
            this.ownerId = ownerId;
        }

        @Override
        public UUID getId() {
            return UUID.randomUUID();
        }

        @Override
        public boolean isOwnedBy(UUID userId) {
            return ownerId != null && ownerId.equals(userId);
        }
    }

    private static class TestStudent implements StudentContext {

        private final UUID teacherId;

        TestStudent(UUID teacherId) {
            this.teacherId = teacherId;
        }

        @Override
        public UUID getId() {
            return UUID.randomUUID();
        }

        @Override
        public boolean isStudentOfTeacher(UUID teacherId) {
            return this.teacherId != null && this.teacherId.equals(teacherId);
        }
    }
}

