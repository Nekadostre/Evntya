
# Sistema de Gestión de Contratos y Presupuestos para Eventos

Este proyecto es una aplicación de escritorio desarrollada con **JavaFX**, orientada a la gestión de clientes, presupuestos, contratos y eventos para un negocio de renta de servicios (como salones de fiestas).

## 🚀 Características principales

- Autenticación de usuarios con roles
- Registro y consulta de clientes
- Creación de presupuestos y contratos personalizados
- Generación automática de PDFs
- Calendario con marcadores visuales para eventos
- Horarios disponibles (matutino/vespertino)
- Selección de extras por evento
- Interfaz moderna y responsiva

## 🛠️ Tecnologías utilizadas

- **Java 21**
- **JavaFX**
- **MySQL** (base de datos en hosting)
- **FXML** para las vistas
- **Maven** para gestión del proyecto
- **PDFBox** o librería interna para generación de PDFs

## 📂 Estructura del proyecto

```
/src
 └── main
     ├── java
     │   ├── controladores
     │   ├── dao
     │   ├── modelos
     │   └── database
     ├── resources
     │   ├── vistas (.fxml)
     │   ├── css
     │   └── img
```

## ⚙️ Cómo ejecutar

1. Clona el repositorio
2. Abre el proyecto en **NetBeans 24** o similar con soporte para JavaFX
3. Configura la conexión a base de datos en `Conexion.java` (usa los datos del hosting)
4. Ejecuta `App.java`

## 🧩 Requisitos

- Java JDK 21 o superior
- Base de datos MySQL activa en tu hosting
- NetBeans con JavaFX configurado
- Maven

## 👤 Autores

- Brandon Reynoso Barrera
