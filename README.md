# AtividadePraticaUnidadeIV

Servidor de Arquivos HTTP (Versão com falha)

Desenvolvido em java com ênfase na biblioteca NIO.
Abordagem correta ao implementar o problema com ferramentas de comunicação fornecida pela lib NIO.
Porém há falhas de implementação.

Descrição:
O servidor escuta requisições na porta 8080 (implementada de forma hardcode).
Aguarda uma requisição http get com o o nome do arquivo que se deseja fazer download.
O arquivo deve estar disponível no servidor na pasta raiz do projeto para o download sem bem-sucedido.
O formato de requisição é o seguinte: http://localhost:8080/NomeDoArquivo.ExtensaoDoArquivo

exemplo: http://localhost:8080/Arquivo1.txt
o termo localhost na url pode ser substituído pelo ip do host.

2 Falhas de Implementação.
Em algum ponto o ThreadPool não está trabalhando bem com o gerenciador de canais (selector).
O download do arquivo vem duplicado onde um arquivo está vazio e o outro contêm os dados do arquivo a ser baixado. Um defeito na implementação de escrita.