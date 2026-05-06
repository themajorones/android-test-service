# Android Test Service

### Prerequisites

- **Java 25** or higher
- **Maven 3.6+**
- **Node.js 22.22.0+**
- **npm 10.9.4+**
- **SQL Server** database instance

### Installation and Running

**Configure environment variables**:
   - Create a `.env` file in the root directory (use `.env.example` as template)
   - Set required values:
     ```
     SPRING_DATASOURCE_URL=jdbc:sqlserver://localhost:1433;databaseName=themajorones
     SPRING_DATASOURCE_USERNAME=sa
     SPRING_DATASOURCE_PASSWORD=YourPassword123
     SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_GITHUB_CLIENTID=your_github_client_id
     SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_GITHUB_CLIENTSECRET=your_github_client_secret
     ```
