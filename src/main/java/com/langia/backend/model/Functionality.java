package com.langia.backend.model;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.CascadeType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Converter;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "functionalities", uniqueConstraints = {
        @UniqueConstraint(name = "uk_functionalities_code", columnNames = "code")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class Functionality {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @NotBlank
    @Size(max = 100)
    @Column(name = "code", nullable = false, unique = true, length = 100)
    private String code;

    @NotBlank
    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @NotNull
    @Convert(converter = Module.ModuleConverter.class)
    @Column(name = "module", nullable = false, length = 100)
    private Module module;

    @Builder.Default
    @Column(name = "active", nullable = false)
    private boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "functionality", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<ProfileFunctionality> profileLinks = new HashSet<>();

    public void addProfileLink(ProfileFunctionality link) {
        profileLinks.add(link);
        link.setFunctionality(this);
    }

    public void removeProfileLink(ProfileFunctionality link) {
        profileLinks.remove(link);
        link.setFunctionality(null);
    }

    public enum Module {
        STUDENTS("Alunos"),
        CLASSES("Aulas"),
        SELF_PROFILE("Perfil Próprio"),
        SYSTEM("Sistema");

        private static final Map<String, Module> LOOKUP = new ConcurrentHashMap<>();

        static {
            for (Module module : values()) {
                LOOKUP.put(module.databaseValue, module);
            }
        }

        private final String databaseValue;

        Module(String databaseValue) {
            this.databaseValue = databaseValue;
        }

        public String getDatabaseValue() {
            return databaseValue;
        }

        public static Module fromDatabaseValue(String value) {
            if (value == null) {
                return null;
            }
            Module module = LOOKUP.get(value);
            if (module == null) {
                throw new IllegalArgumentException("Unknown module value: " + value);
            }
            return module;
        }

        @Converter(autoApply = false)
        public static class ModuleConverter implements AttributeConverter<Module, String> {
            @Override
            public String convertToDatabaseColumn(Module attribute) {
                return attribute == null ? null : attribute.getDatabaseValue();
            }

            @Override
            public Module convertToEntityAttribute(String dbData) {
                return Module.fromDatabaseValue(dbData);
            }
        }
    }
}


