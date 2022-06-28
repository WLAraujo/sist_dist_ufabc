# Detalhes

Para criar um ambiente Java que será usado ao longo do projeto decidi criar um isolamento usando um container. Como a versão java especificada pelo professor foi a JDK 1.8 decidi me basear numa imagem de Ubuntu e instalar essa versão do Java nele, como pode ser visto no Dockerfile que foi usado como base para  a construção da imagem `ambiente_java`.

Após construir o `Dockerfile`, para construir a imagem `ambiente_java` basta executar: 

`docker build -t ambiente_java:1.0 .`

Aí para construir um container interativo que possui volumes com base nessa imagem:

`docker container run -t -d -v ./arquivos:/arquivos --name ambiente_dev ambiente_java`
 
Por fim, quando quiser usar esse ambiente execute o comando `bash` usando a flag `it`:

`docker exec -it ambiente_java bash`