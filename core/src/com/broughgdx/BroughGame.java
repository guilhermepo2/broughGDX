package com.broughgdx;

import com.badlogic.gdx.math.MathUtils;

public class BroughGame {
    // =================================
    // Game State
    // =================================
    public enum EGameState {
        LOADING, TITLE, RUNNING, DEAD, WON
    }
    private EGameState m_currentGameState;

    public void SetCurrentState(EGameState state) { m_currentGameState = state; }
    public EGameState GetCurrentState() { return m_currentGameState; }

    // =================================
    // Generic Counter
    // =================================
    private float m_counter = 0.0f;
    private float TIME_TO_ADVANCE_SCREEN = 5.0f;
    public void StartTimer() { m_counter = 0.0f; }
    public boolean CanAdvanceScreen() { return m_counter >= TIME_TO_ADVANCE_SCREEN; }
    public void TickTimer(float dt) { m_counter += dt; }

    // =================================
    // Player related game related stuff
    // =================================
    private int m_playerScore = 0;
    public void ResetPlayerScore() { m_playerScore = 0; }
    public void AddToPlayerScore(int v) { m_playerScore += 1; }
    public int GetPlayerScore() { return m_playerScore; }

    // =================================
    // Monster Spawning
    // =================================
    private int m_currentLevel = 0;
    public int GetCurrentLevel() { return m_currentLevel; }
    private int m_spawnRate = 15;
    private int m_spawnCounter = m_spawnRate;

    public boolean DecrementSpawnCounter(int amount) {
        m_spawnCounter -= amount;

        if(m_spawnCounter <= 0) {
            m_spawnCounter = m_spawnRate;
            m_spawnRate -= 1;
            return true;
        }

        return false;
    }

    public void ResetMonsterSpawning() {
        m_currentLevel = 0;
        m_spawnRate = 15;
        m_spawnCounter = m_spawnRate;
    }

    public void IncrementLevel(int v) {
        m_currentLevel += v;
        m_spawnRate = 15 - m_currentLevel;
        m_spawnCounter = m_spawnRate;
    }

    // =================================
    // Screenshake
    // =================================
    private int m_shakeAmount = 0;
    public void SetShakeAmount(int amount) { m_shakeAmount = amount; }

    private int m_shakeX = 0;
    public int GetShakeX() { return m_shakeX; }
    private int m_shakeY = 0;
    public int GetShakeY() { return m_shakeY; }

    public void TickScreenShake() {
        if(m_shakeAmount > 0) {
            m_shakeAmount--;
        }

        float shakeAngle = (float)(MathUtils.random() * Math.PI * 2);
        m_shakeX = Math.round((float)(Math.cos(shakeAngle)) * m_shakeAmount);
        m_shakeY = Math.round((float)(Math.sin(shakeAngle)) * m_shakeAmount);
    }

}
