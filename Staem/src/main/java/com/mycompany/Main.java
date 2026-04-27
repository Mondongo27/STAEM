package com.mycompany;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        AuthService auth = new AuthService();
        BibliotecaService biblioteca = new BibliotecaService();

        System.out.println("--- inicio de sesion ---");
        System.out.print("usuario: ");
        String user = sc.nextLine();
        System.out.print("password: ");
        String pass = sc.nextLine();

        // Guardamos el ID que nos devuelve la base de datos
        int idLogueado = auth.login(user, pass);

        if (idLogueado != -1) {
            System.out.println("login correcto. bienvenido.");

            System.out.println("\n--- gestión de biblioteca ---");
            System.out.println("1. añadir juego");
            System.out.println("2. ver mis juegos");
            System.out.println("3. valorar/reseñar juego");
            System.out.println("4. ver valoración global de un juego"); // nueva opción
            System.out.print("opción: ");

            int opcion = sc.nextInt();
            sc.nextLine();

            if (opcion == 1) {
                System.out.print("título: ");
                String titulo = sc.nextLine();
                System.out.print("estado (pendiente/jugando/jugado): ");
                String estado = sc.nextLine();

                // Usamos idLogueado aquí
                if (biblioteca.añadirVideojuego(idLogueado, titulo, estado)) {
                    System.out.println("¡juego guardado!");
                }
            } else if (opcion == 2) {
                biblioteca.mostrarBiblioteca(idLogueado);
            } else if (opcion == 3) {
                System.out.print("título del juego a valorar: ");
                String titulo = sc.nextLine();
                System.out.print("valoración (1-5): ");
                int nota = sc.nextInt();
                sc.nextLine();
                System.out.print("escribe tu reseña: ");
                String resena = sc.nextLine();

                // Usamos idLogueado aquí
                if (biblioteca.valorarJuego(idLogueado, titulo, nota, resena)) {
                    System.out.println("¡reseña guardada!");
                }
            } else if (opcion == 4) {
                System.out.print("introduce el título del juego para ver su media: ");
                String tituloBusqueda = sc.nextLine();
                biblioteca.verValoracionGlobal(tituloBusqueda);
            }
        } else {
            System.out.println("usuario o contraseña incorrectos.");
        }
    }
}