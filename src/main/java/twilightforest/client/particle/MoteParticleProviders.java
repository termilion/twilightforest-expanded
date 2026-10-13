package twilightforest.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.SuspendedTownParticle;
import net.minecraft.core.particles.SimpleParticleType;

/**
 * Motes drifting above the ground of the amethyst straits, built on the same particle as the old "depthsuspend" void fog
 * (vanilla's {@link SuspendedTownParticle}, also used by mycelium).
 */
public class MoteParticleProviders {
	/**
	 * Vanilla grey, like mycelium
	 */
	public record Grey(SpriteSet sprite) implements ParticleProvider<SimpleParticleType> {
		@Override
		public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
			SuspendedTownParticle particle = new SuspendedTownParticle(level, x, y, z, xSpeed, ySpeed, zSpeed);
			particle.pickSprite(this.sprite());
			return particle;
		}
	}

	public record Black(SpriteSet sprite) implements ParticleProvider<SimpleParticleType> {
		@Override
		public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
			SuspendedTownParticle particle = new SuspendedTownParticle(level, x, y, z, xSpeed, ySpeed, zSpeed);
			particle.pickSprite(this.sprite());
			particle.setColor(0.0F, 0.0F, 0.0F);
			return particle;
		}
	}
}
