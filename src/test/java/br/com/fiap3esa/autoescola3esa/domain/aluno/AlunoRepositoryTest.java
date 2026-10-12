package br.com.fiap3esa.autoescola3esa.domain.aluno;

import br.com.fiap3esa.autoescola3esa.domain.endereco.Endereco;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("h2")
class AlunoRepositoryTest {
    @Autowired
    AlunoRepository repository;

    @Autowired
    TestEntityManager em;

    @Test
    @DisplayName("Expectativa: retornar true quando o aluno existe e está ativo.")
    void existsByIdAndAtivoTrueCenario1() {
        Aluno aluno = persistirAluno("12345678901", "aluno1@email.com", true);

        assertThat(repository.existsByIdAndAtivoTrue(aluno.getId())).isTrue();
    }

    @Test
    @DisplayName("Expectativa: retornar false quando o aluno está inativo.")
    void existsByIdAndAtivoTrueCenario2() {
        Aluno aluno = persistirAluno("10987654321", "aluno2@email.com", false);

        assertThat(repository.existsByIdAndAtivoTrue(aluno.getId())).isFalse();
    }

    @Test
    @DisplayName("Expectativa: retornar false quando o aluno não existe.")
    void existsByIdAndAtivoTrueCenario3() {
        assertThat(repository.existsByIdAndAtivoTrue(9999L)).isFalse();
    }

    @Test
    @DisplayName("Expectativa: persistir aluno com endereço embutido e recuperar pelo ID.")
    void salvarEBuscarAluno() {
        Aluno aluno = persistirAluno("11122233344", "aluno3@email.com", true);

        Aluno encontrado = repository.findById(aluno.getId()).orElseThrow();

        assertThat(encontrado.getNome()).isEqualTo("Aluno Teste");
        assertThat(encontrado.getEndereco().getCep()).isEqualTo("01234567");
    }

    private Aluno persistirAluno(String cpf, String email, boolean ativo) {
        Endereco endereco = new Endereco(
                "Rua Teste", "100", null, "Vila Teste", "São Paulo", "SP", "01234567");
        Aluno aluno = new Aluno(null, "Aluno Teste", email, "(11) 98765-4321", cpf, endereco, ativo);
        return em.persistAndFlush(aluno);
    }
}
