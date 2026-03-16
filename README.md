# 🎮 GameManager - Aplicación de Gestión de Videojuegos

**GameManager** es una aplicación en **Java** que permite a los usuarios gestionar su biblioteca de videojuegos.
Puedes organizar tus juegos por categoría, consola, estado de juego y dejar reseñas según tu progreso.

---

## 📝 Características

- Biblioteca de videojuegos por **categoría** (shooter, RPG, etc.) y **consola** (PS5, Xbox, PC).
- Cada videojuego incluye:
  - Logo  
  - Descripción  
  - Valoración del público y propia  
  - Estado de juego: Pendiente, Jugando, Completado  
  - Posibilidad de añadir reseñas (si se está jugando o completado)
- Perfil de usuario que muestra todas sus listas:
  - Juegos pendientes  
  - Juegos en progreso  
  - Juegos completados
- Listado opcional de juegos por puntuación.
- Modular y orientado a objetos para facilitar ampliaciones futuras.

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

## ⚙️ Instalación

1. Clonar el repositorio:
```bash
git clone https://github.com/TU_USUARIO/GameManager.git
```

2. Abrir el proyecto en tu IDE favorito (IntelliJ, Eclipse, NetBeans…).

3. Compilar y ejecutar:
```bash
cd GameManager/src
javac app/Main.java
java app.Main
```

---

## 🚀 Uso

- Añadir videojuegos a la biblioteca:
```java
Videojuego juego = new Videojuego("Halo Infinite", "halo.png", "Shooter futurista", "Shooter", "Xbox");
biblioteca.agregarJuego(juego);
```
- Cambiar estado del juego:
```java
juego.cambiarEstado(EstadoJuego.JUGANDO);
```
- Añadir reseña (solo si estás jugando o completado):
```java
juego.agregarReseña("¡Me está encantando!");
```
- Mostrar listas del usuario:
```java
usuario.mostrarListas();
```
- Ordenar juegos por valoración:
```java
biblioteca.ordenarPorValoracion();
```

---

## 👥 Equipo

- **Álvaro Benítez** – Modelo `Videojuego`  
- **Víctor Aguilera** – Clase `Usuario`  
- **Manolo Campos** – Clase `Biblioteca`  
- **Alejandro Bernal** – `Main` y pruebas integradas  

---

## 📌 Notas

- Proyecto modular, fácil de ampliar con nuevas funcionalidades.  
- Ideal para aprender **Java OOP**, manejo de listas y enums.  

---

## 🖤 ¡Disfruta gestionando tus videojuegos!

