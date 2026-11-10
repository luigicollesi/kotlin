# Regras jurídicas e limitações

## Finalidade

Simulação acadêmica do **requisito objetivo temporal da primeira progressão de regime**, para condenação única por crime comum, a partir de dados informados pelo usuário.

A especificação da disciplina exige regime inicial, duração, datas, progressão e tempo restante, mas não traz fórmulas ou critérios para todos os casos. Por isso, esta versão **não pretende resolver toda a execução penal**.

## Hipóteses aceitas

- Fatos ocorridos a partir de 23/01/2020.
- Pena privativa de liberdade com apenas uma condenação.
- Regime inicial fechado ou semiaberto, sem progressão anterior.
- Crimes comuns sem enquadramento especial.
- Cumprimento contínuo, sem falta grave, detração, remição ou interrupção.
- Dados completos, consistentes e sem hipótese especial do art. 112 da LEP.

Se alguma condição não for atendida, o sistema recusa a simulação com erro HTTP 422 quando for um caso fora do escopo.

## Frações implementadas

| Crime comum | Condição | Fato anterior a 08/05/2026 | Fato a partir de 08/05/2026 |
|---|---|---:|---:|
| Sem violência/grave ameaça | Primário | 16% | 1/6 |
| Sem violência/grave ameaça | Reincidente | 20% | 20% |
| Com violência/grave ameaça | Primário | 25% | 25% |
| Com violência/grave ameaça | Reincidente | 30% | 30% |

A seleção considera a regra mais favorável para fatos anteriores a alterações legais mais gravosas no recorte. O projeto **não aborda conflitos intertemporais ou especiais fora dessa matriz**.

## Convenção de cálculo

- Ano de pena = 365 dias; mês = 30 dias.
- O primeiro dia de cumprimento entra na contagem.
- A fração do requisito é arredondada para cima, evitando antecipação por fração de dia.
- Datas de progressão e término são **estimativas cronológicas** dadas as premissas anteriores.
- Atingir requisito temporal não equivale à concessão judicial do benefício.

O aplicativo não avalia requisitos subjetivos nem substitui conferência profissional ou decisão judicial.

## Referências

- Lei de Execução Penal, art. 112: https://www.planalto.gov.br/ccivil_03/leis/l7210.htm
- Lei nº 13.964/2019: https://www.planalto.gov.br/ccivil_03/_ato2019-2022/2019/lei/l13964.htm
- Lei nº 15.402/2026: https://www2.camara.leg.br/legin/fed/lei/2026/lei-15402-8-maio-2026-799086-norma-pl.html
- CNJ, cartilha de direitos de pessoas privadas de liberdade: https://www.cnj.jus.br/wp-content/uploads/2023/07/cartilha-de-direitos-das-pessoas-privadas-e-egressas.pdf

As regras deverão ser confrontadas com casos jurídicos validados pelo escritório parceiro antes de qualquer utilização profissional.
