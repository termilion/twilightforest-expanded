package twilightforest.world.components.structures.util;

import net.minecraft.core.Holder;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;

import java.util.Optional;

// Landmarks with an icon on the magic map, see MagicMapItem#update
public interface LandmarkMapIcon {
	Optional<Holder<MapDecorationType>> getMapIcon();
}
