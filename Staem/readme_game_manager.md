# 🎮 GameManager - Tu Biblioteca de Videojuegos en Java

¡Bienvenido a **GameManager**, la aplicación en Java que transforma tu colección de videojuegos en una experiencia interactiva! 🎉

---

## 🕹️ ¿Qué es GameManager?

GameManager te permite **organizar, valorar y reseñar tus videojuegos** de forma sencilla. Gestiona tus juegos por categoría, consola y estado, y consulta tus estadísticas personales de juego.

---

## ✨ Características principales

- **Biblioteca por categorías y consolas:** Shooter, RPG, PS5, Xbox, PC... 
- **Gestión de estados de juego:** Pendiente, Jugando, Completado.
- **Reseñas:** Añade comentarios solo si estás jugando o has completado el juego.
- **Valoraciones:** Visualiza la nota del público y tu propia valoración.
- **Listas personalizadas:** Consulta tus juegos pendientes, en progreso y completados.
- **Ordenar por puntuación:** Encuentra tus favoritos al instante.

---

## 📂 Estructura del proyecto

```
GameManager/
│
├─ src/
│   ├─ modelo/
│   │   ├─ Videojuego.java
│   │   ├─ EstadoJuego.java
│   │   └─ Usuario.java
│   │
│   ├─ servicios/
│   │   └─ Biblioteca.java
│   │
│   └─ app/
│       └─ Main.java
│
├─ README.md
└─ .gitignore
```

---

## ⚙️ Instalación rápida

1. Clona el repositorio:
```bash
git clone https://github.com/TU_USUARIO/GameManager.git
```

2. Abre el proyecto en tu IDE favorito (IntelliJ, Eclipse, NetBeans…)

3. Compila y ejecuta:
```bash
cd GameManager/src
javac app/Main.java
java app.Main
```

---

## 🚀 Cómo usar GameManager

- **Añadir un juego:**
```java
Videojuego juego = new Videojuego("Halo Infinite", "halo.png", "Shooter futurista", "Shooter", "Xbox");
biblioteca.agregarJuego(juego);
```
- **Cambiar estado del juego:**
```java
juego.cambiarEstado(EstadoJuego.JUGANDO);
```
- **Añadir reseña:**
```java
juego.agregarReseña("¡Me está encantando!");
```
- **Mostrar listas del usuario:**
```java
usuario.mostrarListas();
```
- **Ordenar juegos por puntuación:**
```java
biblioteca.ordenarPorValoracion();
```

---

## 👥 Nuestro equipo

- **Álvaro Benítez** – Modelo `Videojuego`  
- **Víctor Aguilera** – Clase `Usuario`  
- **Manolo Campos** – Clase `Biblioteca`  
- **Alejandro Bernal** – `Main` y pruebas integradas  

---

## 🌟 Notas finales

- Proyecto modular y escalable, fácil de ampliar con nuevas funcionalidades.
- Ideal para aprender **Java OOP**, manejo de listas y enums.
- ¡Perfecto para presentar una demo funcional y visual para clase!

---

## ❤️ ¡Disfruta gestionando tus videojuegos y compartiendo tus reseñas!

