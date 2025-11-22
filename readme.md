bash# Ver status do LangIA
systemctl status langia

# Ver logs em tempo real
journalctl -u langia -f

# Reiniciar aplicação
systemctl restart langia

# Acessar banco de dados
docker exec -it langia-postgres psql -U langia_user -d langia

# Ver containers rodando
docker ps
# Teste CI/CD
