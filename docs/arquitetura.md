# Arquitetura — Frontend Android + Backend Kotlin

## Objetivo
Separar apresentação e cálculos jurídicos sem criar sistemas de autenticação, persistência ou serviços externos.

## Componentes

| Módulo | Linguagem e biblioteca | Responsabilidade |
|---|---|---|
| `app` | Kotlin + Jetpack Compose | Formulário, calendário, animação, apresentação e mensagens |
| `server` | Kotlin/JVM + Ktor | Validações, seleção de regras, cálculos e resposta JSON |

Os cálculos são executados **somente no servidor**. O Android possui modelos de entrada e saída para o contrato, mas não contém lógica de progressão penal.

## Fluxo

1. Usuário informa os dados no formulário.
2. O Android valida campos básicos e envia JSON por HTTP.
3. O Ktor valida, chama `RegrasProgressao` e `CalculadoraPena`.
4. O servidor devolve resultado ou erro.
5. A interface apresenta o resultado animado e sua fundamentação.

## API

| Método | Rota | Resposta |
|---|---|---|
| GET | `/health` | HTTP 200, `{"status":"ok"}` |
| POST | `/api/calcular` | HTTP 200, resultado |
| POST inválido | `/api/calcular` | HTTP 400, `{"erro":"..."}` |
| POST caso fora do escopo | `/api/calcular` | HTTP 422, `{"erro":"..."}` |

### Requisição

```json
{
  "anos": 1,
  "meses": 0,
  "dias": 0,
  "regime": "FECHADO",
  "tipoCrime": "COMUM_SEM_VIOLENCIA",
  "reincidente": false,
  "dataFato": "2025-01-01",
  "inicio": "2025-02-01",
  "referencia": "2025-02-01",
  "casoSimples": true
}
```

### Resposta de sucesso

```json
{
  "regimeSeguinte": "SEMIABERTO",
  "regra": {
    "descricao": "16%",
    "fundamento": "LEP, art. 112, redação da Lei 13.964/2019, inciso I (lei mais favorável)"
  },
  "totalDias": 365,
  "requisitoDias": 59,
  "diasCumpridos": 1,
  "diasRestantes": 364,
  "diasParaProgressao": 58,
  "dataProgressao": "2025-03-31",
  "terminoTeorico": "2026-01-31"
}
```

Datas são strings ISO `AAAA-MM-DD`. Os valores enviados e recebidos são exclusivamente temporários.

## Conexão local

No Ubuntu, a API escuta em `127.0.0.1:8080`. O emulador Android acessa o host pelo endereço `10.0.2.2:8080`.

- `INTERNET` no Android é necessária para comunicar por HTTP, **mesmo sem internet pública**.
- A opção de tráfego HTTP sem TLS está no manifesto da variante `debug`, somente para desenvolvimento.
- O emulador deve executar no mesmo computador que o backend.
- Um celular físico não resolve `10.0.2.2` para o Ubuntu; precisaria de configuração própria de rede.

## Privacidade e limitações

O servidor não mantém banco de dados, sessões ou histórico. Não adicionamos logs com dados das simulações. O Android também não salva resultados ou restaura formulários após reiniciar o processo.

A solução é **local**, mas não totalmente independente: o usuário deve iniciar o servidor para calcular. Não é um serviço pronto para exposição em rede ou produção.

## Testes
- `./gradlew :server:test`: regras e respostas HTTP.
- `./gradlew :app:assembleDebug`: compilação Android.
- `curl http://127.0.0.1:8080/health`: verificação manual do servidor.
- Teste manual no emulador com backend ligado e desligado.
