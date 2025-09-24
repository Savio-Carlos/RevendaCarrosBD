INSERT INTO statusVeiculo (idStatusVeiculo, status) VALUES (1, 'Disponível');
INSERT INTO statusVeiculo (idStatusVeiculo, status) VALUES (2, 'Vendido');

INSERT INTO Cargo (idCargo, nomeCargo) VALUES (1, 'Vendedor');
 
INSERT INTO TipoCombustivel (idtipoCombustivel, tipoCombustivel) VALUES (1, 'Flex (Gasolina/Etanol)');
INSERT INTO TipoCombustivel (idtipoCombustivel, tipoCombustivel) VALUES (2, 'Diesel');
INSERT INTO TipoCombustivel (idtipoCombustivel, tipoCombustivel) VALUES (3, 'Elétrico');
INSERT INTO TipoCombustivel (idtipoCombustivel, tipoCombustivel) VALUES (4, 'Híbrido');

INSERT INTO Garantia (idGarantia, dataInicio, kmLimiteRevisao, statusGarantia, dataExpiracaoGarantia) 
VALUES (1, '2025-09-22', 10000, 'Fábrica 2 Anos', '2027-09-22');
INSERT INTO Garantia (idGarantia, dataInicio, kmLimiteRevisao, statusGarantia, dataExpiracaoGarantia) 
VALUES (2, '2025-09-22', 5000, 'Loja 1 Ano (Usado)', '2026-09-22');

INSERT INTO Pessoa (TipoPessoa, email, telefone) VALUES ('F', 'joao.silva@email.com', '11987654321');
SET @vendedor1_id = LAST_INSERT_ID();

INSERT INTO PessoaFisica (idPessoaFisica, nome, CPF, dataNascimento) 
VALUES (@vendedor1_id, 'João da Silva', '11122233344', '1985-02-15');

INSERT INTO Funcionario (idFuncionario, senhaHash, dataAdmissao, idCargo) 
VALUES (@vendedor1_id, 'hash_senha_segura_123', '2021-08-01', 1);


INSERT INTO Pessoa (TipoPessoa, email, telefone) VALUES ('F', 'maria.santos@email.com', '21912345678');
SET @vendedor2_id = LAST_INSERT_ID();

INSERT INTO PessoaFisica (idPessoaFisica, nome, CPF, dataNascimento) 
VALUES (@vendedor2_id, 'Maria Oliveira Santos', '55566677788', '1992-11-30');

INSERT INTO Funcionario (idFuncionario, senhaHash, dataAdmissao, idCargo) 
VALUES (@vendedor2_id, 'hash_outra_senha_456', '2022-03-10', 1);

