package br.com.fiap3esa.autoescola3esa.controller;

import br.com.fiap3esa.autoescola3esa.domain.agenda.ValidacaoException;
import br.com.fiap3esa.autoescola3esa.domain.endereco.CepNaoEncontradoException;
import br.com.fiap3esa.autoescola3esa.domain.endereco.DadosEndereco;
import br.com.fiap3esa.autoescola3esa.domain.usuario.UsuarioRepository;
import br.com.fiap3esa.autoescola3esa.infra.exception.TratadorGlobalErros;
import br.com.fiap3esa.autoescola3esa.infra.externo.ServicoExternoIndisponivelException;
import br.com.fiap3esa.autoescola3esa.infra.security.SecurityConfig;
import br.com.fiap3esa.autoescola3esa.infra.security.SecurityFilter;
import br.com.fiap3esa.autoescola3esa.infra.security.TokenService;
import br.com.fiap3esa.autoescola3esa.service.ViaCepService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EnderecoController.class)
@Import({SecurityConfig.class, SecurityFilter.class, TratadorGlobalErros.class})
class EnderecoControllerTest {
    @Autowired MockMvc mockMvc;

    @MockitoBean ViaCepService service;
    @MockitoBean TokenService tokenService;
    @MockitoBean UsuarioRepository usuarioRepository;

    @Test
    @DisplayName("Expectativa: bloquear acesso sem autenticação.")
    void semAutenticacao() throws Exception {
        mockMvc.perform(get("/enderecos/01310100")).andExpect(status().is4xxClientError());
    }

    @Test
    @WithMockUser
    @DisplayName("Expectativa: 200 com o endereço retornado pela API externa.")
    void buscarCenario1() throws Exception {
        when(service.buscarPorCep("01310100")).thenReturn(
                new DadosEndereco("Avenida Paulista", null, null, "Bela Vista", "São Paulo", "SP", "01310100"));

        mockMvc.perform(get("/enderecos/01310100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.logradouro").value("Avenida Paulista"))
                .andExpect(jsonPath("$.cidade").value("São Paulo"));
    }

    @Test
    @WithMockUser
    @DisplayName("Expectativa: 404 quando o CEP não existe.")
    void buscarCenario2() throws Exception {
        when(service.buscarPorCep("99999999")).thenThrow(new CepNaoEncontradoException("CEP não encontrado: 99999999"));

        mockMvc.perform(get("/enderecos/99999999")).andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    @DisplayName("Expectativa: 400 quando o CEP é inválido.")
    void buscarCenario3() throws Exception {
        when(service.buscarPorCep("123")).thenThrow(new ValidacaoException("CEP inválido!"));

        mockMvc.perform(get("/enderecos/123")).andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    @DisplayName("Expectativa: 502 quando a API externa está indisponível.")
    void buscarCenario4() throws Exception {
        when(service.buscarPorCep("01310100"))
                .thenThrow(new ServicoExternoIndisponivelException("Serviço ViaCEP indisponível no momento.", new RuntimeException()));

        mockMvc.perform(get("/enderecos/01310100")).andExpect(status().isBadGateway());
    }
}
