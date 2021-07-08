package com.robotgryphon.compactcrafting.compat.theoneprobe;

import net.minecraftforge.fml.InterModComms;

public class TheOneProbeCompat {


    public static void sendIMC() {
        InterModComms.sendTo("theoneprobe", "getTheOneProbe", TOPMain::new);
    }
}
