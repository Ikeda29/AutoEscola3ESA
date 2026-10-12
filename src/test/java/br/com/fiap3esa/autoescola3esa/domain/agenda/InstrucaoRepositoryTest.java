package br.com.fiap3esa.autoescola3esa.domain.agenda;

import br.com.fiap3esa.autoescola3esa.domain.aluno.Aluno;
import br.com.fiap3esa.autoescola3esa.domain.endereco.Endereco;
import br.com.fiap3esa.autoescola3esa.domain.instrutor.Especialidade;
import br.com.fiap3esa.autoescola3esa.domain.instrutor.Instrutor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("h2")
class InstrucaoRepositoryTest {
    @Autowired
    InstrucaoRepository repository;

    @Autowired
    TestEntityManager em;

    private Aluno aluno;
    private Instrutor instrutor;
    private LocalDateTime segundaAs10;

    @BeforeEach
    void preparar() {
        segundaAs10 = LocalDateTime.now()
                .with(TemporalAdjusters.next(DayOfWeek.MONDAY))
                .withHour(10).withMinute(0).withSecond(0).withNano(0);

        Endereco endereco = new Endereco("Rua Teste", "1", null, "Bairro", "Cidade", "SP", "01234567");
        aluno = em.persist(new Aluno(null, "Aluno", "aluno@email.com", "(11) 90000-0000", "12345678901", endereco, true));
        instrutor = em.persist(new Instrutor(null, "Instrutor", "instrutor@email.com", "(11) 91111-1111",
                "01234567890", Especialidade.CARROS, endereco, true));
        em.persistAndFlush(new Instrucao(null, aluno, instrutor, segundaAs10));
    }

    @Test
    @DisplayName("Expectativa: instrutor com instrução no horário é considerado ocupado.")
    void existsByInstrutorIdAndDataHoraCenario1() {
        assertThat(repository.existsByInstrutorIdAndDataHora(instrutor.getId(), segundaAs10)).isTrue();
    }

    @Test
    @DisplayName("Expectativa: instrutor sem instrução em outro horário está livre.")
    void existsByInstrutorIdAndDataHoraCenario2() {
        assertThat(repository.existsByInstrutorIdAndDataHora(instrutor.getId(), segundaAs10.plusHours(1))).isFalse();
    }

    @Test
    @DisplayName("Expectativa: detectar instrução do aluno dentro do mesmo dia.")
    void existsByAlunoIdAndDataHoraBetweenCenario1() {
        boolean existe = repository.existsByAlunoIdAndDataHoraBetween(
                aluno.getId(), segundaAs10.withHour(6), segundaAs10.withHour(20));

        assertThat(existe).isTrue();
    }

    @Test
    @DisplayName("Expectativa: não detectar instrução do aluno em outro dia.")
    void existsByAlunoIdAndDataHoraBetweenCenario2() {
        LocalDateTime terca = segundaAs10.plusDays(1);

        boolean existe = repository.existsByAlunoIdAndDataHoraBetween(
                aluno.getId(), terca.withHour(6), terca.withHour(20));

        assertThat(existe).isFalse();
    }
}
