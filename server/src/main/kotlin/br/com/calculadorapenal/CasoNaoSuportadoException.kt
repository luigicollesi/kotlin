package br.com.calculadorapenal

/** Indica um cenário que a versão acadêmica ainda não calcula. */
class CasoNaoSuportadoException(mensagem: String) : IllegalArgumentException(mensagem)
