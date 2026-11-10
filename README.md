# Calculadora Penal — Kotlin (V3)

Projeto acadêmico de apoio à execução penal, desenvolvido em Kotlin.

- **app/**: interface Android com Jetpack Compose.
- **server/**: API REST local em Kotlin/JVM com Ktor, responsável por todas as simulações.
- **Sem banco de dados, autenticação ou histórico.** Cada requisição é processada em memória.

> Simulador acadêmico do requisito objetivo de primeira progressão em hipóteses restritas. Não substitui análise jurídica ou decisão judicial.

## Requisitos

- Ubuntu com JDK 17 e Android SDK 37 instalados.
- Emulador Android configurado (ex.: `Small_Phone`).
- O projeto usa Gradle Wrapper 9.6, Android Gradle Plugin 9.4 e Ktor 3.6.1.

## Executar pelo VS Code

Abra a pasta do repositório com `code .`. Em três terminais:

**Terminal 1 — Backend**

```bash
./gradlew :server:run
```

O servidor escuta em `127.0.0.1:8080` por padrão (variável de ambiente `PORT` para outra porta).

Confirme em outro terminal:

```bash
curl http://127.0.0.1:8080/health
```

Resposta: `{"status":"ok"}`.

**Terminal 2 — Emulador**

```bash
~/Android/Sdk/emulator/emulator -avd Small_Phone -gpu swiftshader -no-snapshot-load
```

Se o emulador já estiver aberto, não execute outra instância com o mesmo nome.

**Terminal 3 — Android**

```bash
./gradlew :app:installDebug
~/Android/Sdk/platform-tools/adb shell am start -n br.com.calculadorapenal/.MainActivity
```

A interface envia os dados para `http://10.0.2.2:8080/api/calcular`, alias do `127.0.0.1` do computador no emulador. Não é necessário acesso à internet pública, mas o servidor precisa estar ligado.

Também há tarefas prontas em `.vscode/tasks.json`, acessíveis pelo menu **Terminal > Run Task**.

## Testes e build

```bash
./gradlew :server:test
./gradlew :app:assembleDebug
```

Para testar a API:

```bash
curl -X POST http://127.0.0.1:8080/api/calcular \
  -H "Content-Type: application/json" \
  -d '{"anos":1,"meses":0,"dias":0,"regime":"FECHADO","tipoCrime":"COMUM_SEM_VIOLENCIA","reincidente":false,"dataFato":"2025-01-01","inicio":"2025-02-01","referencia":"2025-02-01","casoSimples":true}'
```

## Estrutura do projeto

```text
app/src/main/java/br/com/calculadorapenal/
├── MainActivity.kt          # Inicializa o Android
├── TelaCalculadora.kt       # Formulário e resultados Compose
├── Tema.kt                  # Estilo visual
├── ModelosApi.kt            # Campos e resultados do frontend
└── ApiClient.kt             # Requisição HTTP para o backend

server/src/main/kotlin/br/com/calculadorapenal/
├── Servidor.kt              # Ktor, rotas e tratamento de erros
├── Modelos.kt               # Contrato JSON
├── CalculadoraPena.kt       # Cálculos temporais
├── RegrasProgressao.kt      # Seleção de frações legais
└── CasoNaoSuportadoException.kt

server/src/test/kotlin/br/com/calculadorapenal/
├── CalculadoraPenaTest.kt   # Testes do cálculo e das regras
└── ServidorTest.kt          # Testes da API HTTP

docs/
├── arquitetura.md
└── regras-juridicas.md
```

## Limites do projeto

Condenação única por crime comum; fatos desde 23/01/2020; primeira progressão; sem detração, remição, faltas graves, interrupções ou hipóteses especiais. Requisitos subjetivos não são avaliados. As regras legais e a convenção de contagem são descritas em [docs/regras-juridicas.md](docs/regras-juridicas.md).

A comunicação HTTP sem TLS é permitida **somente na variante Android debug**. O servidor escuta apenas no loopback do computador, e a URL do frontend foi projetada para o emulador Android.

Veja [docs/arquitetura.md](docs/arquitetura.md) para o fluxo e os endpoints da API.
