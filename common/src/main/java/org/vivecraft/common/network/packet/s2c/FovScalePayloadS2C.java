package org.vivecraft.common.network.packet.s2c;

import net.minecraft.network.FriendlyByteBuf;
import org.vivecraft.common.network.packet.PayloadIdentifier;
import org.vivecraft.data.HMD_TYPE;

import java.util.HashMap;

//PhoenixRa: AR GLASSES FIX (FOV)
public record FovScalePayloadS2C(HashMap<HMD_TYPE, Double> fovScaleSettings) implements VivecraftPayloadS2C {


    @Override
    public PayloadIdentifier payloadId() {
        return PayloadIdentifier.FOV_SCALE;
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeByte(payloadId().ordinal());
        for(HMD_TYPE entry : HMD_TYPE.values()){
            buffer.writeDouble(
                fovScaleSettings.getOrDefault(entry,1.0)
            );
        }
    }

    public static FovScalePayloadS2C read(FriendlyByteBuf buffer) {
        HashMap<HMD_TYPE, Double> fovScaleSettings = new HashMap<>();

        for(HMD_TYPE entry : HMD_TYPE.values()){
            fovScaleSettings.put(
                entry,
                buffer.readDouble()
            );
        }
        return new FovScalePayloadS2C(fovScaleSettings);
    }

}
