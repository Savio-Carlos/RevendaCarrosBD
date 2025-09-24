
USE concessionaria_db;

INSERT INTO UF (siglaUF, nomeUF) VALUES ('PR', 'Paraná');
-- Agora, defina a variável do cargo que você quer usar
SET @idCargoVendedor = 2;

-- Re-execute o bloco de inserção da funcionária Ana
-- (Supondo que @idEnderecoFunc2 já foi definido corretamente)
INSERT INTO Pessoa (TipoPessoa, idEndereco, telefone, email) VALUES
('F', @idEnderecoFunc2, '45988776655', 'vendedora.ana@concessionaria.com');
SET @idPessoaAna = LAST_INSERT_ID();

INSERT INTO PessoaFisica (idPessoaFisica, nome, CPF, dataNascimento) VALUES
(@idPessoaAna, 'Ana Pereira', '55566677788', '1992-11-20');

-- Agora este comando deve funcionar, pois @idCargoVendedor tem um valor.
INSERT INTO Funcionario (idFuncionario, senhaHash, dataAdmissao, status, idCargo) VALUES
(@idPessoaAna, 'senha_hash_placeholder_456', '2021-03-22', TRUE, @idCargoVendedor);

INSERT INTO Cidade (nomeCidade, UF_siglaUF) VALUES ('Foz do Iguaçu', 'PR');
SET @idCidadeFoz = LAST_INSERT_ID();

INSERT INTO Bairro (nomeBairro) VALUES ('Centro'), ('Vila A'), ('Jardim Panorama');
SET @idBairroCentro = 1;
SET @idBairroVilaA = 2;
SET @idBairroPanorama = 3;

INSERT INTO SiglaLogradouro (siglaLogradouro, nomeSigla) VALUES ('R', 'Rua'), ('Av', 'Avenida');

-- Inserindo Logradouros (Ruas/Avenidas)
INSERT INTO Logradouro (CEP, nomeLogradouro, SiglaLogradouro_siglaLogradouro, idBairro, idCidade) VALUES
('85851000', 'Avenida Brasil', 'Av', @idBairroCentro, @idCidadeFoz),
('85852000', 'Rua Almirante Barroso', 'R', @idBairroCentro, @idCidadeFoz),
('85860000', 'Avenida Garibaldi', 'Av', @idBairroVilaA, @idCidadeFoz);

-- Inserindo Endereços específicos para os funcionários
INSERT INTO Endereco (Logradouro_CEP, numeroEndereco, complementoEndereco, referencia) VALUES
('85851000', '123', 'Apto 101', 'Próximo à Praça da Paz'),
('85852000', '456', 'Casa', 'Perto do Supermercado'),
('85860000', '789', NULL, 'Esquina com a Rua B');

SET @idEnderecoFunc1 = LAST_INSERT_ID();
SET @idEnderecoFunc2 = @idEnderecoFunc1 + 1;
SET @idEnderecoFunc3 = @idEnderecoFunc1 + 2;


-- Inserindo Cargos
INSERT INTO Cargo (nomeCargo) VALUES ('Gerente'), ('Vendedor'), ('Mecânico');
SET @idCargoGerente = 1;
SET @idCargoVendedor = 2;
SET @idCargoMecanico = 3;


-- -----------------------------------------------------------
-- INSERINDO FUNCIONÁRIO 1: Gerente
-- -----------------------------------------------------------
-- 1. Tabela Pessoa
INSERT INTO Pessoa (TipoPessoa, idEndereco, telefone, email) VALUES
('F', @idEnderecoFunc1, '45999887766', 'gerente.joao@concessionaria.com');
SET @idPessoaJoao = LAST_INSERT_ID();

-- 2. Tabela PessoaFisica
INSERT INTO PessoaFisica (idPessoaFisica, nome, CPF, dataNascimento) VALUES
(@idPessoaJoao, 'João da Silva', '11122233344', '1980-05-10');

-- 3. Tabela Funcionario
INSERT INTO Funcionario (idFuncionario, senhaHash, dataAdmissao, status, idCargo) VALUES
(@idPessoaJoao, 'senha_hash_placeholder_123', '2020-01-15', TRUE, @idCargoGerente);


-- -----------------------------------------------------------
-- INSERINDO FUNCIONÁRIO 2: Vendedora
-- -----------------------------------------------------------
-- 1. Tabela Pessoa
INSERT INTO Pessoa (TipoPessoa, idEndereco, telefone, email) VALUES
('F', @idEnderecoFunc2, '45988776655', 'vendedora.ana@concessionaria.com');
SET @idPessoaAna = LAST_INSERT_ID();

-- 2. Tabela PessoaFisica
INSERT INTO PessoaFisica (idPessoaFisica, nome, CPF, dataNascimento) VALUES
(@idPessoaAna, 'Ana Pereira', '55566677788', '1992-11-20');

-- 3. Tabela Funcionario
INSERT INTO Funcionario (idFuncionario, senhaHash, dataAdmissao, status, idCargo) VALUES
(@idPessoaAna, 'senha_hash_placeholder_456', '2021-03-22', TRUE, @idCargoVendedor);


-- -----------------------------------------------------------
-- INSERINDO FUNCIONÁRIO 3: Mecânico
-- -----------------------------------------------------------
-- 1. Tabela Pessoa
INSERT INTO Pessoa (TipoPessoa, idEndereco, telefone, email) VALUES
('F', @idEnderecoFunc3, '45977665544', 'mecanico.carlos@concessionaria.com');
SET @idPessoaCarlos = LAST_INSERT_ID();

-- 2. Tabela PessoaFisica
INSERT INTO PessoaFisica (idPessoaFisica, nome, CPF, dataNascimento) VALUES
(@idPessoaCarlos, 'Carlos de Souza', '99988877766', '1988-08-01');

-- 3. Tabela Funcionario
INSERT INTO Funcionario (idFuncionario, senhaHash, dataAdmissao, status, idCargo) VALUES
(@idPessoaCarlos, 'senha_hash_placeholder_789', '2019-07-30', TRUE, @idCargoMecanico);