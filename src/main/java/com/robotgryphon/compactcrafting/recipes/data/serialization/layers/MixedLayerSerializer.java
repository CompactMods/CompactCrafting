package com.robotgryphon.compactcrafting.recipes.data.serialization.layers;

public class MixedLayerSerializer {}
/* extends RecipeLayerSerializer<MixedComponentRecipeLayer> {

    @Override
    public MixedComponentRecipeLayer readLayerData(JsonObject json) throws RecipeLoadingException {
        if(!json.has("pattern"))
            throw new RecipeLoadingException("Mixed layer definition does not have an associated pattern.");

        Map<BlockPos, String> compMap = RecipeHelper.getComponentMapFromPattern(json);

        MixedComponentRecipeLayer mixed = new MixedComponentRecipeLayer();
        for(Map.Entry<BlockPos, String> mapping : compMap.entrySet()) {
            String comp = mapping.getValue();

            // Skip empty and dashed components, treat them as air
            if(comp.trim().isEmpty() || comp.equals("-") || comp.equals("_"))
                continue;

            mixed.add(comp, mapping.getKey());
        }

        return mixed;
    }

    @Override
    public MixedComponentRecipeLayer readLayerData(PacketBuffer buffer) {
        MixedComponentRecipeLayer mixed = new MixedComponentRecipeLayer();

        int numberComponents = buffer.readInt();
        for(int pi = 0; pi < numberComponents; pi++) {
            String reqComp = buffer.readString();
            int numberFilled = buffer.readInt();

            List<BlockPos> filledPositions = new ArrayList<>(numberFilled);
            for(int ci = 0; ci < numberFilled; ci++) {
                BlockPos filledPos = buffer.readBlockPos();
                filledPositions.add(filledPos);
            }

            mixed.addMultiple(reqComp, filledPositions);
        }

        return mixed;
    }

    @Override
    public void writeLayerData(MixedComponentRecipeLayer layer, PacketBuffer buffer) {
        buffer.writeResourceLocation(Registration.MIXED_LAYER_TYPE.getId());

        Set<String> componentKeys = layer.getComponentTotals().keySet();
        buffer.writeInt(componentKeys.size());

        componentKeys.forEach(key -> {
            buffer.writeString(key);
            Collection<BlockPos> positionsForComponent = layer.getPositionsForComponent(key);
            buffer.writeInt(positionsForComponent.size());
            positionsForComponent.forEach(buffer::writeBlockPos);
        });
    }
}*/
