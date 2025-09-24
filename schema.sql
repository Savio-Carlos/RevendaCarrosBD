 -- Apaga o banco de dados antigo se existir e cria um novo
DROP DATABASE IF EXISTS concessionaria_db;
CREATE DATABASE concessionaria_db;
USE concessionaria_db;

-- Tabelas de Endereçamento
CREATE TABLE UF (
    siglaUF VARCHAR(2) PRIMARY KEY,
    nomeUF VARCHAR(45) NOT NULL
);

CREATE TABLE SiglaLogradouro (
    siglaLogradouro VARCHAR(10) PRIMARY KEY,
    nomeSigla VARCHAR(45) NOT NULL
);

CREATE TABLE Bairro (
    idBairro INT PRIMARY KEY AUTO_INCREMENT,
    nomeBairro VARCHAR(45) NOT NULL
);

CREATE TABLE Cidade (
    idCidade INT PRIMARY KEY AUTO_INCREMENT,
    nomeCidade VARCHAR(45) NOT NULL,
    UF_siglaUF VARCHAR(2) NOT NULL,
    FOREIGN KEY (UF_siglaUF) REFERENCES UF(siglaUF)
);

CREATE TABLE Logradouro (
    CEP VARCHAR(8) PRIMARY KEY,
    nomeLogradouro VARCHAR(100) NOT NULL,
    SiglaLogradouro_siglaLogradouro VARCHAR(10) NOT NULL,
    idBairro INT NOT NULL,
    idCidade INT NOT NULL,
    FOREIGN KEY (SiglaLogradouro_siglaLogradouro) REFERENCES SiglaLogradouro(siglaLogradouro),
    FOREIGN KEY (idBairro) REFERENCES Bairro(idBairro),
    FOREIGN KEY (idCidade) REFERENCES Cidade(idCidade)
);

-- Endereco: instancia de endereço (logradouro + número + complemento)
CREATE TABLE Endereco (
    idEndereco INT PRIMARY KEY AUTO_INCREMENT,
    Logradouro_CEP VARCHAR(8) NOT NULL,
    numeroEndereco VARCHAR(10) NOT NULL,
    complementoEndereco VARCHAR(100),
    referencia VARCHAR(200),
    -- opcional: armazenar bairro/cidade redundantes para consultas rápidas (nullable)
    idBairro INT,
    idCidade INT,
    FOREIGN KEY (Logradouro_CEP) REFERENCES Logradouro(CEP),
    FOREIGN KEY (idBairro) REFERENCES Bairro(idBairro),
    FOREIGN KEY (idCidade) REFERENCES Cidade(idCidade)
);

CREATE TABLE Pessoa (
    idPessoa INT PRIMARY KEY AUTO_INCREMENT,
    TipoPessoa CHAR(1) NOT NULL,
    idEndereco INT,
    complementoEndereco VARCHAR(100),
    telefone VARCHAR(20),
    email VARCHAR(100),
    FOREIGN KEY (idEndereco) REFERENCES Endereco(idEndereco)
);

-- Especializações de Pessoa
CREATE TABLE PessoaFisica (
    idPessoaFisica INT PRIMARY KEY,
    nome VARCHAR(45) NOT NULL,
    CPF CHAR(11) NOT NULL UNIQUE,
    dataNascimento DATE,
    FOREIGN KEY (idPessoaFisica) REFERENCES Pessoa(idPessoa)
);

CREATE TABLE PessoaJuridica (
    idPessoaJuridica INT PRIMARY KEY,
    CNPJ VARCHAR(14) NOT NULL UNIQUE,
    nomeFantasia VARCHAR(45),
    RazaoSocial VARCHAR(45) NOT NULL,
    FOREIGN KEY (idPessoaJuridica) REFERENCES Pessoa(idPessoa)
);

-- Tabelas de Contato (ligadas à Pessoa)
CREATE TABLE Telefone (
    idTelefone INT PRIMARY KEY AUTO_INCREMENT,
    numeroTelefone VARCHAR(15) NOT NULL,
    DDD INT,
    DDI INT,
    idPessoa INT NOT NULL,
    FOREIGN KEY (idPessoa) REFERENCES Pessoa(idPessoa)
);

CREATE TABLE Email (
    idEmail INT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(45) NOT NULL,
    idPessoa INT NOT NULL,
    FOREIGN KEY (idPessoa) REFERENCES Pessoa(idPessoa)
);

-- Papéis (Roles) que uma Pessoa pode ter
CREATE TABLE Cliente (
    idCliente INT PRIMARY KEY,
    Senha_Hash VARCHAR(255) NOT NULL,
    FOREIGN KEY (idCliente) REFERENCES Pessoa(idPessoa)
);

CREATE TABLE Cargo (
    idCargo INT PRIMARY KEY AUTO_INCREMENT,
    nomeCargo VARCHAR(45) NOT NULL
);

CREATE TABLE Funcionario (
    idFuncionario INT PRIMARY KEY,
    senhaHash VARCHAR(255) NOT NULL,
    dataAdmissao DATE NOT NULL,
    status BOOLEAN NOT NULL DEFAULT TRUE,
    idCargo INT NOT NULL,
    FOREIGN KEY (idFuncionario) REFERENCES PessoaFisica(idPessoaFisica),
    FOREIGN KEY (idCargo) REFERENCES Cargo(idCargo)
);

-- Tabelas de Catálogo (Veículo, Peças, etc.)
CREATE TABLE statusVeiculo (
    idStatusVeiculo INT PRIMARY KEY AUTO_INCREMENT,
    status VARCHAR(45) NOT NULL -- Ex: 'Disponível', 'Vendido', 'Em Manutenção'
);

CREATE TABLE Garantia (
    idGarantia INT PRIMARY KEY AUTO_INCREMENT,
    statusGarantia VARCHAR(45),
    dataInicio DATE,
    kmLimiteRevisao INT,
    dataExpiracaoGarantia VARCHAR(45) -- Manteve como VARCHAR conforme MER
);

CREATE TABLE TipoCombustivel (
    idtipoCombustivel INT PRIMARY KEY AUTO_INCREMENT,
    tipoCombustivel VARCHAR(45)
);

CREATE TABLE Veiculo (
    numChassi VARCHAR(17) PRIMARY KEY,
    placa VARCHAR(7) NOT NULL UNIQUE,
    marcaCarro VARCHAR(45),
    modeloVeiculo VARCHAR(45),
    anoModelo INT,
    quilometragem INT,
    cor VARCHAR(45),
    precoVeiculo DOUBLE,
    descricao VARCHAR(500),
    fotos VARCHAR(500),
    idStatusVeiculo INT,
	idtipoCombustivel INT,
    idGarantia INT,
	FOREIGN KEY (idtipoCombustivel) REFERENCES TipoCombustivel(idtipoCombustivel),
    FOREIGN KEY (idGarantia) REFERENCES Garantia(idGarantia),
    FOREIGN KEY (idStatusVeiculo) REFERENCES statusVeiculo(idStatusVeiculo)
);	

-- Tabela de Transação Principal: Venda
CREATE TABLE Venda (
    idVenda INT PRIMARY KEY AUTO_INCREMENT,
    dataVenda DATE NOT NULL,
    precoVendaVeiculo DOUBLE NOT NULL,
    formaPagamento VARCHAR(45),
    idCliente INT NOT NULL,
    idFuncionario INT NOT NULL,
    numChassiVeiculo VARCHAR(17) NOT NULL,
    FOREIGN KEY (idCliente) REFERENCES Cliente(idCliente),
    FOREIGN KEY (idFuncionario) REFERENCES Funcionario(idFuncionario),
    FOREIGN KEY (numChassiVeiculo) REFERENCES Veiculo(numChassi)
);


-- Você pode adicionar as tabelas de Ordem de Serviço, Peça, etc. aqui depois