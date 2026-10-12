package br.com.fiap3esa.autoescola3esa.service;

import br.com.fiap3esa.autoescola3esa.domain.agenda.ValidacaoException;
import br.com.fiap3esa.autoescola3esa.domain.endereco.CepNaoEncontradoException;
import br.com.fiap3esa.autoescola3esa.domain.endereco.DadosEndereco;
import br.com.fiap3esa.autoescola3esa.domain.endereco.DadosViaCep;
import br.com.fiap3esa.autoescola3esa.infra.externo.ServicoExternoIndisponivelException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Consome a API externa ViaCEP para consultar endereços a partir do CEP.
 */
@Service
public class ViaCepService {
    private final RestClient restClient;

    @Autowired
    public ViaCepService(@Value("${viacep.url:https://viacep.com.br/ws}") String baseUrl) {
        this(RestClient.builder(), baseUrl);
    }

    // Usado nos testes, para injetar um RestClient.Builder com servidor simulado.
    ViaCepService(RestClient.Builder builder, String baseUrl) {
        this.restClient = builder.baseUrl(baseUrl).build();
    }

    public DadosEndereco buscarPorCep(String cep) {
        String cepLimpo = cep == null ? "" : cep.replaceAll("\\D", "");
        if (cepLimpo.length() != 8) {
            throw new ValidacaoException("CEP inválido! Informe 8 dígitos (ex: 01310100 ou 01310-100).");
        }

        DadosViaCep resposta;
        try {
            resposta = restClient
                    .get()
                    .uri("/{cep}/json/", cepLimpo)
                    .retrieve()
                    .body(DadosViaCep.class);
        } catch (RestClientException ex) {
            throw new ServicoExternoIndisponivelException("Serviço ViaCEP indisponível no momento.", ex);
        }

        if (resposta == null || resposta.naoEncontrado()) {
            throw new CepNaoEncontradoException("CEP não encontrado: " + cepLimpo);
        }

        return new DadosEndereco(
                resposta.logradouro(),
                null,
                resposta.complemento(),
                resposta.bairro(),
                resposta.localidade(),
                resposta.uf(),
                cepLimpo
        );
    }
}
