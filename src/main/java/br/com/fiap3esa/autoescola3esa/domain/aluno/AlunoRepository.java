package br.com.fiap3esa.autoescola3esa.domain.aluno;

import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlunoRepository extends JpaRepository<Aluno, Long> {
    boolean existsByIdAndAtivoTrue(Long id);
}