/*
* Atividade Unidade IV: Servidor de Arquivos
*
* Aluno: Erick Cristian de Oliveira Pereira
* Matricula: 11621BSI265
* */

package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;

@SpringBootApplication
@RestController
public class ServidorDeArquivos
{

	public static void main(String[] args)
	{
		SpringApplication.run(ServidorDeArquivos.class, args);
	}

	@GetMapping("/{fileName:.+}")// Tag para identificar o nome do arquivo na requisicao.
	public ResponseEntity downloadDeArquivos(@PathVariable String fileName)
	{
		Path caminhoArquivo = Paths.get(fileName);//Pega diretorio do arquivo alvo.
		Resource resource = null;//Variavel para armazenar os bytes do arquivo alvo e informacoes como o caminho absoluto do arquivo.
		try
		{
			resource = new UrlResource(caminhoArquivo.toUri());//Instanciando os bytes do arquivo a ser disponibilizado para download.
		}
		catch (MalformedURLException e)
		{
			e.printStackTrace();
		}
		return ResponseEntity.ok()//Codigo de Retorno.
				.contentType(MediaType.parseMediaType("application/octet-stream"))//Cabecalho informando tipo de dado.
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")//Cabecalho informando nome do arquivo enviado.
				.body(resource);//Bytes do arquivo sendo anexados no body da requisicao.
	}

}