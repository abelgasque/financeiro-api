CREATE TABLE usuario (
	id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
	nome VARCHAR(50) NOT NULL,
	email VARCHAR(50) NOT NULL,
	senha VARCHAR(150) NOT NULL,
    situacao VARCHAR(15) NOT NULL
);

CREATE TABLE permissao (
	id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
	descricao VARCHAR(50) NOT NULL
);

CREATE TABLE usuario_permissao (
	id_usuario BIGINT NOT NULL,
	id_permissao BIGINT NOT NULL,
	CONSTRAINT fk_usuario FOREIGN KEY (id_usuario) REFERENCES usuario(id),
	CONSTRAINT fk_permissao FOREIGN KEY (id_permissao) REFERENCES permissao(id)
);

CREATE TABLE pessoa(
	id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
	nome VARCHAR(50) NOT NULL,
	cpf VARCHAR(20) NOT NULL,
	logradouro VARCHAR(30),
	numero VARCHAR(30),
	complemento VARCHAR(30),
	bairro VARCHAR(30),
	cep VARCHAR(30),
	cidade VARCHAR(30),
	uf VARCHAR(30),
	situacao VARCHAR(15) NOT NULL,
    id_usuario BIGINT NOT NULL,
	CONSTRAINT fk_pessoa_usuario FOREIGN KEY (id_usuario) REFERENCES usuario(id)
);

CREATE TABLE contato(
	id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
	nome VARCHAR(50) NOT NULL,
	email VARCHAR(100) NOT NULL,
	telefone VARCHAR(20) NOT NULL,
	id_pessoa BIGINT NOT NULL,
	CONSTRAINT fk_pessoa_contato FOREIGN KEY (id_pessoa) REFERENCES pessoa(id)
);

CREATE TABLE categoria(
	id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    nome VARCHAR(40) NOT NULL
);

CREATE TABLE lancamento(
	id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
	descricao VARCHAR(50) NOT NULL,
	data_vencimento DATE NOT NULL,
	data_pagamento DATE,
	valor DECIMAL(10,2) NOT NULL,
	observacao VARCHAR(100),
	tipo VARCHAR(20) NOT NULL,
	anexo VARCHAR(200),
    url_anexo VARCHAR(200),
	id_categoria BIGINT NOT NULL,
	id_pessoa BIGINT NOT NULL,
	CONSTRAINT fk_categoria_lancamento FOREIGN KEY (id_categoria) REFERENCES categoria(id),
	CONSTRAINT fk_pessoa_lancamento FOREIGN KEY (id_pessoa) REFERENCES pessoa(id)
);

INSERT INTO categoria(nome) VALUES('Lazer');
INSERT INTO categoria(nome) VALUES('Alimentação');
INSERT INTO categoria(nome) VALUES('Supermercado');
INSERT INTO categoria(nome) VALUES('Farmácia');
INSERT INTO categoria(nome) VALUES('Trabalho');
INSERT INTO categoria(nome) VALUES('Outros');

INSERT INTO usuario(nome, email, senha, situacao) 
values('Abel', 'abelgasque20@gmail.com', '$2a$10$X607ZPhQ4EgGNaYKt3n4SONjIv9zc.VMWdEuhCuba7oLAL5IvcL5.', 'ATIVO');

INSERT INTO permissao(descricao) values('ROLE_ADMINISTRADOR');
INSERT INTO permissao(descricao) values('ROLE_PESSOA');

INSERT INTO usuario_permissao(id_usuario, id_permissao) values(1, 1);
