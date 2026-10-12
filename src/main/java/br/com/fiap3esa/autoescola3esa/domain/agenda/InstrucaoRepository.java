package br.com.fiap3esa.autoescola3esa.domain.agenda;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface InstrucaoRepository extends JpaRepository<Instrucao, Long> {
    boolean existsByInstrutorIdAndDataHora(Long idInstrutor, LocalDateTime dataHora);

    boolean existsByAlunoIdAndDataHoraBetween(Long idAluno, LocalDateTime inicio, LocalDateTime fim);
}