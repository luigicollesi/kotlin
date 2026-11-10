# Calculadora Penal — Kotlin Android

Projeto acadêmico de **calculadora penal offline**, desenvolvido em Kotlin e Jetpack Compose, para a disciplina de Desenvolvimento de Aplicativos com Kotlin.

## O que faz (V2 inicial)

- Recebe duração da pena, regime, classificação do crime, reincidência e datas.
- Simula **o requisito objetivo temporal da primeira progressão** para condenação única por crime comum.
- Considera fatos desde **23/01/2020**, diferenciando as redações do art. 112 da LEP:
  - Crimes comuns sem violência/grave ameaça, primário: **16%** para fatos anteriores a 08/05/2026; **1/6** para fatos a partir dessa data.
  - Sem violência, reincidente: **20%**.
  - Com violência/grave ameaça, primário: **25%**.
  - Com violência/grave ameaça, reincidente: **30%**.
- Apresenta data estimada, dias exigidos, saldo estimado e fundamento legal.
- Trabalha totalmente **offline**, sem banco de dados, histórico ou permissão de internet.

### Limitações importantes

Este é um **simulador acadêmico**, não um cálculo processual. Para que a simulação seja executada, deve haver confirmação de que:
- existe **uma única condenação** por crime comum sem hipótese especial;
- não houve **falta grave, interrupção de cumprimento, remição, detração ou progressão anterior**;
- não se aplica nenhuma hipótese especial de progressão (incluindo crimes hediondos/equiparados, art. 112, § 3º, ou categorias especiais).

Casos fora dessas condições são recusados. O aplicativo **não avalia requisitos subjetivos**, como boa conduta e exame criminológico, nem substitui decisão judicial.

**Convenção didática de contagem:** anos = 365 dias; meses = 30 dias; dia inicial incluído; frações de dia arredondadas para cima. Essa é uma projeção simplificada, que deverá ser confrontada com cálculos judiciais/SEEU antes de qualquer utilização profissional.

### Referências

- LEP, art. 112 (texto consolidado do Senado): https://legis.senado.leg.br/norma/549086/publicacao/34619809
- Lei 13.964/2019: https://www2.camara.leg.br/legin/fed/lei/2019/lei-13964-24-dezembro-2019-789639-normaatualizada-pl.html
- Lei 15.402/2026: https://www2.camara.leg.br/legin/fed/lei/2026/lei-15402-8-maio-2026-799086-norma-pl.html
- Cartilha do CNJ sobre cálculo de percentuais: https://www.cnj.jus.br/wp-content/uploads/2023/07/cartilha-de-direitos-das-pessoas-privadas-e-egressas.pdf
- Android Compose: https://developer.android.com/develop/ui/compose

## Executar

Abra este projeto no Android Studio com **JDK 17** e **SDK Android 37**. Aguarde a sincronização Gradle e execute o módulo `app` em um dispositivo ou emulador Android (API 26+).

Executar testes unitários:

```bash
./gradlew testDebugUnitTest
```

## Código

- `CalculadoraPena.kt`: modelos e cálculo de dias.
- `RegrasProgressao.kt`: frações e base legal.
- `TelaCalculadora.kt`: formulário, calendário e resultado.
- `Tema.kt`: cores e estilo.
- `CalculadoraPenaTest.kt`: testes de cenários e exceções.

O código é deliberadamente pequeno: módulo único, sem backend, banco de dados, injeção de dependências ou camadas adicionais.
