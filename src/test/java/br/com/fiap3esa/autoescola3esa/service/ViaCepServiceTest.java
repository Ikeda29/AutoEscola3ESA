package br.com.fiap3esa.autoescola3esa.service;

import br.com.fiap3esa.autoescola3esa.domain.agenda.ValidacaoException;
import br.com.fiap3esa.autoescola3esa.domain.endereco.CepNaoEncontradoException;
import br.com.fiap3esa.autoescola3esa.domain.endereco.DadosEndereco;
import br.com.fiap3esa.autoescola3esa.infra.externo.ServicoExternoIndisponivelException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/** Testa o consumo da API externa ViaCEP sem acessar a internet (resposta simulada). */
class ViaCepServiceTest {
    private static final String BASE = "https://viacep.test/ws";

    private MockRestServiceServer server;
    private ViaCepService service;

    @BeforeEach
    void preparar() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        service = new ViaCepService(builder, BASE);
    }

    @Test
    @DisplayName("Expectativa: converter a resposta do ViaCEP em DadosEndereco.")
    void buscarPorCepCenario1() {
        server.expect(requestTo(BASE + "/01310100/json/"))
                .andRespond(withSuccess("""
                        {"cep":"01310-100","logradouro":"Avenida Paulista","complemento":"de 612 a 1510 - lado par",
                         "bairro":"Bela Vista","localidade":"São Paulo","uf":"SP","ibge":"3550308","ddd":"11"}
                        """, MediaType.APPLICATION_JSON));

        DadosEndereco endereco = service.buscarPorCep("01310-100");

        assertThat(endereco.logradouro()).isEqualTo("Avenida Paulista");
        assertThat(endereco.bairro()).isEqualTo("Bela Vista");
        assertThat(endereco.cidade()).isEqualTo("São Paulo");
        assertThat(endereco.uf()).isEqualTo("SP");
        assertThat(endereco.cep()).isEqualTo("01310100");
        server.verify();
    }

    @Test
    @DisplayName("Expectativa: lançar CepNaoEncontradoException quando o ViaCEP responde {\"erro\": true}.")
    void buscarPorCepCenario2() {
        server.expect(requestTo(BASE + "/99999999/json/"))
                .andRespond(withSuccess("{\"erro\": true}", MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> service.buscarPorCep("99999999"))
                .isInstanceOf(CepNaoEncontradoException.class);
    }

    @Test
    @DisplayName("Expectativa: lançar ValidacaoException para CEP com formato inválido, sem chamar a API.")
    void buscarPorCepCenario3() {
        assertThatThrownBy(() -> service.buscarPorCep("123"))
                .isInstanceOf(ValidacaoException.class);
        server.verify();
    }

    @Test
    @DisplayName("Expectativa: lançar ServicoExternoIndisponivelException quando o ViaCEP falha.")
    void buscarPorCepCenario4() {
        server.expect(requestTo(BASE + "/01310100/json/"))
                .andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));

        assertThatThrownBy(() -> service.buscarPorCep("01310100"))
                .isInstanceOf(ServicoExternoIndisponivelException.class);
    }
}
