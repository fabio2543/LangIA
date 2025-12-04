-- V024: Adiciona coluna secondary_objective para permitir seleção de até 2 objetivos de aprendizado
ALTER TABLE student_learning_preferences
ADD COLUMN secondary_objective VARCHAR(50);
