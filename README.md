# AtividadePraticaUnidadeIV

Servidor de Arquivos HTTP (Versão definitiva)

Desenvolvido em java com a framework Spring Boot.

Descrição:
O servidor escuta requisições na porta 8080 (implementada de forma hardcode).
Aguarda uma requisição http get com o o nome do arquivo que se deseja fazer download.
O arquivo deve estar disponível no servidor na pasta raiz do projeto para o download sem bem-sucedido.
O formato de requisição é o seguinte: http://localhost:8080/NomeDoArquivo.ExtensaoDoArquivo
exemplo: http://localhost:8080/Arquivo1.txt
o termo localhost na url pode ser substituído pelo ip do host.

Suporta 4096 requisições concorrentes.