# Calculadora Penal (Kotlin/Android)

Projeto acadêmico para apoio a cálculos de execução penal.

## Objetivo
Aplicativo Android nativo, em Kotlin e Jetpack Compose, totalmente offline e sem armazenamento de histórico.

## Escopo atual (primeira etapa)
- Formulário temporário com duração da pena, regime e datas.
- Validação de entradas e cálculo **cronológico ilustrativo** de término e dias restantes, **sem interrupções, detração ou remição**.
- Testes unitários para o núcleo de cálculo.
- Não efetua cálculo jurídico de progressão de regime nesta etapa: regras e casos precisam ser validados por profissionais do direito.

**Atenção:** a projeção cronológica não é um cálculo de execução penal para fins processuais; não substitui a decisão judicial.

## Execução
Abra a pasta no Android Studio com JDK 17 e SDK Android 36.
Execute o módulo `app` em um emulador ou dispositivo Android (API 26+).

Build: Android Gradle Plugin 9.4.0 / Gradle 9.6.0 / Compose BOM 2026.09.00.
Para gerar o Gradle Wrapper, caso ainda não esteja presente, execute `gradle wrapper --gradle-version 9.6.0` com Gradle instalado.

## Privacidade
Não há backend, permissões de rede, banco de dados, histórico ou backup. As informações permanecem somente no estado temporário da tela.

## Próximas etapas
1. Validar com o escritório a matriz de regras e respectivas vigências.
2. Implementar cálculo de progressão com base nas regras validadas.
3. Acrescentar detração, remição e incidentes de execução conforme o escopo acordado.
