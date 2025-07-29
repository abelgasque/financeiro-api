# construir a imagem
docker build -t financeiro-api .

Ou

docker pull abelgasque/financeiro-api

# rodar o container
docker run -d --name container-financeiro-api --env-file .env -p 8080:8080 abelgasque/financeiro-api

# Imagem postgres
docker pull postgres:16

docker run --name container-postgres \
  -e POSTGRES_DB=financeiro_db \
  -e POSTGRES_USER=admin \
  -e POSTGRES_PASSWORD=Application@2025 \
  -p 5432:5432 \
  -d postgres:16