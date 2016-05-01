package com.akashbhave.flutterybird;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Timer;

import java.util.Random;

public class FlutteryBird extends ApplicationAdapter {
    // SpriteBatch manages a sprite (image, background, player, etc.)
    SpriteBatch batch;
    Texture background; // Essentially an image

    /* TUBE VARIABLES */
    Texture topTube;
    Texture bottomTube;
    Float tubeGap = 350f; // Distance between the two tubes
    Float maxTubeOffset;
    Random randomGenerator;
    Float tubeVelocity = 4f;
    Integer numberOfTubes = 4; // 4 needed to show there are infinite tubes
    Float[] tubeX = new Float[numberOfTubes]; // Position of tubes along the x-axis
    Float[] tubeOffset = new Float[numberOfTubes];
    Float distanceBtwnTubes;

    /* BIRD VARIABLES */
    Texture[] birds;
    int flapState = 0;
    boolean flapUp = true; // If bird is flapping up or down
    float flapDelay = 0.1f;
    float birdY = 0; // How high the bird is
    float velocity = 0;
    float gravity = 0.8f;

    int gameState = 0;// Keeps track of the state of the game

    @Override
    public void create() {
        batch = new SpriteBatch();
        background = new Texture("bg.png");

        topTube = new Texture("topTube.png");
        bottomTube = new Texture("bottomTube.png");
        maxTubeOffset = Gdx.graphics.getHeight() / 2 - tubeGap / 2 - 100;
        randomGenerator = new Random();
        distanceBtwnTubes = (float) (Gdx.graphics.getWidth() * 3 / 4);

        for(int i = 0; i < numberOfTubes; i++) {
            tubeOffset[i] = (randomGenerator.nextFloat() - 0.5f) * (Gdx.graphics.getHeight() - tubeGap - 200);
            tubeX[i] = ((float) Gdx.graphics.getWidth() / 2 - topTube.getWidth() / 2) + i * distanceBtwnTubes;
        }

        birds = new Texture[3];
        birds[0] = new Texture("bird1.png");
        birds[1] = new Texture("bird2.png");
        birds[2] = new Texture("bird3.png");
        Gdx.graphics.setContinuousRendering(false);

        birdY = Gdx.graphics.getHeight() / 2 - birds[flapState].getHeight() / 2;
        startFlapping();

        // Senses if the screen is touched
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                Gdx.app.log("Screen", "Touched");
                if (gameState == 0) {
                    gameState = 1; // Starts the game
                    Gdx.graphics.setContinuousRendering(true);
                } else if (gameState == 1) {
                    Gdx.graphics.setContinuousRendering(true);
                }
                return super.touchDown(screenX, screenY, pointer, button);
            }
        });
    }

    @Override
    public void render() {
        if(gameState != 0) {
            if (Gdx.input.justTouched()) {
                velocity = -25;
                flapState = 0;
            } else {
                velocity ++;
                flapState = 1;
            }

            // Adds the tubes
            batch.begin();
            batch.draw(background, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            for(int i = 0; i < numberOfTubes; i++) {
                // Loops the four tubes over again
                if(tubeX[i] <- topTube.getWidth()) {
                    tubeX[i] += numberOfTubes * distanceBtwnTubes;
                } else {
                    tubeX[i] -= tubeVelocity;
                }

                batch.draw(topTube,
                        tubeX[i],
                        Gdx.graphics.getHeight() / 2 + tubeGap / 2 + tubeOffset[i]);
                batch.draw(bottomTube,
                        tubeX[i],
                        Gdx.graphics.getHeight() / 2 - tubeGap / 2 - bottomTube.getHeight() + tubeOffset[i]);
            }

            // Starts the 'gravity' of the game
            if(birdY > 0 || velocity < 0) {
                velocity = velocity + gravity;
                birdY -= velocity;
            }

            batch.draw(birds[flapState],
                    (Gdx.graphics.getWidth() / 2) - birds[flapState].getWidth() / 2,
                    birdY);
            batch.end();
        }
    }


    public void startFlapping() {
        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                // Tells the render method that we are going to start displaying sprites
                batch.begin();
                batch.draw(background, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight()); // Displays it on the screen
                // Controls the bird speed
                if (flapState == 0) {
                    flapState = 1;
                    flapUp = true;
                } else if (flapState == 1) {
                    if (flapUp) {
                        flapState = 2;
                        flapUp = true;
                    } else {
                        flapState = 0;
                        flapUp = false;
                    }
                } else if (flapState == 2) {
                    flapState = 1;
                    flapUp = false;
                }
                batch.draw(birds[flapState],
                        (Gdx.graphics.getWidth() / 2) - birds[flapState].getWidth() / 2,
                        birdY);
                batch.end();
                if(gameState == 0) {
                    startFlapping();
                } else {
                    return;
                }
            }
        }, flapDelay);

    }

}
