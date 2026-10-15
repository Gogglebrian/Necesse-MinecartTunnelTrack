package minecarttunneltrack;

import necesse.entity.mobs.summon.MinecartLine;
import necesse.entity.mobs.summon.MinecartLines;
import necesse.level.maps.Level;
import necesse.level.gameObject.GameObject;
import necesse.level.gameObject.MinecartTrackObject;

public class MinecartTrackCompatibility {
    public static boolean isCompatibleTrack(Level level, int tileX, int tileY) {
        GameObject obj = level.getObject(tileX, tileY);
        return obj instanceof MinecartTrackObject;
    }

    public static MinecartLines getMinecartLines(MinecartTrackObject track, Level level, int x, int y, int rotation, float entityDx, float entityDy, boolean ignoreEntityDirection) {
        MinecartLines lines = new MinecartLines(x, y);
        boolean hasUp = isCompatibleTrack(level, x, y-1);
        boolean hasDown = isCompatibleTrack(level, x, y+1);
        boolean hasLeft = isCompatibleTrack(level, x-1, y);
        boolean hasRight = isCompatibleTrack(level, x+1, y);
        float turnThreshold = 0.2F;
        if (rotation != 0 && rotation != 2) {
            boolean straightAcross = false;
            boolean anyConnectionFromSides = false;
            if (hasUp) {
                byte upRotation = level.getObjectRotation(x, y - 1);
                if (upRotation == 0 || upRotation == 2) {
                    boolean success = true;
                    if (hasDown) {
                        byte downRotation = level.getObjectRotation(x, y + 1);
                        if (downRotation == 0 || downRotation == 2) {
                            success = false;
                        }
                    }

                    if (success) {
                        lines.up = MinecartLine.up(x, y);
                        lines.up.nextNegative = () -> track.getMinecartLines(level, x, y - 1, entityDx, entityDy, false).down;
                        anyConnectionFromSides = true;
                    } else {
                        straightAcross = true;
                    }
                }
            }

            if (hasDown) {
                byte downRotation = level.getObjectRotation(x, y + 1);
                if (downRotation == 0 || downRotation == 2) {
                    boolean success = true;
                    if (hasUp) {
                        byte upRotation = level.getObjectRotation(x, y - 1);
                        if (upRotation == 0 || upRotation == 2) {
                            success = false;
                        }
                    }

                    if (success) {
                        lines.down = MinecartLine.down(x, y);
                        lines.down.nextPositive = () -> track.getMinecartLines(level, x, y + 1, entityDx, entityDy, false).up;
                        anyConnectionFromSides = true;
                    } else {
                        straightAcross = true;
                    }
                }
            }

            if (hasLeft) {
                lines.left = MinecartLine.left(x, y);
                lines.left.nextNegative = () -> track.getMinecartLines(level, x - 1, y, entityDx, entityDy, false).right;
            } else if (rotation == 3 || !anyConnectionFromSides) {
                lines.left = MinecartLine.leftEnd(x, y);
                lines.left.nextNegative = null;
            }

            if (hasRight) {
                lines.right = MinecartLine.right(x, y);
                lines.right.nextPositive = () -> track.getMinecartLines(level, x + 1, y, entityDx, entityDy, false).left;
            } else if (rotation == 1 || !anyConnectionFromSides) {
                lines.right = MinecartLine.rightEnd(x, y);
                lines.right.nextPositive = null;
            }

            if (lines.left != null && lines.right != null) {
                lines.left.nextPositive = () -> lines.right;
                lines.right.nextNegative = () -> lines.left;
            }

            if (straightAcross) {
                lines.up = MinecartLine.up(x, y);
                lines.up.nextNegative = () -> track.getMinecartLines(level, x, y - 1, entityDx, entityDy, false).down;
                lines.down = MinecartLine.down(x, y);
                lines.down.nextPositive = () -> track.getMinecartLines(level, x, y + 1, entityDx, entityDy, false).up;
                lines.up.nextPositive = () -> lines.down;
                lines.down.nextNegative = () -> lines.up;
            } else if (lines.down != null) {
                lines.down.nextPositive = () -> track.getMinecartLines(level, x, y + 1, entityDx, entityDy, false).up;
                if (rotation == 1) {
                    if (lines.left == null || entityDy > turnThreshold) {
                        lines.right.nextNegative = () -> lines.down;
                    }

                    lines.down.nextNegative = () -> lines.right;
                } else {
                    if ((lines.right == null || entityDy > turnThreshold) && lines.left != null) {
                        lines.left.nextPositive = () -> lines.down;
                    }

                    lines.down.nextNegative = () -> lines.left;
                }
            } else if (lines.up != null) {
                lines.up.nextNegative = () -> track.getMinecartLines(level, x, y - 1, entityDx, entityDy, false).down;
                if (rotation == 1) {
                    if (lines.left == null || entityDy < -turnThreshold) {
                        lines.right.nextNegative = () -> lines.up;
                    }

                    lines.up.nextPositive = () -> lines.right;
                } else {
                    if ((lines.right == null || entityDy < -turnThreshold) && lines.left != null) {
                        lines.left.nextPositive = () -> lines.up;
                    }

                    lines.up.nextPositive = () -> lines.left;
                }
            }
        } else {
            boolean straightAcross = false;
            boolean anyConnectionFromSides = false;
            if (hasLeft) {
                byte leftRotation = level.getObjectRotation(x - 1, y);
                if (leftRotation == 1 || leftRotation == 3) {
                    boolean success = true;
                    if (hasRight) {
                        byte rightRotation = level.getObjectRotation(x + 1, y);
                        if (rightRotation == 1 || rightRotation == 3) {
                            success = false;
                        }
                    }

                    if (success) {
                        lines.left = MinecartLine.left(x, y);
                        lines.left.nextNegative = () -> track.getMinecartLines(level, x - 1, y, entityDx, entityDy, false).right;
                        anyConnectionFromSides = true;
                    } else {
                        straightAcross = true;
                    }
                }
            }

            if (hasRight) {
                byte rightRotation = level.getObjectRotation(x + 1, y);
                if (rightRotation == 1 || rightRotation == 3) {
                    boolean success = true;
                    if (hasLeft) {
                        byte leftRotation = level.getObjectRotation(x - 1, y);
                        if (leftRotation == 1 || leftRotation == 3) {
                            success = false;
                        }
                    }

                    if (success) {
                        lines.right = MinecartLine.right(x, y);
                        lines.right.nextPositive = () -> track.getMinecartLines(level, x + 1, y, entityDx, entityDy, false).left;
                        anyConnectionFromSides = true;
                    } else {
                        straightAcross = true;
                    }
                }
            }

            if (hasUp) {
                lines.up = MinecartLine.up(x, y);
                lines.up.nextNegative = () -> track.getMinecartLines(level, x, y - 1, entityDx, entityDy, false).down;
            } else if (rotation == 0 || !anyConnectionFromSides) {
                lines.up = MinecartLine.upEnd(x, y);
                lines.up.nextNegative = null;
            }

            if (hasDown) {
                lines.down = MinecartLine.down(x, y);
                lines.down.nextPositive = () -> track.getMinecartLines(level, x, y + 1, entityDx, entityDy, false).up;
            } else if (rotation == 2 || !anyConnectionFromSides) {
                lines.down = MinecartLine.downEnd(x, y);
                lines.down.nextPositive = null;
            }

            if (lines.up != null && lines.down != null) {
                lines.up.nextPositive = () -> lines.down;
                lines.down.nextNegative = () -> lines.up;
            }

            if (straightAcross) {
                lines.left = MinecartLine.left(x, y);
                lines.left.nextNegative = () -> track.getMinecartLines(level, x - 1, y, entityDx, entityDy, false).right;
                lines.right = MinecartLine.right(x, y);
                lines.right.nextPositive = () -> track.getMinecartLines(level, x + 1, y, entityDx, entityDy, false).left;
                lines.left.nextPositive = () -> lines.right;
                lines.right.nextNegative = () -> lines.left;
            } else if (lines.right != null) {
                lines.right.nextPositive = () -> track.getMinecartLines(level, x + 1, y, entityDx, entityDy, false).left;
                if (rotation == 2) {
                    if (lines.up == null || entityDx > turnThreshold && !ignoreEntityDirection) {
                        lines.down.nextNegative = () -> lines.right;
                    }

                    lines.right.nextNegative = () -> lines.down;
                } else {
                    if ((lines.down == null || entityDx > turnThreshold && !ignoreEntityDirection) && lines.up != null) {
                        lines.up.nextPositive = () -> lines.right;
                    }

                    lines.right.nextNegative = () -> lines.up;
                }
            } else if (lines.left != null) {
                lines.left.nextNegative = () -> track.getMinecartLines(level, x - 1, y, entityDx, entityDy, false).right;
                if (rotation == 2) {
                    if (lines.up == null || entityDx < -turnThreshold) {
                        lines.down.nextNegative = () -> lines.left;
                    }

                    lines.left.nextPositive = () -> lines.down;
                } else {
                    if ((lines.down == null || entityDx < -turnThreshold) && lines.up != null) {
                        lines.up.nextPositive = () -> lines.left;
                    }

                    lines.left.nextPositive = () -> lines.up;
                }
            }
        }

        return lines;
    }
}