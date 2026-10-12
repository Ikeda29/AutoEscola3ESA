package br.com.fiap3esa.autoescola3esa.domain.endereco;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Resposta da API externa ViaCEP (https://viacep.com.br).
 * Quando o CEP tem formato válido mas não existe, a API responde 200 com {"erro": true}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record DadosViaCep(
        String cep,
        String logradouro,
        String complemento,
        String bairro,
        String localidade,
        String uf,
        Boolean erro) {

    public boolean naoEncontrado() {
        return Boolean.TRUE.equals(erro);
    }
}
