# Estudos-Java

## Regras comerciais configuradas

- Formações disponíveis: Solo (1 músico), Duo (2) e Trio (3).
- Cachê bruto mínimo: R$ 400 por músico para apresentação de até 3 horas (Solo R$ 400, Duo R$ 800, Trio R$ 1.200).
- Contratos acima de 3 horas são recusados até que exista uma regra de valor aprovada.
- Não foi definida regra de hora extra; a IA não deve inventar valores.
- Se o estabelecimento possui equipamento de som, não se cobra aluguel de som. Se não possui, valor e condições são negociados separadamente e registrados no contrato.
- O pagamento deve ser feito na data da apresentação ou até 7 dias depois.
- Cancelamento do contratante dentro das 48 horas anteriores: multa de 50% do cachê bruto, indicada no contrato como devida ao artista.
- Cancelamento do artista dentro das 48 horas, sem justificativa médica aprovada: registro de um ponto negativo por 3 meses e crédito de R$ 100 a aplicar como desconto ao estabelecimento do próximo show. A comprovação médica é revisada por administrador; o documento médico não é armazenado.
- Contatos privados não aparecem no catálogo. E-mail, endereço e CPF/CNPJ da contraparte só são revelados após interesse mútuo.
- Conversas/interesses sem atividade são apagados após 7 dias; desistências entram na fila de expiração. Contratos e PDFs finais são mantidos.

## Referências legais

O valor de R$ 400 por músico foi informado pelo responsável do projeto como referência da tabela sindical do SINDIMUCE. A tabela oficial não foi anexada nem validada nesta implementação. CLT e Lei Federal nº 3.857/1960 são listadas como referências legais informadas, sem afirmar que estabelecem esses valores. O PDF gerado é uma minuta com registro de aceite na plataforma, não uma assinatura digital qualificada; recomenda-se revisão jurídica antes de uso definitivo.

## Executar

Com Maven:

```bash
mvn spring-boot:run
```

Com Docker, mantenha `OPENROUTER_API_KEY` no `.env`. Configure também `APP_ADMIN_EMAILS` com os e-mails das contas autorizadas a revisar justificativas médicas:

```bash
docker compose up --build
```

A interface de autenticação está em <http://localhost:8080/login.html>.

## Fluxo de teste com curl

Use dados fictícios e e-mails diferentes para cada participante. Os cookies abaixo representam sessões autenticadas distintas.

### 1. Cadastrar artista e contratante

```bash
curl -i -X POST http://localhost:8080/api/auth/cadastro \
  -H "Content-Type: application/json" \
  -d '{"nome":"Artista Exemplo","email":"artista@example.test","senha":"SenhaFicticia123","tipoPerfil":"ARTISTA","cidade":"Fortaleza","generoMusical":"MPB","quantidadeMusicos":1,"descricao":"Projeto acústico"}'

curl -i -X POST http://localhost:8080/api/auth/cadastro \
  -H "Content-Type: application/json" \
  -d '{"nome":"Bar Exemplo","email":"bar@example.test","senha":"SenhaFicticia123","tipoPerfil":"ESTABELECIMENTO","estabelecimento":"Bar Exemplo","tipoEstabelecimento":"Bar","cidade":"Fortaleza","generoDesejado":"MPB","possuiEquipamentoSom":true}'
```

Para cadastrar um perfil existente com outra formação, `quantidadeMusicos` aceita apenas `1`, `2` ou `3`. Senhas são armazenadas com hash; a API não as devolve.

### 2. Abrir sessões

```bash
curl -i -c artista.cookies -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"artista@example.test","senha":"SenhaFicticia123"}'

curl -i -c bar.cookies -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"bar@example.test","senha":"SenhaFicticia123"}'
```

Guarde os IDs retornados pela consulta do catálogo:

```bash
curl -sS http://localhost:8080/api/catalogo/artistas
curl -sS http://localhost:8080/api/catalogo/estabelecimentos
```

Os catálogos mostram dados profissionais e de localização, mas não e-mail, endereço completo ou CPF/CNPJ.

### 3. Demonstrar interesse dos dois lados

Substitua `ID_DO_BAR` pelo ID mostrado no catálogo. O artista inicia o interesse:

```bash
curl -i -b artista.cookies -X POST http://localhost:8080/api/interesses \
  -H "Content-Type: application/json" \
  -d '{"perfilId":ID_DO_BAR}'
```

Use o ID de interesse retornado para o contratante aceitar:

```bash
curl -i -b bar.cookies -X POST http://localhost:8080/api/interesses/ID_DO_INTERESSE/aceite
```

Após o aceite mútuo, ambos podem consultar `/api/interesses`; os contatos são então revelados às duas partes.

### 4. Conversar com a curadoria e persistir o histórico

```bash
curl -i -b artista.cookies -X POST http://localhost:8080/api/agente/chat \
  -H "Content-Type: application/json" \
  -d '{"interesseId":ID_DO_INTERESSE,"mensagem":"Quero propor uma apresentação Solo de MPB por R$ 400, durante 3 horas. O local possui som próprio?","quantidadeMusicos":1}'
```

O histórico fica vinculado ao interesse e é compartilhado somente entre os participantes depois da confirmação mútua. Sem `interesseId`, a conversa não é persistida para compartilhamento.
Quando a conversa estiver vinculada a um interesse mútuo, o backend usa a formação do perfil do artista, não a quantidade enviada no corpo da requisição.

### 5. Preencher identificação necessária para contrato

Faça o pedido com a sessão de cada participante. São dados privados e necessários para a minuta:

```bash
curl -i -b artista.cookies -X PUT http://localhost:8080/api/auth/dados-contrato \
  -H "Content-Type: application/json" \
  -d '{"nomeLegal":"Nome Civil do Artista","cpfCnpj":"000.000.000-00","endereco":"Endereço fictício, Fortaleza"}'

curl -i -b bar.cookies -X PUT http://localhost:8080/api/auth/dados-contrato \
  -H "Content-Type: application/json" \
  -d '{"nomeLegal":"Bar Exemplo LTDA","cpfCnpj":"00.000.000/0001-00","endereco":"Endereço fictício, Fortaleza"}'
```

### 6. Criar e aceitar contrato, baixar PDF

O horário deve ser futuro; o pagamento deve ocorrer até 7 dias após a apresentação. Informe a formação do artista e um cachê bruto igual ou superior ao mínimo. Se não houver som próprio, descreva a negociação separada no campo `condicoesSom`.

```bash
curl -i -b bar.cookies -X POST http://localhost:8080/api/contratos \
  -H "Content-Type: application/json" \
  -d '{"interesseId":ID_DO_INTERESSE,"dataHoraApresentacao":"2030-12-20T20:00:00","duracaoHoras":3,"quantidadeMusicos":1,"cacheBruto":400,"dataPagamento":"2030-12-22","valorSom":0,"condicoesSom":"O estabelecimento possui som próprio."}'
```

Anote o ID do contrato retornado e peça o aceite individual de cada parte:

```bash
curl -i -b bar.cookies -X POST http://localhost:8080/api/contratos/ID_DO_CONTRATO/aceite
curl -i -b artista.cookies -X POST http://localhost:8080/api/contratos/ID_DO_CONTRATO/aceite
curl -i -b artista.cookies -o contrato.pdf http://localhost:8080/api/contratos/ID_DO_CONTRATO/pdf
```

Um contrato abaixo do mínimo, com mais de 3 horas ou pagamento fora do prazo é recusado. O PDF fica armazenado na plataforma e só pode ser baixado pelas partes.

### 7. Cancelamentos e credibilidade

Uma parte autenticada registra cancelamento em `POST /api/contratos/ID_DO_CONTRATO/cancelamento`. Para o artista, informe `{"possuiAtestadoMedico":true}` apenas quando solicitar revisão da justificativa; o atestado não é enviado nem armazenado. A revisão fica disponível para as contas listadas em `APP_ADMIN_EMAILS`:

```bash
curl -i -b admin.cookies http://localhost:8080/api/contratos/admin/cancelamentos-pendentes
curl -i -b admin.cookies -X POST http://localhost:8080/api/contratos/admin/cancelamentos/ID_DO_CANCELAMENTO/revisao \
  -H "Content-Type: application/json" \
  -d '{"aprovada":true}'
```

O sistema registra a multa/crédito no contrato, mas não cobra nem transfere dinheiro. Artistas consultam seus pontos negativos ativos em `GET /api/contratos/credibilidade`.

## Testes automatizados

```bash
mvn test
```
