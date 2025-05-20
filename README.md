# Mercado Libro Mobile 📖

## Descripción del Proyecto 📝
La aplicación móvil MercadoLibro es una extensión de la plataforma web desarrollada previamente en el Módulo de Programador Web. Esta aplicación está diseñada para ofrecer una experiencia fluida y eficiente en dispositivos móviles, permitiendo a los usuarios realizar acciones básicas como la compra de libros, la edición de datos personales, realizar una compra, etc. mediante un sistema CRUD, integrado con un backend desarrollado en Django.

El proyecto sigue las mejores prácticas de desarrollo móvil, con un enfoque en la experiencia de usuario (UX) y el diseño intuitivo, asegurando que la navegación sea clara, accesible y consistente. Además, la app utiliza APIs para mantener la sincronización de datos entre la plataforma web y móvil, y asegura la protección de datos mediante políticas de seguridad bien fundamentadas.

## Características Principales ✨
1. Navegación Intuitiva:
La aplicación cuenta con una navegación bien definida entre diferentes Activities, permitiendo a los usuarios desplazarse de manera fluida entre pantallas.
Implementación de navegación padre-hija, asegurando que los usuarios puedan regresar fácilmente a la pantalla anterior.

2. Gestión CRUD:
Los usuarios pueden gestionar su información personal, productos, turnos, entre otros, utilizando un sistema CRUD que está sincronizado con el backend en Django.
La persistencia de datos se asegura a través de la integración con las APIs del backend.

3. Actividad Multimedia:
La aplicación incluye una Activity que presenta recursos multimedia como videos o imágenes, mejorando la interacción del usuario.

4. Seguridad:
La aplicación implementa un sistema de autenticación basado en JWT (JSON Web Tokens) para asegurar que solo los usuarios autorizados puedan acceder a la plataforma.
El uso de JWT se justifica por su seguridad en la transmisión de datos y su compatibilidad con las tecnologías móviles.

5. Diseño Accesible y Usable:
La interfaz de usuario (UI) está diseñada respetando los principios de accesibilidad, asegurando que la app sea utilizable para un amplio rango de usuarios.
El diseño es coherente, homogéneo y optimizado para pantallas móviles.

## Tecnologías Utilizadas 💻
* Lenguaje de programación: Java
* IDE de desarrollo: Android Studio
* Backend: Django (con APIs REST para la sincronización de datos)
* Persistencia de datos: APIs RESTful
* Autenticación: JWT para la autenticación de usuarios
* Multimedia: Integración de videos e imágenes en las Activities

## Requisitos de Instalación 🛠️
Para ejecutar la aplicación en tu entorno local, sigue los siguientes pasos:
1. Clonar el repositorio:
    ```bash
   git clone https://github.com/CodeSquadMobile/Mercado-Libro-Mobile.git
2. Abrir el proyecto en Android Studio:
* Abre Android Studio y selecciona "Open an existing project".
* Navega a la carpeta donde clonaste el repositorio y selecciona la carpeta del proyecto.
3. Configurar el backend: Asegúrate de tener el backend en Django funcionando y correctamente configurado para interactuar con las APIs.
4. Correr la aplicación en un emulador o dispositivo:
* Selecciona un emulador de Android o conecta un dispositivo físico a tu computadora.
* Haz clic en el botón "Run" en Android Studio para ejecutar la aplicación.

## Uso de la Aplicación 📱
**Pantallas Principales** 🖼️
* Pantalla de Inicio: Permite a los usuarios registrarse o iniciar sesión en la aplicación.
* Pantalla de Gestión: Los usuarios pueden visualizar y editar su información personal, productos y pedidos.
* Pantalla Multimedia: Muestra contenido multimedia: imagenes de portada de los libros.
* Pantalla de Contacto: Opciones de comunicación.

**Navegación entre Activities** 🧭  
Se implementa la navegación entre Activities con un sistema claro y accesible, permitiendo a los usuarios moverse entre las distintas secciones de la app y retornar fácilmente a la pantalla anterior.

## Seguridad 🔒
La autenticación se realiza mediante JWT, lo que garantiza que las credenciales del usuario y la información sensible estén protegidas durante el proceso de autenticación y la interacción con las APIs del backend.

## Multimedia 🎥
La aplicación cuenta con una Activity multimedia que incluye recursos como videos o imágenes, mejorando la interacción y experiencia del usuario.

## Descarga de la Aplicación ⤵

Mientras tanto, te ofrecemos ver un demo en video aquí: https://drive.google.com/file/d/1lFl3A5HsGwVYQQ5GT4aZKUP-lF9OQLAZ/view?usp=sharing.

## Documentación
Toda la documentación del proyecto está disponible en la Wiki del repositorio en GitHub, incluyendo el plan de seguridad, los casos de testing y las especificaciones técnicas. Además, se han adjuntado gráficos y diseños en el formato IEEE830.

-------------------------------------------

Este proyecto está desarrollado bajo la organización en GitHub siguiendo el flujo de trabajo Gitflow. Las ramas main, develop y release se emplean para una correcta gestión del código.

**Equipo de Desarrollo:**  👥  
🧑 Ñañez Nahir Nicolás  
🧑 Luna Dalla Lasta Marcelo Javier  
🧑 Plaza Leonardo  
👩 Nobiltá Ivette Jael  
