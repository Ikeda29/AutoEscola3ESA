package br.com.fiap3esa.autoescola3esa.domain.usuario;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("h2")
class UsuarioRepositoryTest {
    @Autowired
    UsuarioRepository repository;

    @Autowired
    TestEntityManager em;

    @Test
    @DisplayName("Expectativa: encontrar o usuário pelo login e expor o perfil como ROLE_.")
    void findByLoginCenario1() {
        em.persistAndFlush(new Usuario(null, "admin@email.com", "$2a$10$hashbcrypt", Perfil.ADMIN));

        UserDetails usuario = repository.findByLogin("admin@email.com");

        assertThat(usuario).isNotNull();
        assertThat(usuario.getUsername()).isEqualTo("admin@email.com");
        assertThat(usuario.getAuthorities()).extracting("authority").containsExactly("ROLE_ADMIN");
    }

    @Test
    @DisplayName("Expectativa: retornar null quando o login não existe.")
    void findByLoginCenario2() {
        assertThat(repository.findByLogin("naoexiste@email.com")).isNull();
    }
}
