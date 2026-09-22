package com.eclipserealms.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.math.Vector3;
import com.eclipserealms.game.entities.Player;
import com.eclipserealms.game.entities.PlayerClass;
import com.eclipserealms.game.graphics.CharacterModelFactory;
import com.eclipserealms.game.screens.DungeonScreen;
import com.eclipserealms.game.screens.MainMenuScreen;
import com.eclipserealms.game.screens.TownScreen;
import com.eclipserealms.game.world.Dungeon;

/**
 * Root game class / screen orchestrator. Holds the long-lived, expensive
 * resources (character models) so they're created exactly once per run and
 * shared across screens, instead of being rebuilt every time the player
 * walks between town and dungeon - important for avoiding load-time
 * hitches on slow storage (both the phone and the old laptop target).
 */
public class EclipseRealmsGame extends Game {

    private CharacterModelFactory modelFactory;
    private Model warriorModel, mageModel, rogueModel;

    @Override
    public void create() {
        modelFactory = new CharacterModelFactory();
        warriorModel = modelFactory.buildWarrior();
        mageModel = modelFactory.buildMage();
        rogueModel = modelFactory.buildRogue();

        setScreen(new MainMenuScreen(this));
    }

    public void startNewGame(PlayerClass playerClass) {
        Model model = modelForClass(playerClass);
        Player player = new Player(model, playerClass, new Vector3(0, 0, 0));
        switchScreen(new TownScreen(this, player));
    }

    public void enterDungeon(Player player) {
        Dungeon dungeon = new Dungeon("Forsaken Depths", 5, System.currentTimeMillis());
        switchScreen(new DungeonScreen(this, player, dungeon));
    }

    public void returnToTownAfterVictory(Player player) {
        player.health = player.maxHealth;
        player.gold += 100;
        switchScreen(new TownScreen(this, player));
    }

    public void returnToTownAfterDefeat(Player player) {
        player.health = player.maxHealth * 0.5f;
        player.alive = true;
        switchScreen(new TownScreen(this, player));
    }

    /** Game.setScreen() does not dispose the outgoing screen, which would
     *  otherwise leak GL resources (floor/staircase models, batches, HUD
     *  textures) every single time the player walks between town and the
     *  dungeon - a slow, invisible memory leak that eventually causes
     *  exactly the kind of long-session lag this project is meant to fix.
     *  Always route screen changes through here instead of calling
     *  setScreen() directly. */
    private void switchScreen(Screen next) {
        Screen old = getScreen();
        setScreen(next);
        if (old != null) old.dispose();
    }

    private Model modelForClass(PlayerClass playerClass) {
        switch (playerClass) {
            case WARRIOR: return warriorModel;
            case MAGE: return mageModel;
            case ROGUE: return rogueModel;
            default: return warriorModel;
        }
    }

    @Override
    public void dispose() {
        if (getScreen() != null) getScreen().dispose();
        warriorModel.dispose();
        mageModel.dispose();
        rogueModel.dispose();
    }
}
