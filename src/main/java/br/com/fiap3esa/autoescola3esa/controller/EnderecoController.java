package br.com.fiap3esa.autoescola3esa.controller;

import br.com.fiap3esa.autoescola3esa.domain.endereco.DadosEndereco;
import br.com.fiap3esa.autoescola3esa.service.ViaCepService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/enderecos")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-key")
public class EnderecoController {
    private final ViaCepService service;

    @GetMapping("/{cep}")
    @Operation(
            summary = "Consulta endereço por CEP",
            description = "Consome a API externa ViaCEP e devolve o endereço no mesmo formato usado no cadastro de instrutores e alunos."
    )
    public ResponseEntity<DadosEndereco> buscarPorCep(@PathVariable String cep) {
        return ResponseEntity.ok(service.buscarPorCep(cep));
    }
}
