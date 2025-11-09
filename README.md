# KRATOS_AUTHENTICATION_BACKEND

## Microservicio de Autenticación - RidECI
## Integrantes
- David Santiago Palacios Pinzón
- Juan Carlos Leal Cruz
- Juan Sebastian Puentes Julio
- Sebastian Albarracin Silva
- Ana Gabriela Fiquitiva Poveda

## Descripción

Microservicio encargado de gestionar la autenticación, autorización y verificación de usuarios de la plataforma RidECI. Garantiza que solo miembros autorizados de la comunidad universitaria accedan al sistema mediante validación de credenciales institucionales.

## Tabla de Contenidos

- [Características](#características)
- [Tecnologías](#tecnologías)
- [Requisitos Previos](#requisitos-previos)
- [Diagramas](#diagramas)
- [Instalación](#instalación)
- [Configuración](#configuración)
- [Uso](#uso)
- [API Endpoints](#api-endpoints)
- [Estructura del Proyecto](#estructura-del-proyecto)
- [Testing](#testing)
- [Despliegue](#despliegue)
- [Contribución](#contribución)

##  Características

- Registro con correo institucional (@escuelaing.edu.co)
- Autenticación segura con JWT
- Gestión de perfiles (Estudiante, Profesor, Empleado Administrativo)
- Verificación de conductores (licencia, placa, seguro)
- Registro de actividad de usuarios

## Tecnologías

- **Backend:** Java 17 + Spring Boot
- **Base de Datos:** MongoDB
- **Autenticación:** JWT (JSON Web Tokens)
- **Seguridad:** Spring Security
- **Testing:** JUnit 5, Mockito
- **Documentación:** Swagger UI
- **Contenedores:** Docker
- **CI/CD:** GitHub Actions

## Requisitos Previos

- Java JDK 17 
- Maven 
- MongoDB 
- Docker 
- Git

## Diagramas
### Diagrama de Contexto
![Diagrama de Contexto](docs/images/diagrama_contexto.png)
- Explicación:

### Diagrama de Clases
![Diagrama de Clases](docs/images/diagrama_clases.png)
- Explicación:

### Diagrama de Componentes Específico
![Diagrama de Componentes](docs/images/diagrama_componentes.png)
- Explicación:

### Diagrama de Despliegue
![Diagrama de Despliegue](docs/images/diagrama_despliegue.png)
- Explicación:

### Diagrama de Bases de Datos (Por Definir)
![Diagrama de Bases de Datos](docs/images/diagrama_bd.png)
- Explicación:



## Instalación

### Clonar el repositorio
```bash
git clone https://github.com/RIDECI/KRATOS_AUTHENTICATION_BACKEND.git
cd KRATOS_AUTHENTICATION_BACKEND
```

### Instalar dependencias
```bash
mvn clean install
```
