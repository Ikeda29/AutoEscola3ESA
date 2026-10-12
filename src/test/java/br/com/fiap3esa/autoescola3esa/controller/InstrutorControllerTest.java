package br.com.fiap3esa.autoescola3esa.controller;

import br.com.fiap3esa.autoescola3esa.domain.endereco.DadosEndereco;
import br.com.fiap3esa.autoescola3esa.domain.instrutor.*;
import br.com.fiap3esa.autoescola3esa.domain.usuario.UsuarioRepository;
import br.com.fiap3esa.autoescola3esa.infra.exception.TratadorGlobalErros;
import br.com.fiap3esa.autoescola3esa.infra.security.SecurityConfig;
import br.com.fiap3esa.autoescola3esa.infra.security.SecurityFilter;
import br.com.fiap3esa.autoescola3esa.infra.security.TokenService;
import br.com.fiap3esa.autoescola3esa.service.InstrutorService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InstrutorController.class)
@Import({SecurityConfig.class, SecurityFilter.class, TratadorGlobalErros.class})
class InstrutorControllerTest {
    @Autowired MockMvc mockMvc;

    @MockitoBean InstrutorService service;
    @MockitoBean TokenService tokenService;
    @MockitoBean UsuarioRepository usuarioRepository;

    private static final String JSON_VALIDO = """
            {"nome":"João","email":"joao@email.com","telefone":"(11) 91234-5678","cnh":"12345678901",
             "especialidade":"CARROS",
             "endereco":{"logradouro":"Rua A","numero":"10","bairro":"Centro","cidade":"São Paulo","uf":"SP","cep":"01310100"}}
            """;

    private DadosDetalhamentoInstrutor detalhamento() {
        return new DadosDetalhamentoInstrutor(1L, "João", "joao@email.com", "(11) 91234-5678", "12345678901",
                Especialidade.CARROS,
                new DadosEndereco("Rua A", "10", null, "Centro", "São Paulo", "SP", "01310100"), true);
    }

    @Test
    @DisplayName("Expectativa: 401/403 quando não há usuário autenticado.")
    void semAutenticacao() throws Exception {
        mockMvc.perform(get("/instrutores")).andExpect(status().is4xxClientError());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Expectativa: 400 ao cadastrar instrutor com dados inválidos.")
    void cadastrarCenario1() throws Exception {
        mockMvc.perform(post("/instrutores").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Expectativa: 201 com Location ao cadastrar instrutor válido.")
    void cadastrarCenario2() throws Exception {
        when(service.cadastrarInstrutor(any())).thenReturn(detalhamento());

        mockMvc.perform(post("/instrutores").contentType(MediaType.APPLICATION_JSON).content(JSON_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("João"));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Expectativa: 403 quando perfil USER tenta cadastrar instrutor.")
    void cadastrarCenario3() throws Exception {
        mockMvc.perform(post("/instrutores").contentType(MediaType.APPLICATION_JSON).content(JSON_VALIDO))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Expectativa: 200 e lista paginada para perfil USER.")
    void listar() throws Exception {
        var item = new DadosListagemInstrutor(1L, "João", "joao@email.com", Especialidade.CARROS);
        when(service.listarInstrutores(any())).thenReturn(new PageImpl<>(List.of(item)));

        mockMvc.perform(get("/instrutores"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].nome").value("João"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Expectativa: 404 ao detalhar instrutor inexistente.")
    void detalharInexistente() throws Exception {
        when(service.detalharInstrutor(99L)).thenThrow(new InstrutorNotFoundException("ID do instrutor informado não existe!"));

        mockMvc.perform(get("/instrutores/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("ID do instrutor informado não existe!"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Expectativa: 204 ao excluir instrutor.")
    void excluir() throws Exception {
        mockMvc.perform(delete("/instrutores/1")).andExpect(status().isNoContent());
    }
}
