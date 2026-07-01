# 📋 Relatório — Pedido de Entrada em Grupos Privados

## Visão Geral

Quando um usuário tenta entrar em um grupo **privado**, em vez de ser adicionado diretamente, é criado um **pedido de entrada** com status `PENDENTE`. A criadora (ou moderadora) do grupo pode então **aprovar** ou **rejeitar** o pedido.

Grupos **públicos** continuam com entrada direta (comportamento anterior).

---

## Novos Arquivos Criados

| Arquivo | Tipo | Descrição |
|---|---|---|
| `StatusPedidoEntrada.java` | Enum | `PENDENTE`, `APROVADO`, `REJEITADO` |
| `PedidoEntradaGrupo.java` | Entity | Tabela `pedido_entrada_grupo` no banco |
| `PedidoEntradaGrupoRepository.java` | Repository | Consultas ao banco |
| `PedidoEntradaResponseDTO.java` | DTO Response | Retorno dos endpoints |
| `ResponderPedidoRequestDTO.java` | DTO Request | Body do endpoint de resposta |

---

## Endpoints

### 1. Entrar ou solicitar entrada em um grupo

```
POST /grupo-tematico/{id}/entrar
Authorization: Bearer {token}
```

**Comportamento por tipo de grupo:**

| Grupo | Resultado |
|---|---|
| Público | Entra diretamente → `200 "Você entrou no grupo com sucesso!"` |
| Privado | Cria pedido → `200 "Grupo privado: seu pedido de entrada foi enviado..."` |

**Exemplo de resposta (grupo privado):**
```json
"Grupo privado: seu pedido de entrada foi enviado e aguarda aprovação da criadora."
```

---

### 2. Listar pedidos de entrada pendentes

```
GET /grupo-tematico/{id}/pedidos-entrada
Authorization: Bearer {token}
```

> ⚠️ Apenas **criadora** ou **moderadora** do grupo têm acesso.

**Exemplo de resposta:**
```json
[
  {
    "pedidoId": 1,
    "grupoId": 10,
    "nomeGrupo": "Mães do Norte",
    "usuarioId": "uuid-do-usuario",
    "nomeUsuario": "Maria Silva",
    "status": "PENDENTE",
    "dataPedido": "2026-06-29T10:30:00",
    "dataResposta": null
  }
]
```

---

### 3. Aprovar ou rejeitar pedido de entrada

```
PATCH /grupo-tematico/{grupoId}/pedidos-entrada/{pedidoId}
Authorization: Bearer {token}
Content-Type: application/json
```

> ⚠️ Apenas **criadora** ou **moderadora** do grupo podem responder.

**Body da requisição:**
```json
{ "aprovado": true }
```
```json
{ "aprovado": false }
```

**Exemplo de resposta:**
```json
{
  "pedidoId": 1,
  "grupoId": 10,
  "nomeGrupo": "Mães do Norte",
  "usuarioId": "uuid-do-usuario",
  "nomeUsuario": "Maria Silva",
  "status": "APROVADO",
  "dataPedido": "2026-06-29T10:30:00",
  "dataResposta": "2026-06-29T11:00:00"
}
```

---

## Fluxo Completo

```
Usuário tenta entrar no grupo
        │
        ▼
Grupo é privado?
    ├── NÃO → entra diretamente (comportamento anterior)
    └── SIM
            │
            ▼
        Já tem pedido PENDENTE?
            ├── SIM → 400 "Você já possui um pedido pendente"
            └── NÃO → cria PedidoEntradaGrupo (status=PENDENTE)
                        │
                        ▼
              Criadora/Moderadora lista pedidos
              GET /grupo-tematico/{id}/pedidos-entrada
                        │
                        ▼
              Criadora/Moderadora responde
              PATCH /grupo-tematico/{grupoId}/pedidos-entrada/{pedidoId}
              { "aprovado": true/false }
                        │
              ┌─────────┴─────────┐
           APROVADO            REJEITADO
              │                    │
   Cria ParticipanteGrupo    Status = REJEITADO
   Status = APROVADO         (usuário não entra)
   Notifica admin por e-mail
```

---

## Regras de Negócio

| Regra | Detalhe |
|---|---|
| Um pedido por grupo | Constraint UNIQUE em `(grupo_id, usuario_id)` na tabela |
| Pedido já respondido | Não pode ser respondido novamente |
| Aprovação com grupo lotado | Lança erro e **não** adiciona o participante |
| Permissão para listar/responder | Apenas `CRIADORA` ou `MODERADORA` |
| Notificação por e-mail | Disparada ao aprovar (mesmo fluxo de entrada direta em grupo público) |

---

*Relatório gerado em: 29/06/2026*

