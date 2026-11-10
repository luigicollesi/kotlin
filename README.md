# Calculadora Penal (Kotlin/Android)

Projeto acadêmico para apoio a cálculos de execução penal.

## Objetivo
Aplicativo Android nativo, feito com Kotlin e Jetpack Compose, totalmente offline e sem armazenamento de histórico.

## Estado atual — primeira etapa
- Formulário temporário com duração da pena, regime e datas.
- Validação de entradas e **projeção cronológica ilustrativa** de término e dias restantes, sem interrupções, detração ou remição.
- Testes unitários do cálculo.
- **Não implementa progressão de regime** nesta etapa: as regras jurídicas precisam ser validadas por profissionais do Direito.

**Atenção:** a projeção cronológica não é um cálculo de execução penal para fins processuais; não substitui decisão judicial.

## Como executar
1. Abra o projeto no Android Studio (JDK 17 e SDK Android 36).
2. Sincronize o Gradle e execute o módulo `app` no emulador/dispositivo Android (API 26+).
3. Para executar testes, use `./gradlew testDebugUnitTest` (Linux/macOS) ou `gradlew.bat testDebugUnitTest` (Windows).

Versões: Android Gradle Plugin 9.4.0, Gradle 9.6.0, Compose BOM 2026.09.00 e Kotlin Compose Compiler 2.4.20.

## Privacidade
Não há backend, permissões de rede, banco de dados, histórico ou backup. Informações permanecem somente no estado temporário da tela.

## Próximas etapas
1. Validar com o escritório a matriz de regras e respectivas vigências.
2. Implementar progressão com base nas regras validadas.
3. Acrescentar detração, remição e incidentes de execução conforme o escopo acordado.
