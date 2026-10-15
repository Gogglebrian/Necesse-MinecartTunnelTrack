package minecarttunneltrack;

import necesse.engine.modLoader.annotations.ModMethodPatch;
import necesse.entity.mobs.summon.MinecartLines;
import necesse.level.gameObject.MinecartTrackObject;
import necesse.level.maps.Level;
import net.bytebuddy.asm.Advice;

@ModMethodPatch(target = MinecartTrackObject.class, name = "getMinecartLines", arguments = {Level.class, int.class, int.class, int.class, float.class, float.class, boolean.class})
public class MinecartTrackTunnelCompatPatch {
    @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class)
    static boolean onEnter() {
        // Always skip the original method body
        return true;
    }

    @Advice.OnMethodExit
    static void onExit(@Advice.This MinecartTrackObject track,
                           @Advice.Argument(0) Level level,
                           @Advice.Argument(1) int x,
                           @Advice.Argument(2) int y,
                           @Advice.Argument(3) int rotation,
                           @Advice.Argument(4) float entityDx,
                           @Advice.Argument(5) float entityDy,
                           @Advice.Argument(6) boolean ignoreEntityDirection,
                           @Advice.Return(readOnly = false) MinecartLines returned) {
        returned = MinecartTrackCompatibility.getMinecartLines(
                track, level, x, y, rotation, entityDx, entityDy, ignoreEntityDirection
        );
    }
}