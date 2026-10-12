package br.com.fiap3esa.autoescola3esa.service;

import br.com.fiap3esa.autoescola3esa.domain.agenda.*;
import br.com.fiap3esa.autoescola3esa.domain.agenda.validacao.ValidadorAgendamento;
import br.com.fiap3esa.autoescola3esa.domain.aluno.Aluno;
import br.com.fiap3esa.autoescola3esa.domain.aluno.AlunoNotFoundException;
import br.com.fiap3esa.autoescola3esa.domain.aluno.AlunoRepository;
import br.com.fiap3esa.autoescola3esa.domain.endereco.Endereco;
import br.com.fiap3esa.autoescola3esa.domain.instrutor.Especialidade;
import br.com.fiap3esa.autoescola3esa.domain.instrutor.Instrutor;
import br.com.fiap3esa.autoescola3esa.domain.instrutor.InstrutorNotFoundException;
import br.com.fiap3esa.autoescola3esa.domain.instrutor.InstrutorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AgendaDeInstrucoesTest {
    @Mock InstrucaoRepository repository;
    @Mock AlunoRepository alunoRepository;
    @Mock InstrutorRepository instrutorRepository;
    @Mock ValidadorAgendamento validador;

    AgendaDeInstrucoes agenda;

    private final LocalDateTime dataHora = LocalDateTime.now().plusDays(2).withMinute(0).withSecond(0).withNano(0);
    private final Endereco endereco = new Endereco("Rua", "1", null, "Bairro", "Cidade", "SP", "01234567");

    @BeforeEach
    void preparar() {
        agenda = new AgendaDeInstrucoes(repository, alunoRepository, instrutorRepository, List.of(validador));
    }

    @Test
    @DisplayName("Expectativa: lançar AlunoNotFoundException quando o aluno não existe.")
    void agendarAlunoInexistente() {
        when(alunoRepository.existsById(1L)).thenReturn(false);
        var dados = new DadosAgendamentoInstrucao(1L, null, Especialidade.CARROS, dataHora);

        assertThatThrownBy(() -> agenda.agendar(dados)).isInstanceOf(AlunoNotFoundException.class);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Expectativa: lançar InstrutorNotFoundException quando o instrutor informado não existe.")
    void agendarInstrutorInexistente() {
        when(alunoRepository.existsById(1L)).thenReturn(true);
        when(instrutorRepository.existsById(5L)).thenReturn(false);
        var dados = new DadosAgendamentoInstrucao(1L, 5L, null, dataHora);

        assertThatThrownBy(() -> agenda.agendar(dados)).isInstanceOf(InstrutorNotFoundException.class);
    }

    @Test
    @DisplayName("Expectativa: lançar ValidacaoException quando não há instrutor disponível.")
    void agendarSemInstrutorDisponivel() {
        when(alunoRepository.existsById(1L)).thenReturn(true);
        when(instrutorRepository.escolherInstrutorAleatorioDisponivel(Especialidade.CARROS, dataHora)).thenReturn(null);
        var dados = new DadosAgendamentoInstrucao(1L, null, Especialidade.CARROS, dataHora);

        assertThatThrownBy(() -> agenda.agendar(dados))
                .isInstanceOf(ValidacaoException.class)
                .hasMessageContaining("Nenhum instrutor disponível");
    }

    @Test
    @DisplayName("Expectativa: lançar ValidacaoException quando nem instrutor nem especialidade são informados.")
    void agendarSemInstrutorNemEspecialidade() {
        when(alunoRepository.existsById(1L)).thenReturn(true);
        var dados = new DadosAgendamentoInstrucao(1L, null, null, dataHora);

        assertThatThrownBy(() -> agenda.agendar(dados)).isInstanceOf(ValidacaoException.class);
    }

    @Test
    @DisplayName("Expectativa: agendar com instrutor escolhido aleatoriamente pela especialidade.")
    void agendarComSucesso() {
        Aluno aluno = new Aluno(1L, "Maria", "maria@email.com", "(11) 90000-0000", "12345678901", endereco, true);
        Instrutor instrutor = new Instrutor(2L, "Carlos", "carlos@email.com", "(11) 91111-1111",
                "01234567890", Especialidade.CARROS, endereco, true);
        when(alunoRepository.existsById(1L)).thenReturn(true);
        when(alunoRepository.getReferenceById(1L)).thenReturn(aluno);
        when(instrutorRepository.escolherInstrutorAleatorioDisponivel(Especialidade.CARROS, dataHora)).thenReturn(instrutor);
        when(repository.save(any(Instrucao.class))).thenAnswer(inv -> inv.getArgument(0));
        var dados = new DadosAgendamentoInstrucao(1L, null, Especialidade.CARROS, dataHora);

        DadosDetalhamentoAgendamento resposta = agenda.agendar(dados);

        assertThat(resposta.nomeAluno()).isEqualTo("Maria");
        assertThat(resposta.nomeInstrutor()).isEqualTo("Carlos");
        assertThat(resposta.dataHora()).isEqualTo(dataHora);
        verify(validador).validar(dados);
    }
}
