package com.langia.backend.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.langia.backend.dto.MessageResponse;
import com.langia.backend.dto.SessionData;
import com.langia.backend.dto.trail.GenerateTrailRequestDTO;
import com.langia.backend.dto.trail.LessonDTO;
import com.langia.backend.dto.trail.ModuleDTO;
import com.langia.backend.dto.trail.RefreshTrailRequestDTO;
import com.langia.backend.dto.trail.TrailDTO;
import com.langia.backend.dto.trail.TrailProgressDTO;
import com.langia.backend.dto.trail.TrailSummaryDTO;
import com.langia.backend.dto.trail.UpdateLessonProgressDTO;
import com.langia.backend.service.TrailService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller REST para gerenciamento de trilhas de aprendizado.
 */
@RestController
@RequestMapping("/api/trails")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Trilhas", description = "Endpoints para gerenciamento de trilhas de aprendizado")
public class TrailController {

    private final TrailService trailService;

    // ========== TRILHAS ==========

    @GetMapping
    @Operation(summary = "Buscar trilha por idioma", description = "Busca trilha ativa do estudante para um idioma. Se não existir, inicia geração on-demand.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Trilha encontrada ou criada"),
            @ApiResponse(responseCode = "400", description = "Código de idioma inválido"),
            @ApiResponse(responseCode = "401", description = "Não autenticado")
    })
    public ResponseEntity<TrailDTO> getTrailByLanguage(
            @Parameter(description = "Código do idioma (ex: en, es, fr)") @RequestParam String lang,
            @AuthenticationPrincipal SessionData session) {

        UUID studentId = session.getUserId();
        log.info("GET /api/trails?lang={} - Estudante: {}", lang, studentId);

        TrailDTO trail = trailService.getOrCreateTrail(studentId, lang);
        return ResponseEntity.ok(trail);
    }

    @GetMapping("/active")
    @Operation(summary = "Listar trilhas ativas", description = "Lista todas as trilhas ativas do estudante autenticado")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de trilhas ativas"),
            @ApiResponse(responseCode = "401", description = "Não autenticado")
    })
    public ResponseEntity<List<TrailSummaryDTO>> getActiveTrails(
            @AuthenticationPrincipal SessionData session) {

        UUID studentId = session.getUserId();
        log.info("GET /api/trails/active - Estudante: {}", studentId);

        List<TrailSummaryDTO> trails = trailService.getActiveTrails(studentId);
        return ResponseEntity.ok(trails);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar trilha por ID", description = "Retorna detalhes completos de uma trilha")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Trilha encontrada"),
            @ApiResponse(responseCode = "404", description = "Trilha não encontrada")
    })
    public ResponseEntity<TrailDTO> getTrailById(
            @Parameter(description = "ID da trilha") @PathVariable UUID id) {

        log.info("GET /api/trails/{}", id);
        TrailDTO trail = trailService.getTrailById(id);
        return ResponseEntity.ok(trail);
    }

    @PostMapping("/generate")
    @Operation(summary = "Gerar nova trilha", description = "Força a geração de uma nova trilha para o idioma especificado")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Trilha gerada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou limite de trilhas atingido"),
            @ApiResponse(responseCode = "401", description = "Não autenticado")
    })
    public ResponseEntity<TrailDTO> generateTrail(
            @Valid @RequestBody GenerateTrailRequestDTO request,
            @AuthenticationPrincipal SessionData session) {

        UUID studentId = session.getUserId();
        log.info("POST /api/trails/generate - Estudante: {}, Idioma: {}", studentId, request.getLanguageCode());

        TrailDTO trail = trailService.generateTrail(studentId, request);
        return ResponseEntity.ok(trail);
    }

    @PostMapping("/{id}/refresh")
    @Operation(summary = "Regenerar trilha", description = "Arquiva a trilha atual e gera uma nova com as alterações solicitadas")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Trilha regenerada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Trilha não encontrada"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    public ResponseEntity<TrailDTO> refreshTrail(
            @Parameter(description = "ID da trilha") @PathVariable UUID id,
            @Valid @RequestBody RefreshTrailRequestDTO request) {

        log.info("POST /api/trails/{}/refresh - Motivo: {}", id, request.getReason());

        TrailDTO trail = trailService.refreshTrail(id, request);
        return ResponseEntity.ok(trail);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Arquivar trilha", description = "Arquiva uma trilha (soft delete)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Trilha arquivada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Trilha não encontrada")
    })
    public ResponseEntity<MessageResponse> archiveTrail(
            @Parameter(description = "ID da trilha") @PathVariable UUID id) {

        log.info("DELETE /api/trails/{}", id);

        trailService.archiveTrail(id);
        return ResponseEntity.ok(new MessageResponse("Trilha arquivada com sucesso"));
    }

    // ========== MÓDULOS ==========

    @GetMapping("/{trailId}/modules")
    @Operation(summary = "Listar módulos da trilha", description = "Lista todos os módulos de uma trilha com suas lições")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de módulos"),
            @ApiResponse(responseCode = "404", description = "Trilha não encontrada")
    })
    public ResponseEntity<List<ModuleDTO>> getModules(
            @Parameter(description = "ID da trilha") @PathVariable UUID trailId) {

        log.info("GET /api/trails/{}/modules", trailId);

        List<ModuleDTO> modules = trailService.getModulesByTrailId(trailId);
        return ResponseEntity.ok(modules);
    }

    @GetMapping("/{trailId}/modules/{moduleId}")
    @Operation(summary = "Buscar módulo por ID", description = "Retorna detalhes de um módulo específico")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Módulo encontrado"),
            @ApiResponse(responseCode = "404", description = "Módulo não encontrado")
    })
    public ResponseEntity<ModuleDTO> getModuleById(
            @Parameter(description = "ID da trilha") @PathVariable UUID trailId,
            @Parameter(description = "ID do módulo") @PathVariable UUID moduleId) {

        log.info("GET /api/trails/{}/modules/{}", trailId, moduleId);

        ModuleDTO module = trailService.getModuleById(moduleId);
        return ResponseEntity.ok(module);
    }

    // ========== LIÇÕES ==========

    @GetMapping("/lessons/{lessonId}")
    @Operation(summary = "Buscar lição por ID", description = "Retorna detalhes de uma lição específica")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lição encontrada"),
            @ApiResponse(responseCode = "404", description = "Lição não encontrada")
    })
    public ResponseEntity<LessonDTO> getLessonById(
            @Parameter(description = "ID da lição") @PathVariable UUID lessonId) {

        log.info("GET /api/trails/lessons/{}", lessonId);

        LessonDTO lesson = trailService.getLessonById(lessonId);
        return ResponseEntity.ok(lesson);
    }

    @GetMapping("/{trailId}/next-lesson")
    @Operation(summary = "Buscar próxima lição", description = "Retorna a próxima lição não completada da trilha")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Próxima lição encontrada"),
            @ApiResponse(responseCode = "204", description = "Todas as lições foram completadas"),
            @ApiResponse(responseCode = "404", description = "Trilha não encontrada")
    })
    public ResponseEntity<LessonDTO> getNextLesson(
            @Parameter(description = "ID da trilha") @PathVariable UUID trailId) {

        log.info("GET /api/trails/{}/next-lesson", trailId);

        return trailService.getNextLesson(trailId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @PatchMapping("/lessons/{lessonId}/progress")
    @Operation(summary = "Atualizar progresso da lição", description = "Atualiza score, tempo gasto e status de conclusão de uma lição")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Progresso atualizado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Lição não encontrada"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    public ResponseEntity<LessonDTO> updateLessonProgress(
            @Parameter(description = "ID da lição") @PathVariable UUID lessonId,
            @Valid @RequestBody UpdateLessonProgressDTO request) {

        log.info("PATCH /api/trails/lessons/{}/progress - Completed: {}", lessonId, request.getCompleted());

        LessonDTO lesson = trailService.updateLessonProgress(lessonId, request);
        return ResponseEntity.ok(lesson);
    }

    // ========== PROGRESSO ==========

    @GetMapping("/{id}/progress")
    @Operation(summary = "Buscar progresso da trilha", description = "Retorna o progresso consolidado de uma trilha")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Progresso encontrado"),
            @ApiResponse(responseCode = "404", description = "Trilha não encontrada")
    })
    public ResponseEntity<TrailProgressDTO> getProgress(
            @Parameter(description = "ID da trilha") @PathVariable UUID id) {

        log.info("GET /api/trails/{}/progress", id);

        TrailProgressDTO progress = trailService.getProgress(id);
        return ResponseEntity.ok(progress);
    }

}
