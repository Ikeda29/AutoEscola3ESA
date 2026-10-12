package br.com.fiap3esa.autoescola3esa.service;

import br.com.fiap3esa.autoescola3esa.domain.endereco.DadosEndereco;
import br.com.fiap3esa.autoescola3esa.domain.endereco.Endereco;
import br.com.fiap3esa.autoescola3esa.domain.instrutor.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InstrutorServiceTest {
    @Mock
    InstrutorRepository repository;

    @InjectMocks
    InstrutorService service;

    private Instrutor instrutor() {
        Endereco endereco = new Endereco("Rua A", "10", null, "Centro", "São Paulo", "SP", "01310100");
        return new Instrutor(1L, "João", "joao@email.com", "(11) 91234-5678", "12345678901",
                Especialidade.CARROS, endereco, true);
    }

    @Test
    @DisplayName("Expectativa: cadastrar instrutor e devolver o detalhamento.")
    void cadastrarInstrutor() {
        DadosEndereco endereco = new DadosEndereco("Rua A", "10", null, "Centro", "São Paulo", "SP", "01310100");
        var dados = new DadosCadastroInstrutor("João", "joao@email.com", "(11) 91234-5678",
                "12345678901", Especialidade.CARROS, endereco);
        when(repository.save(any(Instrutor.class))).thenReturn(instrutor());

        DadosDetalhamentoInstrutor resposta = service.cadastrarInstrutor(dados);

        assertThat(resposta.id()).isEqualTo(1L);
        assertThat(resposta.nome()).isEqualTo("João");
        assertThat(resposta.ativo()).isTrue();
    }

    @Test
    @DisplayName("Expectativa: listar somente instrutores ativos, paginados.")
    void listarInstrutores() {
        var pageable = PageRequest.of(0, 10);
        when(repository.findAllByAtivoTrue(pageable)).thenReturn(new PageImpl<>(List.of(instrutor())));

        Page<DadosListagemInstrutor> pagina = service.listarInstrutores(pageable);

        assertThat(pagina.getContent()).hasSize(1);
        assertThat(pagina.getContent().get(0).nome()).isEqualTo("João");
    }

    @Test
    @DisplayName("Expectativa: lançar InstrutorNotFoundException ao detalhar ID inexistente.")
    void detalharInstrutorInexistente() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.detalharInstrutor(99L))
                .isInstanceOf(InstrutorNotFoundException.class);
    }

    @Test
    @DisplayName("Expectativa: atualizar apenas os campos informados.")
    void atualizarInstrutor() {
        Instrutor existente = instrutor();
        when(repository.findById(1L)).thenReturn(Optional.of(existente));
        when(repository.save(existente)).thenReturn(existente);
        var dados = new DadosAtualizacaoInstrutor(1L, "João Silva", null, null, null, null);

        DadosDetalhamentoInstrutor resposta = service.atualizarInstrutor(dados);

        assertThat(resposta.nome()).isEqualTo("João Silva");
        assertThat(resposta.email()).isEqualTo("joao@email.com");
    }

    @Test
    @DisplayName("Expectativa: exclusão é lógica (marca o instrutor como inativo).")
    void excluirInstrutor() {
        Instrutor existente = instrutor();
        when(repository.findById(1L)).thenReturn(Optional.of(existente));

        service.excluirInstrutor(1L);

        assertThat(existente.isAtivo()).isFalse();
        verify(repository).save(existente);
    }
}
