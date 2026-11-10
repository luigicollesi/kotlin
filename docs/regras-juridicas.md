# Regras jurídicas e limitações

## Finalidade

Simulação acadêmica do **requisito objetivo temporal da primeira progressão de regime**, para condenação única por crime comum, a partir de dados informados pelo usuário.

A especificação da disciplina exige regime inicial, duração, datas, progressão e tempo restante, mas não traz fórmulas ou critérios para todos os casos. Por isso, esta versão **não pretende resolver toda a execução penal**.

## Hipóteses aceitas

- Fatos ocorridos a partir de 23/01/2020.
- Apenas a pena atual em execução, sem unificação (a pessoa pode ter antecedente já encerrado para fins de reincidência).
- Regime inicial fechado ou semiaberto, sem progressão anterior.
- Crimes comuns sem enquadramento especial.
- Cumprimento contínuo, sem falta grave, detração, remição ou interrupção.
- Dados completos, consistentes e sem hipótese especial do art. 112 da LEP.

Se alguma condição não for atendida, o sistema recusa a simulação com erro HTTP 422 quando for um caso fora do escopo.

## Frações implementadas

| Crime comum | Situação de reincidência | Fato anterior a 08/05/2026 | Fato desde 08/05/2026 |
|---|---|---:|---:|
| Sem violência ou grave ameaça | Primário | 16% | 1/6 |
| Sem violência ou grave ameaça | Reincidente, com ou sem violência no antecedente | 20% | 20% |
| Com violência ou grave ameaça | Primário | 25% | 25% |
| Com violência ou grave ameaça | Reincidente, antecedente sem violência/grave ameaça | **25%** | **30%** |
| Com violência ou grave ameaça | Reincidente, antecedente com violência/grave ameaça | 30% | 30% |

**Fundamentação da diferença entre 25% e 30%:** para fatos anteriores à
Lei nº 15.402/2026, a jurisprudência do STJ reconhece a aplicação mais favorável
do art. 112, III, da redação de 2019, a determinadas hipóteses de
reincidência genérica em crime comum com violência. A redação da
Lei nº 15.402/2026 estabelece expressamente 30% para o reincidente em crime
com violência ou grave ameaça; sua regra mais gravosa não é aplicada
retroativamente para fatos anteriores.

A pergunta sobre reincidência considera a existência de antecedente **reconhecido juridicamente**,
não apenas acusações ou investigações. O enquadramento deve ser confirmado com
a documentação processual; qualquer dúvida relevante torna a hipótese não suportada.
Este recorte não avalia execução unificada, concurso de crimes ou casos especiais.
O sistema **não aborda conflitos intertemporais ou especiais fora dessa matriz**.

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
- STJ, precedentes de reincidência genérica em crimes comuns com violência: https://scon.stj.jus.br/SCON/GetInteiroTeorDoAcordao?dt_publicacao=14%2F03%2F2024&num_registro=202304552915
- STJ, AgRg nos EAREsp, diferenciação por natureza dos delitos: https://processo.stj.jus.br/SCON/GetInteiroTeorDoAcordao?dt_publicacao=30%2F04%2F2024&num_registro=202300160806
- CNJ, cartilha de direitos de pessoas privadas de liberdade: https://www.cnj.jus.br/wp-content/uploads/2023/07/cartilha-de-direitos-das-pessoas-privadas-e-egressas.pdf

As regras deverão ser confrontadas com casos jurídicos validados pelo escritório parceiro antes de qualquer utilização profissional.
