package twilightforest.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.PortalParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import org.jetbrains.annotations.Nullable;

// Copy of the vanilla portal particle, tinted gold instead of purple
public class TFPortalParticle extends PortalParticle {
	protected TFPortalParticle(ClientLevel level, double x, double y, double z, double xd, double yd, double zd) {
		super(level, x, y, z, xd, yd, zd);
		// vanilla stores the random brightness in the blue channel
		float brightness = this.bCol;
		this.rCol = brightness;
		this.gCol = brightness * 0.8F;
		this.bCol = brightness * 0.1F;
	}

	public record Provider(SpriteSet sprites) implements ParticleProvider<SimpleParticleType> {
		@Nullable
		@Override
		public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xd, double yd, double zd) {
			TFPortalParticle particle = new TFPortalParticle(level, x, y, z, xd, yd, zd);
			particle.pickSprite(this.sprites);
			return particle;
		}
	}
}
