package com.spaceinvaders.spaceinvaders;

import javax.sound.sampled.*;
import java.io.File;

public class HiloMusical implements Runnable {

    private final String ruta_musica1;
    private final String ruta_musica2;
    private final Object lock = new Object(); // objeto de bloqueo para la sincronizacion

    private Clip clip; // solo manejamos un clip en la instancia
    private boolean alternar = true; // bandera para alternar entre canciones
    private boolean detenerManual = false; // bandera para diferenciar entre detencion manual y natural

    public HiloMusical(String rutaMusica1, String rutaMusica2) {
        this.ruta_musica1 = rutaMusica1;
        this.ruta_musica2 = rutaMusica2;
        iniciarClip(); // inicializa el clip
    }

    private void iniciarClip() {
        while(detenerManual && Thread.currentThread().isInterrupted()){


        }
        try {
            AudioInputStream audio = AudioSystem.getAudioInputStream(new File(ruta_musica1));
            clip = AudioSystem.getClip();
            clip.open(audio); // inicia con el primer clip

            clip.addLineListener(event -> {
                synchronized (lock) {
                    if (event.getType() == LineEvent.Type.STOP && !detenerManual) {
                        reproducirSiguiente();
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            // iniciamos la reproduccion inicial
            reproducirMusica();
        } catch (Exception e) {
            Thread.currentThread().interrupt(); // si el hilo es interrumpido, lo manejamos adecuadamente
        }
    }

    public void reproducirMusica() {
        synchronized (lock) { // sincronizamos el acceso al clip
            try {
                detenerManual = false; // aseguramos que no sea detencion manual
                clip.setFramePosition(0); // reiniciamos el clip al principio
                clip.start(); // iniciamos la reproduccion
            } catch (Exception e) {
                e.printStackTrace(); // manejo basico de excepciones
            }
        }
    }

    private void reproducirSiguiente() {
        synchronized (lock) { // sincronizamos el acceso al clip
            try {
                clip.close(); // cerramos el clip actual
                clip = AudioSystem.getClip(); // creamos un nuevo clip

                // alternamos entre las dos canciones
                String siguienteRuta = alternar ? ruta_musica2 : ruta_musica1;
                alternar = !alternar; // cambiamos la bandera

                AudioInputStream siguienteAudio = AudioSystem.getAudioInputStream(new File(siguienteRuta));
                clip.open(siguienteAudio); // cargamos el siguiente audio

                clip.setFramePosition(0); // reiniciamos al principio
                clip.start(); // reproducimos la siguiente cancion
            } catch (Exception e) {
                e.printStackTrace(); // manejo basico de excepciones
            }
        }
    }

    // metodo para detener la musica correctamente cuando se cierre la ventana
    public synchronized void detener() {
        synchronized (lock) {
            if (clip != null) {
                detenerManual = true; // marcamos como detencion manual
                clip.stop(); // detenemos el clip
            }
        }
    }

    // metodo para reanudar la musica despues de detenerla
    public synchronized void reanudar() {
        synchronized (lock) {
            if (clip != null && detenerManual) { // verificamos si fue detenido manualmente
                detenerManual = false; // reseteamos la bandera de detencion manual
                clip.start(); // reanudamos la reproduccion desde la posicion actual
            }
        }
    }
}
