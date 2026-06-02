# 🎫 Ticket Manager CLI — Sistema de Gerenciamento de Chamados

🌎 Idioma:
[🇺🇸 English](README.md) | 🇧🇷 Português

---

Aplicação CLI desenvolvida em Java para gerenciamento de tickets de suporte, criada para demonstrar conhecimentos em Java Orientado a Objetos, JDBC, SQL, testes automatizados e padrões de projeto.

O sistema permite criar, acompanhar, atualizar e encerrar chamados, mantendo um histórico completo de alterações através de um mecanismo de auditoria persistido em banco de dados SQLite.

---

## ✨ Funcionalidades

* **Criação de tickets** — cadastro de chamados com título, descrição e prioridade
* **Listagem completa** — visualização formatada de todos os tickets cadastrados
* **Filtros avançados** — busca por status e prioridade
* **Pesquisa por palavras-chave** — pesquisa em título e descrição utilizando SQL `LIKE`
* **Atualização de status** — validações de fluxo de negócio (ex.: tickets fechados não podem ser reabertos)
* **Edição de informações** — atualização parcial de título e descrição
* **Histórico de alterações** — auditoria completa de mudanças realizadas em cada ticket
* **Encerramento de tickets** — fechamento lógico através do status `CLOSED`
* **Exportação CSV** — geração de relatórios compatíveis com planilhas eletrônicas

---

## 🗄️ Conceitos Demonstrados

O projeto foi desenvolvido com foco em boas práticas de backend e persistência de dados:

| Conceito                       | Aplicação                                                    |
| ------------------------------ | ------------------------------------------------------------ |
| JDBC                           | Comunicação direta com SQLite utilizando `PreparedStatement` |
| SQL Parametrizado              | Prevenção contra SQL Injection                               |
| CRUD Completo                  | Operações de criação, leitura, atualização e encerramento    |
| Repository Pattern             | Separação entre acesso a dados e interface da aplicação      |
| Singleton Pattern              | Gerenciamento centralizado da conexão com o banco            |
| Foreign Keys                   | Relacionamento entre tickets e histórico de alterações       |
| Enums                          | Tipagem segura para Status e Prioridade                      |
| Validação de Regras de Negócio | Implementada na camada de repositório                        |
| Testes Unitários               | Cobertura com JUnit 5 utilizando banco em memória            |
| Exportação CSV                 | Tratamento correto de aspas e caracteres especiais           |

---

## 🛠️ Tecnologias

* **Java 17**
* **SQLite**
* **JDBC**
* **Maven**
* **JUnit 5**

---

## 🚀 Rodando Localmente

```bash
# 1. Clone o repositório
git clone https://github.com/seu-usuario/ticket-manager.git

# 2. Acesse a pasta do projeto
cd ticket-manager

# 3. Compile o projeto
mvn clean package

# 4. Execute a aplicação
java -jar target/ticket-manager-1.0.0-jar-with-dependencies.jar

# 5. Execute os testes
mvn test
```

---

## 📁 Estrutura do Projeto

```text
ticket-manager/
├── pom.xml
├── README.md
├── .gitignore
└── src/
    ├── main/java/com/ticketmanager/
    │   ├── Main.java
    │   ├── model/
    │   │   └── Ticket.java
    │   ├── database/
    │   │   └── DatabaseManager.java
    │   ├── repository/
    │   │   └── TicketRepository.java
    │   └── ui/
    │       └── Menu.java
    └── test/java/com/ticketmanager/
        └── TicketRepositoryTest.java
```

---

## 🗃️ Estrutura do Banco de Dados

O sistema utiliza duas tabelas principais:

### Tickets

Responsável pelo armazenamento dos chamados.

* ID
* Título
* Descrição
* Status
* Prioridade
* Data de criação
* Data de atualização

### Ticket Logs

Responsável pela auditoria de alterações realizadas.

* ID
* Ticket relacionado
* Ação executada
* Detalhes da alteração
* Data do registro

Essa estrutura permite rastrear completamente o ciclo de vida de cada ticket.

---

## 🧪 Testes

O projeto inclui uma suíte de aproximadamente **20 testes unitários** utilizando JUnit 5 e banco SQLite em memória, cobrindo:

* Criação de tickets
* Atualização de status
* Validações de negócio
* Pesquisas e filtros
* Operações de edição
* Exportação de dados

---

## 📌 Autor

**Giovanni Cabral** — Estudante de Engenharia da Computação na Facens | Estagiário de Python & Automação na Huawei Technologies

GitHub: https://github.com/GiovanniCabralO

LinkedIn: https://www.linkedin.com/in/giovannicabraldeoliveira
