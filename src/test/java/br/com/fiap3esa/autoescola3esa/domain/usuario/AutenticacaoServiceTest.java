package br.com.fiap3esa.autoescola3esa.domain.usuario;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AutenticacaoServiceTest {
    @Mock
    UsuarioRepository repository;

    @InjectMocks
    AutenticacaoService service;

    @Test
    @DisplayName("Expectativa: carregar o usuário pelo login via repositório.")
    void loadUserByUsername() {
        Usuario usuario = new Usuario(1L, "user@email.com", "hash", Perfil.USER);
        when(repository.findByLogin("user@email.com")).thenReturn(usuario);

        UserDetails carregado = service.loadUserByUsername("user@email.com");

        assertThat(carregado.getUsername()).isEqualTo("user@email.com");
        assertThat(carregado.getAuthorities()).extracting("authority").containsExactly("ROLE_USER");
    }
}
