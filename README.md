# AtividadePraticaUnidadeIV

Servidor de Arquivos HTTP (Versão mal-sucedida)

Desenvolvido em java com bibliotecas padrão.

Descrição:
O servidor escuta requisições na porta 8080 (implementada de forma hardcode).
Aguarda uma requisição http get com o o nome do arquivo que se deseja fazer download.
O arquivo deve estar disponível no servidor na pasta raiz do projeto para o download sem bem-sucedido.
O formato de requisição é o seguinte: http://localhost:8080/NomeDoArquivo.ExtensaoDoArquivo

exemplo: http://localhost:8080/Arquivo1.txt
o termo localhost na url pode ser substituído pelo ip do host.

Problema:
Falha com um volume de concorrência de 4096 requisições simultâneas pois sua implementação de comunicação de sockets é implementada com a biblioteca IO.