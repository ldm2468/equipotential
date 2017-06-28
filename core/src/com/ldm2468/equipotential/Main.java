package com.ldm2468.equipotential;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.HashMap;

public class Main extends ApplicationAdapter {
    Viewport v, ui;
    SpriteBatch sb;
    Vector2 tmpVector = new Vector2();
    Pixmap px;
    int ssaa = 1;
    float epsilon = 0.1f;
    HashMap<Integer, Color> colors = new HashMap<Integer, Color>();
    PixelFunction func = new PixelFunction() {
        int dist = 5;

        @Override
        public float f(float x, float y) {
            if (y * y + (Math.abs(x) - dist) * (Math.abs(x) - dist) < 0.5) {
                return 0;
            }
            return 20f * (float) (1 / Math.sqrt((y * y + (x - dist) * (x - dist))) - 1 / Math.sqrt((y * y + (x + dist) * (x + dist))));
        }
    }, func2 = new PixelFunction() { // http://hyperphysics.phy-astr.gsu.edu/hbase/electric/potlin.html#c1
        int d = 4, l = 4;

        @Override
        public float f(float x, float y) {
            if (Math.abs(x) < d + 0.5 && Math.abs(x) > d - 0.5 && y < l && y > -l) return 0;
            float a = MathUtils.log(MathUtils.E, (float) (
                    (l - y + Math.sqrt((l - y) * (l - y) + (d + x) * (d + x))) /
                            (-l - y + Math.sqrt((l + y) * (l + y) + (d + x) * (d + x)))));
            float b = MathUtils.log(MathUtils.E, (float) (
                    (l - y + Math.sqrt((l - y) * (l - y) + (d - x) * (d - x))) /
                            (-l - y + Math.sqrt((l + y) * (l + y) + (d - x) * (d - x)))));
            return 2.6f * (a - b);
        }
    }, func3 = new PixelFunction() {
        float d = 5, l = 4;
        @Override
        public float f(float x, float y) {
            if (x < d + 0.5 && x > d - 0.5 && y < l && y > -l) return 0;
            if (y * y + (x + d) * (x + d) < 0.5) {
                return 0;
            }
            float a = MathUtils.log(MathUtils.E, (float) (
                    (l - y + Math.sqrt((l - y) * (l - y) + (d - x) * (d - x))) /
                            (-l - y + Math.sqrt((l + y) * (l + y) + (d - x) * (d - x)))));
            return 2.6f * (a - 7.75f / (float) Math.sqrt((y * y + (x + d) * (x + d))));
        }
    };

    abstract class PixelFunction {
        public abstract float f(float x, float y);

        public float eval(int x, int y) {
            v.unproject(tmpVector.set(x / ssaa, y / ssaa));
            return f(tmpVector.x, tmpVector.y);
        }
    }

    @Override
    public void create() {
        v = new ExtendViewport(32, 18);
        ui = new ScreenViewport();
        sb = new SpriteBatch();
        sb.setProjectionMatrix(ui.getCamera().combined);
        colors.put(103, new Color(0xFF5E5EFF));
        colors.put(102, new Color(0xFF7E7EFF));
        colors.put(101, new Color(0xFF9E9EFF));
        colors.put(100, new Color(0xABABABFF));
        colors.put(99, new Color(0x9F9FFFFF));
        colors.put(98, new Color(0x7F7FFFFF));
        colors.put(97, new Color(0x5F5FFFFF));
    }

    @Override
    public void render() {
        px = new Pixmap(ssaa * Gdx.graphics.getWidth(), ssaa * Gdx.graphics.getHeight(), Pixmap.Format.RGB888);
        for (int y = 0; y < ssaa * Gdx.graphics.getHeight(); y++) {
            for (int x = 0; x < ssaa * Gdx.graphics.getWidth(); x++) {
                float c = func3.eval(x, y);
                c += 100;
                c = (Math.abs(c - 100) < 3 && ((c - epsilon) < (int) c || (c + epsilon) > ((int) c + 1))) ? ((int) (c + epsilon)) : 0;
                px.setColor(colors.containsKey((int) c) ? colors.get((int) c) : Color.WHITE);
                px.drawPixel(x, y);
            }
        }
        Texture tex = new Texture(px);
        tex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        sb.begin();
        sb.draw(tex, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        sb.end();
        px.dispose();
    }

    @Override
    public void resize(int width, int height) {
        v.update(width, height);
        ui.update(width, height, true);
        sb.setProjectionMatrix(ui.getCamera().combined);
    }

    @Override
    public void dispose() {
        sb.dispose();
    }
}
