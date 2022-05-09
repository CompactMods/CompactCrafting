package dev.compactmods.crafting.compat;

import dev.compactmods.crafting.compat.theoneprobe.TOPMain;
import net.minecraftforge.fml.InterModComms;

public class TheOneProbe {
    public static void sendIMC() {
        InterModComms.sendTo("theoneprobe", "getTheOneProbe", TOPMain::new);
    }
}
