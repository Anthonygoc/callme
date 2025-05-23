package com.example.login_auth_api.controller;

import com.example.login_auth_api.domain.user.ChamadoInterno;
import com.example.login_auth_api.domain.user.Role;
import com.example.login_auth_api.dto.ChamadoInternoDto;
import com.example.login_auth_api.dto.FeedDto;
import com.example.login_auth_api.dto.FeedItemDto;
import com.example.login_auth_api.repositories.ChamadoInternoRepository;
import com.example.login_auth_api.repositories.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@RestController
public class ChamadoInternoController {

    private final ChamadoInternoRepository chamadoRepository;

    private final UserRepository userRepository;

    public ChamadoInternoController(ChamadoInternoRepository chamadoRepository, UserRepository userRepository) {

        this.chamadoRepository = chamadoRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/feed")
    public ResponseEntity<FeedDto> feed(@RequestParam(value = "page", defaultValue = "0") int page,
                                        @RequestParam(value = "pageSize", defaultValue = "10") int pageSize){

        var chamados = chamadoRepository.findAll(
                        PageRequest.of(page, pageSize, Sort.Direction.DESC, "creationTimestamp"))
                .map(chamado ->
                        new FeedItemDto(chamado.getChamadoID(),
                                chamado.getContent(),
                                chamado.getUser().getUsername()));

        return ResponseEntity.ok(new FeedDto(chamados.getContent(), page, pageSize,
                chamados.getTotalPages(),
                chamados.getTotalElements()));
    }

    @PostMapping("/chamados")
    public ResponseEntity<Void>Createchamado(@RequestBody ChamadoInternoDto Dto,
                                             JwtAuthenticationToken token) {

        var user = userRepository.findById(UUID.fromString(token.getName()));

        var chamado = new ChamadoInterno();
        chamado.setUser(user.get());
        chamado.setContent(Dto.content());

        chamadoRepository.save(chamado);
        return ResponseEntity.ok().build();
    }
    @DeleteMapping("/chamados/{id}")
    public ResponseEntity<Void> deletechamado(@PathVariable ("id") Long chamadoId,
                                              JwtAuthenticationToken token){
        var user = userRepository.findById(UUID.fromString(token.getName()));
        var chamado = chamadoRepository.findById(chamadoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        var isAdmin = user.get().getRoles().stream().anyMatch(role -> role.getName().equalsIgnoreCase(Role.Values.ADMIN.name()));

        if (isAdmin || chamado.getUser().getUserid().equals(UUID.fromString(token.getName()))) {
            chamadoRepository.deleteById(chamadoId);

        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }


        return ResponseEntity.ok().build();

    }

}