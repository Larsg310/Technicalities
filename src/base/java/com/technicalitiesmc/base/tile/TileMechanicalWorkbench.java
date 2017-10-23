package com.technicalitiesmc.base.tile;

public class TileMechanicalWorkbench extends TileWorkbench {
    //
    // private CraftingTask task;
    //
    // public void craft() {
    // // Recipe recipe = new Recipe(true);
    // // CraftingTask task = new CraftingTask(0, recipe);
    // // task.craft();
    // }
    //
    // private class CraftingTask {
    //
    // private final int layer;
    // private final Recipe recipe;
    // private final ItemStack[] grid = new ItemStack[9];
    // private int index = 0;
    // private CraftingTask child;
    // private boolean childDone;
    // private NonNullList<ItemStack> results = NonNullList.create();
    //
    // public CraftingTask(int layer, Recipe recipe) {
    // this.layer = layer;
    // this.recipe = recipe;
    // Arrays.fill(grid, ItemStack.EMPTY);
    // for (int i = 0; i < 9; i++) {
    // if (!recipe.grid[i].isEmpty()) {
    // index = i; // Initialize recipe item locator
    // break;
    // }
    // }
    // }
    //
    // public boolean tick() {
    // if (child != null) {
    // if (childDone) {
    // if (child.craft()) {
    // child = null;
    // childDone = false;
    // }
    // } else {
    // childDone = child.tick();
    // }
    // return false;
    // }
    //
    // ItemStack needed = recipe.grid[index];
    // for (int i = 0; i < 18; i++) {
    // ItemStack stack = inventory.getStackInSlot(i);
    // if (ItemHandlerHelper.canItemStacksStack(stack, needed)) {
    // grid[index] = inventory.extractItem(i, 1, false);
    // if (!grid[index].isEmpty()) {
    // do {
    // index++;
    // if (index == 9) {
    // return true;
    // }
    // } while (recipe.grid[index].isEmpty());
    // return false;
    // }
    // }
    // }
    //
    // if (layer < 8) {
    // Recipe recipe = findRecipeFor(needed);
    // if (recipe != null) {
    // child = new CraftingTask(layer + 1, recipe);
    // return false;
    // }
    // }
    //
    // return false;
    // }
    //
    // public boolean craft() {
    // Arrays.fill(grid, ItemStack.EMPTY);
    // results.add(recipe.result);
    // recipe.remainder.forEach(results::add);
    // Iterator<ItemStack> it = results.iterator();
    // while (it.hasNext()) {
    // ItemStack stack = it.next();
    // ItemStack leftover = ItemHandlerHelper.insertItemStacked(exposedInventory, stack, true);
    // if (leftover.isEmpty()) {
    // ItemHandlerHelper.insertItemStacked(exposedInventory, stack, false);
    // it.remove();
    // }
    // }
    // return results.isEmpty();
    // }
    //
    // public ItemStack[] getGrid() {
    // return child != null ? child.getGrid() : grid;
    // }
    //
    // }

}
