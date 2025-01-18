package com.spaceinvaders.spaceinvaders;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.CheckBox;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import javax.sound.sampled.*;
import javax.swing.*;

public class SpaceInvaders extends Application {

	//Variable menu esc
	boolean pausaESC = false;

	//variable pausa con la P
	boolean pausaP = false;

	//Variable sonidosFX
	boolean sonidoGameOver = false;
	boolean sonidoMuerte = false;

	//Variable del checkBox
	private BooleanProperty checkBoxMusicaMarcado = new SimpleBooleanProperty(true);

	// Variables
	static final Random RAND = new Random();
	static final int WIDTH = 800;
	static final int HEIGHT = 600;
	static final int PLAYER_SIZE = 60;
	static final Image PLAYER_IMG = new Image("file:images/player.png");
	static final Image EXPLOSION_IMG = new Image("file:images/explosion.png");
	static final int EXPLOSION_W = 128;
	static final int EXPLOSION_ROWS = 3;
	static final int EXPLOSION_COL = 3;
	static final int EXPLOSION_H = 128;
	static final int EXPLOSION_STEPS = 15;
	static GraphicsContext gc;

	static final Image BOMBS_IMG[] = {
			new Image("file:images/1.png"),
			new Image("file:images/2.png"),
			new Image("file:images/3.png"),
			new Image("file:images/4.png"),
			new Image("file:images/5.png"),
			new Image("file:images/6.png"),
			new Image("file:images/7.png"),
			new Image("file:images/8.png"),
			new Image("file:images/9.png"),
			new Image("file:images/10.png"),
	};
	final int MAX_BOMBS = 10,  MAX_SHOTS = MAX_BOMBS * 2;
	boolean gameOver = false;

	static Rocket player;
	List<Shot> shots;
	List<Universe> univ;
	List<Bomb> Bombs;

	private double mouseX;
	// Hilo de música
	private HiloMusical hiloMusical;

	// Start
	public void start(Stage stage) throws Exception {
		// se inicia el hilo de música antes que la interfaz del juego
		hiloMusical = new HiloMusical("music/musica1.wav", "music/musica2.wav");
		Thread hiloMusicalThread = new Thread(hiloMusical);
		hiloMusicalThread.start();

		Canvas canvas = new Canvas(WIDTH, HEIGHT);
		gc = canvas.getGraphicsContext2D();
		Timeline timeline = new Timeline(new KeyFrame(Duration.millis(100), e -> run(gc)));
		timeline.setCycleCount(Timeline.INDEFINITE);
		timeline.play();
		canvas.setCursor(Cursor.MOVE);
		canvas.setOnMouseMoved(e -> mouseX = e.getX());
		canvas.setOnMouseClicked(e -> {
			if(shots.size() < MAX_SHOTS) shots.add(player.shoot());
			if(gameOver) {
				gameOver = false;
				setup();
			}
		});
		setup();

		canvas.setFocusTraversable(true);
		canvas.setOnKeyPressed(e -> {
			if (e.getCode() == KeyCode.ESCAPE) {
				mostrarMenu();
				// Pausa y reanuda
			} else if (e.getCode() == KeyCode.P) {
				pausaP = !pausaP;
				if (pausaP) {
					// Pausar la música
					hiloMusical.detener();
				} else {
					// Reanudar la música
					hiloMusical.reanudar();
				}			}
		});
		canvas.requestFocus();
		stage.setScene(new Scene(new StackPane(canvas)));
		stage.setTitle("Space Invaders");



		//configurar el evento de cierre de la ventana
		stage.setOnCloseRequest(e ->{
			System.out.println("Cerrando el juego...");
			//detener el hilo de la musica
			hiloMusical.detener();
			try {
				//esperar a que el hilo musical finalice antes de cerrar el juego
				hiloMusicalThread.join();
				System.out.println("Hilo musical terminado, procediendo a cerrar el juego...");
			}catch (InterruptedException ex){
				Thread.currentThread().interrupt();
			}
			Platform.exit(); //Cerrar la aplicacion
			System.out.println("Juego cerrado completamente!");
		});


		stage.show();

	}

	// Setup the game, only executed once
	private void setup() {
		univ = new ArrayList<>();
		shots = new ArrayList<>();
		Bombs = new ArrayList<>();
		player = new Rocket(WIDTH / 2, HEIGHT - PLAYER_SIZE, PLAYER_SIZE);
		player.score = 0;
		IntStream.range(0, MAX_BOMBS).mapToObj(i -> this.newBomb()).forEach(Bombs::add);
	}

	// Run graphics
	private void run(GraphicsContext gc) {

		// Pausar el juego con la letraP
		if (pausaP) {
			gc.setFont(Font.font(35));
			gc.setFill(Color.YELLOW);
			gc.fillText("Pausa", WIDTH / 2, HEIGHT / 2);
			return;
		} else if (pausaESC) {
			return;
		}
		gc.setFill(Color.grayRgb(20));
		gc.fillRect(0, 0, WIDTH, HEIGHT);
		gc.setTextAlign(TextAlignment.CENTER);
		gc.setFont(Font.font(20));
		gc.setFill(Color.WHITE);
		gc.fillText("Score: " + player.score, 60,  20);

		if(gameOver) {
			gc.setFont(Font.font(35));
			gc.setFill(Color.YELLOW);
			gc.fillText("Game Over \n Your Score is: " + player.score + " \n Click to play again", WIDTH / 2, HEIGHT /2.5);
			//sonido de game over al salir el texto de game over
			if (!sonidoGameOver){
				reproducirSonido("music/gameover.wav");
				sonidoGameOver = true;
			}

		//	return;

		}
		univ.forEach(Universe::draw);
		player.update();
		player.draw();
		player.posX = (int) mouseX;

		Bombs.stream().peek(Rocket::update).peek(Rocket::draw).forEach(e -> {
			if(player.colide(e) && !player.exploding) {
				player.explode();
				//sonido cuando el jugador explota
				if(!sonidoMuerte){
					reproducirSonido("music/sonidoMuerto.wav");
					sonidoMuerte = true;
				}

			}
		});

		for (int i = shots.size() - 1; i >=0 ; i--) {
			Shot shot = shots.get(i);
			if(shot.posY < 0 || shot.toRemove)  {
				shots.remove(i);
				continue;
			}
			shot.update();
			shot.draw();
			for (Bomb bomb : Bombs) {
				if(shot.colide(bomb) && !bomb.exploding) {
					player.score++;
					bomb.explode();
					shot.toRemove = true;
					// reproducir sonido cuando el jugador mata
					reproducirSonido("music/sonidoBaja.wav");
				}
			}
		}

		for (int i = Bombs.size() - 1; i >= 0; i--){
			if(Bombs.get(i).destroyed)  {
				Bombs.set(i, newBomb());
			}
		}

		gameOver = player.destroyed;
		if(RAND.nextInt(10) > 2) {
			univ.add(new Universe());
		}
		for (int i = 0; i < univ.size(); i++) {
			if(univ.get(i).posY > HEIGHT)
				univ.remove(i);
		}
	}

	private Bomb newBomb() {
		return new Bomb(50 + RAND.nextInt(WIDTH - 100), 0, PLAYER_SIZE, RAND.nextInt(BOMBS_IMG.length));
	}

	static int distance(int x1, int y1, int x2, int y2) {
		return (int) Math.sqrt(Math.pow((x1 - x2), 2) + Math.pow((y1 - y2), 2));
	}

	//Metodo para mostrar menu pulsando el ESCAPE checkBox
	private void mostrarMenu(){
		pausaESC = true; //pausar el jeugo al abrir el menu

		//Crear la ventana del menu:
		Stage menu = new Stage();
		menu.initModality(Modality.APPLICATION_MODAL);
		menu.setWidth(250);
		menu.setHeight(300);
		menu.setTitle("Opciones de sonido");

		VBox ventana = new VBox(20);
		ventana.setPadding(new Insets(5));
		ventana.setAlignment(Pos.CENTER);
		//Diseño de la ventana
		ventana.setStyle("-fx-background-color: black");

		CheckBox checkBoxMusica = new CheckBox("Activar Sonido");
		checkBoxMusica.selectedProperty().bindBidirectional(checkBoxMusicaMarcado);
		//estilo checkbox
		checkBoxMusica.setStyle("-fx-text-fill: yellow;"+ "-fx-box-border-width: 0.5px;	" + "-fx-box-border: yellow;" + "-fx-box-background: black;" + "-fx-mark-color: yellow");
		checkBoxMusica.setOnAction(e -> {
			if(!checkBoxMusica.isSelected()){
				hiloMusical.detener();
			}else{
				hiloMusical.reanudar();
			}
		});

		ventana.getChildren().add(checkBoxMusica);
		Scene menuScene = new Scene(ventana);
		menu.setScene(menuScene);
		menu.showAndWait();
	}

	//Metodo para reproducir los sonidos de efectos (tiritos y muerte)
	public static void reproducirSonido(String rutaSonido) {
		try {
			AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(rutaSonido));
			Clip clip = AudioSystem.getClip();
			clip.open(audioInputStream);
			clip.start();
		} catch (UnsupportedAudioFileException | LineUnavailableException | IOException e) {
			e.printStackTrace();
		}
	}



	public static void main(String[] args) {
		launch();
	}
}
