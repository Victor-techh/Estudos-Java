package com.estudos.controller;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.estudos.dto.AtendimentoRequestDTO;
import com.estudos.entidade.UsuarioEntity;
import com.estudos.modelo.Estabelecimento;
import com.estudos.repositorio.UsuarioRepository;
import com.estudos.servico.AgenteVendasService;

public class AgenteControllerTest {

    public static void main(String[] args) {
        System.out.println("Iniciando testes do AgenteController...");

        // Mock repository usando HashMap em memória
        Map<Long, UsuarioEntity> idMap = new HashMap<>();
        Map<String, UsuarioEntity> emailMap = new HashMap<>();
        Map<String, UsuarioEntity> uidMap = new HashMap<>();
        long[] idSeq = {1L};

        UsuarioRepository mockRepo = (UsuarioRepository) java.lang.reflect.Proxy.newProxyInstance(
                UsuarioRepository.class.getClassLoader(),
                new Class<?>[]{UsuarioRepository.class},
                (proxy, method, mArgs) -> {
                    String name = method.getName();
                    if (name.equals("findByEmail")) {
                        String email = (String) mArgs[0];
                        return Optional.ofNullable(emailMap.get(email));
                    }
                    if (name.equals("findByUid")) {
                        String uid = (String) mArgs[0];
                        return Optional.ofNullable(uidMap.get(uid));
                    }
                    if (name.equals("findById")) {
                        Long id = (Long) mArgs[0];
                        return Optional.ofNullable(idMap.get(id));
                    }
                    if (name.equals("save")) {
                        UsuarioEntity u = (UsuarioEntity) mArgs[0];
                        if (u.getId() == null) {
                            u.setId(idSeq[0]++);
                        }
                        idMap.put(u.getId(), u);
                        if (u.getEmail() != null) emailMap.put(u.getEmail(), u);
                        if (u.getUid() != null) uidMap.put(u.getUid(), u);
                        return u;
                    }
                    return null;
                }
        );

        // AgenteVendasService mock ou real com override de chamada OpenRouter
        AgenteVendasService service = new AgenteVendasService(mockRepo) {
            @Override
            public String processarAtendimentoPorUsuario(UsuarioEntity usuario, String mensagem, java.util.List<com.estudos.dto.MensagemDTO> historico, int quantidadeMusicos) {
                return "[OK_USUARIO]: " + usuario.getNome() + " (" + usuario.getEmail() + ") - " + mensagem;
            }

            @Override
            public String processarAtendimento(Estabelecimento est, String mensagem, java.util.List<com.estudos.dto.MensagemDTO> historico, int quantidadeMusicos) {
                return "[OK_ESTABELECIMENTO]: " + est.getNome() + " - " + mensagem;
            }
        };

        AgenteController controller = new AgenteController(service, mockRepo);

        // Teste 1: Usuário autenticado via Firebase (com token contendo email e uid)
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "usuario@teste.com",
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
        Map<String, Object> details = new HashMap<>();
        details.put("uid", "firebase-uid-123");
        details.put("email", "usuario@teste.com");
        details.put("name", "Usuario Teste");
        auth.setDetails(details);

        AtendimentoRequestDTO req1 = new AtendimentoRequestDTO();
        req1.setMensagem("Olá Agente!");
        String resp1 = controller.conversar(req1, auth, null);
        System.out.println("Teste 1 (Firebase Auth novo usuario): " + resp1);
        assert resp1.contains("[OK_USUARIO]") : "Deveria identificar o usuário pelo token Firebase";
        assert resp1.contains("Usuario Teste") : "Deveria usar o nome extraído do Firebase";

        // Teste 2: Segundo chamado com mesmo usuário (já persistido no mockRepo)
        AtendimentoRequestDTO req2 = new AtendimentoRequestDTO();
        req2.setMensagem("Segunda mensagem");
        String resp2 = controller.conversar(req2, auth, null);
        System.out.println("Teste 2 (Firebase Auth usuario existente): " + resp2);
        assert resp2.contains("[OK_USUARIO]") : "Deveria reutilizar o usuário do banco";

        // Teste 3: Chamada sem auth, mas com usuarioId numérico no DTO
        AtendimentoRequestDTO req3 = new AtendimentoRequestDTO();
        req3.setUsuarioId(1L);
        req3.setMensagem("Chamada com usuarioId");
        String resp3 = controller.conversar(req3, null, null);
        System.out.println("Teste 3 (Via usuarioId): " + resp3);
        assert resp3.contains("[OK_USUARIO]") : "Deveria identificar o usuário pelo usuarioId";

        // Teste 4: Chamada sem auth, mas com uid do Firebase no DTO (setUsuarioId string)
        AtendimentoRequestDTO req4 = new AtendimentoRequestDTO();
        req4.setUsuarioId("firebase-uid-123");
        req4.setMensagem("Chamada com uid");
        String resp4 = controller.conversar(req4, null, null);
        System.out.println("Teste 4 (Via UID string): " + resp4);
        assert resp4.contains("[OK_USUARIO]") : "Deveria identificar o usuário pelo UID";

        // Teste 5: Chamada sem auth, sem usuarioId, mas com Estabelecimento
        AtendimentoRequestDTO req5 = new AtendimentoRequestDTO();
        req5.setEstabelecimento(new Estabelecimento("Bar Central", "Bar", "Rua 1", 1000.0, "Rock", true));
        req5.setMensagem("Proposta banda");
        String resp5 = controller.conversar(req5, null, null);
        System.out.println("Teste 5 (Via Estabelecimento): " + resp5);
        assert resp5.contains("[OK_ESTABELECIMENTO]") : "Deveria processar via estabelecimento";

        // Teste 6: Chamada sem nada -> deve retornar erro amigável
        AtendimentoRequestDTO req6 = new AtendimentoRequestDTO();
        req6.setMensagem("Mensagem anônima sem identificação");
        String resp6 = controller.conversar(req6, null, null);
        System.out.println("Teste 6 (Sem identificação): " + resp6);
        assert resp6.equals("[ERRO]: Usuário não identificado para iniciar o atendimento.") : "Deveria retornar erro de não identificado";

        System.out.println("TODOS OS TESTES PASSARAM COM SUCESSO!");
    }
}
