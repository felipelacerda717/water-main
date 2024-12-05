# AquaMonitor - Sistema de Monitoramento de Água

## 📝 Sobre o Projeto

AquaMonitor é um sistema web desenvolvido em Java com Spring Boot para monitoramento e gestão do consumo de água. O sistema inclui funcionalidades de gamificação para incentivar a economia de água, além de fornecer análises detalhadas de consumo.

## 🚀 Funcionalidades Principais

- **Dashboard Interativa**: Visualização de consumo diário e mensal com gráficos
- **Registro de Consumo**: Sistema flexível para registros diários e mensais
- **Sistema de Metas**: Definição e acompanhamento de metas de economia
- **Dicas de Economia**: Sugestões personalizadas com calculadora de economia
- **Gamificação**: Sistema de conquistas e ranking entre usuários
- **Análises**: Gráficos e estatísticas detalhadas de consumo

## 🛠️ Tecnologias Utilizadas

### Backend
- Java 17
- Spring Boot 3.4.0
- Spring Security
- Spring Data JPA
- H2 Database
- Maven

### Frontend
- Thymeleaf
- Tailwind CSS 2.2.19
- Chart.js
- Font Awesome 6.0.0

## 📦 Instalação e Execução

1. **Pré-requisitos**
   - Java 17 ou superior
   - Maven

2. **Clone o repositório**
   ```bash
   git clone https://github.com/felipelacerda717/water-main
   cd [para onde você descompactou o projeto, exemplo: Documentos/...]
   ```

3. **Execute o projeto**
   ```bash
   ./mvnw spring-boot:run
   ```

4. **Acesse no navegador**
   ```
   http://localhost:8080
   ```

## 💡 Configuração do Banco de Dados

O projeto utiliza H2 Database. As configurações podem ser encontradas em `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:h2:file:./data/waterdb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=password
```

## 📊 Estrutura do Projeto

```
src/
├── main/
│   ├── java/
│   │   └── com/control/water/
│   │       ├── config/
│   │       ├── controllers/
│   │       ├── models/
│   │       ├── repositories/
│   │       └── services/
│   └── resources/
│       ├── static/
│       └── templates/
└── test/
```

## 🔒 Segurança

O sistema implementa:
- Autenticação de usuários
- Proteção contra CSRF
- Criptografia de senhas
- Controle de sessão

## 🎯 Recursos da Interface

- Design responsivo
- Tema moderno e intuitivo
- Gráficos interativos
- Feedback visual em tempo real
- Sistema de notificações

## 👥 Tipos de Usuário

- **Usuários Residenciais**: Monitoramento de consumo doméstico
- **Administradores**: Gestão do sistema e análise de dados

## 📱 Screenshots

![image](https://github.com/user-attachments/assets/bc123a69-3879-4660-9d40-539327d39bfd)
![image](https://github.com/user-attachments/assets/02d0280b-d053-4ff5-bc70-01e63783a092)
![image](https://github.com/user-attachments/assets/3c215699-556c-4af2-aed4-0c8ce511be3d)
![image](https://github.com/user-attachments/assets/608f1751-8eda-4492-b2c4-0ebf5069f1fb)
![image](https://github.com/user-attachments/assets/576b94b5-ba8d-483b-b9d1-299e9e1d72c9)
![image](https://github.com/user-attachments/assets/e655a813-d775-4d65-b5ff-1f1c05a2366b)
![image](https://github.com/user-attachments/assets/62517470-6a3b-4d93-9ad3-0b02fff93a37)


