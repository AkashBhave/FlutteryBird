package com.akashbhave.flutterybird;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Timer;

public class FlutteryBird extends ApplicationAdapter {
    // SpriteBatch manages a sprite (image, background, player, etc.)
    SpriteBatch batch;
    Texture background; // Essentially an image

    // Data for the birds
    Texture[] birds;
    int flapState = 0;
    boolean flapUp = true; // If bird is flapping up or down
    float flapDelay = 0.1f;

    @Override
    public void create() {
        batch = new SpriteBatch();
        background = new Texture("bg.png");
        birds = new Texture[3];
        birds[0] = new Texture("bird1.png");
        birds[1] = new Texture("bird2.png");
        birds[2] = new Texture("bird3.png");
        Gdx.graphics.setContinuousRendering(false);
        startFlapping();
    }

    @Override
    public void render() {
    }

    public void senseTouch() {
        if(Gdx.input.justTouched()) {
            Gdx.app.log("Screen", "Touched");
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
                        (Gdx.graphics.getHeight() / 2) - birds[flapState].getHeight() / 2);
                batch.end();
                startFlapping();
            }
        }, flapDelay);

    }

}
