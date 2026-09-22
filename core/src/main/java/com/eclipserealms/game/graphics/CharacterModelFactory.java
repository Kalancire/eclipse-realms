package com.eclipserealms.game.graphics;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
   import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;

/**
 * Builds every character model procedurally out of primitives.
 *
 * Why: this avoids shipping large textured 3D asset files (a big part of
 * what makes RPGs heavy on low-RAM phones and slow storage), while still
 * guaranteeing every character has a genuinely different silhouette,
 * proportions and color identity - not a recolored copy of the same mesh.
 * Each model is a handful of untextured, flat-shaded primitives (well under
 * 300 triangles total), which is extremely cheap for an Adreno 610/612
 * (Snapdragon 680) or an old Intel HD 4000 iGPU to draw - one draw call per
 * character instance, no texture sampling at all.
 *
 * Each part is attached to its own Node with a translation, which is the
 * standard, version-safe way to position primitives with libGDX's
 * ModelBuilder/MeshPartBuilder.
 */
public class CharacterModelFactory {

    private final ModelBuilder builder = new ModelBuilder();
    private static final long ATTRS = Usage.Position | Usage.Normal;

    /** Creates a new named node. NOTE: ModelBuilder.node(String, Model) dereferences
     *  its Model argument, so passing null throws a NullPointerException at startup;
     *  the no-arg node() + setting the id is the correct way. */
    private Node newNode(String id) {
        Node node = builder.node();
        node.id = id;
        return node;
    }

    private Material mat(Color color) {
        return new Material(ColorAttribute.createDiffuse(color));
    }

    private void box(String id, float w, float h, float d, float x, float y, float z, Material material) {
        Node node = newNode(id);
        node.translation.set(x, y, z);
        MeshPartBuilder mpb = builder.part(id, GL20.GL_TRIANGLES, ATTRS, material);
        mpb.box(w, h, d);
    }

    private void sphere(String id, float diameter, float x, float y, float z, Material material) {
        Node node = newNode(id);
        node.translation.set(x, y, z);
        MeshPartBuilder mpb = builder.part(id, GL20.GL_TRIANGLES, ATTRS, material);
        mpb.sphere(diameter, diameter, diameter, 8, 8);
    }

    /** Warrior: broad-shouldered, boxy, heavy silhouette, warm steel/red palette. */
    public Model buildWarrior() {
        builder.begin();
        Color skin = new Color(0.85f, 0.68f, 0.55f, 1f);
        Color armor = new Color(0.45f, 0.47f, 0.52f, 1f);
        Color trim = new Color(0.65f, 0.12f, 0.10f, 1f);

        box("torso", 0.60f, 0.70f, 0.34f, 0f, 0.75f, 0f, mat(armor));
        box("shoulderL", 0.24f, 0.20f, 0.24f, -0.42f, 1.05f, 0f, mat(trim));
        box("shoulderR", 0.24f, 0.20f, 0.24f, 0.42f, 1.05f, 0f, mat(trim));
        box("head", 0.30f, 0.30f, 0.30f, 0f, 1.30f, 0f, mat(skin));
        box("legL", 0.22f, 0.60f, 0.24f, -0.16f, 0.30f, 0f, mat(armor));
        box("legR", 0.22f, 0.60f, 0.24f, 0.16f, 0.30f, 0f, mat(armor));
        box("weapon", 0.08f, 1.1f, 0.05f, 0f, 1.0f, -0.28f, mat(new Color(0.75f, 0.76f, 0.78f, 1f)));
        return builder.end();
    }

    /** Mage: slim, tall, robed, cool blue/purple palette with a floating orb. */
    public Model buildMage() {
        builder.begin();
        Color skin = new Color(0.90f, 0.78f, 0.68f, 1f);
        Color robe = new Color(0.24f, 0.20f, 0.55f, 1f);
        Color trim = new Color(0.55f, 0.80f, 0.95f, 1f);

        box("robe", 0.46f, 0.95f, 0.46f, 0f, 0.62f, 0f, mat(robe));
        box("collar", 0.30f, 0.10f, 0.30f, 0f, 1.05f, 0f, mat(trim));
        box("head", 0.22f, 0.24f, 0.22f, 0f, 1.28f, 0f, mat(skin));
        box("hatBrim", 0.34f, 0.05f, 0.34f, 0f, 1.42f, 0f, mat(trim));
        box("hatCone", 0.16f, 0.36f, 0.16f, 0f, 1.62f, 0f, mat(robe));
        box("armL", 0.10f, 0.55f, 0.10f, -0.30f, 0.85f, 0.10f, mat(robe));
        box("armR", 0.10f, 0.55f, 0.10f, 0.30f, 0.85f, 0.10f, mat(robe));
        sphere("orb", 0.14f, 0f, 1.55f, 0.35f, mat(new Color(0.55f, 0.85f, 1f, 1f)));
        return builder.end();
    }

    /** Rogue: short, narrow, hooded, dark green/black palette, asymmetric daggers. */
    public Model buildRogue() {
        builder.begin();
        Color skin = new Color(0.80f, 0.63f, 0.50f, 1f);
        Color cloth = new Color(0.16f, 0.30f, 0.18f, 1f);
        Color dark = new Color(0.10f, 0.10f, 0.10f, 1f);

        box("torso", 0.38f, 0.58f, 0.24f, 0f, 0.66f, 0f, mat(cloth));
        box("hood", 0.28f, 0.26f, 0.28f, 0f, 1.15f, 0.02f, mat(dark));
        box("face", 0.16f, 0.10f, 0.05f, 0f, 1.08f, 0.14f, mat(skin));
        box("legL", 0.16f, 0.55f, 0.18f, -0.12f, 0.27f, 0f, mat(dark));
        box("legR", 0.16f, 0.55f, 0.18f, 0.12f, 0.27f, 0f, mat(dark));
        box("daggerHip", 0.04f, 0.30f, 0.04f, 0.26f, 0.55f, 0f, mat(new Color(0.7f, 0.7f, 0.7f, 1f)));
        box("daggerHand", 0.04f, 0.24f, 0.04f, -0.32f, 0.95f, 0.18f, mat(new Color(0.7f, 0.7f, 0.7f, 1f)));
        return builder.end();
    }

    /** Slime enemy: single squashed sphere, flat green. */
    public Model buildSlime() {
        builder.begin();
        Node node = newNode("body");
        MeshPartBuilder mpb = builder.part("body", GL20.GL_TRIANGLES, ATTRS,
                mat(new Color(0.30f, 0.75f, 0.35f, 1f)));
        mpb.sphere(0.5f, 0.32f, 0.5f, 10, 10);
        return builder.end();
    }

    /** Skeleton enemy: tall, very thin, bone-white, boxy joints. */
    public Model buildSkeleton() {
        builder.begin();
        Color bone = new Color(0.90f, 0.88f, 0.80f, 1f);
        box("torso", 0.26f, 0.55f, 0.16f, 0f, 0.70f, 0f, mat(bone));
        box("skull", 0.24f, 0.26f, 0.24f, 0f, 1.14f, 0f, mat(bone));
        box("legL", 0.10f, 0.62f, 0.10f, -0.10f, 0.30f, 0f, mat(bone));
        box("legR", 0.10f, 0.62f, 0.10f, 0.10f, 0.30f, 0f, mat(bone));
        box("armL", 0.08f, 0.48f, 0.08f, -0.20f, 0.75f, 0f, mat(bone));
        box("armR", 0.08f, 0.48f, 0.08f, 0.20f, 0.75f, 0f, mat(bone));
        return builder.end();
    }

    /** Golem enemy: massive, blocky, slow-looking, stone-grey with glowing core. */
    public Model buildGolem() {
        builder.begin();
        Color stone = new Color(0.42f, 0.40f, 0.38f, 1f);
        Color core = new Color(0.95f, 0.55f, 0.15f, 1f);
        box("torso", 0.95f, 1.05f, 0.55f, 0f, 0.90f, 0f, mat(stone));
        box("head", 0.34f, 0.30f, 0.34f, 0f, 1.62f, 0f, mat(stone));
        box("legL", 0.34f, 0.65f, 0.34f, -0.28f, 0.32f, 0f, mat(stone));
        box("legR", 0.34f, 0.65f, 0.34f, 0.28f, 0.32f, 0f, mat(stone));
        sphere("core", 0.18f, 0f, 0.95f, 0.30f, mat(core));
        return builder.end();
    }

    /** Bandit enemy: medium build, ragged, dull red/brown, asymmetric shoulder pad. */
    public Model buildBandit() {
        builder.begin();
        Color skin = new Color(0.75f, 0.58f, 0.45f, 1f);
        Color cloth = new Color(0.55f, 0.30f, 0.15f, 1f);
        box("torso", 0.44f, 0.60f, 0.28f, 0f, 0.68f, 0f, mat(cloth));
        box("head", 0.26f, 0.26f, 0.26f, 0f, 1.12f, 0f, mat(skin));
        box("shoulderPad", 0.20f, 0.16f, 0.20f, 0.32f, 1.0f, 0f, mat(new Color(0.35f, 0.12f, 0.10f, 1f)));
        box("legL", 0.18f, 0.56f, 0.20f, -0.13f, 0.28f, 0f, mat(cloth));
        box("legR", 0.18f, 0.56f, 0.20f, 0.13f, 0.28f, 0f, mat(cloth));
        return builder.end();
    }

    /** Shopkeeper NPC: rounded, friendly proportions, warm yellow apron. */
    public Model buildShopkeeper() {
        builder.begin();
        Color skin = new Color(0.88f, 0.70f, 0.58f, 1f);
        Color apron = new Color(0.85f, 0.70f, 0.20f, 1f);
        Color boots = new Color(0.3f, 0.2f, 0.15f, 1f);
        box("torso", 0.50f, 0.55f, 0.36f, 0f, 0.65f, 0f, mat(apron));
        box("head", 0.32f, 0.32f, 0.32f, 0f, 1.10f, 0f, mat(skin));
        box("legL", 0.20f, 0.45f, 0.22f, -0.14f, 0.22f, 0f, mat(boots));
        box("legR", 0.20f, 0.45f, 0.22f, 0.14f, 0.22f, 0f, mat(boots));
        return builder.end();
    }
}
