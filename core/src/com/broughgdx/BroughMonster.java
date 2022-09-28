package com.broughgdx;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

public class BroughMonster {
    // used for AI purposes
    // ideally there should be a better way to create modular behavior
    // mapping enum => function is a way
    // when creating a new class for every monster seems a bit too much
    public enum MonsterType {
        BIRD,
        SNAKE,
        TANK,
        EATER,
        JESTER
    }

    private boolean m_isFacingRight;
    public boolean Right() { return m_isFacingRight; }

    private Vector2 m_position;
    public Vector2 Position() { return m_position; }
    private Vector2 m_offset;
    public Vector2 Offset() { return m_offset; }
    public Vector2 RenderPosition() { return new Vector2(m_position.x + m_offset.x, m_position.y + m_offset.y); }
    private int m_hp;
    public int HP() { return m_hp; }
    private TextureRegion m_monsterTexture;
    public TextureRegion Texture() { return m_monsterTexture;}
    private boolean m_isPlayer;
    public boolean IsPlayer() { return m_isPlayer;}
    private boolean m_stunned;
    public boolean Stunned() { return m_stunned; }
    public void Stun(boolean value) { m_stunned = value; }

    private MonsterType m_monsterType;
    public MonsterType Type() { return m_monsterType; }

    private int m_teleportCount;
    public int TeleportCount() { return m_teleportCount; }

    BroughMonster(TextureRegion monsterTexture, Vector2 position, int hp, MonsterType type) {
        this.m_position = position;
        this.m_offset = new Vector2(0.0f, 0.0f);
        this.m_hp = hp;
        this.m_monsterTexture = monsterTexture;
        this.m_isPlayer = false;
        this.m_monsterType = type;

        m_teleportCount = 2;
    }

    BroughMonster(TextureRegion monsterTexture, Vector2 position, int hp, boolean isPlayer) {
        this.m_position = position;
        this.m_offset = new Vector2(0.0f, 0.0f);
        this.m_hp = hp;
        this.m_monsterTexture = monsterTexture;
        this.m_isPlayer = isPlayer;

        if(m_isPlayer) {
            m_teleportCount = 0;
        }
    }

    public void Move(int dx, int dy) {
        if(dx != 0) {
            m_isFacingRight = dx > 0;
        }

        m_position.x += dx;
        m_position.y += dy;

        m_offset.x -= dx;
        m_offset.y -= dy;
    }

    public void TickOffset(float amount) {
        if(m_offset.x == 0.0f && m_offset.y == 0.0f) {
            return;
        }

        m_offset.x -= Math.signum(m_offset.x) * amount;
        m_offset.y -= Math.signum(m_offset.y) * amount;

        // zero-ing out the offset - keep in mind that the offset is in pixels, so 3 is not really a big value.

        if(Math.abs(m_offset.x) < 3.0f) {
            m_offset.x = 0.f;
        }

        if(Math.abs(m_offset.y) < 3.0f) {
            m_offset.y = 0.0f;
        }
    }

    public void DealDamage(int amount) {
        m_hp -= amount;
    }
    public void TickTeleportCount() { m_teleportCount -= 1; }
}