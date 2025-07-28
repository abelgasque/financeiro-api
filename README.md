# construir a imagem
docker build -t financeiro-api .

# rodar o container
docker run -d --name container-financeiro-api -p 8080:8080 financeiro-api