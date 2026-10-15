package minecarttunneltrack;

import necesse.engine.modLoader.annotations.ModEntry;
import necesse.engine.registries.ObjectRegistry;
import necesse.engine.registries.RecipeTechRegistry;
import necesse.inventory.recipe.Recipes;
import necesse.inventory.recipe.Recipe;
import necesse.inventory.recipe.Ingredient;

@ModEntry
public class MinecartTunnelTrackMod {

    public void init() {
        ObjectRegistry.registerObject("minecarttunneltrack", new MinecartTunnelTrackObject(), 35F, true);
    }

    public void postInit() {
        Recipes.registerModRecipe(new Recipe(
                "minecarttunneltrack",
                1,
                RecipeTechRegistry.WORKSTATION,
                new Ingredient[] {
                        new Ingredient("minecarttrack", 1),
                        new Ingredient("ironbar",5),
                }).showAfter("minecarttrack"));
    }
}