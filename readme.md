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


Como usar no dia a dia:

Faça alterações no código
Commit local: git commit -m "sua mensagem"
Push: git push origin main
Pronto! O GitHub faz tudo sozinho:

Conecta no VPS
Baixa código novo
Recompila
Reinicia a aplicação


Em ~1 minuto, suas mudanças estão no ar em https://api.langia.com.br
