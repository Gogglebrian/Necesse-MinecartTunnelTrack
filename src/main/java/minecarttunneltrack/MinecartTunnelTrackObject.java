package minecarttunneltrack;

import necesse.engine.Settings;
import necesse.engine.gameLoop.tickManager.TickManager;
import necesse.engine.window.WindowManager;
import necesse.entity.mobs.PlayerMob;
import necesse.entity.mobs.summon.MinecartLines;
import necesse.gfx.camera.GameCamera;
import necesse.gfx.drawOptions.DrawOptionsList;
import necesse.gfx.drawables.LevelSortedDrawable;
import necesse.gfx.drawables.OrderableDrawables;
import necesse.gfx.gameTexture.GameTexture;
import necesse.level.gameObject.GameObject;
import necesse.level.gameObject.MinecartTrackObject;
import necesse.level.gameObject.WallObject;
import necesse.level.maps.Level;
import necesse.level.maps.light.GameLight;
import necesse.level.maps.regionSystem.RegionType;

import java.awt.*;
import java.util.List;

public class MinecartTunnelTrackObject extends MinecartTrackObject {
    public GameTexture tunnelTexture;

    public MinecartTunnelTrackObject() {
        super();
        this.isWall = true;
        this.regionType = RegionType.WALL;
        this.collision = new Rectangle(32, 32);
        this.hoverHitbox = new Rectangle(0, -16, 32, 48);
        this.setCraftingCategory("misc");
        this.replaceCategories.add("wall");
        this.canReplaceCategories.add("wall");
        this.canReplaceCategories.add("door");
        this.canReplaceCategories.add("fence");
        this.canReplaceCategories.add("fencegate");
        this.replaceRotations = true;
    }

    @Override
    public void loadTextures() {
        super.loadTextures();
        this.tunnelTexture = GameTexture.fromFile("objects/minecarttunneltrack");
    }

    @Override
    public MinecartLines getMinecartLines(Level level, int x, int y, int rotation, float entityDx, float entityDy, boolean ignoreEntityDirection) {
        return MinecartTrackCompatibility.getMinecartLines(
                this,level,x,y,rotation,entityDx,entityDy,ignoreEntityDirection
        );
    }

    public boolean[] getTunnelTileAdjacency(Level level, int tileX, int tileY) {
        // Adjacency
        GameObject[] adj = level.getAdjacentObjects(tileX, tileY);
        boolean[] same = new boolean[8];
        for (int i = 0; i < 8; i++) {
            same[i] = adj[i] instanceof MinecartTunnelTrackObject;
        }
        return same;
    }

    public int getTopFaceSpriteX (boolean isLeftHalf, boolean[] adjacency) {
        boolean nw = adjacency[0];
        boolean n = adjacency[1];
        boolean ne = adjacency[2];
        boolean w = adjacency[3];
        boolean e = adjacency[4];

        if (!n) {
            if (isLeftHalf) {
                return (w ? 2 : 0);
            }
            else {
                return (e ? 3 : 1);
            }
        }
        else {
            if (isLeftHalf) {
                if (w && !nw) {
                    return 4;
                }
                else if (nw && !w) {
                    return 6;
                }
                else {
                    return (w ? 2 : 0);
                }
            }
            else {
                if (e && !ne) {
                    return 5;
                }
                else if (ne && !e) {
                    return 7;
                }
                else {
                    return (e ? 3 : 1);
                }
            }
        }
    }

    public int getTopFaceSpriteY (boolean[] adjacency) {
        return (adjacency[1] ? 1 : 0); // adjacency[1] = n
    }
    public int getFrontFaceSpriteX (boolean isLeftHalf, boolean[] adjacency) {
        if (isLeftHalf) {
            return  (adjacency[3] ? 2 : 0); // adjacency[3] = w
        }
        else {
            return (adjacency[4] ? 3 : 1); // adjacency[4] = e
        }
    }
    public int getFrontFaceSpriteY (Level level, int tileX, int tileY, int rotation) {
        TrackSprite sprite = this.getSprite(level, tileX, tileY, rotation);
        return (sprite.goingDown ? 2 : 3);
    }
    public int getFrontFaceSpriteY (Level level, int tileX, int tileY) {
        int rotation = level.getObjectRotation(tileX, tileY);
        return getFrontFaceSpriteY(level, tileX, tileY, rotation);
    }


    @Override
    public void addDrawables(List<LevelSortedDrawable> list, OrderableDrawables tileList,
                             Level level, int tileX, int tileY, TickManager tickManager,
                             GameCamera camera, PlayerMob perspective) {

        // Rails first
        super.addDrawables(list, tileList, level, tileX, tileY, tickManager, camera, perspective);

        // Lighting
        GameLight[] lights;
        if (Settings.smoothLighting) {
            lights = level.getRelative(tileX, tileY, Level.adjacentGettersWithCenter,
                    level::getLightLevelWall, size -> new GameLight[size]);
        } else {
            // Fallback – still better than regular getLightLevel
            GameLight center = level.getLightLevelWall(tileX, tileY);
            lights = new GameLight[9];
            java.util.Arrays.fill(lights, center);
        }

        int drawX = camera.getTileDrawX(tileX);
        int drawY = camera.getTileDrawY(tileY);

        boolean[] adjacency = getTunnelTileAdjacency(level, tileX, tileY);

        DrawOptionsList options = new DrawOptionsList();

        // ========== TOP FACE (one tile up) ==========
        // We draw two 16×32 strips side-by-side to make a 32×32 top

        int topFaceSpriteLeftX = getTopFaceSpriteX(true, adjacency);
        int topFaceSpriteRightX = getTopFaceSpriteX(false, adjacency);
        int topFaceSpriteY = getTopFaceSpriteY(adjacency);

        float alpha = 1.0F;
        if (perspective != null && !Settings.hideUI && !Settings.hideCursor) {
            Rectangle alphaRec = new Rectangle(tileX * 32, tileY * 32 - 16, 32, 48);
            if (perspective.getCollision().intersects(alphaRec)) {
                alpha = 0.5F;
            } else if (alphaRec.contains(
                    camera.getX() + WindowManager.getWindow().mousePos().sceneX,
                    camera.getY() + WindowManager.getWindow().mousePos().sceneY)) {
                alpha = 0.5F;
            }
        }

        options.add(this.tunnelTexture.initDraw()
                .sprite(topFaceSpriteLeftX, topFaceSpriteY, 16, 32)
                .addObjectDamageOverlay(this, level, tileX, tileY)
                .advColor(WallObject.getAdvancedLight(lights, alpha, 0, -1))
                .pos(drawX, drawY - 32));

        options.add(this.tunnelTexture.initDraw()
                .sprite(topFaceSpriteRightX, topFaceSpriteY, 16, 32)
                .addObjectDamageOverlay(this, level, tileX, tileY)
                .advColor(WallObject.getAdvancedLight(lights, alpha, 0, -1))
                .pos(drawX + 16, drawY - 32));

        // ========== FRONT FACE (south) ==========
        // Only drawn when there is no tunnel to the south
        if (!adjacency[6]) {
            int frontFaceY = getFrontFaceSpriteY(level, tileX, tileY);
            int frontFaceLeftX = getFrontFaceSpriteX(true, adjacency);
            int frontFaceRightX = getFrontFaceSpriteX(false, adjacency);

            options.add(this.tunnelTexture.initDraw()
                    .sprite(frontFaceLeftX, frontFaceY, 16, 32)
                    .addObjectDamageOverlay(this, level, tileX, tileY)
                    .advColor(WallObject.getAdvancedLight(lights, alpha, 0, 0))
                    .pos(drawX, drawY));

            options.add(this.tunnelTexture.initDraw()
                    .sprite(frontFaceRightX, frontFaceY, 16, 32)
                    .addObjectDamageOverlay(this, level, tileX, tileY)
                    .advColor(WallObject.getAdvancedLight(lights, alpha, 0, 0))
                    .pos(drawX + 16, drawY));
        }

        // Sorted so the tall parts draw in front of objects behind
        list.add(new LevelSortedDrawable(this, tileX, tileY) {
            @Override
            public int getSortY() {
                return 20;
            }

            @Override
            public void draw(TickManager tm) {
                options.draw();
            }
        });
    }

    @Override
    public void drawPreview(Level level, int tileX, int tileY, int rotation, float alpha,
                            PlayerMob player, GameCamera camera) {

        super.drawPreview(level, tileX, tileY, rotation, alpha, player, camera);

        int drawX = camera.getTileDrawX(tileX);
        int drawY = camera.getTileDrawY(tileY);

        boolean[] adjacency = getTunnelTileAdjacency(level, tileX, tileY);
        int topFaceY = getTopFaceSpriteY(adjacency);
        int topFaceLeftX = getTopFaceSpriteX(true, adjacency);
        int topFaceRightX = getTopFaceSpriteX(false, adjacency);

        // Top face
        this.tunnelTexture.initDraw().sprite(topFaceLeftX, topFaceY, 16, 32).alpha(alpha)
                .draw(drawX, drawY - 32);
        this.tunnelTexture.initDraw().sprite(topFaceRightX, topFaceY, 16, 32).alpha(alpha)
                .draw(drawX + 16, drawY - 32);

        // Front face (draw this in the preview regardless
        int frontFaceY = getFrontFaceSpriteY(level, tileX, tileY, rotation);
        int frontFaceLeftX = getFrontFaceSpriteX(true, adjacency);
        int frontFaceRightX = getFrontFaceSpriteX(false, adjacency);

        this.tunnelTexture.initDraw().sprite(frontFaceLeftX, frontFaceY, 16, 32).alpha(alpha)
                .draw(drawX, drawY);
        this.tunnelTexture.initDraw().sprite(frontFaceRightX, frontFaceY, 16, 32).alpha(alpha)
                .draw(drawX + 16, drawY);
    }
}