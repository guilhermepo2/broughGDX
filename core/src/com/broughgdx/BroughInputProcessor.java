package com.broughgdx;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;

public class BroughInputProcessor implements InputProcessor {
    private boolean canConsumeA = false;
    private boolean isADown = false;
    public boolean Left() {
        boolean canMove = isADown && canConsumeA;
        canConsumeA = false;
        return canMove;
    }

    private boolean canConsumeD = false;
    private boolean isDDown = false;
    public boolean Right() {
        boolean canMove = isDDown && canConsumeD;
        canConsumeD = false;
        return canMove;
    }
    private boolean canConsumeS = false;
    private boolean isSDown = false;
    public boolean Down() {
        boolean canMove = isSDown && canConsumeS;
        canConsumeS = false;
        return canMove;
    }
    private boolean canConsumeW = false;
    private boolean isWDown = false;
    public boolean Up() {
        boolean canMove = isWDown && canConsumeW;
        canConsumeW = false;
        return canMove;
    }

    public boolean keyDown (int keycode) {
        switch(keycode) {
            case Input.Keys.A:
                isADown = true;
                return true;
            case Input.Keys.D:
                isDDown = true;
                return true;
            case Input.Keys.S:
                isSDown = true;
                return true;
            case Input.Keys.W:
                isWDown = true;
                return true;
        }

        return false;
    }

    public boolean keyUp (int keycode) {
        switch(keycode) {
            case Input.Keys.A:
                canConsumeA = true;
                return true;
            case Input.Keys.D:
                canConsumeD = true;
                return true;
            case Input.Keys.S:
                canConsumeS = true;
                return true;
            case Input.Keys.W:
                canConsumeW = true;
                return true;
        }

        return false;
    }

    public boolean keyTyped (char character) {
        return false;
    }

    public boolean touchDown (int x, int y, int pointer, int button) {
        return false;
    }

    public boolean touchUp (int x, int y, int pointer, int button) {
        return false;
    }

    public boolean touchDragged (int x, int y, int pointer) {
        return false;
    }

    public boolean mouseMoved (int x, int y) {
        return false;
    }

    public boolean scrolled (float amountX, float amountY) {
        return false;
    }
}
