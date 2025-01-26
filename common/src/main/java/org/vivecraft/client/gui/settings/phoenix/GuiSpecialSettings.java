package org.vivecraft.client.gui.settings.phoenix;

import net.minecraft.client.gui.screens.Screen;
import org.vivecraft.client.gui.framework.GuiVROptionsBase;
import org.vivecraft.client.gui.framework.VROptionEntry;
import org.vivecraft.client_vr.settings.VRSettings;

//PhoenixRa: AR GLASSES FIX
public class GuiSpecialSettings extends GuiVROptionsBase {
    private static final VROptionEntry[] MODEL_OPTIONS = new VROptionEntry[]{
        new VROptionEntry(VRSettings.VrOptions.FOV_SCALE),
        new VROptionEntry(VRSettings.VrOptions.USE_SERVER_FOV_SCALE),
        new VROptionEntry(VRSettings.VrOptions.HMD_FOV_TYPE)
    };
    public GuiSpecialSettings(Screen lastScreen) {
        super(lastScreen);
    }

    @Override
    public void init() {
        this.vrTitle = "vivecraft.options.screen.specialrender";
        super.init(MODEL_OPTIONS, true);

        super.addDefaultButtons();
    }
}
